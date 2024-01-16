package software.amazon.datazone.domain;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DomainSummary;
import software.amazon.awssdk.services.datazone.model.UpdateDomainRequest;
import software.amazon.awssdk.services.datazone.model.UpdateDomainResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.domain.client.DataZoneClientWrapper;
import software.amazon.datazone.domain.helper.Constants;
import software.amazon.datazone.domain.helper.LoggerWrapper;
import software.amazon.datazone.domain.helper.ResourceStabilizer;
import software.amazon.datazone.domain.helper.TagHelper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class UpdateHandler extends BaseHandlerStd {

    public static final String SIGN_ON_ERROR_STATUS = "SingleSignOn status can not be modified after enabled.";
    public static final String DISABLED_SIGN_ON_TYPE = "DISABLED";

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);
        this.stabilizer = new ResourceStabilizer(dataZoneClientWrapper, logger);

        // Create the context
        // This would be used for retrying when the resource is in TRANSIENT states, and we need to retry again.
        final CallbackContext currentContext = callbackContext == null || callbackContext.getStabilizationRetriesRemaining() == null ?
                CallbackContext.builder().stabilizationRetriesRemaining(Constants.MAXIMUM_STABILIZATION_ATTEMPTS).build() :
                callbackContext;

        Boolean isSingleSignOnUpdateRequired = this.isSingleSignOnUpdateRequired(request.getDesiredResourceState(), request.getPreviousResourceState());

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                // Make update call
                .then(progress -> updateDomain(proxy, proxyClient, progress, isSingleSignOnUpdateRequired, request))
                // stabilize the resource i.e. wait till the resource is in the expected state (AVAILABLE)
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext(), DataZoneClientWrapper.STABILIZED_DOMAIN_STATUS))
                // read the resource
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private Boolean isSingleSignOnUpdateRequired(ResourceModel desiredResourceState,
                                                 ResourceModel previousResourceState) {
        SingleSignOn previousSignOn = previousResourceState.getSingleSignOn();
        SingleSignOn newSignOn = desiredResourceState.getSingleSignOn();

        // If SignOn was not specified previously or it was disabled
        if ((Objects.isNull(previousSignOn) || previousSignOn.getType().equals(DISABLED_SIGN_ON_TYPE))
                // and specified now then the same needs to be updated.
                && !Objects.isNull(newSignOn)) {
            logger.info("Updating signOn as the same was not specified previously.");
            return true;
        }

        // If both are null
        if ((Objects.isNull(previousSignOn) && Objects.isNull(newSignOn)) ||
                // or both are same
                previousSignOn.equals(newSignOn)) {
            // then the update is not required.
            return false;
        } else {
            // Else if the two are different, then we should throw exception as
            // SingleSignOn status can not be modified after enabled.
            throw new CfnInvalidRequestException(SIGN_ON_ERROR_STATUS);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateDomain(AmazonWebServicesClientProxy proxy,
                                                                       ProxyClient<DataZoneClient> proxyClient,
                                                                       ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                       Boolean isSingleSignOnUpdateRequired,
                                                                       ResourceHandlerRequest<ResourceModel> request) {
        final DomainSummary domainSummary = progress.getCallbackContext().getDomainSummary();
        // If the domain summary is not null then this implies that we created the domain in the previous stabilization
        // attempt and this attempt we just need to wait till domain gets stabilized.
        if (!Objects.isNull(domainSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-Domain::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(model -> Translator.translateToUpdateRequest(model, isSingleSignOnUpdateRequired))
                .makeServiceCall((updateDomainRequest, client) -> dataZoneClientWrapper.updateDomain(updateDomainRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext)
                // finally update tags if required
                .then(progress1 -> updateTags(proxy, proxyClient, progress, request));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(UpdateDomainRequest updateDomainRequest,
                                                                                      UpdateDomainResponse updateDomainResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully updated Domain with name %s and id %s", resourceModel.getName(), resourceModel.getId());
        callbackContext = CallbackContext.builder()
                .domainSummary(DomainSummary.builder()
                        .id(resourceModel.getId())
                        .arn(resourceModel.getArn())
                        .name(resourceModel.getName())
                        .build())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, callbackContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<DataZoneClient> proxyClient,
            ProgressEvent<ResourceModel, CallbackContext> progressEvent,
            ResourceHandlerRequest<ResourceModel> request) {
        ResourceModel model = progressEvent.getResourceModel();
        CallbackContext callbackContext = progressEvent.getCallbackContext();

        if (!TagHelper.shouldUpdateTags(request)) {
            return ProgressEvent.progress(model, callbackContext);
        }

        logger.info("Fetching domainArn to update tags...");
        ResourceModel receivedResourceModel =
                new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, this.logger.logger).getResourceModel();

        // Update ARN as CFn doesn't pass the same.
        model.setArn(receivedResourceModel.getArn());

        Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
        Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);

        Map<String, String> addedTags = TagHelper.generateTagsToAdd(previousTags, desiredTags);
        Set<String> removedTags = TagHelper.generateTagsToRemove(previousTags, desiredTags);

        return this.untagResource(proxy, proxyClient, model, request, callbackContext, removedTags)
                .then(progressEvent1 -> this.tagResource(proxy, proxyClient, model, request, callbackContext, addedTags));
    }

    private ProgressEvent<ResourceModel, CallbackContext>
    untagResource(final AmazonWebServicesClientProxy proxy,
                  final ProxyClient<DataZoneClient> serviceClient,
                  final ResourceModel resourceModel,
                  final ResourceHandlerRequest<ResourceModel> handlerRequest,
                  final CallbackContext callbackContext,
                  final Set<String> removedTags) {
        if (CollectionUtils.isEmpty(removedTags)) {
            return ProgressEvent.progress(resourceModel, callbackContext);
        }

        logger.info("[UPDATE][IN PROGRESS] Going to remove tags for resource: %s with AccountId: %s, removed tags %s",
                resourceModel.getName(), handlerRequest.getAwsAccountId(), removedTags);

        return proxy.initiate("AWS-DataZone-Domain::DeleteTags", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.untagResourceRequest(model, removedTags))
                .makeServiceCall((request, client) -> dataZoneClientWrapper.deleteTagsFromDomain(request))
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext>
    tagResource(final AmazonWebServicesClientProxy proxy,
                final ProxyClient<DataZoneClient> serviceClient,
                final ResourceModel resourceModel,
                final ResourceHandlerRequest<ResourceModel> handlerRequest,
                final CallbackContext callbackContext,
                final Map<String, String> addedTags) {
        if (Objects.isNull(addedTags) || addedTags.isEmpty()) {
            return ProgressEvent.progress(resourceModel, callbackContext);
        }

        logger.info("[UPDATE][IN PROGRESS] Going to add tags for resource: %s with AccountId: %s, tags %s",
                resourceModel.getName(), handlerRequest.getAwsAccountId(), addedTags);

        return proxy.initiate("AWS-DataZone-Domain::AddTags", serviceClient, resourceModel, callbackContext)
                .translateToServiceRequest(model -> Translator.tagResourceRequest(model, addedTags))
                .makeServiceCall((request, client) -> dataZoneClientWrapper.addTagsToDomain(request))
                .progress();
    }
}
