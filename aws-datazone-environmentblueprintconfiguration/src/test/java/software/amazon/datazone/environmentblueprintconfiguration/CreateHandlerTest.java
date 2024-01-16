package software.amazon.datazone.environmentblueprintconfiguration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsRequest;
import software.amazon.awssdk.services.datazone.model.PutEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    CreateHandler createHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        createHandler = new CreateHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        final ResourceModel model = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.listEnvironmentBlueprints(Mockito.any(ListEnvironmentBlueprintsRequest.class)))
                .thenReturn(getListEnvironmentBlueprintsResponse());

        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build()) // Indicates that configuration doesn't exist
                // At this point the configuration should have been created, so we will return enabled regions
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(model.getEnabledRegions()));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_GetBlueprintConfigurationThrowsRNF_ShouldAllowCreation() {
        final ResourceModel model = getResourceModel(List.of("us-east-1"));
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.listEnvironmentBlueprints(Mockito.any(ListEnvironmentBlueprintsRequest.class)))
                .thenReturn(getListEnvironmentBlueprintsResponse());

        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build()) // Indicates that configuration doesn't exist
                // At this point the configuration should have been created, so we will return enabled regions
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(model.getEnabledRegions()));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @ParameterizedTest
    @MethodSource("getInvalidRequestsForCreateHandler")
    public void testHandleRequest_InvalidRequests_ThrowsInvalidRequestException(final String testName,
                                                                                final ResourceModel model) {
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Assertions.assertThrows(CfnInvalidRequestException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        Mockito.verifyNoInteractions(dataZoneClient);
    }

    @Test
    public void testHandleRequest_InvalidRequest_BlueprintConfigurationAlreadyExists_ThrowsAlreadyExistsException() {
        final ResourceModel model = getResourceModel(List.of("us-east-1"));
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.listEnvironmentBlueprints(Mockito.any(ListEnvironmentBlueprintsRequest.class)))
                .thenReturn(getListEnvironmentBlueprintsResponse());
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(List.of("us-east-1")));

        Assertions.assertThrows(CfnAlreadyExistsException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_InvalidRequest_EnvBlueprintDoesNotExist_ShouldThrowInvalidRequestException() {
        final ResourceModel model = getResourceModel(List.of("us-east-1"));
        model.setEnvironmentBlueprintIdentifier("NON_EXISTING_IDENTIFIER");
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.listEnvironmentBlueprints(Mockito.any(ListEnvironmentBlueprintsRequest.class)))
                .thenReturn(getListEnvironmentBlueprintsResponse());

        Assertions.assertThrows(CfnInvalidRequestException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_PutEnvironmentBlueprintConfigurationThrowsException_ShouldThrowException() {
        final ResourceModel model = getResourceModel(List.of("us-east-1"));
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.listEnvironmentBlueprints(Mockito.any(ListEnvironmentBlueprintsRequest.class)))
                .thenReturn(getListEnvironmentBlueprintsResponse());

        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build())
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(model.getEnabledRegions()));

        Mockito.when(dataZoneClient.putEnvironmentBlueprintConfiguration(Mockito.any(PutEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(InternalServerException.builder().build());

        Assertions.assertThrows(CfnInternalFailureException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    private static Stream<Arguments> getInvalidRequestsForCreateHandler() {
        return Stream.of(
                Arguments.of("Managed is null",
                        ResourceModel.builder()
                                .managed(null)
                                .domainIdentifier(DOMAIN_IDENTIFIER)
                                .build()),
                Arguments.of("Managed is non-null, but it's false indicating custom blueprint",
                        ResourceModel.builder()
                                .managed(false)
                                .domainIdentifier(DOMAIN_IDENTIFIER)
                                .build()),
                Arguments.of("Managed is true but Domain Identifier is null",
                        ResourceModel.builder()
                                .managed(true)
                                .domainIdentifier(null)
                                .build()),
                Arguments.of("Managed is true, Domain Identifier is non-null, but Environment Blueprint Identifier is null",
                        ResourceModel.builder()
                                .managed(true)
                                .domainIdentifier(DOMAIN_IDENTIFIER)
                                .environmentBlueprintIdentifier(null)
                                .build())
        );
    }
}
