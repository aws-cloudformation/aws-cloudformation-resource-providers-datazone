package software.amazon.datazone.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DomainStatus;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.UpdateDomainRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static software.amazon.awssdk.services.datazone.model.AuthType.IAM_IDC;
import static software.amazon.awssdk.services.datazone.model.UserAssignment.AUTOMATIC;
import static software.amazon.awssdk.services.datazone.model.UserAssignment.MANUAL;
import static software.amazon.datazone.domain.UpdateHandler.SIGN_ON_ERROR_STATUS;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    public static final String UPDATED_DESCRIPTION = "Updated Description";
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @Mock
    DataZoneClient dataZoneClient;

    ArgumentCaptor<UpdateDomainRequest> updateDomainRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateDomainRequest.class);

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
        final ResourceModel oldResourceModel = getResourceModel();
        final ResourceModel newResourceModel = getResourceModel();
        final String updatedDescription = "Updated Description";
        newResourceModel.setDescription(updatedDescription);

        final GetDomainResponse availableDomainResponse = getAvailableDomainResponse(updatedDescription).build();

        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(availableDomainResponse);

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

        // Verify that the Single SignOn was not passed
        Mockito.verify(dataZoneClient, Mockito.times(1)).updateDomain(updateDomainRequestArgumentCaptor.capture());
        UpdateDomainRequest updateDomainRequest = updateDomainRequestArgumentCaptor.getValue();
        assertThat(updateDomainRequest.singleSignOn()).isNull();
    }

    @Test
    public void testHandleRequest_TestStabilization_DoesNotThrowException() {
        // setup
        final ResourceModel oldResourceModel = getResourceModel();
        final ResourceModel newResourceModel = getResourceModel();
        newResourceModel.setDescription(UPDATED_DESCRIPTION);

        final GetDomainResponse.Builder domainResponse = getAvailableDomainResponse(UPDATED_DESCRIPTION);

        // First return CREATING and then AVAILABLE for stabilization
        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(domainResponse.status(DomainStatus.CREATING).build())
                .thenReturn(domainResponse.status(DomainStatus.AVAILABLE).build());

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
    public void testHandleRequest_SingleSignOnUpdateRequired_DoesNotThrowException() {
        // setup
        final ResourceModel oldResourceModel = getResourceModel();
        final ResourceModel newResourceModel = getResourceModel();
        oldResourceModel.setSingleSignOn(null);
        newResourceModel.setDescription(UPDATED_DESCRIPTION);

        final GetDomainResponse.Builder domainResponse = getAvailableDomainResponse(UPDATED_DESCRIPTION);

        // First return CREATING and then AVAILABLE for stabilization
        Mockito.when(dataZoneClient.getDomain(Mockito.any(GetDomainRequest.class)))
                .thenReturn(domainResponse.status(DomainStatus.AVAILABLE).build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(oldResourceModel)
                .desiredResourceState(newResourceModel)
                .build();

        // make call
        ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // assert that the event received is now Complete
        assertCfnResponse(response, OperationStatus.SUCCESS);
        // and description is the updated description
        assertThat(response.getResourceModel().getDescription().equals(UPDATED_DESCRIPTION));

        // Verify that the Single SignOn was passed
        Mockito.verify(dataZoneClient, Mockito.times(1)).updateDomain(updateDomainRequestArgumentCaptor.capture());
        UpdateDomainRequest updateDomainRequest = updateDomainRequestArgumentCaptor.getValue();
        assertThat(updateDomainRequest.singleSignOn()).isNotNull();
    }

    @Test
    public void testHandleRequest_SingleSignOnBeingUpdated_WhenItWasPreviouslySpecified_ShouldThrowException() {
        // setup
        final ResourceModel oldResourceModel = getResourceModel();
        final ResourceModel newResourceModel = getResourceModel();
        newResourceModel.setSingleSignOn(SingleSignOn.builder()
                .type(IAM_IDC.toString())
                .userAssignment(MANUAL.toString())
                .build());
        newResourceModel.setDescription(UPDATED_DESCRIPTION);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(oldResourceModel)
                .desiredResourceState(newResourceModel)
                .build();

        // make call and assert that it throws exception
        assertThatThrownBy(() -> updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnInvalidRequestException.class)
                .hasMessageContaining(SIGN_ON_ERROR_STATUS);
    }


    private GetDomainResponse.Builder getAvailableDomainResponse(String updatedDescription) {
        return GetDomainResponse.builder()
                .arn(DOMAIN_ARN)
                .id(DOMAIN_ID)
                .portalUrl(PORTAL_URL)
                .description(updatedDescription)
                .domainExecutionRole(DOMAIN_EXECUTION_ROLE)
                .name("CFN-TEST")
                .singleSignOn(software.amazon.awssdk.services.datazone.model.SingleSignOn.builder()
                        .type(IAM_IDC.toString())
                        .userAssignment(AUTOMATIC.toString())
                        .build())
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .tags(TAGS)
                .kmsKeyIdentifier(KMS_KEY_IDENTIFIER)
                .status(DomainStatus.AVAILABLE.toString());
    }
}
