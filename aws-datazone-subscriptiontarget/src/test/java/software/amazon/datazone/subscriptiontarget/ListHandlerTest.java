package software.amazon.datazone.subscriptiontarget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ListSubscriptionTargetsResponse;
import software.amazon.awssdk.services.datazone.model.SubscriptionTargetSummary;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    DataZoneClient dataZoneClient;

    @Mock
    private ProxyClient<DataZoneClient> proxyClient;

    ListHandler listHandler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        dataZoneClient = mock(DataZoneClient.class);
        proxyClient = MOCK_PROXY(proxy, dataZoneClient);
        listHandler = new ListHandler();
    }

    @Test
    public void testHandleRequest_ValidRequest_DoesNotThrowException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getModel())
                .build();

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any())).thenReturn(ListSubscriptionTargetsResponse.builder()
                .items(SubscriptionTargetSummary.builder()
                        .id(SUBSCRIPTION_TARGET_IDENTIFIER)
                        .domainId(DOMAIN_IDENTIFIER)
                        .environmentId(ENVIRONMENT_IDENTIFIER)
                        .applicableAssetTypes(APPLICABLE_ASSET_TYPES)
                        .authorizedPrincipals(AUTHORIZED_PRINCIPALS)
                        .manageAccessRole(SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE)
                        .name(SUBSCRIPTION_TARGET_NAME)
                        .projectId(PROJECT_ID)
                        .provider(PROVIDER)
                        .type(TARGET_TYPE)
                        .build()
                )
                .nextToken("NEXT_TOKEN")
                .build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).size().isEqualTo(1);

        ResourceModel model = response.getResourceModels().get(0);
        assertThat(model)
                .returns(SUBSCRIPTION_TARGET_IDENTIFIER, from(ResourceModel::getId))
                .returns(DOMAIN_IDENTIFIER, from(ResourceModel::getDomainId))
                .returns(ENVIRONMENT_IDENTIFIER, from(ResourceModel::getEnvironmentId))
                .returns(PROJECT_ID, from(ResourceModel::getProjectId))
                .returns(SUBSCRIPTION_TARGET_NAME, from(ResourceModel::getName))
                .returns(PROVIDER, from(ResourceModel::getProvider))
                .returns(TARGET_TYPE, from(ResourceModel::getType));
    }

    @Test
    public void testHandleRequest_ListSubscriptionTargetsThrowsException_ShouldReThrowException() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getModel())
                .build();

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any()))
                .thenThrow(AccessDeniedException.class);

        assertThatThrownBy(() -> listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnAccessDeniedException.class);
    }
}
