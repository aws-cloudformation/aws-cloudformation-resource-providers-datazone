package software.amazon.datazone.domain.helper;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.DomainStatus;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.datazone.domain.CallbackContext;
import software.amazon.datazone.domain.ResourceModel;
import software.amazon.datazone.domain.Translator;
import software.amazon.datazone.domain.client.DataZoneClientWrapper;

import java.util.Set;

import static software.amazon.datazone.domain.helper.Constants.INVALID_STATUS_ERROR;
import static software.amazon.datazone.domain.helper.Constants.MAXIMUM_STABILIZATION_ATTEMPTS;

@AllArgsConstructor
public class ResourceStabilizer {
    private final DataZoneClientWrapper dataZoneClientWrapper;
    private final LoggerWrapper logger;

    public ProgressEvent<ResourceModel, CallbackContext> stabilizeResource(final @NonNull ResourceModel model,
                                                                           final @NonNull CallbackContext callbackContext,
                                                                           final @NonNull Set<DomainStatus> stabilizedDomainStatuses) {
        String domainName = model.getName();
        logger.info("Validating status for domain with id %s and name %s", model.getId(), domainName);

        // Get the domain using getDomain call
        GetDomainResponse getDomainResponse = dataZoneClientWrapper.getDomain(Translator.translateToReadRequest(model));

        // Validate the current status for the domain
        DomainStatus status = getDomainResponse.status();
        if (stabilizedDomainStatuses.contains(status)) {
            logger.info("Domain with name %s and id %s is stabilized.", domainName, model.getId());
            return ProgressEvent.progress(model, callbackContext);
        } else if (DataZoneClientWrapper.TRANSIENT_DOMAIN_STATUS.contains(status)) {
            final Integer stabilizationRetriesRemaining = callbackContext.getStabilizationRetriesRemaining() - 1;
            if (stabilizationRetriesRemaining == 0) {
                logger.info("Failed to stabilize domain with name %s and id %s, status %s after %s retries.",
                        domainName, model.getId(), status, MAXIMUM_STABILIZATION_ATTEMPTS);
                String errorMessage = String.format("Domain %s failed to stabilize after all attempts", domainName);
                return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
            }
            return ProgressEvent.defaultInProgressHandler(CallbackContext.builder()
                            .stabilizationRetriesRemaining(stabilizationRetriesRemaining)
                            .domainSummary(callbackContext.getDomainSummary())
                            .build(),
                    Constants.CALLBACK_DELAY_SECONDS, model);
        } else if (DataZoneClientWrapper.FAILED_DOMAIN_STATUS.contains(status)) {
            logger.info("Failed to stabilize domain with name %s and id %s, status %s.", domainName, model.getId(), status);
            String errorMessage = String.format("Domain %s failed to stabilize due to internal failure", domainName);
            return ProgressEvent.failed(model, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
        }

        // If the current status for the domain is anything else than the above status then we received a wrong status
        // i.e. the resource went to an unexpected status for e.g. DELETED when we wanted to create the resource.
        String invalidStatusErrorMessage = String.format(INVALID_STATUS_ERROR, domainName, status.toString());
        throw new CfnInternalFailureException(new RuntimeException(invalidStatusErrorMessage));
    }
}
