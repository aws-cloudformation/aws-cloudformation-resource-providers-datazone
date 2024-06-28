package software.amazon.datazone.userprofile.helper;

import lombok.Getter;

@Getter
public enum DomainOperation {
    GET_DOMAIN("GetDomain");

    private final String name;

    DomainOperation(final String name) {
        this.name = name;
    }
}
