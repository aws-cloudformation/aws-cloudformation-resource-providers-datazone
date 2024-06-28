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
import software.amazon.awssdk.services.datazone.model.CreateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.UserAssignment;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

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

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.when(dataZoneClient.createUserProfile(Mockito.any(CreateUserProfileRequest.class)))
                .thenReturn(getCreateUserProfileResponse());

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenThrow(ResourceNotFoundException.class)
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ASSIGNED.toString()));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_ValidRequest_update_DoesNotThrowException() {
        // setup
        final ResourceModel resourceModel = getActivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.DEACTIVATED.toString()))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ASSIGNED.toString()));


        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel());
    }

    @Test
    public void testHandleRequest_ValidRequest_validation_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getDeactivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        // make call
        Assertions.assertThrows(CfnInvalidRequestException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_ValidRequest_update_deactivated_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.DEACTIVATED.toString()));

        // make call
        Assertions.assertThrows(CfnAlreadyExistsException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_ValidRequest_update_not_assigned_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getActivatedModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.NOT_ASSIGNED.toString()));

        // make call
        Assertions.assertThrows(CfnAlreadyExistsException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_ValidRequest_alreadyExists_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.when(dataZoneClient.getUserProfile(Mockito.any(GetUserProfileRequest.class)))
                .thenReturn(getGetUserProfileResponse(UserProfileStatus.ASSIGNED.toString()));

        // make call
        Assertions.assertThrows(CfnAlreadyExistsException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_SSOUser_AutomaticDomain_ShouldThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.AUTOMATIC));

        // make call
        Assertions.assertThrows(CfnInvalidRequestException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(UserAssignment.MANUAL));

        Mockito.doThrow(AccessDeniedException.class)
                .when(dataZoneClient).getUserProfile(Mockito.any(GetUserProfileRequest.class));

        // make call
        Assertions.assertThrows(CfnAccessDeniedException.class, () ->
                createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }
}
