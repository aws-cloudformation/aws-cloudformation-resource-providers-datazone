package software.amazon.datazone.datasource;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.DataSourceSummary;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.LoggerWrapper;
import software.amazon.datazone.datasource.helper.ResourceStabilizer;

import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

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
                // Make create call
                .then(progress -> createDataSource(proxy, proxyClient, progress))
                // stabilize the resource i.e. wait till the resource is in the expected state.
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext()))
                // read the resource
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createDataSource(AmazonWebServicesClientProxy proxy,
                                                                           ProxyClient<DataZoneClient> proxyClient,
                                                                           ProgressEvent<ResourceModel, CallbackContext> progress) {
        this.validateRequiredInputs(progress.getResourceModel());
        final DataSourceSummary dataSourceSummary = progress.getCallbackContext().getDataSourceSummary();
        // If the dataSource Summary is not null then this implies that we created the dataSource in the previous stabilization
        // attempt and this attempt we just need to wait till dataSource gets stabilized.
        if (!Objects.isNull(dataSourceSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-DataSource::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model, getNewClientToken()))
                .makeServiceCall((createDomainRequest, client) -> dataZoneClientWrapper.createDataSource(createDomainRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(CreateDataSourceRequest createDataSourceRequest,
                                                                                      CreateDataSourceResponse createDataSourceResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully created DataSource with name %s and id %s", createDataSourceRequest.name(), createDataSourceResponse.id());
        resourceModel.setId(createDataSourceResponse.id());
        resourceModel.setDomainId(createDataSourceRequest.domainIdentifier());

        CallbackContext updatedContext = CallbackContext.builder()
                .dataSourceSummary(DataSourceSummary.builder()
                        .dataSourceId(resourceModel.getId())
                        .domainId(resourceModel.getDomainId())
                        .build())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

    private void validateRequiredInputs(ResourceModel resourceModel) {
        if (StringUtils.isNullOrEmpty(resourceModel.getDomainIdentifier())) {
            throw new CfnInvalidRequestException("DomainIdentifier can not be empty.");
        } else if (StringUtils.isNullOrEmpty(resourceModel.getEnvironmentIdentifier())) {
            throw new CfnInvalidRequestException("EnvironmentProfileIdentifier can not be empty.");
        } else if (StringUtils.isNullOrEmpty(resourceModel.getProjectIdentifier())) {
            throw new CfnInvalidRequestException("ProjectIdentifier can not be empty.");
        }
    }

}
