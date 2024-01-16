package software.amazon.datazone.environmentblueprintconfiguration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.EnvironmentBlueprintConfigurationItem;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsResponse;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        final ResourceModel model = ResourceModel.builder()
                .domainIdentifier("dzd_66zup2ahl2wg4n")
                .build();
        final ResourceHandlerRequest<ResourceModel> request = getResourceHandlerRequest(model);

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any()))
                .thenReturn(getListEnvironmentBlueprintConfigurationsResponse(ENABLED_REGIONS));

        final ProgressEvent<ResourceModel, CallbackContext> response =
                listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModels()).size().isEqualTo(1);

        ResourceModel receivedModel = response.getResourceModels().get(0);
        assertResponseModel(receivedModel);
    }

    @Test
    public void testHandleRequest_ListSubscriptionTargetsThrowsException_ShouldReThrowException() {
        final ResourceModel model = ResourceModel.builder()
                .domainIdentifier("dzd_66zup2ahl2wg4n")
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Mockito.when(proxyClient.injectCredentialsAndInvokeV2(Mockito.any(), Mockito.any()))
                .thenThrow(AccessDeniedException.class);

        assertThatThrownBy(() -> listHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger))
                .isInstanceOf(CfnAccessDeniedException.class);
    }

    private static ListEnvironmentBlueprintConfigurationsResponse getListEnvironmentBlueprintConfigurationsResponse(List<String> enabledRegions) {
        return ListEnvironmentBlueprintConfigurationsResponse.builder()
                .items(EnvironmentBlueprintConfigurationItem.builder()
                        .domainId(DOMAIN_IDENTIFIER)
                        .environmentBlueprintId(ENV_BLUEPRINT_ID)
                        .manageAccessRoleArn(MANAGE_ACCESS_ROLE_ARN)
                        .enabledRegions(enabledRegions)
                        .provisioningRoleArn(PROVISIONING_ROLE_ARN)
                        .regionalParameters(REGIONAL_PARAMETERS)
                        .build()
                )
                .build();
    }
}
