package software.amazon.datazone.datasource.client;

import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.cloudformation.LambdaWrapper;
import software.amazon.awssdk.services.datazone.DataZoneClient;

public class DataZoneClientBuilder {
    public static DataZoneClient getClient() {
        return DataZoneClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}
