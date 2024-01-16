package software.amazon.datazone.environment;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.EnvironmentStatus;
import software.amazon.awssdk.services.datazone.model.EnvironmentSummary;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;
import software.amazon.datazone.environment.helper.Constants;
import software.amazon.datazone.environment.helper.LoggerWrapper;
import software.amazon.datazone.environment.helper.ResourceStabilizer;

import java.util.Objects;

import static software.amazon.datazone.environment.helper.Constants.MAXIMUM_TIMEOUT_ATTEMPTS;

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

        final CallbackContext currentContext = getCallbackContext(callbackContext);

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                .then(progress -> deleteEnvironment(proxy, proxyClient, progress))
                .then(progress -> this.stabilizeEnvironmentForDeletion(progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    public ProgressEvent<ResourceModel, CallbackContext> deleteEnvironment(AmazonWebServicesClientProxy proxy,
                                                                           ProxyClient<DataZoneClient> proxyClient,
                                                                           ProgressEvent<ResourceModel, CallbackContext> progress) {
        final ResourceModel resourceModel = progress.getResourceModel();
        final CallbackContext callbackContext = progress.getCallbackContext();
        final EnvironmentSummary environmentSummary = progress.getCallbackContext().getEnvironmentSummary();
        // If the environmentSummary is not null then this implies that we deleted the environment in the previous stabilization
        // attempt and this attempt we just need to wait till environment gets stabilized.
        if (!Objects.isNull(environmentSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to delete the resource.
        try {
            return proxy.initiate("AWS-DataZone-Environment::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteEnvironmentRequest, client) -> dataZoneClientWrapper.deleteEnvironment(deleteEnvironmentRequest))
                    // and update the model fields and context
                    .done(this::updateContext);
        } catch (ResourceNotFoundException | CfnNotFoundException e) {
            logger.info("Environment with id %s and domainId %s does not exist, skipping deletion...",
                    resourceModel.getId(), resourceModel.getDomainIdentifier());
            if (progress.getCallbackContext().getTimeOutRetriesRemaining().equals(MAXIMUM_TIMEOUT_ATTEMPTS)) {
                // This indicates that no deletion call was previously made and hence Not found should be thrown.
                throw new CfnNotFoundException(e);
            } else {
                // This indicates that deletion call was previously made and hence success should be returned.
                return ProgressEvent.success(resourceModel, callbackContext);
            }
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateContext(DeleteEnvironmentRequest deleteEnvironmentRequest,
                                                                        DeleteEnvironmentResponse deleteEnvironmentResponse,
                                                                        ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                        ResourceModel resourceModel,
                                                                        CallbackContext callbackContext) {
        logger.info("Successfully Deleted Environment with id %s and domain id %s",
                deleteEnvironmentRequest.identifier(), deleteEnvironmentRequest.domainIdentifier());
        EnvironmentSummary environmentSummary = EnvironmentSummary.builder()
                .id(deleteEnvironmentRequest.identifier())
                .domainId(deleteEnvironmentRequest.domainIdentifier())
                .build();

        CallbackContext updatedContext = CallbackContext.builder()
                .environmentSummary(environmentSummary)
                .timeOutRetriesRemaining(callbackContext.getTimeOutRetriesRemaining())
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> stabilizeEnvironmentForDeletion(ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return stabilizer.stabilizeResource(resourceModel, callbackContext);
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("Environment with name %s and id %s is deleted...", resourceModel.getName(), resourceModel.getId());
            return ProgressEvent.success(resourceModel, callbackContext);
        } catch (CfnNotStabilizedException exception) {
            GetEnvironmentResponse getEnvironmentResponse = dataZoneClientWrapper.getEnvironment(Translator.translateToReadRequest(resourceModel));
            if (getEnvironmentResponse.status().equals(EnvironmentStatus.DELETE_FAILED) && isDeploymentTimedOut(getEnvironmentResponse)) {
                return retryDeletionForEnvironment(resourceModel, getEnvironmentResponse, callbackContext);
            } else {
                throw exception;
            }
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> retryDeletionForEnvironment(ResourceModel resourceModel, GetEnvironmentResponse getEnvironmentResponse, CallbackContext callbackContext) {
        String environmentName = resourceModel.getName();
        String environmentId = resourceModel.getId();
        String domainId = resourceModel.getDomainIdentifier();
        logger.info("Failed to stabilize environment with name %s and id %s and with domain id %s, status %s as the deployment timed out.",
                environmentName, environmentId, domainId, getEnvironmentResponse.status());

        final Integer timedOutRetriesRemaining = callbackContext.getTimeOutRetriesRemaining() - 1;

        if (timedOutRetriesRemaining == 0) {
            logger.info("Failed to stabilize environment with name %s, id %s and with domain id %s, status %s after %s retries after timed out.",
                    environmentName, environmentId, domainId, getEnvironmentResponse.status(), MAXIMUM_TIMEOUT_ATTEMPTS);
            String errorMessage = String.format("Environment %s with id %s and domain id %s failed to stabilize after all attempts, %s",
                    environmentName, environmentId, domainId, getEnvironmentResponse.lastDeployment());
            return ProgressEvent.failed(resourceModel, callbackContext, HandlerErrorCode.NotStabilized, errorMessage);
        } else {
            return ProgressEvent.defaultInProgressHandler(CallbackContext.builder()
                            .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining() - 1)
                            .timeOutRetriesRemaining(timedOutRetriesRemaining)
                            .environmentSummary(null) // A null here indicates that the next retry attempt should make another deletion call
                            .build(),
                    Constants.CALLBACK_DELAY_SECONDS, resourceModel);
        }
    }

    private static boolean isDeploymentTimedOut(GetEnvironmentResponse getEnvironmentResponse) {
        return getEnvironmentResponse.lastDeployment().failureReason().code().toString().equals("408");
    }
}
