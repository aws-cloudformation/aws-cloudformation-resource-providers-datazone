package software.amazon.datazone.environment;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environment.client.DataZoneClientBuilder;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;
import software.amazon.datazone.environment.helper.Constants;
import software.amazon.datazone.environment.helper.LoggerWrapper;
import software.amazon.datazone.environment.helper.ResourceStabilizer;

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

    protected static CallbackContext getCallbackContext(CallbackContext callbackContext) {
        // Create the context
        // This would be used for retrying when the resource is in TRANSIENT states, and we need to retry again.
        final CallbackContext currentContext = callbackContext == null || callbackContext.getStabilizationRetriesRemaining() == null ?
                CallbackContext.builder().stabilizationRetriesRemaining(Constants.MAXIMUM_STABILIZATION_ATTEMPTS).timeOutRetriesRemaining(Constants.MAXIMUM_TIMEOUT_ATTEMPTS).build() :
                callbackContext;
        return currentContext;
    }
}
