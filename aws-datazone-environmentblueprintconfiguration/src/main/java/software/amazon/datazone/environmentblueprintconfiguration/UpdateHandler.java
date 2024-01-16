package software.amazon.datazone.environmentblueprintconfiguration;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentblueprintconfiguration.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentblueprintconfiguration.helper.LoggerWrapper;

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
                .then(progress -> validateEnvironmentBlueprintConfigurationForUpdate(proxy, proxyClient, progress))
                .then(progress -> putEnvironmentBlueprintConfiguration("Update", proxy, proxyClient, progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> validateEnvironmentBlueprintConfigurationForUpdate(
            AmazonWebServicesClientProxy proxy,
            ProxyClient<DataZoneClient> proxyClient,
            ProgressEvent<ResourceModel,
                    CallbackContext> progress) {
        return proxy.initiate("AWS-DataZone-EnvironmentBlueprintConfiguration::Update::PreExistenceCheck", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(model -> Translator.translateToReadRequest(model))
                .makeServiceCall((getEnvironmentBlueprintConfigurationRequest, client) -> dataZoneClientWrapper.getEnvironmentBlueprintConfiguration(getEnvironmentBlueprintConfigurationRequest))
                .progress();
    }
}
