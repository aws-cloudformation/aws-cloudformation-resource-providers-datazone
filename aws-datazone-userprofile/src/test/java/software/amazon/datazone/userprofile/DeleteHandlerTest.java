package software.amazon.datazone.userprofile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UpdateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

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
        final ResourceModel resourceModel = getDeactivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ACTIVATED.toString()));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

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

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenThrow(RuntimeException.class);

        // call handle request and validate that exception is thrown
        Assertions.assertThrows(CfnGeneralServiceException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_nonExistingResource_throwsException() {
        // setup
        AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        DataZoneClient dataZoneClient = mock(DataZoneClient.class);
        ProxyClient<DataZoneClient> proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        DeleteHandler deleteHandler = new DeleteHandler();
        final ResourceModel resourceModel = getDeactivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.DEACTIVATED.toString()));

        // call handle request and validate that exception is thrown
        Assertions.assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_DeleteThrowsException_ShouldReThrowException() {
        // setup
        AmazonWebServicesClientProxy proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        DataZoneClient dataZoneClient = mock(DataZoneClient.class);
        ProxyClient<DataZoneClient> proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        DeleteHandler deleteHandler = new DeleteHandler();
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ACTIVATED.toString()));

        Mockito.when(dataZoneClient.updateUserProfile(Mockito.any(UpdateUserProfileRequest.class)))
                .thenThrow(RuntimeException.class);

        // call handle request and validate that exception is thrown
        Assertions.assertThrows(CfnGeneralServiceException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }
}
