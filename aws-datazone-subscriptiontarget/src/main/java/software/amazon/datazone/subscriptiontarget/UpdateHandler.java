package software.amazon.datazone.subscriptiontarget;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientWrapper;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;

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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateSubscriptionTarget(proxy, proxyClient, progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateSubscriptionTarget(AmazonWebServicesClientProxy proxy,
                                                                                   ProxyClient<DataZoneClient> proxyClient,
                                                                                   ProgressEvent<ResourceModel, CallbackContext> progress) {
        // Call DataZone Control Plane to update the resource.
        return proxy.initiate("AWS-DataZone-SubscriptionTarget::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Update Request
                .translateToServiceRequest(model -> Translator.translateToUpdateRequest(model))
                // make service call
                .makeServiceCall((updateSubscriptionTargetRequest, client) -> dataZoneClientWrapper.updateSubscriptionTarget(updateSubscriptionTargetRequest))
                .progress();
    }

}
