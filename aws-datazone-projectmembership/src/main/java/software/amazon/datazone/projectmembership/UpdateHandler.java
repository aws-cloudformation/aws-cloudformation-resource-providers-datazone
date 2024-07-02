package software.amazon.datazone.projectmembership;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        logger.info("Received request for update for DomainId %s and MemberIdentifier %s ",
                request.getDesiredResourceState().getDomainIdentifier(), request.getDesiredResourceState().getMember());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> deleteProjectMembership(proxy, proxyClient, progress, dataZoneClientWrapper))
                .then(progress -> updateProjectMembership(proxy, proxyClient, progress, dataZoneClientWrapper));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateProjectMembership(AmazonWebServicesClientProxy proxy,
                                                                                  ProxyClient<DataZoneClient> proxyClient,
                                                                                  ProgressEvent<ResourceModel,
                                                                                          CallbackContext> progress,
                                                                                  DataZoneClientWrapper dataZoneClientWrapper) {
        return proxy.initiate("AWS-DataZone-ProjectMembership::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model))
                // make service call
                .makeServiceCall((createProjectMembershipRequest, client) -> dataZoneClientWrapper.createProjectMembership(createProjectMembershipRequest))
                // and update the model fields.
                .done((createProjectMembershipRequest, createProjectMembershipResponse, dataZoneClientProxyClient, resourceModel, callbackContext) ->
                        updateModelFields(createProjectMembershipRequest, resourceModel, dataZoneClientWrapper));
    }
}
