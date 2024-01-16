package software.amazon.datazone.environmentblueprintconfiguration.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentBlueprintConfigurationResponse;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsResponse;
import software.amazon.awssdk.services.datazone.model.PutEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.PutEnvironmentBlueprintConfigurationResponse;
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
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.datazone.environmentblueprintconfiguration.helper.EnvironmentBlueprintConfigurationOperation;
import software.amazon.datazone.environmentblueprintconfiguration.helper.LoggerWrapper;

import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public PutEnvironmentBlueprintConfigurationResponse putEnvironmentBlueprintConfiguration(final @NonNull PutEnvironmentBlueprintConfigurationRequest putEnvironmentBlueprintConfiguration) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentBlueprintConfigurationOperation.PUT_ENVIRONMENT_BLUEPRINT_CONFIGURATION, putEnvironmentBlueprintConfiguration, client::putEnvironmentBlueprintConfiguration, putEnvironmentBlueprintConfiguration.environmentBlueprintIdentifier(), proxyClient);
        }
    }

    public GetEnvironmentBlueprintConfigurationResponse getEnvironmentBlueprintConfiguration(final @NonNull GetEnvironmentBlueprintConfigurationRequest getEnvironmentBlueprintConfigurationRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentBlueprintConfigurationOperation.GET_ENVIRONMENT_BLUEPRINT_CONFIGURATION, getEnvironmentBlueprintConfigurationRequest, client::getEnvironmentBlueprintConfiguration, getEnvironmentBlueprintConfigurationRequest.environmentBlueprintIdentifier(), proxyClient);
        }
    }

    public ListEnvironmentBlueprintConfigurationsResponse listEnvironmentBlueprintConfigurations(ListEnvironmentBlueprintConfigurationsRequest listEnvironmentBlueprintConfigurationsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentBlueprintConfigurationOperation.LIST_ENVIRONMENT_BLUEPRINT_CONFIGURATIONS, listEnvironmentBlueprintConfigurationsRequest, client::listEnvironmentBlueprintConfigurations, listEnvironmentBlueprintConfigurationsRequest.domainIdentifier(), proxyClient);
        }
    }

    public ListEnvironmentBlueprintsResponse listEnvironmentBlueprints(final @NonNull ListEnvironmentBlueprintsRequest listEnvironmentBlueprintsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentBlueprintConfigurationOperation.LIST_ENVIRONMENT_BLUEPRINTS, listEnvironmentBlueprintsRequest, client::listEnvironmentBlueprints, listEnvironmentBlueprintsRequest.domainIdentifier(), proxyClient);
        }
    }

    public DeleteEnvironmentBlueprintConfigurationResponse deleteEnvironmentBlueprintConfiguration(final @NonNull DeleteEnvironmentBlueprintConfigurationRequest deleteEnvironmentBlueprintConfigurationRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(EnvironmentBlueprintConfigurationOperation.DELETE_ENVIRONMENT_BLUEPRINT_CONFIGURATION, deleteEnvironmentBlueprintConfigurationRequest, client::deleteEnvironmentBlueprintConfiguration, deleteEnvironmentBlueprintConfigurationRequest.environmentBlueprintIdentifier(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final EnvironmentBlueprintConfigurationOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on EnvironmentBlueprintConfiguration with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final EnvironmentBlueprintConfigurationOperation operation) {
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
