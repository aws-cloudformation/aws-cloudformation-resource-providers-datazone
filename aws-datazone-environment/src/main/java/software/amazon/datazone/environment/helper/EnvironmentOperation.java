package software.amazon.datazone.environment.helper;

import lombok.Getter;

@Getter
public enum EnvironmentOperation {
    CREATE_ENVIRONMENT("CreateEnvironment"),
    GET_ENVIRONMENT("GetEnvironment"),
    UPDATE_ENVIRONMENT("UpdateEnvironment"),
    DELETE_ENVIRONMENT("DeleteEnvironment"),
    LIST_ENVIRONMENTS("ListEnvironments");

    private final String name;

    EnvironmentOperation(final String name) {
        this.name = name;
    }

}
