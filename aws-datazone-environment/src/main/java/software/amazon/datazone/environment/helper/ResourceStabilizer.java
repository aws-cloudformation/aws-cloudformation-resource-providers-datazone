package software.amazon.datazone.environment.helper;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.EnvironmentStatus;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.datazone.environment.CallbackContext;
import software.amazon.datazone.environment.ResourceModel;
import software.amazon.datazone.environment.Translator;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;

import static software.amazon.datazone.environment.helper.Constants.INVALID_STATUS_ERROR;
import static software.amazon.datazone.environment.helper.Constants.MAXIMUM_STABILIZATION_ATTEMPTS;

@AllArgsConstructor
public class ResourceStabilizer {
    private final DataZoneClientWrapper dataZoneClientWrapper;
    private final LoggerWrapper logger;

    public ProgressEvent<ResourceModel, CallbackContext> stabilizeResource(final @NonNull ResourceModel model,
                                                                           final @NonNull CallbackContext callbackContext) {
        String environmentName = model.getName();
        String environmentId = model.getId();
        String domainId = model.getDomainIdentifier();
        logger.info("Validating status for environment with id %s and name %s with domain id %s",
                environmentId, environmentName, domainId);

        // Get the environment using getEnvironment call
        GetEnvironmentResponse getEnvironmentResponse = dataZoneClientWrapper.getEnvironment(Translator.translateToReadRequest(model));

        // Validate the current status for the environment
        EnvironmentStatus status = getEnvironmentResponse.status();
        if (DataZoneClientWrapper.STABILIZED_ENVIRONMENT_STATUS.contains(status)) {
            logger.info("Environment with name %s and id %s and with domain id %s is stabilized.",
                    environmentName, environmentId, domainId);
            return ProgressEvent.progress(model, callbackContext);
        } else if (DataZoneClientWrapper.TRANSIENT_ENVIRONMENT_STATUS.contains(status)) {
            final Integer stabilizationRetriesRemaining = callbackContext.getStabilizationRetriesRemaining() - 1;
            if (stabilizationRetriesRemaining == 0) {
                logger.info("Failed to stabilize environment with name %s, id %s and with domain id %s, status %s after %s retries.",
                        environmentName, environmentId, domainId, status, MAXIMUM_STABILIZATION_ATTEMPTS);
                String errorMessage = String.format("Environment %s with id %s and domain id %s failed to stabilize after all attempts, error %s",
                        environmentName, environmentId, domainId, getEnvironmentResponse.lastDeployment());
                return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
            }
            return ProgressEvent.defaultInProgressHandler(CallbackContext.builder()
                            .stabilizationRetriesRemaining(stabilizationRetriesRemaining)
                            .timeOutRetriesRemaining(callbackContext.getTimeOutRetriesRemaining())
                            .environmentSummary(callbackContext.getEnvironmentSummary())
                            .build(),
                    Constants.CALLBACK_DELAY_SECONDS, model);
        } else if (DataZoneClientWrapper.FAILED_ENVIRONMENT_STATUS.contains(status)) {
            logger.info("Failed to stabilize environment with name %s and id %s and with domain id %s, status %s, deployment status",
                    environmentName, environmentId, domainId, status, getEnvironmentResponse.lastDeployment());
            String errorMessage = String.format("Environment %s with id %s and domain id %s failed to stabilize due to internal failure, last deployment status %s",
                    environmentName, environmentId, domainId, getEnvironmentResponse.lastDeployment());
            throw new CfnNotStabilizedException(new RuntimeException(errorMessage));
        }

        // If the current status for the environment is anything else than the above status then we received a wrong status
        // i.e. the resource went to an unexpected status for e.g. DELETED when we wanted to create the resource.
        String invalidStatusErrorMessage = String.format(INVALID_STATUS_ERROR, environmentName, status.toString());
        throw new CfnInternalFailureException(new RuntimeException(invalidStatusErrorMessage));
    }
}
