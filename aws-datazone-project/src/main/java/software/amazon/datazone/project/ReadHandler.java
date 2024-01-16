package software.amazon.datazone.project;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetProjectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.project.client.DataZoneClientWrapper;
import software.amazon.datazone.project.helper.LoggerWrapper;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient);

        return proxy.initiate("AWS-DataZone-Project::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getProjectRequest, client) -> {
                    GetProjectResponse getProjectResponse = this.dataZoneClientWrapper.getProject(getProjectRequest);
                    logger.info(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                    return getProjectResponse;
                })
                .done(getProjectResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getProjectResponse)));
    }
}
