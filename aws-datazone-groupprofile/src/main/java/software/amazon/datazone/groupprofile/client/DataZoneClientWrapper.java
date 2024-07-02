package software.amazon.datazone.groupprofile.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.SearchGroupProfilesRequest;
import software.amazon.awssdk.services.datazone.model.SearchGroupProfilesResponse;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.UpdateGroupProfileResponse;
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
import software.amazon.datazone.groupprofile.helper.GroupProfileOperation;
import software.amazon.datazone.groupprofile.helper.LoggerWrapper;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final GroupProfileOperation operation) {
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

    public CreateGroupProfileResponse createGroupProfile(final CreateGroupProfileRequest createGroupProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", createGroupProfileRequest.domainIdentifier(),
                    createGroupProfileRequest.groupIdentifier());
            return executeCall(GroupProfileOperation.CREATE_GROUP_PROFILE, createGroupProfileRequest,
                    client::createGroupProfile, resourceIdentifier, proxyClient);
        }
    }

    public UpdateGroupProfileResponse updateGroupProfile(UpdateGroupProfileRequest updateGroupProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", updateGroupProfileRequest.domainIdentifier(),
                    updateGroupProfileRequest.groupIdentifier());
            return executeCall(GroupProfileOperation.UPDATE_GROUP_PROFILE, updateGroupProfileRequest,
                    client::updateGroupProfile, resourceIdentifier, proxyClient);
        }
    }

    public GetGroupProfileResponse readGroupProfile(GetGroupProfileRequest readGroupProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", readGroupProfileRequest.domainIdentifier(),
                    readGroupProfileRequest.groupIdentifier());
            return executeCall(GroupProfileOperation.GET_GROUP_PROFILE, readGroupProfileRequest,
                    client::getGroupProfile, resourceIdentifier, proxyClient);
        }
    }

    public SearchGroupProfilesResponse searchGroupProfile(SearchGroupProfilesRequest searchGroupProfilesRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.format("%s-%s", searchGroupProfilesRequest.domainIdentifier(),
                    searchGroupProfilesRequest.groupType());
            return executeCall(GroupProfileOperation.GET_GROUP_PROFILE, searchGroupProfilesRequest,
                    client::searchGroupProfiles, resourceIdentifier, proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final GroupProfileOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on Group Profile with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }
}
