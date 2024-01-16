package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.model.DataSourceSummary;
import software.amazon.awssdk.services.datazone.model.GetDataSourceResponse;
import software.amazon.awssdk.services.datazone.model.RecommendationConfiguration;
import software.amazon.awssdk.services.datazone.model.ScheduleConfiguration;

import java.util.Objects;

public class ResponseTranslator {

    /**
     * Helper function to convert the GetDataSourceResponse (received via the GetDataSource Call) into
     * the resource model.
     *
     * @param getDataSourceResponse The GetDataSourceResponse for the DataSource.
     * @return model Resource model.
     */
    static ResourceModel translateFromReadResponse(final GetDataSourceResponse getDataSourceResponse) {
        final Double lastRunAssetCount = Objects.isNull(getDataSourceResponse.lastRunAssetCount()) ? null : Double.valueOf(getDataSourceResponse.lastRunAssetCount());

        return ResourceModel.builder()
                .createdAt(String.valueOf(getDataSourceResponse.createdAt()))
                .description(getDataSourceResponse.description())
                .domainId(getDataSourceResponse.domainId())
                .enableSetting(getDataSourceResponse.enableSettingAsString())
                .environmentId(getDataSourceResponse.environmentId())
                .id(getDataSourceResponse.id())
                .lastRunAssetCount(lastRunAssetCount)
                .lastRunAt(String.valueOf(getDataSourceResponse.lastRunAt()))
                .lastRunStatus(String.valueOf(getDataSourceResponse.lastRunStatus()))
                .name(getDataSourceResponse.name())
                .projectId(getDataSourceResponse.projectId())
                .publishOnImport(getDataSourceResponse.publishOnImport())
                .recommendation(getRecommendationConfigurationFromResponse(getDataSourceResponse.recommendation()))
                .schedule(getScheduleConfigurationFromResponse(getDataSourceResponse.schedule()))
                .status(getDataSourceResponse.statusAsString())
                .type(getDataSourceResponse.type())
                .updatedAt(String.valueOf(getDataSourceResponse.updatedAt()))
                .build();
    }

    /**
     * Helper function to convert DataSourceSummary to ResourceModel.
     *
     * @param dataSourceSummary DataSourceSummary for the data source.
     * @return model Resource model
     */
    static ResourceModel getResourceModelFromDataSourceSummary(DataSourceSummary dataSourceSummary) {
        Double lastRunAssetCount = Objects.isNull(dataSourceSummary.lastRunAssetCount()) ? null : Double.valueOf(dataSourceSummary.lastRunAssetCount());
        return ResourceModel.builder()
                .id(dataSourceSummary.dataSourceId())
                .domainId(dataSourceSummary.domainId())
                .environmentId(dataSourceSummary.environmentId())
                .name(dataSourceSummary.name())
                .status(dataSourceSummary.statusAsString())
                .type(dataSourceSummary.type())
                .createdAt(dataSourceSummary.createdAt().toString())
                .enableSetting(dataSourceSummary.enableSettingAsString())
                .lastRunAssetCount(lastRunAssetCount)
                .lastRunAt(Objects.isNull(dataSourceSummary.lastRunAt()) ? null : String.valueOf(dataSourceSummary.lastRunAt()))
                .lastRunStatus(String.valueOf(dataSourceSummary.lastRunStatus()))
                .schedule(getScheduleConfigurationFromResponse(dataSourceSummary.schedule()))
                .updatedAt(String.valueOf(dataSourceSummary.updatedAt()))
                .build();
    }

    private static software.amazon.datazone.datasource.ScheduleConfiguration getScheduleConfigurationFromResponse(ScheduleConfiguration schedule) {
        return Objects.isNull(schedule) ? null : software.amazon.datazone.datasource.ScheduleConfiguration.builder()
                .schedule(schedule.schedule())
                .timezone(schedule.timezoneAsString())
                .build();
    }

    private static software.amazon.datazone.datasource.RecommendationConfiguration getRecommendationConfigurationFromResponse(
            RecommendationConfiguration recommendation) {
        return Objects.isNull(recommendation) ? null : software.amazon.datazone.datasource.RecommendationConfiguration.builder()
                .enableBusinessNameGeneration(recommendation.enableBusinessNameGeneration())
                .build();
    }
}
