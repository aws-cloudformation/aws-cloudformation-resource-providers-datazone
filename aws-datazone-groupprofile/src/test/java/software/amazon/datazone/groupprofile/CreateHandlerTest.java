package software.amazon.datazone.groupprofile;

import java.time.Duration;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
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
public class CreateHandlerTest extends AbstractTestBase {
    @Mock
    DataZoneClient dataZoneClient;
    CreateHandler createHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        createHandler = new CreateHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_create_DoesNotThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.createGroupProfile(Mockito.any(CreateGroupProfileRequest.class)))
                .thenReturn(getCreateGroupProfileResponse());

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(getGetGroupProfileResponse(GroupProfileStatus.ASSIGNED.toString()));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request,
                new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_ValidRequest_alreadyExists_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getGroupProfile(Mockito.any(GetGroupProfileRequest.class)))
                .thenReturn(getGetGroupProfileResponse(GroupProfileStatus.ASSIGNED.toString()));

        // make call
        Assertions.assertThrows(CfnAlreadyExistsException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.doThrow(AccessDeniedException.class)
                .when(dataZoneClient).getGroupProfile(Mockito.any(GetGroupProfileRequest.class));

        // make call
        Assertions.assertThrows(CfnAccessDeniedException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }
}
