package software.amazon.datazone.project;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateProjectResponse;
import software.amazon.awssdk.services.datazone.model.DeleteProjectResponse;
import software.amazon.awssdk.services.datazone.model.GetProjectResponse;
import software.amazon.awssdk.services.datazone.model.ListProjectsResponse;
import software.amazon.awssdk.services.datazone.model.ProjectSummary;
import software.amazon.awssdk.services.datazone.model.UpdateProjectResponse;
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

    protected static CreateProjectResponse getMockCreateProjectResponse(Instant time) {
        return CreateProjectResponse.builder()
                .id("project1")
                .domainId("mockdomainId")
                .createdBy("mockUser")
                .createdAt(time)
                .lastUpdatedAt(time)
                .glossaryTerms("glossary1", "glossary2")
                .description("Test project")
                .name("Project 1")
                .build();
    }

    protected static ListProjectsResponse getMockListProjectsResponse(Instant time) {
        return ListProjectsResponse.builder()
                .items(List.of(
                        ProjectSummary.builder()
                                .id("project1")
                                .domainId("mockdomainId")
                                .createdBy("mockUser1")
                                .createdAt(time)
                                .updatedAt(time)
                                .description("Test project 1")
                                .name("Project 1")
                                .build(),
                        ProjectSummary.builder()
                                .id("project2")
                                .domainId("mockdomainId")
                                .createdBy("mockUser2")
                                .createdAt(time)
                                .updatedAt(time)
                                .description("Test project 2")
                                .name("Project 2")
                                .build()
                ))
                .nextToken(null)
                .build();
    }

    protected static UpdateProjectResponse getMockUpdateProjectResponse(Instant time) {
        return UpdateProjectResponse.builder()
                .id("project1")
                .domainId("mockdomainId")
                .createdBy("mockUser")
                .createdAt(time)
                .lastUpdatedAt(time)
                .glossaryTerms("glossary1", "glossary2")
                .description("Test project")
                .name("Project 1")
                .build();
    }

    protected static GetProjectResponse getMockGetProjectResponse(Instant time) {
        return GetProjectResponse.builder()
                .id("project1")
                .domainId("mockdomainId")
                .createdBy("mockUser")
                .createdAt(time)
                .lastUpdatedAt(time)
                .glossaryTerms("glossary1", "glossary2")
                .description("Test project")
                .name("Project 1")
                .build();
    }

    protected static ResourceModel getResourceModelInput(Instant time) {
        return ResourceModel.builder()
                .id("project1")
                .domainIdentifier("mockdomainId")
                .createdBy("mockUser")
                .createdAt(time.toString())
                .lastUpdatedAt(time.toString())
                .glossaryTerms(List.of("glossary1", "glossary2"))
                .description("Test project")
                .name("Project 1")
                .build();
    }

    protected static ResourceModel getResourceModelOutput(Instant time) {
        return ResourceModel.builder()
                .id("project1")
                .domainId("mockdomainId")
                .createdBy("mockUser")
                .createdAt(time.toString())
                .lastUpdatedAt(time.toString())
                .glossaryTerms(List.of("glossary1", "glossary2"))
                .description("Test project")
                .name("Project 1")
                .build();
    }

    protected static ResourceModel getResourceModelForList() {
        return ResourceModel.builder()
                .domainIdentifier("mockdomainId")
                .build();
    }

    protected static ResourceModel getResourceModelForDelete() {
        return ResourceModel.builder()
                .domainIdentifier("mockdomainId")
                .id("project1")
                .build();
    }

    protected static DeleteProjectResponse getMockDeleteProjectResponse() {
        return (DeleteProjectResponse) DeleteProjectResponse.builder()
                .sdkHttpResponse(
                        SdkHttpResponse.builder()
                                .statusCode(204)
                                .build()
                )
                .build();
    }

    protected static DeleteProjectResponse getMockDeleteProjectFailedResponse() {
        return (DeleteProjectResponse) DeleteProjectResponse.builder()
                .sdkHttpResponse(
                        SdkHttpResponse.builder()
                                .statusCode(404)
                                .build()
                )
                .build();
    }
}
