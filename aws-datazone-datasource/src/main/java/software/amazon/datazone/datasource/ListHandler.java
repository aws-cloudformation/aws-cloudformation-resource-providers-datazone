package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListDataSourcesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.LoggerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                          ResourceHandlerRequest<ResourceModel> request,
                                                                          CallbackContext callbackContext,
                                                                          ProxyClient<DataZoneClient> proxyClient,
                                                                          Logger externalLogger) {
        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        // Call the API and get response
        ListDataSourcesResponse response = dataZoneClientWrapper.listDataSources(Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken()));
        String nextToken = response.nextToken();

        // Convert received DataSourceSummary to Resource Model.
        final List<ResourceModel> models = response.items().stream()
                .map(dataSourceSummary -> ResponseTranslator.getResourceModelFromDataSourceSummary(dataSourceSummary))
                .collect(Collectors.toList());

        // Return the event.
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
