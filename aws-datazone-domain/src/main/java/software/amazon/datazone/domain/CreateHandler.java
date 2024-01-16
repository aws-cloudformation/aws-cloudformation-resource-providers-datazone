package software.amazon.datazone.domain;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateDomainRequest;
import software.amazon.awssdk.services.datazone.model.CreateDomainResponse;
import software.amazon.awssdk.services.datazone.model.DomainSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.domain.client.DataZoneClientWrapper;
import software.amazon.datazone.domain.helper.Constants;
import software.amazon.datazone.domain.helper.LoggerWrapper;
import software.amazon.datazone.domain.helper.ResourceStabilizer;

import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

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

        // Create the context
        // This would be used for retrying when the resource is in TRANSIENT states, and we need to retry again.
        final CallbackContext currentContext = callbackContext == null || callbackContext.getStabilizationRetriesRemaining() == null ?
                CallbackContext.builder().stabilizationRetriesRemaining(Constants.MAXIMUM_STABILIZATION_ATTEMPTS).build() :
                callbackContext;

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                // Make create call
                .then(progress -> createDomain(proxy, proxyClient, progress))
                // stabilize the resource i.e. wait till the resource is in the expected state (AVAILABLE)
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext(), DataZoneClientWrapper.STABILIZED_DOMAIN_STATUS))
                // read the resource
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createDomain(AmazonWebServicesClientProxy proxy,
                                                                       ProxyClient<DataZoneClient> proxyClient,
                                                                       ProgressEvent<ResourceModel, CallbackContext> progress) {
        final DomainSummary domainSummary = progress.getCallbackContext().getDomainSummary();
        // If the domain summary is not null then this implies that we created the domain in the previous stabilization
        // attempt and this attempt we just need to wait till domain gets stabilized.
        if (!Objects.isNull(domainSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-Domain::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model, getNewClientToken()))
                .makeServiceCall((createDomainRequest, client) -> dataZoneClientWrapper.createDomain(createDomainRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(CreateDomainRequest createDomainRequest,
                                                                                      CreateDomainResponse createDomainResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully created Domain with name %s and id %s", createDomainResponse.name(), createDomainResponse.id());
        resourceModel.setId(createDomainResponse.id());
        resourceModel.setArn(createDomainResponse.arn());
        resourceModel.setPortalUrl(createDomainResponse.portalUrl());

        CallbackContext updatedContext = CallbackContext.builder()
                .domainSummary(DomainSummary.builder()
                        .id(createDomainResponse.id())
                        .arn(createDomainResponse.arn())
                        .name(createDomainResponse.name())
                        .build())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

}
