package software.amazon.datazone.subscriptiontarget;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientWrapper;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;

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

        this.validateRequiredInputs(request.getDesiredResourceState());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                // Make create call
                .then(progress -> createSubscriptionTarget(proxy, proxyClient, progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createSubscriptionTarget(AmazonWebServicesClientProxy proxy,
                                                                                   ProxyClient<DataZoneClient> proxyClient,
                                                                                   ProgressEvent<ResourceModel, CallbackContext> progress) {
        // Call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-SubscriptionTarget::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model, getNewClientToken()))
                // make service call
                .makeServiceCall((createSubscriptionTargetRequest, client) -> dataZoneClientWrapper.createSubscriptionTarget(createSubscriptionTargetRequest))
                // and update the model fields.
                .done(this::updateModelFields);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFields(CreateSubscriptionTargetRequest createSubscriptionTargetRequest,
                                                                            CreateSubscriptionTargetResponse createSubscriptionTargetResponse,
                                                                            ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                            ResourceModel resourceModel,
                                                                            CallbackContext callbackContext) {
        logger.info("Successfully created SubscriptionTarget with name %s and id %s", createSubscriptionTargetRequest.name(),
                createSubscriptionTargetResponse.id());
        resourceModel.setId(createSubscriptionTargetResponse.id());
        resourceModel.setDomainId(createSubscriptionTargetRequest.domainIdentifier());
        resourceModel.setEnvironmentId(createSubscriptionTargetRequest.environmentIdentifier());

        return ProgressEvent.progress(resourceModel, callbackContext);
    }

}
