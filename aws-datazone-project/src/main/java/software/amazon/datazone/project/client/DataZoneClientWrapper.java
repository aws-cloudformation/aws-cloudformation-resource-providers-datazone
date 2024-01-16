package software.amazon.datazone.project.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateProjectRequest;
import software.amazon.awssdk.services.datazone.model.CreateProjectResponse;
import software.amazon.awssdk.services.datazone.model.DeleteProjectRequest;
import software.amazon.awssdk.services.datazone.model.DeleteProjectResponse;
import software.amazon.awssdk.services.datazone.model.GetProjectRequest;
import software.amazon.awssdk.services.datazone.model.GetProjectResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListProjectsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateProjectRequest;
import software.amazon.awssdk.services.datazone.model.UpdateProjectResponse;
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
import software.amazon.datazone.project.helper.ProjectOperation;

import java.util.function.Function;

@AllArgsConstructor
@Slf4j
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;

    public CreateProjectResponse createProject(final @NonNull CreateProjectRequest createProjectRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(ProjectOperation.CREATE_PROJECT, createProjectRequest, client::createProject, createProjectRequest.name(), proxyClient);
        }
    }

    public GetProjectResponse getProject(final @NonNull GetProjectRequest getProjectRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(ProjectOperation.GET_PROJECT, getProjectRequest, client::getProject, getProjectRequest.identifier(), proxyClient);
        }
    }

    public ListProjectsResponse listProject(final @NonNull ListProjectsRequest listProjectsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(ProjectOperation.LIST_PROJECT, listProjectsRequest, client::listProjects, listProjectsRequest.domainIdentifier(), proxyClient);
        }
    }

    public UpdateProjectResponse updateProject(UpdateProjectRequest updateProjectRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(ProjectOperation.UPDATE_PROJECT, updateProjectRequest, client::updateProject, updateProjectRequest.identifier(), proxyClient);
        }
    }

    public DeleteProjectResponse deleteProject(DeleteProjectRequest deleteProjectRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(ProjectOperation.DELETE_PROJECT, deleteProjectRequest, client::deleteProject, deleteProjectRequest.identifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final ProjectOperation operation,
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
                                                                           final ProjectOperation operation) {
        if (e instanceof AccessDeniedException) {
            return new CfnNotFoundException(e);
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

}
