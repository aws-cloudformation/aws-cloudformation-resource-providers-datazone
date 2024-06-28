package software.amazon.datazone.userprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.userprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;

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
                .then(progress -> readUserProfile(proxy, proxyClient, progress, dataZoneClientWrapper));
    }

    private ProgressEvent<ResourceModel, CallbackContext> readUserProfile(AmazonWebServicesClientProxy proxy,
                                                                          ProxyClient<DataZoneClient> proxyClient,
                                                                          ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                          DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to update the resource.
        return proxy.initiate("AWS-DataZone-UserProfile::Read", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall((getUserProfileRequest, client) -> dataZoneClientWrapper.readUserProfile(getUserProfileRequest))
                .done(this::validateResponse);
    }

    private ProgressEvent<ResourceModel, CallbackContext> validateResponse(GetUserProfileResponse getUserProfileResponse) {
        String errorMessage = String.format("User profile found for Domain %s and User Identifier %s is DEACTIVATED",
                getUserProfileResponse.domainId(), getUserProfileResponse.id());
        if (UserProfileStatus.DEACTIVATED.equals(getUserProfileResponse.status()) ||
                UserProfileStatus.NOT_ASSIGNED.equals(getUserProfileResponse.status())) {
            logger.error("User profile found for Domain %s and User Identifier %s is in %s state",
                    getUserProfileResponse.domainId(), getUserProfileResponse.id(), getUserProfileResponse.status());
            throw new CfnNotFoundException(new RuntimeException(errorMessage));
        }
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getUserProfileResponse));
    }
}
