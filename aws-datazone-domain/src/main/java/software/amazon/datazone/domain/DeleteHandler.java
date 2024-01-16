package software.amazon.datazone.domain;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.domain.client.DataZoneClientWrapper;
import software.amazon.datazone.domain.helper.Constants;
import software.amazon.datazone.domain.helper.LoggerWrapper;
import software.amazon.datazone.domain.helper.ResourceStabilizer;

import static software.amazon.datazone.domain.client.DataZoneClientWrapper.STABILIZED_DOMAIN_STATUS_FOR_DELETION;

public class DeleteHandler extends BaseHandlerStd {

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
                // STEP 2.0 [delete/stabilize progress chain - required for resource deletion]
                .then(progress -> this.deleteDomain(proxy, proxyClient, progress))
                .then(progress -> this.stabilizeDomainForDeletion(progress))
                .then(progress -> ProgressEvent.defaultSuccessHandler(progress.getResourceModel()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteDomain(AmazonWebServicesClientProxy proxy,
                                                                       ProxyClient<DataZoneClient> proxyClient,
                                                                       ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return proxy.initiate("AWS-DataZone-Domain::Delete", proxyClient, resourceModel, callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToDeleteRequest(model, getNewClientToken()))
                    .makeServiceCall((deleteDomainRequest, client) -> dataZoneClientWrapper.deleteDomain(deleteDomainRequest))
                    .progress();
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("Domain with name %s and id %s does not exist, skipping deletion...", resourceModel.getName(), resourceModel.getId());
            throw new CfnNotFoundException(exception);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeDomainForDeletion(ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return stabilizer.stabilizeResource(resourceModel, callbackContext, STABILIZED_DOMAIN_STATUS_FOR_DELETION);
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("Domain with name %s and id %s is deleted...", resourceModel.getName(), resourceModel.getId());
            return ProgressEvent.success(resourceModel, callbackContext);
        }
    }
}
