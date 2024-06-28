package software.amazon.datazone.groupprofile;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {
    @Mock
    DataZoneClient dataZoneClient;
    ReadHandler readHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        readHandler = new ReadHandler();
    }

    @Test
    public void testHandleRequest_ShouldNotThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenReturn(getGetGroupProfileResponse(GroupProfileStatus.ASSIGNED.toString()));

        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_ShouldThrowException() {
        // setup
        ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.doThrow(ResourceNotFoundException.class)
                .when(dataZoneClient).getGroupProfile(Mockito.any(GetGroupProfileRequest.class));

        // make call and assert exception is thrown
        Assertions.assertThrows(CfnNotFoundException.class, () ->
                readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
