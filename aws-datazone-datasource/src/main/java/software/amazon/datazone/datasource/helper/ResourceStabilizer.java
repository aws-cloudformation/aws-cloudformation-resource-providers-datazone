package software.amazon.datazone.datasource.helper;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.DataSourceStatus;
import software.amazon.awssdk.services.datazone.model.GetDataSourceResponse;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.datazone.datasource.CallbackContext;
import software.amazon.datazone.datasource.ResourceModel;
import software.amazon.datazone.datasource.Translator;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;

import static software.amazon.datazone.datasource.helper.Constants.INVALID_STATUS_ERROR;
import static software.amazon.datazone.datasource.helper.Constants.MAXIMUM_STABILIZATION_ATTEMPTS;


@AllArgsConstructor
public class ResourceStabilizer {
    private final DataZoneClientWrapper dataZoneClientWrapper;
    private final LoggerWrapper logger;

    public ProgressEvent<ResourceModel, CallbackContext> stabilizeResource(final @NonNull ResourceModel model,
                                                                           final @NonNull CallbackContext callbackContext) {
        String dataSourceName = model.getName();
        logger.info("Validating status for datasource with id %s and name %s", model.getId(), dataSourceName);

        // Get the datasource using getDataSourceResponse call
        GetDataSourceResponse getDataSourceResponse = dataZoneClientWrapper.getDataSource(Translator.translateToReadRequest(model));

        // Validate the current status for the data source
        DataSourceStatus status = getDataSourceResponse.status();
        if (DataZoneClientWrapper.AVAILABLE_DATASOURCE_STATUS.contains(status)) {
            logger.info("Datasource with name %s and id %s is stabilized.", dataSourceName, model.getId());
            return ProgressEvent.progress(model, callbackContext);
        } else if (DataZoneClientWrapper.TRANSIENT_DATASOURCE_STATUS.contains(status)) {
            final Integer stabilizationRetriesRemaining = callbackContext.getStabilizationRetriesRemaining() - 1;
            if (stabilizationRetriesRemaining == 0) {
                logger.info("Failed to stabilize datasource with name %s and id %s, status %s after %s retries.",
                        dataSourceName, model.getId(), status, MAXIMUM_STABILIZATION_ATTEMPTS);
                String errorMessage = String.format("DataSource %s failed to stabilize after all attempts, error %s",
                        dataSourceName, getDataSourceResponse.errorMessage());
                return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
            }
            return ProgressEvent.defaultInProgressHandler(CallbackContext.builder()
                            .stabilizationRetriesRemaining(stabilizationRetriesRemaining)
                            .dataSourceSummary(callbackContext.getDataSourceSummary())
                            .build(),
                    Constants.CALLBACK_DELAY_SECONDS, model);
        } else if (DataZoneClientWrapper.FAILED_DATASOURCE_STATUS.contains(status)) {
            logger.info("Failed to stabilize datasource with name %s and id %s, status %s, error: %s.",
                    dataSourceName, model.getId(), status, getDataSourceResponse.errorMessage());
            String errorMessage = String.format("Datasource %s failed to stabilize due to internal failure, error: %s",
                    dataSourceName, getDataSourceResponse.errorMessage());
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
        }

        // If the current status for the datasource is anything else than the above status then we received a wrong status.
        // Ideally this should never occur unless new states are introduced.
        String invalidStatusErrorMessage = String.format(INVALID_STATUS_ERROR, dataSourceName, status.toString());
        throw new CfnInternalFailureException(new RuntimeException(invalidStatusErrorMessage));
    }
}
