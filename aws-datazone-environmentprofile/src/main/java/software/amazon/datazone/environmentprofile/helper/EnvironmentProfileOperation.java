package software.amazon.datazone.environmentprofile.helper;

import lombok.Getter;

@Getter
public enum EnvironmentProfileOperation {
    CREATE_ENVIRONMENT_PROFILE("CreateEnvironmentProfile"),
    GET_ENVIRONMENT_PROFILE("GetEnvironmentProfile"),
    UPDATE_ENVIRONMENT_PROFILE("UpdateEnvironmentProfile"),
    DELETE_ENVIRONMENT_PROFILE("DeleteEnvironmentProfile"),
    LIST_ENVIRONMENT_PROFILES("ListEnvironmentProfiles");

    private final String name;

    EnvironmentProfileOperation(final String name) {
        this.name = name;
    }

}
