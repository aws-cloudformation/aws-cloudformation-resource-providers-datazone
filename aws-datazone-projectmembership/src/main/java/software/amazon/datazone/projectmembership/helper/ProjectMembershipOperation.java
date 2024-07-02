package software.amazon.datazone.projectmembership.helper;

import lombok.Getter;

@Getter
public enum ProjectMembershipOperation {
    CREATE_PROJECT_MEMBERSHIP("CreateProjectMembership"),
    DELETE_PROJECT_MEMBERSHIP("DeleteProjectMembership"),
    GET_GROUP_PROFILE("GetGroupProfile"),
    GET_USER_PROFILE("GetUserProfile"),
    LIST_PROJECT_MEMBERSHIPS("ListProjectMembership");

    private final String name;

    ProjectMembershipOperation(final String name) {
        this.name = name;
    }

}
