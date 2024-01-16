package software.amazon.datazone.environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.EnvironmentStatus;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final DeleteHandler handler = new DeleteHandler();
        Instant currTime = Instant.now();

        final ResourceModel model = getResourceModelForDelete();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when(sdkClient.deleteEnvironment(Mockito.any(DeleteEnvironmentRequest.class))).thenReturn(getDeleteEnvironmentResponse());
        Mockito.when(sdkClient.getEnvironment(Mockito.any(GetEnvironmentRequest.class)))
                .thenReturn(getGetEnvironmentResponse(EnvironmentStatus.DELETING, currTime))
                .thenReturn(getGetEnvironmentResponse(EnvironmentStatus.DELETED, currTime));

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertCfnResponse(response, OperationStatus.IN_PROGRESS);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getStatus()).isEqualTo(EnvironmentStatus.DELETED.toString());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testHandleRequest_ValidRequest_DeploymentTimesOut_ShouldRetry() {
        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = getResourceModelForDelete();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        Instant currTime = Instant.now();

        // First we would return DELETING and then throw RNF since the resource would be deleted.
        Mockito.when(sdkClient.deleteEnvironment(Mockito.any(DeleteEnvironmentRequest.class))).thenReturn(getDeleteEnvironmentResponse());
        Mockito.when(sdkClient.getEnvironment(Mockito.any(GetEnvironmentRequest.class)))
                .thenReturn(getGetEnvironmentResponseBuilder(EnvironmentStatus.DELETE_FAILED, currTime)
                        .lastDeployment(getLastDeployment("408"))
                        .build())
                .thenReturn(getGetEnvironmentResponseBuilder(EnvironmentStatus.DELETE_FAILED, currTime)
                        .lastDeployment(getLastDeployment("408"))
                        .build())
                .thenReturn(getGetEnvironmentResponse(EnvironmentStatus.DELETED, currTime));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.IN_PROGRESS);

        // Make second call with the context that was received from the previous response
        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        // assertions
        // This time response should be SUCCESS as the getDomain would have returned AVAILABLE.
        assertCfnResponse(response, OperationStatus.SUCCESS);
        verify(sdkClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_ValidRequest_DeploymentTimesOut_RetriesExhausted_ShouldRetry() {
        final DeleteHandler handler = new DeleteHandler();
        final ResourceModel model = getResourceModelForDelete();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        Instant currTime = Instant.now();

        // First we would return DELETING and then throw RNF since the resource would be deleted.
        Mockito.when(sdkClient.deleteEnvironment(Mockito.any(DeleteEnvironmentRequest.class))).thenReturn(getDeleteEnvironmentResponse());
        Mockito.when(sdkClient.getEnvironment(Mockito.any(GetEnvironmentRequest.class)))
                .thenReturn(getGetEnvironmentResponseBuilder(EnvironmentStatus.DELETE_FAILED, currTime)
                        .lastDeployment(getLastDeployment("408"))
                        .build())
                .thenReturn(getGetEnvironmentResponseBuilder(EnvironmentStatus.DELETE_FAILED, currTime)
                        .lastDeployment(getLastDeployment("408"))
                        .build())
                .thenReturn(getGetEnvironmentResponse(EnvironmentStatus.DELETED, currTime));

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, CallbackContext.builder().timeOutRetriesRemaining(1).stabilizationRetriesRemaining(15).build(), proxyClient, logger);

        // assertions
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
    }

    private static software.amazon.awssdk.services.datazone.model.Deployment getLastDeployment(String errorCode) {
        return software.amazon.awssdk.services.datazone.model.Deployment.builder().failureReason(
                        software.amazon.awssdk.services.datazone.model.EnvironmentError.builder()
                                .code(errorCode)
                                .message("Environment deployment for environment timed out")
                                .build())
                .build();
    }
}
