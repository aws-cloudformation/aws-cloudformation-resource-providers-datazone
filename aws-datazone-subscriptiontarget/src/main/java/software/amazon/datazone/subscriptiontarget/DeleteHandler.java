package software.amazon.datazone.subscriptiontarget;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientWrapper;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;

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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                // Make create call
                .then(progress -> deleteSubscriptionTarget(proxy, proxyClient, progress))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteSubscriptionTarget(AmazonWebServicesClientProxy proxy,
                                                                                   ProxyClient<DataZoneClient> proxyClient,
                                                                                   ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return proxy.initiate("AWS-DataZone-SubscriptionTarget::Delete", proxyClient, resourceModel, callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToDeleteRequest(model))
                    .makeServiceCall((deleteSubscriptionTargetRequest, client) -> dataZoneClientWrapper.deleteSubscriptionTarget(deleteSubscriptionTargetRequest))
                    .progress();
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("Domain with name %s and id %s does not exist, skipping deletion...", resourceModel.getName(), resourceModel.getId());
            throw new CfnNotFoundException(exception);
        }
    }
}
