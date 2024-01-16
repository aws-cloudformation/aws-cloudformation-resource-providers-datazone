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
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    ReadHandler readHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        readHandler = new ReadHandler();
    }

    @Test
    public void testHandleRequest_EnvironmentBlueprintConfigurationExists_ShouldNotThrowException() {
        ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenReturn(getGetEnvironmentBlueprintConfigurationResponse(resourceModel.getEnabledRegions()));

        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_EnvironmentBlueprintConfigurationDoesNotExist_ShouldThrowException() {
        ResourceModel resourceModel = getResourceModel(ENABLED_REGIONS);
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getEnvironmentBlueprintConfiguration(Mockito.any(GetEnvironmentBlueprintConfigurationRequest.class)))
                .thenThrow(ResourceNotFoundException.builder().build());

        // make call and assert exception is thrown
        Assertions.assertThrows(CfnNotFoundException.class, () ->
                readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
