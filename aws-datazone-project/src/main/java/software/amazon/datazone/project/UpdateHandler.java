package software.amazon.datazone.project;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.UpdateProjectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.project.client.DataZoneClientWrapper;
import software.amazon.datazone.project.helper.LoggerWrapper;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)

                .then(progress ->
                        proxy.initiate("AWS-DataZone-Project::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToFirstUpdateRequest)
                                .makeServiceCall((updateProjectRequest, client) -> {
                                    UpdateProjectResponse updateProjectResponse = dataZoneClientWrapper.updateProject(updateProjectRequest);

                                    logger.info(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
                                    return updateProjectResponse;
                                })
                                .progress()
                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, externalLogger));
    }
}
