package software.amazon.datazone.environmentblueprintconfiguration;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentblueprintconfiguration.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentblueprintconfiguration.helper.LoggerWrapper;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return proxy.initiate("AWS-DataZone-EnvironmentBlueprintConfiguration::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(model -> Translator.translateToReadRequest(model))
                .makeServiceCall((getEnvironmentBlueprintConfigurationRequest, client) -> dataZoneClientWrapper.getEnvironmentBlueprintConfiguration(getEnvironmentBlueprintConfigurationRequest))
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse)));
    }
}
