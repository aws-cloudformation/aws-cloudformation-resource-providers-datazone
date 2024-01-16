package software.amazon.datazone.environmentprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentprofile.helper.LoggerWrapper;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> deleteEnvironmentProfile(proxy, proxyClient, progress))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteEnvironmentProfile(AmazonWebServicesClientProxy proxy,
                                                                                   ProxyClient<DataZoneClient> proxyClient,
                                                                                   ProgressEvent<ResourceModel, CallbackContext> progress) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return proxy.initiate("AWS-DataZone-EnvironmentProfile::Delete", proxyClient, resourceModel, callbackContext)
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((deleteEnvironmentProfileRequest, client) -> {
                        DeleteEnvironmentProfileResponse deleteEnvironmentProfileResponse = this.dataZoneClientWrapper.deleteEnvironmentProfile(deleteEnvironmentProfileRequest);
                        logger.info(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                        if (!deleteEnvironmentProfileResponse.sdkHttpResponse().isSuccessful()) {
                            String errorMessage = String.format("EnvironmentProfile with Id: %s and domain Id: %s",
                                    deleteEnvironmentProfileRequest.identifier(), deleteEnvironmentProfileRequest.domainIdentifier());
                            throw new CfnServiceInternalErrorException(errorMessage);
                        }
                        return deleteEnvironmentProfileResponse;
                    })
                    .progress();
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("EnvironmentProfile with id %s and domain id %s does not exist, skipping deletion...", resourceModel.getId(), resourceModel.getDomainId());
            throw new CfnNotFoundException(exception);
        }
    }
}
