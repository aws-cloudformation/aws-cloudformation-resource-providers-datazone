package software.amazon.datazone.userprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.userprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> deleteUserProfile(progress, dataZoneClientWrapper))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteUserProfile(
            ProgressEvent<ResourceModel, CallbackContext> progress,
            DataZoneClientWrapper dataZoneClientWrapper
    ) {
        ResourceModel resourceModel = progress.getResourceModel();
        String domainIdentifier = getDomain(resourceModel);
        String userIdentifier = getUserId(resourceModel);
        logger.info("Checking for existence of User Profile for Domain %s and User Identifier %s",
                domainIdentifier, userIdentifier);
        GetUserProfileResponse response;
        try {
            response = dataZoneClientWrapper.readUserProfile(Translator.translateToReadRequest(resourceModel));
        } catch (ResourceNotFoundException | CfnNotFoundException e) {
            logger.info("User Profile for Domain %s and User Identifier %s does not exist",
                    domainIdentifier, userIdentifier);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to check for existence for User Profile for Domain %s and User Identifier " +
                    "%s, error %s", domainIdentifier, userIdentifier, e.getMessage());
            throw e;
        }

        UserProfileStatus currentStatus = response.status();

        // No action if status is DEACTIVATED or NOT_ASSIGNED
        if (currentStatus.equals(UserProfileStatus.DEACTIVATED) || currentStatus.equals(UserProfileStatus.NOT_ASSIGNED)) {
            String errorMessage = String.format("UserProfile %s for domain %s has status as %s.",
                    userIdentifier, domainIdentifier, currentStatus);
            logger.info(errorMessage);
            throw new CfnNotFoundException(new RuntimeException(errorMessage));
        }

        // If status is ACTIVATED/ASSIGNED, updating to DEACTIVATED/NOT_ASSIGNED
        if (currentStatus.equals(UserProfileStatus.ACTIVATED) || currentStatus.equals(UserProfileStatus.ASSIGNED)) {
            logger.info("Transitioning user profile %s for domain %s from %s to NOT_ASSIGNED/DEACTIVATED " +
                    "to delete the same.", userIdentifier, domainIdentifier, currentStatus);
            try {
                dataZoneClientWrapper.updateUserProfile(
                        Translator.translateToUpdateRequest(getDomain(resourceModel), getUserId(resourceModel),
                                currentStatus.equals(UserProfileStatus.ACTIVATED) ?
                                        UserProfileStatus.DEACTIVATED.toString() :
                                        UserProfileStatus.NOT_ASSIGNED.toString()));
            } catch (Exception e) {
                logger.error("Failed to check for existence for User Profile for Domain %s and User Identifier " +
                        "%s, error %s", domainIdentifier, userIdentifier, e.getMessage());
                throw e;
            }
        }
        return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
    }
}
