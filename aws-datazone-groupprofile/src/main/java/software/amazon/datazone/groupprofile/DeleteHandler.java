package software.amazon.datazone.groupprofile;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.groupprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.groupprofile.helper.LoggerWrapper;

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
                .then(progress -> deleteGroupProfile(progress, dataZoneClientWrapper))
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteGroupProfile(
            ProgressEvent<ResourceModel, CallbackContext> progress, DataZoneClientWrapper dataZoneClientWrapper) {
        ResourceModel resourceModel = progress.getResourceModel();
        String domainIdentifier = getDomain(resourceModel);
        String groupIdentifier = getGroupId(resourceModel);
        logger.info("Checking for existence of Group Profile for Domain %s and Group Identifier %s",
                domainIdentifier, groupIdentifier);
        GetGroupProfileResponse response;
        try {
            response = dataZoneClientWrapper.readGroupProfile(Translator.translateToReadRequest(resourceModel));
        } catch (ResourceNotFoundException | CfnNotFoundException e) {
            logger.info("Group Profile for Domain %s and Group Identifier %s does not exist",
                    domainIdentifier, groupIdentifier);
            return ProgressEvent.success(resourceModel, progress.getCallbackContext());
        } catch (Exception e) {
            logger.error("Failed to check for existence for Group Profile for Domain %s and Group Identifier " +
                    "%s, error %s", domainIdentifier, groupIdentifier, e.getMessage());
            throw e;
        }

        GroupProfileStatus currentStatus = response.status();

        // No action if status is NOT_ASSIGNED
        if (currentStatus.equals(GroupProfileStatus.NOT_ASSIGNED)) {
            String errorMessage = String.format("GroupProfile %s for domain %s has status as NOT_ASSIGNED.",
                    groupIdentifier, domainIdentifier);
            logger.info(errorMessage);
            throw new CfnNotFoundException(new RuntimeException(errorMessage));
        }
        // If status is ASSIGNED, updating to NOT_ASSIGNED
        else if (currentStatus.equals(GroupProfileStatus.ASSIGNED)) {
            logger.info("Transitioning group profile %s for domain %s from ASSIGNED to NOT_ASSIGNED " +
                    "to delete the same.", groupIdentifier, domainIdentifier);
            try {
                dataZoneClientWrapper.updateGroupProfile(
                        Translator.translateToUpdateRequest(getDomain(resourceModel), getGroupId(resourceModel),
                                GroupProfileStatus.NOT_ASSIGNED.toString()));
            } catch (Exception e) {
                logger.error("Failed to check for existence for Group Profile for Domain %s and Group Identifier " +
                        "%s, error %s", domainIdentifier, groupIdentifier, e.getMessage());
                throw e;
            }
        }
        return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
    }
}
