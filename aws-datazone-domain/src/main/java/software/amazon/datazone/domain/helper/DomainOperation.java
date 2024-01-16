package software.amazon.datazone.domain.helper;

import lombok.Getter;

@Getter
public enum DomainOperation {
    CREATE_DOMAIN("CreateDomain"),
    GET_DOMAIN("GetDomain"),
    UPDATE_DOMAIN("UpdateDomain"),
    DELETE_DOMAIN("DeleteDomain"),
    LIST_DOMAINS("ListDomains"),
    ADD_TAGS("AddTags"),
    DELETE_TAGS("DeleteTags"),
    ;

    private final String name;

    DomainOperation(final String name) {
        this.name = name;
    }

}
