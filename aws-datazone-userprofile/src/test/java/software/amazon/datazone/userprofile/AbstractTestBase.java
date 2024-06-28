package software.amazon.datazone.userprofile;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.SingleSignOn;
import software.amazon.awssdk.services.datazone.model.SsoUserProfileDetails;
import software.amazon.awssdk.services.datazone.model.UserAssignment;
import software.amazon.awssdk.services.datazone.model.UserProfileDetails;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

public class AbstractTestBase {
    public static final String DOMAIN_IDENTIFIER = "dzd_aivmklr8fuu2ef";
    public static final String USER_IDENTIFIER = "44d8b438-00c1-7028-91d4-233e4f949bbf";
    public static final String DOMAIN_ID = "test-domain-id";
    public static final String DOMAIN_NAME = "test-domain-name";
    public static final String USER_TYPE = "SSO_USER";
    public static final String TYPE = "SSO";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String ACTIVATED = "ACTIVATED";
    public static final String DEACTIVATED = "DEACTIVATED";
    public static final String NOT_ASSIGNED = "NOT_ASSIGNED";
    public static final UserProfileDetails DETAILS = UserProfileDetails.builder()
            .sso(SsoUserProfileDetails.builder()
                    .firstName("FIRST_NAME")
                    .lastName("LAST_NAME")
                    .username("USER_NAME")
                    .build())
            .build();

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    static ProxyClient<DataZoneClient> MOCK_PROXY(
            final AmazonWebServicesClientProxy proxy,
            final DataZoneClient dataZoneClient) {
        return new ProxyClient<>() {
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

    protected static ResourceModel getModel() {
        return ResourceModel.builder()
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .userIdentifier(USER_IDENTIFIER)
                .userType(USER_TYPE)
                .status(ASSIGNED)
                .build();
    }

    protected static ResourceModel getActivatedModel() {
        return ResourceModel.builder()
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .userIdentifier(USER_IDENTIFIER)
                .userType(USER_TYPE)
                .status(ACTIVATED)
                .build();
    }

    protected static ResourceModel getDeactivatedModel() {
        return ResourceModel.builder()
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .userIdentifier(USER_IDENTIFIER)
                .userType(USER_TYPE)
                .status(DEACTIVATED)
                .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    protected static GetDomainResponse getGetDomainResponse(UserAssignment userAssignment) {
        return GetDomainResponse.builder()
                .id(DOMAIN_ID)
                .name(DOMAIN_NAME)
                .singleSignOn(SingleSignOn.builder()
                        .type("IAM_IDC")
                        .userAssignment(userAssignment)
                        .build())
                .build();
    }

    protected static CreateUserProfileResponse getCreateUserProfileResponse() {
        return CreateUserProfileResponse.builder()
                .id(USER_IDENTIFIER)
                .type(TYPE)
                .domainId(DOMAIN_IDENTIFIER)
                .status(ASSIGNED)
                .build();
    }

    protected static GetUserProfileResponse getGetUserProfileResponse(String status) {
        return GetUserProfileResponse.builder()
                .id(USER_IDENTIFIER)
                .type(TYPE)
                .domainId(DOMAIN_IDENTIFIER)
                .status(status)
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
                .returns(USER_IDENTIFIER, from(ResourceModel::getId))
                .returns(DOMAIN_IDENTIFIER, from(ResourceModel::getDomainId))
                .returns(TYPE, from(ResourceModel::getType))
                .returns(ASSIGNED, from(ResourceModel::getStatus));
    }

}
