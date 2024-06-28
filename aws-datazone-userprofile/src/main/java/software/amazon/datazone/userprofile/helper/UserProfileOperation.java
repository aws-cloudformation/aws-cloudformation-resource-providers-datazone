package software.amazon.datazone.userprofile.helper;

import lombok.Getter;

@Getter
public enum UserProfileOperation {
    CREATE_USER_PROFILE("CreateUserProfile"),
    GET_USER_PROFILE("GetUserProfile"),
    UPDATE_USER_PROFILE("UpdateUserProfile"),
    SEARCH_USER_PROFILES("SearchUserProfiles");

    private final String name;

    UserProfileOperation(final String name) {
        this.name = name;
    }

}
