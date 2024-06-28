package software.amazon.datazone.projectmembership.client;

import com.amazonaws.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateProjectMembershipRequest;
import software.amazon.awssdk.services.datazone.model.CreateProjectMembershipResponse;
import software.amazon.awssdk.services.datazone.model.DeleteProjectMembershipRequest;
import software.amazon.awssdk.services.datazone.model.DeleteProjectMembershipResponse;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;
import software.amazon.datazone.projectmembership.helper.ProjectMembershipOperation;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    public static final String USER_ALREADY_EXISTS_EXCEPTION_MESSAGE = "User is already in the project";
    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    private static String getIdentifier(DeleteProjectMembershipRequest deleteProjectMembershipRequest) {
        return !StringUtils.isNullOrEmpty(deleteProjectMembershipRequest.member().groupIdentifier()) ?
                deleteProjectMembershipRequest.member().groupIdentifier() :
                deleteProjectMembershipRequest.member().userIdentifier();
    }

    private static String getIdentifier(CreateProjectMembershipRequest createProjectMembershipRequest) {
        return !StringUtils.isNullOrEmpty(createProjectMembershipRequest.member().groupIdentifier()) ?
                createProjectMembershipRequest.member().groupIdentifier() :
                createProjectMembershipRequest.member().userIdentifier();
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final ProjectMembershipOperation operation) {
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
            if (e.getMessage().contains(USER_ALREADY_EXISTS_EXCEPTION_MESSAGE)) {
                throw new CfnAlreadyExistsException(e);
            }
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

    public CreateProjectMembershipResponse createProjectMembership(final @NonNull CreateProjectMembershipRequest createProjectMembershipRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.join("|", createProjectMembershipRequest.domainIdentifier(),
                    createProjectMembershipRequest.projectIdentifier(), getIdentifier(createProjectMembershipRequest));
            return executeCall(ProjectMembershipOperation.CREATE_PROJECT_MEMBERSHIP, createProjectMembershipRequest, client::createProjectMembership, resourceIdentifier, proxyClient);
        }
    }

    public GetUserProfileResponse getUserProfile(final @NonNull GetUserProfileRequest getUserProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = getUserProfileRequest.domainIdentifier() + "|" + getUserProfileRequest.userIdentifier();
            return executeCall(ProjectMembershipOperation.GET_USER_PROFILE, getUserProfileRequest, client::getUserProfile, resourceIdentifier, proxyClient);
        }
    }

    public GetGroupProfileResponse getGroupProfile(final @NonNull GetGroupProfileRequest getGroupProfileRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = getGroupProfileRequest.domainIdentifier() + "|" + getGroupProfileRequest.groupIdentifier();
            return executeCall(ProjectMembershipOperation.GET_USER_PROFILE, getGroupProfileRequest, client::getGroupProfile, resourceIdentifier, proxyClient);
        }
    }

    public ListProjectMembershipsResponse listProjectMemberships(ListProjectMembershipsRequest listProjectMembershipsRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = listProjectMembershipsRequest.domainIdentifier() + "|" + listProjectMembershipsRequest.projectIdentifier();
            return executeCall(ProjectMembershipOperation.LIST_PROJECT_MEMBERSHIPS, listProjectMembershipsRequest, client::listProjectMemberships, resourceIdentifier, proxyClient);
        }
    }

    public DeleteProjectMembershipResponse deleteProjectMembership(final @NonNull DeleteProjectMembershipRequest deleteProjectMembershipRequest) {
        try (final var client = proxyClient.client()) {
            String resourceIdentifier = String.join("|", deleteProjectMembershipRequest.domainIdentifier(),
                    deleteProjectMembershipRequest.projectIdentifier(), getIdentifier(deleteProjectMembershipRequest));
            return executeCall(ProjectMembershipOperation.DELETE_PROJECT_MEMBERSHIP, deleteProjectMembershipRequest, client::deleteProjectMembership, resourceIdentifier, proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final ProjectMembershipOperation operation,
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
}
