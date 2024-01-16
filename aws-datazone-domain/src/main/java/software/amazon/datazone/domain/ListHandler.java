package software.amazon.datazone.domain;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListDomainsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.domain.client.DataZoneClientWrapper;
import software.amazon.datazone.domain.helper.LoggerWrapper;

import java.util.List;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        // Call the API and get response
        ListDomainsResponse response = dataZoneClientWrapper.listDomains(Translator.translateToListRequest(request.getNextToken()));
        String nextToken = response.nextToken();

        // Convert received DomainSummary to Resource Model.
        final List<ResourceModel> models = response.items().stream()
                .map(domainSummary -> Translator.getResourceModelFromDomainSummary(domainSummary))
                .collect(Collectors.toList());

        // Return the event.
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }

}
