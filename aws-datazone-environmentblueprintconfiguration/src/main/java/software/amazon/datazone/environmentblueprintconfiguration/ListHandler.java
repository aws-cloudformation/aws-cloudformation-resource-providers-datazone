package software.amazon.datazone.environmentblueprintconfiguration;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentblueprintconfiguration.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentblueprintconfiguration.helper.LoggerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                          ResourceHandlerRequest<ResourceModel> request,
                                                                          CallbackContext callbackContext,
                                                                          ProxyClient<DataZoneClient> proxyClient,
                                                                          Logger externalLogger) {
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        final ListEnvironmentBlueprintConfigurationsRequest listRequest =
                Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken());

        ListEnvironmentBlueprintConfigurationsResponse listEnvironmentBlueprintConfigurationsResponse =
                dataZoneClientWrapper.listEnvironmentBlueprintConfigurations(listRequest);

        final List<ResourceModel> models = listEnvironmentBlueprintConfigurationsResponse.items().stream()
                .map(environmentBlueprintConfigurationItem -> Translator.getResourceModelFromItem(environmentBlueprintConfigurationItem))
                .collect(Collectors.toList());

        // Return the event.
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(listEnvironmentBlueprintConfigurationsResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
