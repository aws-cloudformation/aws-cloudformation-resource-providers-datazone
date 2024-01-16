package software.amazon.datazone.project;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DeleteProjectResponse;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.project.client.DataZoneClientWrapper;
import software.amazon.datazone.project.helper.LoggerWrapper;

public class DeleteHandler extends BaseHandlerStd {

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
                        proxy.initiate("AWS-DataZone-Project::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall((deleteProjectRequest, client) -> {
                                    DeleteProjectResponse deleteProjectResponse = dataZoneClientWrapper.deleteProject(deleteProjectRequest);
                                    logger.info(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                                    if (!deleteProjectResponse.sdkHttpResponse().isSuccessful()) {
                                        throw new CfnServiceInternalErrorException("Project was not successfully deleted with id" + deleteProjectRequest.identifier());
                                    }
                                    return deleteProjectResponse;
                                })
                                .progress()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
