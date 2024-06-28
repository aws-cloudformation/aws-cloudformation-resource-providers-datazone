package software.amazon.datazone.projectmembership;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateProjectMembershipResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsResponse;
import software.amazon.awssdk.services.datazone.model.MemberDetails;
import software.amazon.awssdk.services.datazone.model.ProjectMember;
import software.amazon.awssdk.services.datazone.model.UserDesignation;
import software.amazon.awssdk.services.datazone.model.UserDetails;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class AbstractTestBase {
    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected final ResourceModel model = ResourceModel.builder()
            .designation(UserDesignation.PROJECT_OWNER.toString())
            .domainIdentifier("dzd_66zup2ahl2wg4n")
            .projectIdentifier("659r11cckj5cx3")
            .member(Member.builder()
                    .userIdentifier("arn:aws:iam::200611448287:role/AmazonEKSLoadBalancerControllerRoleGamma")
                    .build())
            .memberIdentifier("user-id")
            .memberIdentifierType("USER_IDENTIFIER")
            .build();

    static ProxyClient<DataZoneClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final DataZoneClient dataZoneClient) {
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
                return dataZoneClient;
            }
        };
    }

    protected CreateProjectMembershipResponse getCreateProjectMembershipResponse() {
        return CreateProjectMembershipResponse.builder()
                .build();
    }

    protected ListProjectMembershipsResponse getListProjectMembershipsResponse() {
        return ListProjectMembershipsResponse.builder()
                .members(ProjectMember.builder()
                        .designation("PROJECT_OWNER")
                        .memberDetails(MemberDetails.fromUser(UserDetails.builder()
                                .userId("user-id")
                                .build()))
                        .build())
                .build();
    }

    protected GetUserProfileResponse getGetUserProfileResponse() {
        return GetUserProfileResponse.builder()
                .build();
    }
}
