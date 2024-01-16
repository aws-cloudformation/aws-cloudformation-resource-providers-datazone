package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DataSourceSummary;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.LoggerWrapper;
import software.amazon.datazone.datasource.helper.ResourceStabilizer;

import java.util.Objects;

public class DeleteHandler extends BaseHandlerStd {

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

        logger.error("Received request for Delete %s", request);

        // Create the context
        // This would be used for retrying when the resource is in TRANSIENT states, and we need to retry again.
        final CallbackContext currentContext = getCallbackContext(callbackContext);

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                .then(progress -> this.deleteDataSource(proxy, proxyClient, progress))
                .then(progress -> this.stabilizeDataSourceForDeletion(progress))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteDataSource(AmazonWebServicesClientProxy proxy,
                                                                       ProxyClient<DataZoneClient> proxyClient,
                                                                       ProgressEvent<ResourceModel, CallbackContext> progress) {
        final ResourceModel resourceModel = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();
        final DataSourceSummary dataSourceSummary = callbackContext.getDataSourceSummary();
        // If the dataSource Summary is not null then this implies that we deleted the dataSource in the previous stabilization
        // attempt and this attempt we just need to wait till dataSource gets stabilized.
        if (!Objects.isNull(dataSourceSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        try {
            return proxy.initiate("AWS-DataZone-Domain::Delete", proxyClient, resourceModel, callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToDeleteRequest(model, getNewClientToken()))
                    .makeServiceCall((deleteDomainRequest, client) -> dataZoneClientWrapper.deleteDataSource(deleteDomainRequest))
                    .done(this::updateModelFieldsAndContext);
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("DataSource with name %s and id %s does not exist, skipping deletion...", resourceModel.getName(), resourceModel.getId());
            throw new CfnNotFoundException(exception);
        }

    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(DeleteDataSourceRequest deleteDataSourceRequest,
                                                                                      DeleteDataSourceResponse deleteDataSourceResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully deleted DataSource with name %s and id %s", resourceModel.getName(), resourceModel.getId());
        CallbackContext updatedContext = CallbackContext.builder()
                .dataSourceSummary(DataSourceSummary.builder()
                        .dataSourceId(resourceModel.getId())
                        .domainId(resourceModel.getDomainId())
                        .build())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeDataSourceForDeletion(ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return stabilizer.stabilizeResource(resourceModel, callbackContext);
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("DataSource with name %s and id %s is deleted...", resourceModel.getName(), resourceModel.getId());
            return ProgressEvent.success(resourceModel, callbackContext);
        }
    }
}
