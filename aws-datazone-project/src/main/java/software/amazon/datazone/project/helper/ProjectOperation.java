package software.amazon.datazone.project.helper;

public enum ProjectOperation {
    CREATE_PROJECT("CreateProject"),
    GET_PROJECT("GetProject"),
    UPDATE_PROJECT("UpdateProject"),
    DELETE_PROJECT("DeleteProject"),
    LIST_PROJECT("ListProjects");
    private final String name;

    ProjectOperation(final String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
