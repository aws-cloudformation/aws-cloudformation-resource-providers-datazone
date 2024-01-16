package software.amazon.datazone.project.client;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.LambdaWrapper;

public class DataZoneClientBuilder {

    public static DataZoneClient getClient() {
        return DataZoneClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
