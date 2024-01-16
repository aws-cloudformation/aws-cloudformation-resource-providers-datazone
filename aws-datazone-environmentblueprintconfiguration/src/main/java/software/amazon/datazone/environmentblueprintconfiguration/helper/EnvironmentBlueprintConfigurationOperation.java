package software.amazon.datazone.environmentblueprintconfiguration.helper;

import lombok.Getter;

@Getter
public enum EnvironmentBlueprintConfigurationOperation {
    PUT_ENVIRONMENT_BLUEPRINT_CONFIGURATION("PutEnvironmentBlueprintConfiguration"),
    GET_ENVIRONMENT_BLUEPRINT_CONFIGURATION("GetEnvironmentBlueprintConfiguration"),
    DELETE_ENVIRONMENT_BLUEPRINT_CONFIGURATION("DeleteEnvironmentBlueprintConfiguration"),
    LIST_ENVIRONMENT_BLUEPRINTS("ListEnvironmentBlueprints"),
    LIST_ENVIRONMENT_BLUEPRINT_CONFIGURATIONS("ListEnvironmentBlueprintConfigurations");

    private final String name;

    EnvironmentBlueprintConfigurationOperation(final String name) {
        this.name = name;
    }

}
