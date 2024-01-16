package software.amazon.datazone.environmentblueprintconfiguration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.PutEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.List;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    public static final List<String> UPDATED_ENABLED_REGIONS = List.of("us-west-2");
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> dataZoneProxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    UpdateHandler updateHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        dataZoneProxyClient = MOCK_PROXY(proxy, dataZoneClient);
        updateHandler = new UpdateHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        final ResourceModel desiredModel = getResourceModel(ENABLED_REGIONS);
        desiredModel.setEnabledRegions(UPDATED_ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(desiredModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(desiredModel.getEnabledRegions()));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                updateHandler.handleRequest(proxy, request, new CallbackContext(), dataZoneProxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel(), UPDATED_ENABLED_REGIONS);
    }

    @Test
    public void testHandleRequest_GetBlueprintConfigurationThrowsRNF_ShouldThrowException() {
        final ResourceModel desiredModel = getResourceModel(ENABLED_REGIONS);
        desiredModel.setEnabledRegions(UPDATED_ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(desiredModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        Assertions.assertThrows(CfnNotFoundException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), dataZoneProxyClient, logger));
    }

    @Test
    public void testHandleRequest_ConfigurationDoesNotExist_ShouldThrowException() {
        final ResourceModel desiredModel = getResourceModel(ENABLED_REGIONS);
        desiredModel.setEnabledRegions(UPDATED_ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(desiredModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        Assertions.assertThrows(CfnNotFoundException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), dataZoneProxyClient, logger));
    }

    @Test
    public void testHandleRequest_PutEnvironmentBlueprintConfigurationThrowsException_ShouldThrowException() {
        final ResourceModel desiredModel = getResourceModel(ENABLED_REGIONS);
        desiredModel.setEnabledRegions(UPDATED_ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(desiredModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(desiredModel.getEnabledRegions()));

        Mockito.when(dataZoneClient.putEnvironmentBlueprintConfiguration(Mockito.any(PutEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(InternalServerException.builder().build());

        Assertions.assertThrows(CfnInternalFailureException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), dataZoneProxyClient, logger));
    }
}
