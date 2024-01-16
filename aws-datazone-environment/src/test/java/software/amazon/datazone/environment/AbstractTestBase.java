package software.amazon.datazone.environment;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CloudFormationProperties;
import software.amazon.awssdk.services.datazone.model.ConfigurableActionParameter;
import software.amazon.awssdk.services.datazone.model.ConfigurableEnvironmentAction;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.CustomParameter;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.Deployment;
import software.amazon.awssdk.services.datazone.model.DeploymentProperties;
import software.amazon.awssdk.services.datazone.model.DeploymentStatus;
import software.amazon.awssdk.services.datazone.model.EnvironmentStatus;
import software.amazon.awssdk.services.datazone.model.EnvironmentSummary;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentsResponse;
import software.amazon.awssdk.services.datazone.model.ProvisioningProperties;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

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

    protected static void assertCfnResponse(ProgressEvent<ResourceModel, CallbackContext> response,
                                            OperationStatus responseStatus) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(responseStatus);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    protected ResourceModel getResourceModelForHandlerRequest() {
        return ResourceModel.builder()
                .description("Test Description")
                .domainIdentifier("domain1")
                .environmentProfileIdentifier("envProfile1")
                .name("envName")
                .projectIdentifier("project1")
                .userParameters(List.of(
                        software.amazon.datazone.environment.EnvironmentParameter.builder()
                                .name("workgroupName")
                                .value("workgroupName1")
                                .build()
                ))
                .build();
    }

    protected ResourceModel getResourceModelForDelete() {
        return ResourceModel.builder()
                .domainId("domain1")
                .id("envId1")
                .build();
    }

    protected ResourceModel getResourceModelForList() {
        return ResourceModel.builder()
                .domainId("domain1")
                .projectId("project1")
                .build();
    }

    protected CreateEnvironmentResponse getCreateEnvironmentResponse(EnvironmentStatus status) {
        return CreateEnvironmentResponse.builder()
                .id("envId1")
                .domainId("domain1")
                .description("Test Description")
                .environmentProfileId("envProfile1")
                .name("envName")
                .projectId("project1")
                .status(String.valueOf(status))
                .build();
    }

    protected DeleteEnvironmentResponse getDeleteEnvironmentResponse() {
        return DeleteEnvironmentResponse.builder()
                .build();
    }

    protected UpdateEnvironmentResponse getUpdateEnvironmentResponse(EnvironmentStatus status) {
        return UpdateEnvironmentResponse.builder()
                .id("envId1")
                .domainId("domain1")
                .description("Test Description")
                .environmentProfileId("envProfile1")
                .name("envName")
                .projectId("project1")
                .status(String.valueOf(status))
                .build();
    }

    protected GetEnvironmentResponse getGetEnvironmentResponse(EnvironmentStatus status, Instant currTime) {
        return getGetEnvironmentResponseBuilder(status, currTime).build();
    }

    protected GetEnvironmentResponse.Builder getGetEnvironmentResponseBuilder(EnvironmentStatus status, Instant currTime) {
        return GetEnvironmentResponse.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(currTime)
                .createdBy("user1")
                .deploymentProperties(
                        DeploymentProperties.builder()
                                .endTimeoutMinutes(5)
                                .startTimeoutMinutes(5)
                                .build()
                )
                .description("Test Description")
                .domainId("domain1")
                .environmentActions(List.of(
                        ConfigurableEnvironmentAction.builder()
                                .type("openConsole")
                                .auth("IAM")
                                .parameters(ConfigurableActionParameter.builder()
                                        .key("icon")
                                        .value("ImageSearch")
                                        .build())
                                .build()
                ))
                .environmentBlueprintId("envBlueprintId1")
                .environmentProfileId("envProfile1")
                .glossaryTerms("glossaryTerms1")
                .id("envId1")
                .lastDeployment(
                        Deployment.builder()
                                .deploymentId("5157krc2f3big7")
                                .deploymentType("CREATE")
                                .deploymentStatus(DeploymentStatus.SUCCESSFUL)
                                .messages("")
                                .isDeploymentComplete(true)
                                .build()
                )
                .name("envName")
                .projectId("project1")
                .provider("Amazon DataZone")
                .provisioningProperties(
                        ProvisioningProperties.builder()
                                .cloudFormation(
                                        CloudFormationProperties.builder()
                                                .templateUrl("https://env-blueprint-access-.s3-accesspoint.us-east-1.amazonaws.com/v1/DataZone-DataLakeDefault-prod.template.json\"")
                                                .build()
                                )
                                .build()
                )
                .provisionedResources(List.of())
                .status(String.valueOf(status))
                .updatedAt(currTime)
                .userParameters(List.of(
                        CustomParameter.builder()
                                .fieldType("String")
                                .description("Use this input to customize the name of the Athena Workgroup that is created and used to store query history of queries that are run.")
                                .isEditable(true)
                                .isOptional(true)
                                .keyName("workgroupName")
                                .defaultValue("workgroupName1")
                                .build()
                ));
    }

    protected ListEnvironmentsResponse getListEnvironmentsResponse(Instant currTime) {
        return ListEnvironmentsResponse.builder()
                .items(
                        List.of(
                                EnvironmentSummary.builder()
                                        .awsAccountId("123456789012")
                                        .awsAccountRegion("us-east-1")
                                        .createdAt(currTime)
                                        .createdBy("user1")
                                        .description("Test Description")
                                        .domainId("domain1")
                                        .environmentProfileId("envProfile1")
                                        .id("envId1")
                                        .name("envName1")
                                        .projectId("project1")
                                        .provider("Amazon DataZone")
                                        .status(EnvironmentStatus.ACTIVE)
                                        .updatedAt(currTime.plusSeconds(60 * 15))
                                        .build(),
                                EnvironmentSummary.builder()
                                        .awsAccountId("123456789012")
                                        .awsAccountRegion("us-east-1")
                                        .createdAt(currTime)
                                        .createdBy("user1")
                                        .description("Test Description")
                                        .domainId("domain1")
                                        .environmentProfileId("envProfile1")
                                        .id("envId2")
                                        .name("envName2")
                                        .projectId("project1")
                                        .provider("Amazon DataZone")
                                        .status(EnvironmentStatus.ACTIVE)
                                        .updatedAt(currTime.plusSeconds(60 * 15))
                                        .build()
                        )
                )
                .build();
    }

}
