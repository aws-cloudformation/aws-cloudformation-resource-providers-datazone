package software.amazon.datazone.groupprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.groupprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.groupprofile.helper.LoggerWrapper;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> readGroupProfile(proxy, proxyClient, progress, dataZoneClientWrapper));
    }

    private ProgressEvent<ResourceModel, CallbackContext> readGroupProfile(AmazonWebServicesClientProxy proxy,
                                                                           ProxyClient<DataZoneClient> proxyClient,
                                                                           ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                           DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to update the resource.
        return proxy.initiate("AWS-DataZone-GroupProfile::Read", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getGroupProfileRequest, client) -> dataZoneClientWrapper.readGroupProfile(getGroupProfileRequest))
                .done(this::validateResponse);
    }

    private ProgressEvent<ResourceModel, CallbackContext> validateResponse(GetGroupProfileResponse getGroupProfileResponse) {
        if (GroupProfileStatus.NOT_ASSIGNED.equals(getGroupProfileResponse.status())) {
            String errorMessage = String.format("Group profile found for Domain %s and Group Identifier %s is NOT_ASSIGNED",
                    getGroupProfileResponse.domainId(), getGroupProfileResponse.id());
            logger.error(errorMessage);
            throw new CfnNotFoundException(new RuntimeException(errorMessage));
        }
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getGroupProfileResponse));
    }
}
