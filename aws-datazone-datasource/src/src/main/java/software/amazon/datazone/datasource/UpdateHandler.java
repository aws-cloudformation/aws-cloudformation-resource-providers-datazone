package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DataSourceSummary;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.LoggerWrapper;
import software.amazon.datazone.datasource.helper.ResourceStabilizer;

import java.util.Objects;

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
        this.stabilizer = new ResourceStabilizer(dataZoneClientWrapper, logger);

        // Create the context
        // This would be used for retrying when the resource is in TRANSIENT states, and we need to retry again.
        final CallbackContext currentContext = getCallbackContext(callbackContext);

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                // Make update call
                .then(progress -> updateDataSource(proxy, proxyClient, progress))
                // stabilize the resource i.e. wait till the resource is in the expected state.
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext()))
                // read the resource
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateDataSource(AmazonWebServicesClientProxy proxy,
                                                                           ProxyClient<DataZoneClient> proxyClient,
                                                                           ProgressEvent<ResourceModel, CallbackContext> progress) {
        final DataSourceSummary dataSourceSummary = progress.getCallbackContext().getDataSourceSummary();
        // If the dataSource Summary is not null then this implies that we updated the dataSource in the previous stabilization
        // attempt and this attempt we just need to wait till dataSource gets stabilized.
        if (!Objects.isNull(dataSourceSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-DataSource::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(model -> Translator.translateToUpdateRequest(model))
                .makeServiceCall((updateDataSourceRequest, client) -> dataZoneClientWrapper.updateDataSource(updateDataSourceRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(UpdateDataSourceRequest updateDataSourceRequest,
                                                                                      UpdateDataSourceResponse updateDataSourceResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully updated DataSource with name %s and id %s", updateDataSourceRequest.name(), updateDataSourceRequest.identifier());
        CallbackContext updatedContext = CallbackContext.builder()
                .dataSourceSummary(DataSourceSummary.builder()
                        .dataSourceId(resourceModel.getId())
                        .domainId(resourceModel.getDomainId())
                        .build())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

}
