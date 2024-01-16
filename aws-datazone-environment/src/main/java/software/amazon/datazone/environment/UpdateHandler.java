package software.amazon.datazone.environment;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.EnvironmentSummary;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;
import software.amazon.datazone.environment.helper.LoggerWrapper;
import software.amazon.datazone.environment.helper.ResourceStabilizer;

import java.util.Objects;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialize
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);
        this.stabilizer = new ResourceStabilizer(dataZoneClientWrapper, logger);

        final CallbackContext currentContext = getCallbackContext(callbackContext);

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                .then(progress -> updateEnvironment(proxy, proxyClient, progress))
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext()))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateEnvironment(AmazonWebServicesClientProxy proxy,
                                                                            ProxyClient<DataZoneClient> proxyClient,
                                                                            ProgressEvent<ResourceModel, CallbackContext> progress) {
        final EnvironmentSummary environmentSummary = progress.getCallbackContext().getEnvironmentSummary();
        // If the environmentSummary is not null then this implies that we updated the environment in the previous stabilization
        // attempt and this attempt we just need to wait till environment gets stabilized.
        if (!Objects.isNull(environmentSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to update the resource.
        return proxy.initiate("AWS-DataZone-Environment::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::translateToFirstUpdateRequest)
                .makeServiceCall((updateEnvironmentRequest, client) -> dataZoneClientWrapper.updateEnvironment(updateEnvironmentRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(UpdateEnvironmentRequest updateEnvironmentRequest,
                                                                                      UpdateEnvironmentResponse updateEnvironmentResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully updated Environment with name %s and id %s with domain id %s",
                updateEnvironmentResponse.name(), updateEnvironmentResponse.id(), updateEnvironmentResponse.domainId());
        resourceModel.setId(updateEnvironmentResponse.id());
        resourceModel.setDomainIdentifier(updateEnvironmentRequest.domainIdentifier());
        EnvironmentSummary environmentSummary = EnvironmentSummary.builder()
                .id(updateEnvironmentResponse.id())
                .domainId(updateEnvironmentResponse.domainId())
                .name(updateEnvironmentResponse.name())
                .projectId(updateEnvironmentResponse.projectId())
                .build();

        CallbackContext updatedContext = CallbackContext.builder()
                .environmentSummary(environmentSummary)
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }
}
