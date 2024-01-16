package software.amazon.datazone.environmentprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentprofile.helper.LoggerWrapper;

import java.util.List;

public class ListHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        final ListEnvironmentProfilesRequest listEnvironmentProfilesRequest = Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken());
        ListEnvironmentProfilesResponse listEnvironmentProfilesResponse = this.dataZoneClientWrapper.listEnvironmentProfile(listEnvironmentProfilesRequest);
        String nextToken = listEnvironmentProfilesResponse.nextToken();
        final List<ResourceModel> models = Translator.translateFromListRequest(listEnvironmentProfilesResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
