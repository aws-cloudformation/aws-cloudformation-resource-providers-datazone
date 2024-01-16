package software.amazon.datazone.environmentprofile.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentProfileResponse;
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
import software.amazon.datazone.environmentprofile.helper.EnvironmentProfileOperation;
import software.amazon.datazone.environmentprofile.helper.LoggerWrapper;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public CreateEnvironmentProfileResponse createEnvironmentProfile(final @NonNull CreateEnvironmentProfileRequest createEnvironmentProfileRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentProfileOperation.CREATE_ENVIRONMENT_PROFILE, createEnvironmentProfileRequest, client::createEnvironmentProfile, createEnvironmentProfileRequest.name(), proxyClient);
        }
    }

    public GetEnvironmentProfileResponse getEnvironmentProfile(final @NonNull GetEnvironmentProfileRequest getEnvironmentProfileRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentProfileOperation.GET_ENVIRONMENT_PROFILE, getEnvironmentProfileRequest, client::getEnvironmentProfile, getEnvironmentProfileRequest.identifier(), proxyClient);
        }
    }

    public UpdateEnvironmentProfileResponse updateEnvironmentProfile(UpdateEnvironmentProfileRequest updateEnvironmentProfileRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentProfileOperation.UPDATE_ENVIRONMENT_PROFILE, updateEnvironmentProfileRequest, client::updateEnvironmentProfile, updateEnvironmentProfileRequest.identifier(), proxyClient);
        }
    }

    public ListEnvironmentProfilesResponse listEnvironmentProfile(ListEnvironmentProfilesRequest listEnvironmentProfilesRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentProfileOperation.LIST_ENVIRONMENT_PROFILES, listEnvironmentProfilesRequest, client::listEnvironmentProfiles, null, proxyClient);
        }
    }

    public DeleteEnvironmentProfileResponse deleteEnvironmentProfile(final @NonNull DeleteEnvironmentProfileRequest deleteEnvironmentProfileRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentProfileOperation.DELETE_ENVIRONMENT_PROFILE, deleteEnvironmentProfileRequest, client::deleteEnvironmentProfile, deleteEnvironmentProfileRequest.identifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final EnvironmentProfileOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on EnvironmentProfile with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final EnvironmentProfileOperation operation) {
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
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

}
