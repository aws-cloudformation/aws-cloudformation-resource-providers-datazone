package software.amazon.datazone.environmentblueprintconfiguration;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentblueprintconfiguration.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentblueprintconfiguration.helper.LoggerWrapper;

import java.util.Objects;


public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialize
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        ResourceModel desiredResourceState = request.getDesiredResourceState();
        this.validateInputs(desiredResourceState);

        String envBluePrintId = null;
        if (desiredResourceState.getManaged()) {
            envBluePrintId = this.getEnvironmentBlueprintIdentifier(desiredResourceState);
        }

        desiredResourceState.setDomainId(desiredResourceState.getDomainIdentifier());
        desiredResourceState.setEnvironmentBlueprintId(envBluePrintId);

        return ProgressEvent.progress(desiredResourceState, callbackContext)
                // Make create call
                .then(progress -> validateEnvironmentBlueprintConfiguration(proxy, proxyClient, progress))
                .then(progress -> putEnvironmentBlueprintConfiguration("Create", proxy, proxyClient, progress))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private void validateInputs(ResourceModel desiredResourceState) {
        if (Objects.isNull(desiredResourceState.getManaged())) {
            logger.info("Managed is not specified for creating blueprint configuration, defaulting to True");
            desiredResourceState.setManaged(Boolean.TRUE);
        }

        if (!desiredResourceState.getManaged()) {
            throw new CfnInvalidRequestException("Custom Blueprints are not supported for creating blueprint configuration");
        }

        if (Objects.isNull(desiredResourceState.getDomainIdentifier())) {
            throw new CfnInvalidRequestException("Domain Identifier is required for creating blueprint configuration");
        }

        if (Objects.isNull(desiredResourceState.getEnvironmentBlueprintIdentifier())) {
            throw new CfnInvalidRequestException("Environment Blueprint Identifier is required for creating blueprint configuration");
        }

    }

    private ProgressEvent<ResourceModel, CallbackContext> validateEnvironmentBlueprintConfiguration(AmazonWebServicesClientProxy proxy,
                                                                                                    ProxyClient<DataZoneClient> proxyClient,
                                                                                                    ProgressEvent<ResourceModel, CallbackContext> progress) {
        // Call DataZone Control Plane to create the resource.
        try {
            return proxy.initiate("AWS-DataZone-EnvironmentBlueprintConfiguration::Create::PreExistenceCheck", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(model -> Translator.translateToReadRequest(model))
                    .makeServiceCall((getEnvironmentBlueprintConfigurationRequest, client) -> dataZoneClientWrapper.getEnvironmentBlueprintConfiguration(getEnvironmentBlueprintConfigurationRequest))
                    .done(this::validateIsEnvironmentBlueprintConfigurationCreatable);
        } catch (CfnNotFoundException | ResourceNotFoundException e) {
            logger.info("Create::PreExistenceCheck passed for default blueprint %s in domain %s",
                    progress.getResourceModel().getEnvironmentBlueprintIdentifier(), progress.getResourceModel().getDomainIdentifier());
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> validateIsEnvironmentBlueprintConfigurationCreatable(
            GetEnvironmentBlueprintConfigurationRequest getEnvironmentBlueprintConfigurationRequest,
            GetEnvironmentBlueprintConfigurationResponse getEnvironmentBlueprintConfigurationResponse,
            ProxyClient<DataZoneClient> dataZoneClientProxyClient,
            ResourceModel resourceModel,
            CallbackContext callbackContext) {
        if (getEnvironmentBlueprintConfigurationResponse.domainId().equals(resourceModel.getDomainIdentifier())) {
            String errorMessage = String.format("Configuration for default blueprint %s in domain %s already exists.",
                    resourceModel.getEnvironmentBlueprintIdentifier(), resourceModel.getDomainIdentifier());
            logger.error(errorMessage);
            throw new CfnAlreadyExistsException(new Exception(errorMessage));
        }

        return ProgressEvent.progress(resourceModel, callbackContext);
    }

    private String getEnvironmentBlueprintIdentifier(ResourceModel desiredResourceState) {
        ListEnvironmentBlueprintsRequest request = ListEnvironmentBlueprintsRequest.builder()
                .domainIdentifier(desiredResourceState.getDomainIdentifier())
                .managed(Boolean.TRUE)
                .build();

        ListEnvironmentBlueprintsResponse response = dataZoneClientWrapper.listEnvironmentBlueprints(request);
        return response.items().stream()
                .filter(environmentBlueprintSummary -> environmentBlueprintSummary.name().equals(desiredResourceState.getEnvironmentBlueprintIdentifier()))
                .map(environmentBlueprintSummary -> environmentBlueprintSummary.id())
                .findFirst()
                .orElseThrow(() -> {
                    String errorMessage = String.format("Managed Environment Blueprint with %s doesn't exist.", desiredResourceState.getEnvironmentBlueprintIdentifier());
                    throw new CfnInvalidRequestException(new Exception(errorMessage));
                });
    }
}
