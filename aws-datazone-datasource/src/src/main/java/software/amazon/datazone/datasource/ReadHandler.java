package software.amazon.datazone.datasource;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.datasource.client.DataZoneClientWrapper;
import software.amazon.datazone.datasource.helper.LoggerWrapper;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<DataZoneClient> proxyClient,
        final Logger externalLogger) {

        // Initialize
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return proxy.initiate("AWS-DataZone-DataSource::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getDataSourceRequest, client) -> dataZoneClientWrapper.getDataSource(getDataSourceRequest))
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(ResponseTranslator.translateFromReadResponse(awsResponse)));
    }
}
