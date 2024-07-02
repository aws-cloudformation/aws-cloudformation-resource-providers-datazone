package software.amazon.datazone.projectmembership;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        logger.info("Received request for delete for DomainId %s and MemberIdentifier  %s",
                request.getDesiredResourceState().getDomainIdentifier(), request.getDesiredResourceState().getMember());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> deleteProjectMembership(proxy, proxyClient, progress, dataZoneClientWrapper))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
