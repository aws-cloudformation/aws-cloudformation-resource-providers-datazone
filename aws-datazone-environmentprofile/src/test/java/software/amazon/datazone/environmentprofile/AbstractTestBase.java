package software.amazon.datazone.environmentprofile;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.CustomParameter;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.EnvironmentProfileSummary;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesResponse;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentProfileResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

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

    protected ResourceModel getResourceModelInput(Instant time) {
        return ResourceModel.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(time.toString())
                .createdBy("user1")
                .description("Test description for Environment Profile")
                .environmentBlueprintIdentifier("envBluePrint1")
                .id("envProfile1")
                .name("environmentProfileName1")
                .domainIdentifier("domainId1")
                .projectIdentifier("project1")
                .updatedAt(time.plusSeconds(60 * 15).toString())
                .userParameters(List.of(
                        software.amazon.datazone.environmentprofile.EnvironmentParameter.builder()
                                .value("someWorkgroupName1")
                                .name("workgroupName")
                                .build()
                ))
                .build();
    }

    protected ResourceModel getResourceModelOutput(Instant time) {
        return ResourceModel.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(time.toString())
                .createdBy("user1")
                .description("Test description for Environment Profile")
                .environmentBlueprintId("envBluePrint1")
                .id("envProfile1")
                .name("environmentProfileName1")
                .domainId("domainId1")
                .projectId("project1")
                .updatedAt(time.plusSeconds(60 * 15).toString())
                .userParameters(List.of(
                        software.amazon.datazone.environmentprofile.EnvironmentParameter.builder()
                                .value("someWorkgroupName1")
                                .name("workgroupName")
                                .build()
                ))
                .build();
    }

    protected ResourceModel getResourceModelForList() {
        return ResourceModel.builder()
                .domainIdentifier("domainId")
                .build();
    }

    protected ResourceModel getResourceModelForDelete() {
        return ResourceModel.builder()
                .id("envProfile1")
                .domainIdentifier("domainId1")
                .build();
    }

    protected CreateEnvironmentProfileResponse getCreateEnvironmentProfileResponse(Instant time) {
        return CreateEnvironmentProfileResponse.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(time)
                .createdBy("user1")
                .description("Test description for Environment Profile")
                .environmentBlueprintId("envBluePrint1")
                .id("envProfile1")
                .domainId("domainId1")
                .name("environmentProfileName1")
                .projectId("project1")
                .updatedAt(null)
                .userParameters(List.of(
                        CustomParameter.builder()
                                .defaultValue("someWorkgroupName1")
                                .description("Use this input to customize the name of the Athena Workgroup that is created and used to store query history of queries that are run.")
                                .isEditable(true)
                                .isOptional(true)
                                .fieldType("String")
                                .keyName("workgroupName")
                                .build()
                ))
                .build();
    }

    protected UpdateEnvironmentProfileResponse getUpdateEnvironmentProfileResponse(Instant time) {
        return UpdateEnvironmentProfileResponse.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(time)
                .createdBy("user1")
                .description("Test description for Environment Profile")
                .environmentBlueprintId("envBluePrint1")
                .id("envProfile1")
                .domainId("domainId1")
                .name("environmentProfileName1")
                .projectId("project1")
                .updatedAt(null)
                .userParameters(List.of(
                        CustomParameter.builder()
                                .defaultValue("someWorkgroupName1")
                                .description("Use this input to customize the name of the Athena Workgroup that is created and used to store query history of queries that are run.")
                                .isEditable(true)
                                .isOptional(true)
                                .fieldType("String")
                                .keyName("workgroupName")
                                .build()
                ))
                .build();
    }

    protected GetEnvironmentProfileResponse getGetEnvironmentProfileResponse(Instant time) {
        return GetEnvironmentProfileResponse.builder()
                .awsAccountId("123456789012")
                .awsAccountRegion("us-east-1")
                .createdAt(time)
                .createdBy("user1")
                .description("Test description for Environment Profile")
                .environmentBlueprintId("envBluePrint1")
                .id("envProfile1")
                .domainId("domainId1")
                .name("environmentProfileName1")
                .projectId("project1")
                .updatedAt(time.plusSeconds(60 * 15))
                .userParameters(List.of(
                        CustomParameter.builder()
                                .defaultValue("someWorkgroupName1")
                                .description("Use this input to customize the name of the Athena Workgroup that is created and used to store query history of queries that are run.")
                                .isEditable(true)
                                .isOptional(true)
                                .fieldType("String")
                                .keyName("workgroupName")
                                .build()
                ))
                .build();
    }

    protected ListEnvironmentProfilesResponse getListEnvironmentProfilesResponse(Instant time) {
        return ListEnvironmentProfilesResponse.builder()
                .items(
                        List.of(
                                EnvironmentProfileSummary.builder()
                                        .awsAccountId("123456789012")
                                        .awsAccountRegion("us-east-1")
                                        .createdAt(time)
                                        .createdBy("user1")
                                        .description("Test description for Environment Profile 1")
                                        .environmentBlueprintId("envBluePrint1")
                                        .id("envProfile1")
                                        .name("environmentProfileName1")
                                        .domainId("domainId1")
                                        .projectId("project1")
                                        .updatedAt(time.plusSeconds(60 * 15))
                                        .build(),
                                EnvironmentProfileSummary.builder()
                                        .awsAccountId("123456789012")
                                        .awsAccountRegion("us-east-1")
                                        .createdAt(time.plusSeconds(60 * 15))
                                        .createdBy("user1")
                                        .description("Test description for Environment Profile 2")
                                        .environmentBlueprintId("envBluePrint1")
                                        .id("envProfile2")
                                        .name("environmentProfileName2")
                                        .domainId("domainId1")
                                        .projectId("project1")
                                        .updatedAt(time.plusSeconds(60 * 30))
                                        .build()
                        )
                )
                .build();
    }

    protected DeleteEnvironmentProfileResponse getDeleteEnvironmentProfileResponse() {
        return (DeleteEnvironmentProfileResponse) DeleteEnvironmentProfileResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(204)
                        .build())
                .build();
    }

    protected DeleteEnvironmentProfileResponse getDeleteEnvironmentProfileResponseWithError() {
        return (DeleteEnvironmentProfileResponse) DeleteEnvironmentProfileResponse.builder()
                .sdkHttpResponse(SdkHttpResponse.builder()
                        .statusCode(404)
                        .build())
                .build();
    }

}
