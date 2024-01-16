package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientBuilder;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.Constants;
import software.amazon.datazone.datasource.helper.LoggerWrapper;
import software.amazon.datazone.datasource.helper.ResourceStabilizer;

import java.util.UUID;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    protected LoggerWrapper logger;
    protected DataZoneClientWrapper dataZoneClientWrapper;
    protected ResourceStabilizer stabilizer;

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

    protected CallbackContext getCallbackContext(CallbackContext callbackContext) {
        return callbackContext == null || callbackContext.getStabilizationRetriesRemaining() == null ?
                CallbackContext.builder().stabilizationRetriesRemaining(Constants.MAXIMUM_STABILIZATION_ATTEMPTS).build() : callbackContext;
    }
}
