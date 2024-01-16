package software.amazon.datazone.subscriptiontarget;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.GetSubscriptionTargetResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

public class AbstractTestBase {
    public static final String SUBSCRIPTION_TARGET_IDENTIFIER = "611nkvhuji00iv";
    public static final String DOMAIN_IDENTIFIER = "dzd_66zup2ahl2wg4n";
    public static final String ENVIRONMENT_IDENTIFIER = "6g1nkvhuji00iv";
    public static final List<String> APPLICABLE_ASSET_TYPES = List.of("GlueTableAssetType");
    public static final String SUBSCRIPTION_TARGET_NAME = "subscriptionTarget";
    public static final String SUBSCRIPTION_TARGET_DATABASE_NAME = "projectx-zone-y-sub-db";
    public static final String SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE = "arn:aws:iam::123456789012:role/zone-x232-viewer";
    public static final String GLUE_SUBSCRIPTION_TARGET_CONFIG = "{\"databaseName\": \""
            + SUBSCRIPTION_TARGET_DATABASE_NAME +
            "\",\"manageAccessRole\": \"" + SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE + "\"}";
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;
    protected static final List<String> AUTHORIZED_PRINCIPALS = List.of("arn:aws:iam::12345678:role/datazone_usr_6g1nkvhuji00iv");
    public static final String CREATED_AT = "2023-10-30T13:22:21.277000+05:30";
    public static final String PROJECT_ID = "b3ovsfpg5srfon";
    public static final String PROVIDER = "Amazon DataZone";
    public static final String GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM_NAME = "GlueSubscriptionTargetConfigForm";
    public static final SubscriptionTargetForm GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM = SubscriptionTargetForm.builder()
            .formName(GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM_NAME)
            .content(GLUE_SUBSCRIPTION_TARGET_CONFIG)
            .build();
    public static final String TARGET_TYPE = "GlueSubscriptionTargetType";

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    static ProxyClient<DataZoneClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final DataZoneClient sdkClient) {
        return new ProxyClient<DataZoneClient>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public DataZoneClient client() {
                return sdkClient;
            }
        };
    }

    protected static ResourceModel getModel() {
        return ResourceModel.builder()
                .id(SUBSCRIPTION_TARGET_IDENTIFIER)
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .domainId(DOMAIN_IDENTIFIER)
                .environmentIdentifier(ENVIRONMENT_IDENTIFIER)
                .environmentId(ENVIRONMENT_IDENTIFIER)
                .applicableAssetTypes(APPLICABLE_ASSET_TYPES)
                .authorizedPrincipals(AUTHORIZED_PRINCIPALS)
                .createdAt(CREATED_AT)
                .manageAccessRole(SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE)
                .name(SUBSCRIPTION_TARGET_NAME)
                .projectId(PROJECT_ID)
                .provider(PROVIDER)
                .subscriptionTargetConfig(List.of(GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM))
                .type(TARGET_TYPE)
                .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    protected static CreateSubscriptionTargetResponse getCreateSubscriptionTargetResponse() {
        return CreateSubscriptionTargetResponse.builder()
                .id(SUBSCRIPTION_TARGET_IDENTIFIER)
                .domainId(DOMAIN_IDENTIFIER)
                .environmentId(ENVIRONMENT_IDENTIFIER)
                .applicableAssetTypes(APPLICABLE_ASSET_TYPES)
                .authorizedPrincipals(AUTHORIZED_PRINCIPALS)
                .manageAccessRole(SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE)
                .name(SUBSCRIPTION_TARGET_NAME)
                .projectId(PROJECT_ID)
                .provider(PROVIDER)
                .subscriptionTargetConfig(List.of(software.amazon.awssdk.services.datazone.model.SubscriptionTargetForm.builder()
                        .formName(GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM_NAME)
                        .content(GLUE_SUBSCRIPTION_TARGET_CONFIG)
                        .build()))
                .type(TARGET_TYPE)
                .build();
    }

    protected static GetSubscriptionTargetResponse getGetSubscriptionTargetResponse() {
        return GetSubscriptionTargetResponse.builder()
                .id(SUBSCRIPTION_TARGET_IDENTIFIER)
                .domainId(DOMAIN_IDENTIFIER)
                .environmentId(ENVIRONMENT_IDENTIFIER)
                .applicableAssetTypes(APPLICABLE_ASSET_TYPES)
                .authorizedPrincipals(AUTHORIZED_PRINCIPALS)
                .manageAccessRole(SUBSCRIPTION_TARGET_MANAGE_ACCESS_ROLE)
                .name(SUBSCRIPTION_TARGET_NAME)
                .projectId(PROJECT_ID)
                .provider(PROVIDER)
                .subscriptionTargetConfig(List.of(software.amazon.awssdk.services.datazone.model.SubscriptionTargetForm.builder()
                        .formName(GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM_NAME)
                        .content(GLUE_SUBSCRIPTION_TARGET_CONFIG)
                        .build()))
                .type(TARGET_TYPE)
                .build();
    }

    protected static void assertCfnResponse(ProgressEvent<ResourceModel, CallbackContext> response,
                                            OperationStatus responseStatus) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(responseStatus);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    protected static void assertResponseModel(ResourceModel resourceModel) {
        assertThat(resourceModel)
                .returns(SUBSCRIPTION_TARGET_IDENTIFIER, from(ResourceModel::getId))
                .returns(DOMAIN_IDENTIFIER, from(ResourceModel::getDomainId))
                .returns(ENVIRONMENT_IDENTIFIER, from(ResourceModel::getEnvironmentId))
                .returns(PROJECT_ID, from(ResourceModel::getProjectId))
                .returns(SUBSCRIPTION_TARGET_NAME, from(ResourceModel::getName))
                .returns(PROVIDER, from(ResourceModel::getProvider))
                .returns(TARGET_TYPE, from(ResourceModel::getType));

        // assert SubscriptionTargetConfig Name
        assertThat(resourceModel.getSubscriptionTargetConfig()).isNotNull();
        assertThat(resourceModel.getSubscriptionTargetConfig().size()).isEqualTo(1);
        assertThat(resourceModel.getSubscriptionTargetConfig().get(0))
                .returns(GLUE_SUBSCRIPTION_TARGET_CONFIG_FORM_NAME, from(SubscriptionTargetForm::getFormName))
                .returns(GLUE_SUBSCRIPTION_TARGET_CONFIG, from(SubscriptionTargetForm::getContent));
    }

}
