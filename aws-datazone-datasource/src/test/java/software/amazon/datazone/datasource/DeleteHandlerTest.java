package software.amazon.datazone.datasource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.DataSourceStatus;
import software.amazon.awssdk.services.datazone.model.DataSourceSummary;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.GetDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

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
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.getDataSource(Mockito.any(GetDataSourceRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // Assertions
        assertCfnResponse(response, OperationStatus.SUCCESS);
        verify(dataZoneClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_ValidRequest_TestStabilisation_DoesNotThrowException() {
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        // First we would return DELETING and then throw RNF since the resource would be deleted.
        Mockito.when(dataZoneClient.getDataSource(Mockito.any(GetDataSourceRequest.class)))
                .thenReturn(getGetDataSourceResponse(DataSourceStatus.DELETING))
                .thenThrow(ResourceNotFoundException.class);

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.IN_PROGRESS);

        // Make second call with the context that was received from the previous response
        response = deleteHandler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        // assertions
        // This time response should be SUCCESS as the getDomain would have returned AVAILABLE.
        assertCfnResponse(response, OperationStatus.SUCCESS);
        verify(dataZoneClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_ResourceAlreadyDeleted_ShouldNotThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.deleteDataSource(Mockito.any(DeleteDataSourceRequest.class)))
                .thenThrow(ResourceNotFoundException.class);

        Assertions.assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }

    @Test
    public void testHandleRequest_ValidRequest_StabilisationAttemptsExhausted_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        // Return DELETING in the last attempt as well.
        Mockito.when(dataZoneClient.getDataSource(Mockito.any(GetDataSourceRequest.class)))
                .thenReturn(getGetDataSourceResponse(DataSourceStatus.DELETING));

        CallbackContext callbackContext = CallbackContext.builder()
                .dataSourceSummary(DataSourceSummary.builder().dataSourceId(DATA_SOURCE_IDENTIFIER).domainId(DOMAIN_IDENTIFIER).build())
                .stabilizationRetriesRemaining(1)
                .build();

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        // assertions
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.doThrow(AccessDeniedException.class)
                .when(dataZoneClient).deleteDataSource(Mockito.any(DeleteDataSourceRequest.class));

        // make call
        Assertions.assertThrows(CfnAccessDeniedException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }
}
