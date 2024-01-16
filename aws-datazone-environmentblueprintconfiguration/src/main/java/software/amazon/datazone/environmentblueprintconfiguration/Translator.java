package software.amazon.datazone.environmentblueprintconfiguration;

import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.EnvironmentBlueprintConfigurationItem;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentBlueprintConfigurationResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentBlueprintConfigurationsRequest;
import software.amazon.awssdk.services.datazone.model.PutEnvironmentBlueprintConfigurationRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

    static PutEnvironmentBlueprintConfigurationRequest translateToPutRequest(final @NonNull ResourceModel model) {
        return PutEnvironmentBlueprintConfigurationRequest.builder()
                .environmentBlueprintIdentifier(model.getEnvironmentBlueprintId())
                .domainIdentifier(getDomainId(model))
                .enabledRegions(model.getEnabledRegions())
                .manageAccessRoleArn(model.getManageAccessRoleArn())
                .provisioningRoleArn(model.getProvisioningRoleArn())
                .regionalParameters(getRegionalParametersFromResourceModel(model.getRegionalParameters()))
                .build();
    }

    static GetEnvironmentBlueprintConfigurationRequest translateToReadRequest(final @NonNull ResourceModel model) {
        return GetEnvironmentBlueprintConfigurationRequest.builder()
                .environmentBlueprintIdentifier(model.getEnvironmentBlueprintId())
                .domainIdentifier(getDomainId(model))
                .build();
    }

    static ResourceModel translateFromReadResponse(final @NonNull GetEnvironmentBlueprintConfigurationResponse response) {
        return ResourceModel.builder()
                .environmentBlueprintId(response.environmentBlueprintId())
                .domainIdentifier(response.domainId())
                .domainId(response.domainId())
                .createdAt(String.valueOf(response.createdAt()))
                .enabledRegions(response.enabledRegions())
                .manageAccessRoleArn(response.manageAccessRoleArn())
                .managed(Boolean.TRUE) // Currently only Managed Blueprints are supported
                .provisioningRoleArn(response.provisioningRoleArn())
                .regionalParameters(getRegionalParametersForResourceModel(response.regionalParameters()))
                .updatedAt(String.valueOf(response.updatedAt()))
                .build();
    }

    static DeleteEnvironmentBlueprintConfigurationRequest translateToDeleteRequest(final @NonNull ResourceModel model) {
        return DeleteEnvironmentBlueprintConfigurationRequest.builder()
                .domainIdentifier(getDomainId(model))
                .environmentBlueprintIdentifier(model.getEnvironmentBlueprintId())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListEnvironmentBlueprintConfigurationsRequest translateToListRequest(
            final @NonNull ResourceModel model,
            final String nextToken) {
        return ListEnvironmentBlueprintConfigurationsRequest.builder()
                .domainIdentifier(getDomainId(model))
                .nextToken(nextToken)
                .build();
    }

    public static ResourceModel getResourceModelFromItem(final @NonNull EnvironmentBlueprintConfigurationItem environmentBlueprintConfigurationItem) {
        return ResourceModel.builder()
                .environmentBlueprintId(environmentBlueprintConfigurationItem.environmentBlueprintId())
                .domainIdentifier(environmentBlueprintConfigurationItem.domainId())
                .domainId(environmentBlueprintConfigurationItem.domainId())
                .createdAt(String.valueOf(environmentBlueprintConfigurationItem.createdAt()))
                .enabledRegions(environmentBlueprintConfigurationItem.enabledRegions())
                .manageAccessRoleArn(environmentBlueprintConfigurationItem.manageAccessRoleArn())
                .provisioningRoleArn(environmentBlueprintConfigurationItem.provisioningRoleArn())
                .regionalParameters(getRegionalParametersForResourceModel(environmentBlueprintConfigurationItem.regionalParameters()))
                .updatedAt(String.valueOf(environmentBlueprintConfigurationItem.updatedAt()))
                .build();
    }

    public static String getDomainId(final @NonNull ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    public static Map<String, Map<String, String>> getRegionalParametersFromResourceModel(Set<RegionalParameter> regionalParameters) {
        Map<String, Map<String, String>> parameters = new HashMap<>();
        for (RegionalParameter parameter : regionalParameters) {
            String region = parameter.getRegion();
            if (parameters.containsKey(region)) {
                throw new CfnInvalidRequestException(new Exception(String.format("Duplicate configuration defined for region %s", region)));
            }
            parameters.put(region, parameter.getParameters());
        }

        return parameters;
    }

    public static Set<RegionalParameter> getRegionalParametersForResourceModel(Map<String, Map<String, String>> parametersMap) {
        return parametersMap.entrySet().stream()
                .map(stringMapEntry -> RegionalParameter.builder()
                        .region(stringMapEntry.getKey())
                        .parameters(stringMapEntry.getValue())
                        .build()
                )
                .collect(Collectors.toSet());
    }
}
