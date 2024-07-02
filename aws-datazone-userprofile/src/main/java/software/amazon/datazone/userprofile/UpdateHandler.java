package software.amazon.datazone.userprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.userprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        validateRequest(request.getDesiredResourceState());

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateUserProfile(proxy, proxyClient, progress, dataZoneClientWrapper))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserProfile(AmazonWebServicesClientProxy proxy,
                                                                            ProxyClient<DataZoneClient> proxyClient,
                                                                            ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                            DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to update the resource.
        return proxy.initiate("AWS-DataZone-UserProfile::Update", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Update Request
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                // make service call
                .makeServiceCall((updateUserProfileRequest, client) -> dataZoneClientWrapper.updateUserProfile(updateUserProfileRequest))
                .progress();
    }

    private void validateRequest(ResourceModel resourceModel) {
        if (UserProfileStatus.DEACTIVATED.toString().equals(resourceModel.getStatus()) ||
                UserProfileStatus.NOT_ASSIGNED.toString().equals(resourceModel.getStatus())) {
            String errorMessage = String.format("Cannot update User Profile with status %s for Domain %s and" +
                            " User Identifier %s, instead delete the user profile.",
                    resourceModel.getStatus(), resourceModel.getDomainId(), resourceModel.getId());
            throw new CfnGeneralServiceException(new RuntimeException(errorMessage));
        }
    }
}
