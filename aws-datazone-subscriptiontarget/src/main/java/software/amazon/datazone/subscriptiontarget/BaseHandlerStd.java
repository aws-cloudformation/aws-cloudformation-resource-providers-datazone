package software.amazon.datazone.subscriptiontarget;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientBuilder;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientWrapper;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;

import java.util.UUID;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected LoggerWrapper logger;
    protected DataZoneClientWrapper dataZoneClientWrapper;

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

    void validateRequiredInputs(ResourceModel desiredResourceState) {
        if (StringUtils.isNullOrEmpty(desiredResourceState.getDomainIdentifier())) {
            throw new CfnInvalidRequestException("DomainIdentifier can not be empty.");
        }

        if (StringUtils.isNullOrEmpty(desiredResourceState.getEnvironmentIdentifier())) {
            throw new CfnInvalidRequestException("EnvironmentIdentifier can not be empty.");
        }
    }
}
