package software.amazon.datazone.environment.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.EnvironmentStatus;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.datazone.environment.helper.EnvironmentOperation;
import software.amazon.datazone.environment.helper.LoggerWrapper;

import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    public static final String ENVIRONMENT_IN_DELETED_STATE_ERROR_MESSAGE = "Cannot delete the environment in DELETED state";
    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public static final Set<EnvironmentStatus> TRANSIENT_ENVIRONMENT_STATUS = Set.of(
            EnvironmentStatus.CREATING,
            EnvironmentStatus.DELETING,
            EnvironmentStatus.UPDATING);

    public static final Set<EnvironmentStatus> FAILED_ENVIRONMENT_STATUS = Set.of(
            EnvironmentStatus.CREATE_FAILED,
            EnvironmentStatus.UPDATE_FAILED,
            EnvironmentStatus.DELETE_FAILED,
            EnvironmentStatus.VALIDATION_FAILED);

    public static final Set<EnvironmentStatus> STABILIZED_ENVIRONMENT_STATUS = Set.of(
            EnvironmentStatus.ACTIVE,
            EnvironmentStatus.SUSPENDED,
            EnvironmentStatus.DISABLED,
            EnvironmentStatus.EXPIRED,
            EnvironmentStatus.DELETED,
            EnvironmentStatus.INACCESSIBLE);

    public CreateEnvironmentResponse createEnvironment(final @NonNull CreateEnvironmentRequest createEnvironment) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentOperation.CREATE_ENVIRONMENT, createEnvironment, client::createEnvironment, createEnvironment.name(), proxyClient);
        }
    }

    public GetEnvironmentResponse getEnvironment(final @NonNull GetEnvironmentRequest getEnvironmentRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentOperation.GET_ENVIRONMENT, getEnvironmentRequest, client::getEnvironment, getEnvironmentRequest.identifier(), proxyClient);
        }
    }

    public UpdateEnvironmentResponse updateEnvironment(UpdateEnvironmentRequest updateEnvironmentRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentOperation.UPDATE_ENVIRONMENT, updateEnvironmentRequest, client::updateEnvironment, updateEnvironmentRequest.identifier(), proxyClient);
        }
    }

    public DeleteEnvironmentResponse deleteEnvironment(DeleteEnvironmentRequest deleteEnvironmentRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentOperation.DELETE_ENVIRONMENT, deleteEnvironmentRequest, client::deleteEnvironment, deleteEnvironmentRequest.identifier(), proxyClient);
        }
    }

    public ListEnvironmentsResponse listEnvironment(ListEnvironmentsRequest listEnvironmentsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentOperation.LIST_ENVIRONMENTS, listEnvironmentsRequest, client::listEnvironments, listEnvironmentsRequest.domainIdentifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final EnvironmentOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final EnvironmentOperation operation) {
        if (e instanceof AccessDeniedException) {
            return new CfnAccessDeniedException(e);
        } else if (e instanceof ConflictException) {
            return new CfnAlreadyExistsException(e);
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
            if (e.getMessage().contains(ENVIRONMENT_IN_DELETED_STATE_ERROR_MESSAGE)) {
                return new CfnNotFoundException(e);
            }
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

}
