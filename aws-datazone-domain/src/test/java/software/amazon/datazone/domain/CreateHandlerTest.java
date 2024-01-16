package software.amazon.datazone.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateDomainRequest;
import software.amazon.awssdk.services.datazone.model.DomainStatus;
import software.amazon.awssdk.services.datazone.model.DomainSummary;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    CreateHandler createHandler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(6000).toMillis());
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        createHandler = new CreateHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        // setup
        final ResourceModel model = getResourceModel();
        Mockito.when(dataZoneClient.createDomain(Mockito.any(CreateDomainRequest.class)))
                .thenReturn(getCreateDomainResponse(DomainStatus.CREATING));
        // First we would return CREATING that should give us a callback context and then we would return AVAILABLE.
        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(DomainStatus.CREATING))
                .thenReturn(getGetDomainResponse(DomainStatus.AVAILABLE));

        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assertions
        assertCfnResponse(response, OperationStatus.IN_PROGRESS);

        // Make second call with the context that was received from the previous response
        response = createHandler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        // assertions
        // This time response should be SUCCESS as the getDomain would have returned AVAILABLE.
        assertCfnResponse(response, OperationStatus.SUCCESS);
        assertResponseModel(response.getResourceModel(), model);
        verify(dataZoneClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_ValidRequest_StabilisationAttemptsExhausted_ShouldThrowException() {
        // setup
        final ResourceModel model = getResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(DomainStatus.CREATING));

        CallbackContext callbackContext = CallbackContext.builder()
                .domainSummary(DomainSummary.builder().id(DOMAIN_ID).build())
                .stabilizationRetriesRemaining(1)
                .build();

        // make call
        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        // assertions
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @Test
    public void testHandleRequest_ValidRequest_DomainDoesNotStabilize_ShouldThrowException() {
        // setup
        final ResourceModel model = getResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(getGetDomainResponse(DomainStatus.CREATION_FAILED));

        CallbackContext callbackContext = CallbackContext.builder()
                .domainSummary(DomainSummary.builder().id(DOMAIN_ID).build())
                .stabilizationRetriesRemaining(1)
                .build();

        // make call
        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        // assertions
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @Test
    public void testHandleRequest_DataZoneClientThrowsException_ShouldThrowException() {
        // setup
        final CreateHandler handler = new CreateHandler();
        final ResourceModel model = getResourceModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.doThrow(ConflictException.create("Resource already exists.", null))
                .when(dataZoneClient).createDomain(Mockito.any(CreateDomainRequest.class));

        // make call
        Assertions.assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }


}
