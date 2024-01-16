package software.amazon.datazone.environment;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;
import software.amazon.datazone.environment.helper.LoggerWrapper;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialize
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return proxy.initiate("AWS-DataZone-Environment::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getEnvironmentRequest, client) -> {
                    GetEnvironmentResponse getEnvironmentResponse = dataZoneClientWrapper.getEnvironment(getEnvironmentRequest);
                    logger.info(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return getEnvironmentResponse;
                })

                .done(getEnvironmentResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getEnvironmentResponse)));
    }
}
