package software.amazon.datazone.environmentblueprintconfiguration;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.EnvironmentBlueprintSummary;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTestBase {
    public static final String ENV_BLUEPRINT_ID = "asfdeaxxc";
    public static final List<String> ENABLED_REGIONS = List.of("us-east-1");
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;
    public static final String DOMAIN_IDENTIFIER = "dzd_66zup2ahl2wg4n";
    public static final String DATA_LAKE_ENV_BLUEPRINT_IDENTIFIER = "DefaultDataLake";
    public static final String MANAGE_ACCESS_ROLE_ARN = "arn:aws:iam::200611448287:role/service-role/AmazonDataZoneGlueAccess";
    public static final String PROVISIONING_ROLE_ARN = "arn:aws:iam::200611448287:role/AmazonDataZoneProvisioning";
    public static final Map<String, Map<String, String>> REGIONAL_PARAMETERS = Map.of("us-east-1", Map.of("S3Location", "s3://amazon-datazone-200611448287-us-east-1-420967702"));

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

    protected static ResourceModel getResourceModel(List<String> enabledRegions) {
        return ResourceModel.builder()
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .managed(true)
                .environmentBlueprintIdentifier(DATA_LAKE_ENV_BLUEPRINT_IDENTIFIER)
                .manageAccessRoleArn(MANAGE_ACCESS_ROLE_ARN)
                .enabledRegions(enabledRegions)
                .provisioningRoleArn(PROVISIONING_ROLE_ARN)
                .regionalParameters(Translator.getRegionalParametersForResourceModel(REGIONAL_PARAMETERS))
                .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    protected static GetEnvironmentBlueprintConfigurationResponse getGetEnvironmentBlueprintConfigurationResponse(List<String> enabledRegions) {
        return GetEnvironmentBlueprintConfigurationResponse.builder()
                .domainId(DOMAIN_IDENTIFIER)
                .environmentBlueprintId(ENV_BLUEPRINT_ID)
                .manageAccessRoleArn(MANAGE_ACCESS_ROLE_ARN)
                .enabledRegions(enabledRegions)
                .provisioningRoleArn(PROVISIONING_ROLE_ARN)
                .regionalParameters(REGIONAL_PARAMETERS)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    protected static ListEnvironmentBlueprintsResponse getListEnvironmentBlueprintsResponse() {
        return ListEnvironmentBlueprintsResponse.builder()
                .items(EnvironmentBlueprintSummary.builder()
                        .id(ENV_BLUEPRINT_ID)
                        .name(DATA_LAKE_ENV_BLUEPRINT_IDENTIFIER)
                        .build())
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
        assertResponseModel(resourceModel, ENABLED_REGIONS);
    }

    protected static void assertResponseModel(ResourceModel resourceModel, List<String> enabledRegions) {
        assertThat(resourceModel).isNotNull();
        assertThat(resourceModel.getDomainIdentifier()).isEqualTo(DOMAIN_IDENTIFIER);
        assertThat(resourceModel.getDomainId()).isEqualTo(DOMAIN_IDENTIFIER);
        assertThat(resourceModel.getEnvironmentBlueprintId()).isEqualTo(ENV_BLUEPRINT_ID);
        assertThat(resourceModel.getEnvironmentBlueprintIdentifier()).isNull(); // This would be null
        assertThat(resourceModel.getManageAccessRoleArn()).isEqualTo(MANAGE_ACCESS_ROLE_ARN);
        assertThat(resourceModel.getProvisioningRoleArn()).isEqualTo(PROVISIONING_ROLE_ARN);
        assertThat(resourceModel.getEnabledRegions()).isEqualTo(enabledRegions);
        assertThat(resourceModel.getRegionalParameters()).isEqualTo(Translator.getRegionalParametersForResourceModel(REGIONAL_PARAMETERS));
    }
}
