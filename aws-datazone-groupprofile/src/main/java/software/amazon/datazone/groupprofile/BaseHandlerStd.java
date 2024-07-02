package software.amazon.datazone.groupprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.groupprofile.client.DataZoneClientBuilder;
import software.amazon.datazone.groupprofile.helper.LoggerWrapper;

import java.util.Optional;
import java.util.UUID;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected LoggerWrapper logger;

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(DataZoneClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger logger);

    /**
     * Helper function to return a unique token for the request.
     *
     * @return A unique token for the request.
     */
    protected String getNewClientToken() {
        return UUID.randomUUID().toString();
    }

    protected String getDomain(ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }

    protected String getGroupId(ResourceModel model) {
        return Optional.ofNullable(model.getId()).orElse(model.getGroupIdentifier());
    }
}
