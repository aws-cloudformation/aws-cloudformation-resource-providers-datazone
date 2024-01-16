package software.amazon.datazone.project;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListProjectsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.project.client.DataZoneClientWrapper;
import software.amazon.datazone.project.helper.LoggerWrapper;

import java.util.List;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient);

        final ListProjectsRequest listProjectsRequest = Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken());

        ListProjectsResponse listProjectsResponse = dataZoneClientWrapper.listProject(listProjectsRequest);
        String nextToken = listProjectsResponse.nextToken();
        final List<ResourceModel> models = Translator.translateFromListRequest(listProjectsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
