package software.amazon.datazone.groupprofile.helper;

import lombok.Getter;

@Getter
public enum GroupProfileOperation {
    CREATE_GROUP_PROFILE("CreateGroupProfile"),
    GET_GROUP_PROFILE("GetGroupProfile"),
    UPDATE_GROUP_PROFILE("UpdateGroupProfile"),
    SEARCH_GROUP_PROFILES("SearchGroupProfiles");

    private final String name;

    GroupProfileOperation(final String name) {
        this.name = name;
    }

}
