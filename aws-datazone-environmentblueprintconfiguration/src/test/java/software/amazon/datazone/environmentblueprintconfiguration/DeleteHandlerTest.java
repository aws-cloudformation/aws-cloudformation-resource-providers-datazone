package software.amazon.datazone.environmentblueprintconfiguration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    DeleteHandler deleteHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        deleteHandler = new DeleteHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        final ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(resourceModel.getEnabledRegions()));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void testHandleRequest_GetBlueprintConfigurationThrowsRNF_ShouldThrowException() {
        final ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        Assertions.assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_ConfigurationDoesNotExist_ShouldThrowException() {
        final ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        Assertions.assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_DeleteEnvironmentBlueprintConfigurationThrowsException_ShouldThrowException() {
        final ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);
        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(resourceModel.getEnabledRegions()));

        Mockito.when(dataZoneClient.deleteEnvironmentBlueprintConfiguration(Mockito.any(DeleteEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(InternalServerException.builder().build());

        Assertions.assertThrows(CfnInternalFailureException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
