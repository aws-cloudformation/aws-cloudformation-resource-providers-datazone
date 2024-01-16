package software.amazon.datazone.subscriptiontarget;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListSubscriptionTargetsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.subscriptiontarget.client.DataZoneClientWrapper;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;

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
        logger.info("Request for list is %s", request);

        // Call the API and get response
        ListSubscriptionTargetsResponse response = dataZoneClientWrapper.listSubscriptionTargets(
                Translator.translateToListRequest(request.getDesiredResourceState(), request.getNextToken())
        );

        String nextToken = response.nextToken();

        // Convert received DomainSummary to Resource Model.
        final List<ResourceModel> models = response.items().stream()
                .map(subscriptionTargetSummary -> Translator.getResourceModelFromSummary(subscriptionTargetSummary))
                .collect(Collectors.toList());

        // Return the event.
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
