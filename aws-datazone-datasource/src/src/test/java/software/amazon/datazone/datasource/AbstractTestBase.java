package software.amazon.datazone.datasource;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.DataSourceConfigurationOutput;
import software.amazon.awssdk.services.datazone.model.DataSourceStatus;
import software.amazon.awssdk.services.datazone.model.EnableSetting;
import software.amazon.awssdk.services.datazone.model.FilterExpressionType;
import software.amazon.awssdk.services.datazone.model.GetDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.GlueRunConfigurationOutput;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

public class AbstractTestBase {
  public static final String DATA_SOURCE_TYPE = "GLUE";
  protected static final Credentials MOCK_CREDENTIALS;
  protected static final LoggerProxy logger;
  public static final String DATA_SOURCE_IDENTIFIER = "611nkvhuji00iv";
  public static final String DATABASE_NAME = "defaultdatalakeenvironment_pub_db";
  public static final String DOMAIN_IDENTIFIER = "dzd_66zup2ahl2wg4n";
  public static final String DATA_ACCESS_ROLE = "arn:aws:iam::200611448287:role/service-role/AmazonDataZoneGlueAccess-us-east-1-dzd_66zup2ahl2wg4n";
  public static final String DESCRIPTION = "Created via CFN";
  public static final String ENABLED_STRING = EnableSetting.ENABLED.toString();
  public static final String ENVIRONMENT_IDENTIFIER = "6g1nkvhuji00iv";
  public static final String DATA_SOURCE_NAME = "DS1";
  public static final String PROJECT_IDENTIFIER = "b3ovsfpg5srfon";
  public static final RecommendationConfiguration RECOMMENDATION_CONFIGURATION = RecommendationConfiguration.builder().enableBusinessNameGeneration(true).build();
  public static final String SCHEDULE_CRON = "cron(52 7 * * ? *)";
  public static final ScheduleConfiguration SCHEDULE_CONFIGURATION = ScheduleConfiguration.builder().schedule(SCHEDULE_CRON).build();
  public static software.amazon.awssdk.services.datazone.model.RelationalFilterConfiguration RELATION_FILTER_CONFIGURATIONS =
          software.amazon.awssdk.services.datazone.model.RelationalFilterConfiguration.builder()
          .databaseName(DATABASE_NAME)
          .filterExpressions(List.of(software.amazon.awssdk.services.datazone.model.FilterExpression.builder()
                  .expression("*")
                  .type(FilterExpressionType.INCLUDE.toString())
                  .build()))
          .build();

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
            .domainIdentifier(DOMAIN_IDENTIFIER)
            .domainId(DOMAIN_IDENTIFIER)
            .configuration(DataSourceConfigurationInput.builder().glueRunConfiguration(
                            GlueRunConfigurationInput.builder()
                                    .dataAccessRole(DATA_ACCESS_ROLE)
                                    .relationalFilterConfigurations(List.of(RelationalFilterConfiguration.builder()
                                            .databaseName(DATABASE_NAME)
                                            .filterExpressions(List.of(FilterExpression.builder()
                                                    .expression("*")
                                                    .type(FilterExpressionType.INCLUDE.toString())
                                                    .build()))
                                            .build()))
                                    .build())
                    .build()
            )
            .description(DESCRIPTION)
            .enableSetting(ENABLED_STRING)
            .environmentIdentifier(ENVIRONMENT_IDENTIFIER)
            .name(DATA_SOURCE_NAME)
            .projectIdentifier(PROJECT_IDENTIFIER)
            .publishOnImport(false)
            .recommendation(RECOMMENDATION_CONFIGURATION)
            .schedule(SCHEDULE_CONFIGURATION)
            .type(DATA_SOURCE_TYPE)
            .build();
  }

  protected CreateDataSourceResponse getCreateDataSourceResponse(DataSourceStatus dataSourceStatus) {
    return CreateDataSourceResponse.builder()
            .id(DATA_SOURCE_IDENTIFIER)
            .domainId(DOMAIN_IDENTIFIER)
            .configuration(DataSourceConfigurationOutput.builder().glueRunConfiguration(
                            GlueRunConfigurationOutput.builder()
                                    .dataAccessRole(DATA_ACCESS_ROLE)
                                    .relationalFilterConfigurations(List.of(RELATION_FILTER_CONFIGURATIONS))
                                    .build())
                    .build()
            )
            .description(DESCRIPTION)
            .status(dataSourceStatus)
            .enableSetting(ENABLED_STRING)
            .environmentId(ENVIRONMENT_IDENTIFIER)
            .name(DATA_SOURCE_NAME)
            .projectId(PROJECT_IDENTIFIER)
            .publishOnImport(false)
            .recommendation(software.amazon.awssdk.services.datazone.model.RecommendationConfiguration.builder().enableBusinessNameGeneration(true).build())
            .schedule(software.amazon.awssdk.services.datazone.model.ScheduleConfiguration.builder().schedule(SCHEDULE_CRON).build())
            .type(DATA_SOURCE_TYPE)
            .build();
  }

  protected GetDataSourceResponse getGetDataSourceResponse(DataSourceStatus dataSourceStatus) {
    return getGetDataSourceResponseBuilder(dataSourceStatus).build();
  }

  protected GetDataSourceResponse.Builder getGetDataSourceResponseBuilder(DataSourceStatus dataSourceStatus) {
    return GetDataSourceResponse.builder()
            .id(DATA_SOURCE_IDENTIFIER)
            .domainId(DOMAIN_IDENTIFIER)
            .configuration(DataSourceConfigurationOutput.builder().glueRunConfiguration(
                            GlueRunConfigurationOutput.builder()
                                    .dataAccessRole(DATA_ACCESS_ROLE)
                                    .relationalFilterConfigurations(List.of(RELATION_FILTER_CONFIGURATIONS))
                                    .build())
                    .build()
            )
            .description(DESCRIPTION)
            .status(dataSourceStatus)
            .enableSetting(ENABLED_STRING)
            .environmentId(ENVIRONMENT_IDENTIFIER)
            .name(DATA_SOURCE_NAME)
            .projectId(PROJECT_IDENTIFIER)
            .publishOnImport(false)
            .recommendation(software.amazon.awssdk.services.datazone.model.RecommendationConfiguration.builder().enableBusinessNameGeneration(true).build())
            .schedule(software.amazon.awssdk.services.datazone.model.ScheduleConfiguration.builder().schedule(SCHEDULE_CRON).build())
            .type(DATA_SOURCE_TYPE);
  }

  protected static void assertCfnResponse(ProgressEvent<ResourceModel, CallbackContext> response,
                                          OperationStatus responseStatus) {
    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(responseStatus);
    assertThat(response.getResourceModels()).isNull();
    assertThat(response.getMessage()).isNull();
    assertThat(response.getErrorCode()).isNull();
  }

  protected static void assertResponseModel(ResourceModel actualResourceModel,
                                            ResourceModel expectedResourceModel) {
    assertThat(actualResourceModel)
            .returns(expectedResourceModel.getDescription(), from(ResourceModel::getDescription))
            .returns(expectedResourceModel.getDomainId(), from(ResourceModel::getDomainId))
            .returns(expectedResourceModel.getName(), from(ResourceModel::getName));

   }

  protected static ResourceHandlerRequest<ResourceModel> getResourceHandlerRequest(ResourceModel model) {
    return ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();
  }
}
