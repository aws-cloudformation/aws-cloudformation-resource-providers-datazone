package software.amazon.datazone.groupprofile;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.from;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AbstractTestBase {
    public static final String DOMAIN_IDENTIFIER = "dzd_5sewe4x1y5zajr";
    public static final String GROUP_IDENTIFIER = "d4d8e438-c041-7090-3e72-19f471ccedc3";
    public static final String ASSIGNED = "ASSIGNED";
    public static final String NOT_ASSIGNED = "NOT_ASSIGNED";
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
                .groupIdentifier(GROUP_IDENTIFIER)
                .status(ASSIGNED)
                .build();
    }

    protected static ResourceModel getNotAssignedModel() {
        return ResourceModel.builder()
                .domainIdentifier(DOMAIN_IDENTIFIER)
                .groupIdentifier(GROUP_IDENTIFIER)
                .status(NOT_ASSIGNED)
                .build();
    }

    protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
    }

    protected static CreateGroupProfileResponse getCreateGroupProfileResponse() {
        return CreateGroupProfileResponse.builder()
                .domainId(DOMAIN_IDENTIFIER)
                .id(GROUP_IDENTIFIER)
                .status(ASSIGNED)
                .build();
    }

    protected static GetGroupProfileResponse getGetGroupProfileResponse(String status) {
        return GetGroupProfileResponse.builder()
                .domainId(DOMAIN_IDENTIFIER)
                .id(GROUP_IDENTIFIER)
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
                .returns(GROUP_IDENTIFIER, from(ResourceModel::getId))
                .returns(DOMAIN_IDENTIFIER, from(ResourceModel::getDomainId))
                .returns(ASSIGNED, from(ResourceModel::getStatus));
    }
}
