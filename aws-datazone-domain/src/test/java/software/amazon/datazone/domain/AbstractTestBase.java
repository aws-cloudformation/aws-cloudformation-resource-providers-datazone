package software.amazon.datazone.domain;

import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateDomainResponse;
import software.amazon.awssdk.services.datazone.model.DomainStatus;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static software.amazon.awssdk.services.datazone.model.AuthType.IAM_IDC;
import static software.amazon.awssdk.services.datazone.model.UserAssignment.AUTOMATIC;

public class AbstractTestBase {

    public static final String DOMAIN_ID = "dzd_2334up2ahl2wg4n";
    public static final String ACCOUNT_ID = "1234611448287";
    public static final String DOMAIN_ARN = "arn:aws:datazone:us-east-1:" + ACCOUNT_ID + ":domain/" + DOMAIN_ID;
    public static final String PORTAL_URL = "https://dzd_2334up2ahl2wg4n.datazone.us-east-1.on.aws";
    public static final String DOMAIN_NAME = "CFN-TEST";
    public static final String KMS_KEY_IDENTIFIER = "arn:aws:kms:us-east-1:" + ACCOUNT_ID + ":key/asdffs-eadb-4d6a-b6c1-fa51900fde9f";

    protected static final Credentials MOCK_CREDENTIALS;
    protected static final LoggerProxy logger;
    public static final String DOMAIN_DESCRIPTION = "Testing Domain";
    public static final String DOMAIN_EXECUTION_ROLE = "arn:aws:iam::" + ACCOUNT_ID + ":role/service-role/AmazonDataZoneDomainExecution";
    public static final Map<String, String> TAGS = Map.of("TAG1", "VALUE1");

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

    protected static ResourceModel getResourceModel() {
        final ResourceModel model = ResourceModel.builder()
                .description(DOMAIN_DESCRIPTION)
                .domainExecutionRole(DOMAIN_EXECUTION_ROLE)
                .id(DOMAIN_ID)
                .name(DOMAIN_NAME)
                .singleSignOn(SingleSignOn.builder()
                        .type(IAM_IDC.toString())
                        .userAssignment(AUTOMATIC.toString())
                        .build())
                .kmsKeyIdentifier(KMS_KEY_IDENTIFIER)
                .tags(Set.of(Tag.builder().key("TAG1").value("VALUE1").build()))
                .build();
        return model;
    }


    protected static void assertResponseModel(ResourceModel actualResourceModel,
                                              ResourceModel expectedResourceModel) {
        assertThat(actualResourceModel)
                .returns(expectedResourceModel.getDescription(), from(ResourceModel::getDescription))
                .returns(expectedResourceModel.getDomainExecutionRole(), from(ResourceModel::getDomainExecutionRole))
                .returns(expectedResourceModel.getName(), from(ResourceModel::getName))
                .returns(expectedResourceModel.getSingleSignOn(), from(ResourceModel::getSingleSignOn))
                .returns(expectedResourceModel.getKmsKeyIdentifier(), from(ResourceModel::getKmsKeyIdentifier))
                .returns(expectedResourceModel.getTags(), from(ResourceModel::getTags));
    }

    protected static void assertCfnResponse(ProgressEvent<ResourceModel, CallbackContext> response,
                                            OperationStatus responseStatus) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(responseStatus);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    protected static GetDomainResponse getGetDomainResponse(final @NonNull DomainStatus domainStatus) {
        return GetDomainResponse.builder()
                .arn(DOMAIN_ARN)
                .id(DOMAIN_ID)
                .portalUrl(PORTAL_URL)
                .description(DOMAIN_DESCRIPTION)
                .domainExecutionRole(DOMAIN_EXECUTION_ROLE)
                .name("CFN-TEST")
                .singleSignOn(software.amazon.awssdk.services.datazone.model.SingleSignOn.builder()
                        .type(IAM_IDC.toString())
                        .userAssignment(AUTOMATIC.toString())
                        .build())
                .createdAt(Instant.now())
                .lastUpdatedAt(Instant.now())
                .tags(TAGS)
                .kmsKeyIdentifier(KMS_KEY_IDENTIFIER)
                .status(domainStatus.toString())
                .build();
    }

    protected static CreateDomainResponse getCreateDomainResponse(final @NonNull DomainStatus domainStatus) {
        CreateDomainResponse createDomainResponse = CreateDomainResponse.builder()
                .arn(DOMAIN_ARN)
                .id(DOMAIN_ID)
                .portalUrl(PORTAL_URL)
                .description(DOMAIN_DESCRIPTION)
                .domainExecutionRole(DOMAIN_EXECUTION_ROLE)
                .name(DOMAIN_NAME)
                .singleSignOn(software.amazon.awssdk.services.datazone.model.SingleSignOn.builder()
                        .type(IAM_IDC.toString())
                        .userAssignment(AUTOMATIC.toString())
                        .build())
                .kmsKeyIdentifier(KMS_KEY_IDENTIFIER)
                .status(domainStatus.toString())
                .tags(TAGS)
                .build();
        return createDomainResponse;
    }

    protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        return request;
    }
}
