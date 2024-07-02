package software.amazon.datazone.userprofile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.SearchUserProfilesResponse;
import software.amazon.awssdk.services.datazone.model.UserProfileSummary;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    DataZoneClient dataZoneClient;
    ListHandler listHandler;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        listHandler = new ListHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any())).thenReturn(
                SearchUserProfilesResponse.builder()
                        .items(UserProfileSummary.builder()
                                .id(USER_IDENTIFIER)
                                .domainId(DOMAIN_IDENTIFIER)
                                .type(TYPE)
                                .status(ASSIGNED)
                                .details(DETAILS)
                                .build()
                        )
                        .nextToken("NEXT_TOKEN")
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).size().isEqualTo(1);
        assertResponseModel(response.getResourceModels().get(0));
    }

    @Test
    public void testHandleRequest_ListSubscriptionTargetsThrowsException_ShouldReThrowException() {
        // setup
        final ResourceModel resourceModel = getModel();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(resourceModel);

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any()))
                .thenThrow(AccessDeniedException.class);

        assertThatThrownBy(() -> listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnAccessDeniedException.class);
    }
}
