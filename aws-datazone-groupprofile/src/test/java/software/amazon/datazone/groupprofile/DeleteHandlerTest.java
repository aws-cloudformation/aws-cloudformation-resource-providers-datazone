package software.amazon.datazone.groupprofile;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {
    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        // setup
        AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        DataZoneClient dataZoneClient = mock(DataZoneClient.class);
        ProxyClient<DataZoneClient> proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        DeleteHandler deleteHandler = new DeleteHandler();
        final ResourceModel resourceModel = getNotAssignedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenReturn(getGetGroupProfileResponse(GroupProfileStatus.ASSIGNED.toString()));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
    }

    @Test
    public void testHandleRequest_invalidResource_throwsException() {
        // setup
        AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        DataZoneClient dataZoneClient = mock(DataZoneClient.class);
        ProxyClient<DataZoneClient> proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        DeleteHandler deleteHandler = new DeleteHandler();
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenThrow(RuntimeException.class);

        // call handle request and validate that exception is thrown
        Assertions.assertThrows(CfnGeneralServiceException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_nonExistingResource_ThrowsException() {
        // setup
        AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        DataZoneClient dataZoneClient = mock(DataZoneClient.class);
        ProxyClient<DataZoneClient> proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        DeleteHandler deleteHandler = new DeleteHandler();
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        // call handle request and validate that exception is not thrown
        Assertions.assertDoesNotThrow(() ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
