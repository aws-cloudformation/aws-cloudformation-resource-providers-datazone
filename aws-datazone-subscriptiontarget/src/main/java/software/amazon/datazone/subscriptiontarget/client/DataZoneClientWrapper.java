package software.amazon.datazone.subscriptiontarget.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.DeleteSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.DeleteSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.GetSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.GetSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListSubscriptionTargetsRequest;
import software.amazon.awssdk.services.datazone.model.ListSubscriptionTargetsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.UpdateSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.datazone.subscriptiontarget.helper.LoggerWrapper;
import software.amazon.datazone.subscriptiontarget.helper.SubscriptionTargetOperation;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public CreateSubscriptionTargetResponse createSubscriptionTarget(final @NonNull CreateSubscriptionTargetRequest createSubscriptionTargetRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(SubscriptionTargetOperation.CREATE_SUBSCRIPTION_TARGET, createSubscriptionTargetRequest, client::createSubscriptionTarget, createSubscriptionTargetRequest.name(), proxyClient);
        }
    }

    public GetSubscriptionTargetResponse getSubscriptionTarget(final @NonNull GetSubscriptionTargetRequest getSubscriptionTargetRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(SubscriptionTargetOperation.GET_SUBSCRIPTION_TARGET, getSubscriptionTargetRequest, client::getSubscriptionTarget, getSubscriptionTargetRequest.identifier(), proxyClient);
        }
    }

    public UpdateSubscriptionTargetResponse updateSubscriptionTarget(UpdateSubscriptionTargetRequest updateSubscriptionTargetRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(SubscriptionTargetOperation.UPDATE_SUBSCRIPTION_TARGET, updateSubscriptionTargetRequest, client::updateSubscriptionTarget, updateSubscriptionTargetRequest.identifier(), proxyClient);
        }
    }

    public ListSubscriptionTargetsResponse listSubscriptionTargets(ListSubscriptionTargetsRequest listSubscriptionTargetsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(SubscriptionTargetOperation.LIST_SUBSCRIPTION_TARGET, listSubscriptionTargetsRequest, client::listSubscriptionTargets, null, proxyClient);
        }
    }

    public DeleteSubscriptionTargetResponse deleteSubscriptionTarget(final @NonNull DeleteSubscriptionTargetRequest deleteSubscriptionTargetRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(SubscriptionTargetOperation.DELETE_SUBSCRIPTION_TARGET, deleteSubscriptionTargetRequest, client::deleteSubscriptionTarget, deleteSubscriptionTargetRequest.identifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final SubscriptionTargetOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to execute operation %s for resource %s, error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final SubscriptionTargetOperation operation) {
        if (e instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(e);
        } else if (e instanceof ConflictException) {
            return new CfnResourceConflictException(e);
        } else if (e instanceof InternalServerException) {
            return new CfnInternalFailureException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        } else if (e instanceof ServiceQuotaExceededException) {
            return new CfnServiceLimitExceededException(e);
        } else if (e instanceof ThrottlingException) {
            return new CfnThrottlingException(e);
        } else if (e instanceof UnauthorizedException) {
            return new CfnAccessDeniedException(e);
        } else if (e instanceof ValidationException) {
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

}
