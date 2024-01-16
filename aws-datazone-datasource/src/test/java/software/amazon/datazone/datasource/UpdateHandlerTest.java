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
import software.amazon.awssdk.services.datazone.model.GetDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.GetDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceRequest;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    public static final String UPDATED_DESCRIPTION = "Updated Description";

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    UpdateHandler updateHandler;

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
        final ResourceModel oldResourceModel = getModel();
        final ResourceModel newResourceModel = getModel();
        newResourceModel.setDescription(UPDATED_DESCRIPTION);

        final GetDataSourceResponse dataSourceResponse = getGetDataSourceResponseBuilder(DataSourceStatus.READY).description(UPDATED_DESCRIPTION).build();

        Mockito.when(dataZoneClient.getDataSource(Mockito.any(GetDataSourceRequest.class)))
                .thenReturn(dataSourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(oldResourceModel)
                .desiredResourceState(newResourceModel)
                .build();

        // make call
        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assert that the event received is in SUCCESSFUL
        assertCfnResponse(response, OperationStatus.SUCCESS);
        // and description is the updated description
        assertThat(response.getResourceModel().getDescription().equals(UPDATED_DESCRIPTION));
    }

    @Test
    public void testHandleRequest_TestStabilization_DoesNotThrowException() {
        // setup
        final ResourceModel oldResourceModel = getModel();
        final ResourceModel newResourceModel = getModel();
        newResourceModel.setDescription(UPDATED_DESCRIPTION);

        final GetDataSourceResponse.Builder dataSourceResponseBuilder = getGetDataSourceResponseBuilder(DataSourceStatus.UPDATING);

        // First return CREATING and then READY with UPDATED_DESCRIPTION for stabilization
        Mockito.when(dataZoneClient.getDataSource(Mockito.any(GetDataSourceRequest.class)))
                .thenReturn(dataSourceResponseBuilder.build())
                .thenReturn(dataSourceResponseBuilder.status(DataSourceStatus.READY).description(UPDATED_DESCRIPTION).build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(oldResourceModel)
                .desiredResourceState(newResourceModel)
                .build();

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        // assert that the event received is in In_Progress
        assertCfnResponse(response, OperationStatus.IN_PROGRESS);


        // make call using the callback received from the previous call
        response = updateHandler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        // assert that the event received is now Complete
        assertCfnResponse(response, OperationStatus.SUCCESS);
        // and description is the updated description
        assertThat(response.getResourceModel().getDescription().equals(UPDATED_DESCRIPTION));
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final ResourceModel model = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.doThrow(AccessDeniedException.class)
                .when(dataZoneClient).updateDataSource(Mockito.any(UpdateDataSourceRequest.class));

        // make call
        Assertions.assertThrows(CfnAccessDeniedException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger)
        );
    }

}
