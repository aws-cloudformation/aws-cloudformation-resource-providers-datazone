package software.amazon.datazone.userprofile.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.SearchUserProfilesRequest;
import software.amazon.awssdk.services.datazone.model.SearchUserProfilesResponse;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UpdateUserProfileResponse;
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
import software.amazon.datazone.userprofile.helper.DomainOperation;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;
import software.amazon.datazone.userprofile.helper.UserProfileOperation;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final UserProfileOperation operation) {
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
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final DomainOperation operation) {
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
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

    public GetDomainResponse getDomain(final @NonNull GetDomainRequest getDomainRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.GET_DOMAIN, getDomainRequest, client::getDomain, getDomainRequest.identifier(), proxyClient);
        }
    }

    public CreateUserProfileResponse createUserProfile(final CreateUserProfileRequest createUserProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", createUserProfileRequest.domainIdentifier(),
                    createUserProfileRequest.userIdentifier());
            return executeCall(UserProfileOperation.CREATE_USER_PROFILE, createUserProfileRequest,
                    client::createUserProfile, resourceIdentifier, proxyClient);
        }
    }

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileRequest updateUserProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", updateUserProfileRequest.domainIdentifier(),
                    updateUserProfileRequest.userIdentifier());
            return executeCall(UserProfileOperation.UPDATE_USER_PROFILE, updateUserProfileRequest,
                    client::updateUserProfile, resourceIdentifier, proxyClient);
        }
    }

    public GetUserProfileResponse readUserProfile(GetUserProfileRequest readUserProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", readUserProfileRequest.domainIdentifier(),
                    readUserProfileRequest.userIdentifier());
            return executeCall(UserProfileOperation.GET_USER_PROFILE, readUserProfileRequest,
                    client::getUserProfile, resourceIdentifier, proxyClient);
        }
    }

    public SearchUserProfilesResponse searchUserProfile(SearchUserProfilesRequest searchUserProfilesRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", searchUserProfilesRequest.domainIdentifier(),
                    searchUserProfilesRequest.searchText());
            return executeCall(UserProfileOperation.SEARCH_USER_PROFILES, searchUserProfilesRequest,
                    client::searchUserProfiles, resourceIdentifier, proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final UserProfileOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on User Profile with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final DomainOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on Domain with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

}
