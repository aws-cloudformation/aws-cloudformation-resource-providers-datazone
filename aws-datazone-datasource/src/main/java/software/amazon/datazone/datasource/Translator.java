package software.amazon.datazone.datasource;

import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.CreateDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.DataSourceConfigurationInput;
import software.amazon.awssdk.services.datazone.model.DeleteDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.FilterExpression;
import software.amazon.awssdk.services.datazone.model.FormInput;
import software.amazon.awssdk.services.datazone.model.GetDataSourceRequest;
import software.amazon.awssdk.services.datazone.model.GlueRunConfigurationInput;
import software.amazon.awssdk.services.datazone.model.ListDataSourcesRequest;
import software.amazon.awssdk.services.datazone.model.RecommendationConfiguration;
import software.amazon.awssdk.services.datazone.model.RedshiftClusterStorage;
import software.amazon.awssdk.services.datazone.model.RedshiftCredentialConfiguration;
import software.amazon.awssdk.services.datazone.model.RedshiftRunConfigurationInput;
import software.amazon.awssdk.services.datazone.model.RedshiftServerlessStorage;
import software.amazon.awssdk.services.datazone.model.RedshiftStorage;
import software.amazon.awssdk.services.datazone.model.RelationalFilterConfiguration;
import software.amazon.awssdk.services.datazone.model.ScheduleConfiguration;
import software.amazon.awssdk.services.datazone.model.UpdateDataSourceRequest;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

    /**
     * Helper function to convert the Resource Model to CreateDataSourceRequest which would be used for creating
     * the DataSource.
     *
     * @param model              Resource model for the DataSource.
     * @param clientRequestToken The unique identifier for the request.
     * @return The CreateDataSourceRequest for the DataSource.
     */
    public static CreateDataSourceRequest translateToCreateRequest(final @NonNull ResourceModel model,
                                                                   final @NonNull String clientRequestToken) {
        final List<FormInput> formInput = Objects.isNull(model.getAssetFormsInput()) ? null : getFormInputFromModel(model);
        final DataSourceConfigurationInput inputConfiguration = getInputConfiguration(model);
        final RecommendationConfiguration recommendationConfiguration = getRecommendationConfiguration(model);
        final ScheduleConfiguration scheduleConfiguration = getScheduleConfiguration(model);

        return CreateDataSourceRequest.builder()
                .assetFormsInput(formInput)
                .domainIdentifier(model.getDomainIdentifier())
                .clientToken(clientRequestToken)
                .configuration(inputConfiguration)
                .description(model.getDescription())
                .enableSetting(model.getEnableSetting())
                .environmentIdentifier(model.getEnvironmentIdentifier())
                .name(model.getName())
                .projectIdentifier(model.getProjectIdentifier())
                .publishOnImport(model.getPublishOnImport())
                .recommendation(recommendationConfiguration)
                .schedule(scheduleConfiguration)
                .type(model.getType())
                .build();
    }

    /**
     * Helper function to convert the Resource Model to GetDataSourceRequest which would be used for fetching
     * the DataSource.
     *
     * @param model Resource model for the DataSource.
     * @return The GetDataSourceRequest for the DataSource.
     */
    public static GetDataSourceRequest translateToReadRequest(final @NonNull ResourceModel model) {
        return GetDataSourceRequest.builder()
                .identifier(model.getId())
                .domainIdentifier(getDomainId(model))
                .build();
    }

    /**
     * Helper function to convert the Resource Model to DeleteDataSourceRequest which would be used for
     * deleting the DataSource.
     *
     * @param model              Resource model for the DataSource
     * @param clientRequestToken The unique identifier for the request.
     * @return The DeleteDataSourceRequest for the DataSource.
     */
    static DeleteDataSourceRequest translateToDeleteRequest(final @NonNull ResourceModel model,
                                                            final @NonNull String clientRequestToken) {
        return DeleteDataSourceRequest.builder()
                .identifier(model.getId())
                .domainIdentifier(getDomainId(model))
                .clientToken(clientRequestToken)
                .build();
    }

    /**
     * Helper function to convert the Resource Model to UpdateDataSourceRequest which would be used for
     * updating the DataSource.
     *
     * @param model Resource model for the DataSource
     * @return The UpdateDataSourceRequest for the DataSource.
     */
    public static UpdateDataSourceRequest translateToUpdateRequest(final @NonNull ResourceModel model) {
        final List<FormInput> formInput = Objects.isNull(model.getAssetFormsInput()) ? null : getFormInputFromModel(model);
        final DataSourceConfigurationInput inputConfiguration = getInputConfiguration(model);
        final RecommendationConfiguration recommendationConfiguration = getRecommendationConfiguration(model);
        final ScheduleConfiguration scheduleConfiguration = getScheduleConfiguration(model);

        return UpdateDataSourceRequest.builder()
                .identifier(model.getId())
                .domainIdentifier(model.getDomainId())
                .assetFormsInput(formInput)
                .configuration(inputConfiguration)
                .description(model.getDescription())
                .enableSetting(model.getEnableSetting())
                .name(model.getName())
                .publishOnImport(model.getPublishOnImport())
                .recommendation(recommendationConfiguration)
                .schedule(scheduleConfiguration)
                .build();
    }

    /**
     * Helper function to convert the Resource Model to ListDataSourcesRequest which would be used for
     * listing the DataSources under a domain.
     *
     * @param model     Resource model containing DomainIdentifier which would be used for listing the DataSources under the same.
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListDataSourcesRequest translateToListRequest(final @NonNull ResourceModel model,
                                                         final String nextToken) {
        return ListDataSourcesRequest.builder()
                .domainIdentifier(getDomainId(model))
                .projectIdentifier(Optional.ofNullable(model.getProjectIdentifier()).orElse(model.getProjectId()))
                .nextToken(nextToken)
                .build();
    }

    private static String getDomainId(ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }


    private static RecommendationConfiguration getRecommendationConfiguration(ResourceModel model) {
        if (Objects.isNull(model.getRecommendation())) {
            return null;
        }
        return RecommendationConfiguration.builder()
                .enableBusinessNameGeneration(model.getRecommendation().getEnableBusinessNameGeneration())
                .build();
    }

    private static DataSourceConfigurationInput getInputConfiguration(ResourceModel model) {
        if (Objects.isNull(model.getConfiguration())) {
            return null;
        }
        return DataSourceConfigurationInput.builder()
                .glueRunConfiguration(getGlueRunConfigurationInput(model))
                .redshiftRunConfiguration(getRedshiftRunConfiguration(model))
                .build();
    }

    private static RedshiftRunConfigurationInput getRedshiftRunConfiguration(ResourceModel model) {
        software.amazon.datazone.datasource.RedshiftRunConfigurationInput redshiftRunConfiguration = model.getConfiguration().getRedshiftRunConfiguration();
        if (Objects.isNull(redshiftRunConfiguration)) {
            return null;
        }

        RedshiftCredentialConfiguration redshiftCredentialConfiguration = getRedshiftCredentialConfiguration(redshiftRunConfiguration.getRedshiftCredentialConfiguration());
        RedshiftStorage redshiftStorage = getRedshiftStorage(redshiftRunConfiguration.getRedshiftStorage());
        List<RelationalFilterConfiguration> filterConfigurationsForRedshift = getRelationalFilterConfigurations(redshiftRunConfiguration.getRelationalFilterConfigurations());

        return RedshiftRunConfigurationInput.builder()
                .redshiftCredentialConfiguration(redshiftCredentialConfiguration)
                .redshiftStorage(redshiftStorage)
                .relationalFilterConfigurations(filterConfigurationsForRedshift)
                .dataAccessRole(redshiftRunConfiguration.getDataAccessRole())
                .build();
    }

    private static RedshiftCredentialConfiguration getRedshiftCredentialConfiguration(
            software.amazon.datazone.datasource.RedshiftCredentialConfiguration redshiftCredentialConfiguration) {
        return Objects.isNull(redshiftCredentialConfiguration) ? null : RedshiftCredentialConfiguration.builder()
                .secretManagerArn(redshiftCredentialConfiguration.getSecretManagerArn())
                .build();
    }

    private static RedshiftStorage getRedshiftStorage(software.amazon.datazone.datasource.RedshiftStorage redshiftStorage) {
        if (Objects.isNull(redshiftStorage)) {
            return null;
        }

        return RedshiftStorage.builder()
                .redshiftClusterSource(getRedshiftClusterSource(redshiftStorage.getRedshiftClusterSource()))
                .redshiftServerlessSource(getRedshiftServerlessSource(redshiftStorage.getRedshiftServerlessSource()))
                .build();
    }

    private static RedshiftServerlessStorage getRedshiftServerlessSource(software.amazon.datazone.datasource.RedshiftServerlessStorage redshiftServerlessSource) {
        return Objects.isNull(redshiftServerlessSource) ? null : RedshiftServerlessStorage.builder()
                .workgroupName(redshiftServerlessSource.getWorkgroupName())
                .build();
    }

    private static RedshiftClusterStorage getRedshiftClusterSource(software.amazon.datazone.datasource.RedshiftClusterStorage redshiftClusterSource) {
        return Objects.isNull(redshiftClusterSource) ? null : RedshiftClusterStorage.builder()
                .clusterName(redshiftClusterSource.getClusterName())
                .build();
    }

    private static GlueRunConfigurationInput getGlueRunConfigurationInput(ResourceModel model) {
        software.amazon.datazone.datasource.GlueRunConfigurationInput glueRunConfiguration = model.getConfiguration().getGlueRunConfiguration();
        if (Objects.isNull(glueRunConfiguration)) {
            return null;
        }

        List<RelationalFilterConfiguration> filterConfigurationsForGlue = getRelationalFilterConfigurations(glueRunConfiguration.getRelationalFilterConfigurations());

        return GlueRunConfigurationInput.builder()
                .dataAccessRole(glueRunConfiguration.getDataAccessRole())
                .relationalFilterConfigurations(filterConfigurationsForGlue)
                .build();
    }

    private static List<RelationalFilterConfiguration> getRelationalFilterConfigurations(
            List<software.amazon.datazone.datasource.RelationalFilterConfiguration> filterConfigurations) {
        List<RelationalFilterConfiguration> filterConfigurationsForGlue = getNullSafeStream(filterConfigurations)
                .map(relationalFilterConfiguration -> RelationalFilterConfiguration.builder()
                        .databaseName(relationalFilterConfiguration.getDatabaseName())
                        .filterExpressions(getNullSafeStream(relationalFilterConfiguration.getFilterExpressions())
                                .map(filterExpression -> FilterExpression.builder()
                                        .expression(filterExpression.getExpression())
                                        .type(filterExpression.getType())
                                        .build()
                                )
                                .collect(Collectors.toList()))
                        .schemaName(relationalFilterConfiguration.getSchemaName())
                        .build()
                )
                .collect(Collectors.toList());
        return filterConfigurationsForGlue;
    }

    private static List<FormInput> getFormInputFromModel(final @NonNull ResourceModel model) {
        return model.getAssetFormsInput().stream()
                .map(formInput -> FormInput.builder()
                        .content(formInput.getContent())
                        .formName(formInput.getFormName())
                        .typeIdentifier(formInput.getTypeIdentifier())
                        .typeRevision(formInput.getTypeRevision())
                        .build())
                .collect(Collectors.toList());
    }

    private static ScheduleConfiguration getScheduleConfiguration(ResourceModel model) {
        software.amazon.datazone.datasource.ScheduleConfiguration schedule = model.getSchedule();
        if (Objects.isNull(schedule)) {
            return null;
        }
        return ScheduleConfiguration.builder()
                .schedule(schedule.getSchedule())
                .timezone(schedule.getTimezone())
                .build();
    }

    private static <T> Stream<T> getNullSafeStream(Collection<T> collection) {
        return Stream.ofNullable(collection)
                .flatMap(Collection::stream);
    }
}
