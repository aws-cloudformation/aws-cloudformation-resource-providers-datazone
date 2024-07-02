package software.amazon.datazone.userprofile;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UpdateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    DataZoneClient dataZoneClient;
    UpdateHandler updateHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        updateHandler = new UpdateHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ASSIGNED.toString()));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
        verify(dataZoneClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_ValidateRequest_throwException() {
        // setup
        final ResourceModel resourceModel = getDeactivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        // make call and assert exception is thrown
        Assertions.assertThrows(CfnGeneralServiceException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.doThrow(AccessDeniedException.class)
                .when(dataZoneClient).updateUserProfile(Mockito.any(UpdateUserProfileRequest.class));

        // make call
        Assertions.assertThrows(CfnAccessDeniedException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }
}
