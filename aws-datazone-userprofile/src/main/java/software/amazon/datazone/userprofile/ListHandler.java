package software.amazon.datazone.userprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.SearchUserProfilesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.userprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        // Call the API and get response
        SearchUserProfilesResponse response = dataZoneClientWrapper.searchUserProfile(
                Translator.translateToSearchRequest(request.getDesiredResourceState(), request.getNextToken())
        );

        String nextToken = response.nextToken();

        // Convert received DomainSummary to Resource Model.
        final List<ResourceModel> models = response.items().stream()
                .map(userSummary -> Translator.getResourceModelFromSummary(userSummary, getDomain(request.getDesiredResourceState())))
                .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
