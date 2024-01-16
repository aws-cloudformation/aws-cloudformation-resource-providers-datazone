package software.amazon.datazone.datasource.helper;

import lombok.Getter;

@Getter
public enum DataSourceOperation {
    CREATE_DATASOURCE("CreateDataSource"),
    GET_DATASOURCE("GetDataSource"),
    UPDATE_DATASOURCE("UpdateDataSource"),
    DELETE_DATASOURCE("DeleteDataSource"),
    LIST_DATASOURCE("ListDataSource");

    private final String name;

    DataSourceOperation(final String name) {
        this.name = name;
    }

}
