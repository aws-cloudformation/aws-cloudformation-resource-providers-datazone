package software.amazon.datazone.environmentprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentProfileResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environmentprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.environmentprofile.helper.LoggerWrapper;


public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)

                .then(progress ->
                        proxy.initiate("AWS-DataZone-EnvironmentProfile::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall((createEnvironmentProfileRequest, client) -> {
                                    CreateEnvironmentProfileResponse createEnvironmentProfileResponse = this.dataZoneClientWrapper.createEnvironmentProfile(createEnvironmentProfileRequest);
                                    logger.info(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
                                    request.getDesiredResourceState().setId(createEnvironmentProfileResponse.id());
                                    return createEnvironmentProfileResponse;
                                })
                                .progress()
                )

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, externalLogger));
    }
}
