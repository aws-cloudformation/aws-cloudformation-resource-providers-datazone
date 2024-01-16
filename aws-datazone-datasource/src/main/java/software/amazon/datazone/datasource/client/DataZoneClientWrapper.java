package software.amazon.datazone.datasource.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.DataSourceStatus;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.GetDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.GetDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListDataSourcesRequest;
import software.amazon.awssdk.services.datazone.model.ListDataSourcesResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceResponse;
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
import software.amazon.datazone.datasource.helper.DataSourceOperation;
import software.amazon.datazone.datasource.helper.LoggerWrapper;

import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private static final String NON_UNIQUE_DATA_SOURCE_ERROR_MESSAGE = "DataSource name must be unique for a given project.";

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public static final Set<DataSourceStatus> TRANSIENT_DATASOURCE_STATUS = Set.of(DataSourceStatus.CREATING, DataSourceStatus.DELETING, DataSourceStatus.UPDATING);
    public static final Set<DataSourceStatus> FAILED_DATASOURCE_STATUS = Set.of(DataSourceStatus.FAILED_CREATION, DataSourceStatus.FAILED_UPDATE, DataSourceStatus.FAILED_DELETION);
    public static final Set<DataSourceStatus> AVAILABLE_DATASOURCE_STATUS = Set.of(DataSourceStatus.READY, DataSourceStatus.RUNNING);

    public CreateDataSourceResponse createDataSource(final @NonNull CreateDataSourceRequest createDataSourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DataSourceOperation.CREATE_DATASOURCE, createDataSourceRequest, client::createDataSource, createDataSourceRequest.name(), proxyClient);
        }
    }

    public GetDataSourceResponse getDataSource(final @NonNull GetDataSourceRequest getDataSourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DataSourceOperation.GET_DATASOURCE, getDataSourceRequest, client::getDataSource, getDataSourceRequest.identifier(), proxyClient);
        }
    }

    public UpdateDataSourceResponse updateDataSource(UpdateDataSourceRequest updateDataSourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DataSourceOperation.UPDATE_DATASOURCE, updateDataSourceRequest, client::updateDataSource, updateDataSourceRequest.identifier(), proxyClient);
        }
    }

    public ListDataSourcesResponse listDataSources(ListDataSourcesRequest listDataSourcesRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DataSourceOperation.LIST_DATASOURCE, listDataSourcesRequest, client::listDataSources, null, proxyClient);
        }
    }

    public DeleteDataSourceResponse deleteDataSource(final @NonNull DeleteDataSourceRequest deleteDataSourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DataSourceOperation.DELETE_DATASOURCE, deleteDataSourceRequest, client::deleteDataSource, deleteDataSourceRequest.identifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final DataSourceOperation operation,
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
                                                                           final DataSourceOperation operation) {
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
            if (e.getMessage().contains(NON_UNIQUE_DATA_SOURCE_ERROR_MESSAGE)) {
                // For conflicts on DataSource Name Validation Exception is thrown.
                return new CfnAlreadyExistsException(e);
            }
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

}
