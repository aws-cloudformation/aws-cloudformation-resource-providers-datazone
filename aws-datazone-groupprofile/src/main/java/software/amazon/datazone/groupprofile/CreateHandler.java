package software.amazon.datazone.groupprofile;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GroupProfileStatus;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.groupprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.groupprofile.helper.LoggerWrapper;

public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                // Make create call
                .then(progress -> checkForPreExistence(progress, dataZoneClientWrapper))
                // Checking for pre-existence using null ID
                .then(progress -> StringUtils.isNullOrEmpty(progress.getResourceModel().getId()) ? createGroupProfile(proxy,
                        proxyClient, progress, dataZoneClientWrapper) : transitionToActive(proxy, proxyClient, progress, dataZoneClientWrapper))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(),
                        proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> transitionToActive(AmazonWebServicesClientProxy proxy,
                                                                             ProxyClient<DataZoneClient> proxyClient,
                                                                             ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                             DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-GroupProfile::TransitionToActive", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                // make service call
                .makeServiceCall((updateGroupProfileRequest, client) -> dataZoneClientWrapper.updateGroupProfile(updateGroupProfileRequest))
                // and update the model fields.
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkForPreExistence(ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                               DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to get the resource.
        ResourceModel resourceModel = progress.getResourceModel();
        String domainIdentifier = getDomain(resourceModel);
        String groupIdentifier = getGroupId(resourceModel);
        logger.info("Checking for pre-existence of Group Profile for Domain %s and Group Identifier %s",
                domainIdentifier, groupIdentifier);
        GetGroupProfileResponse response;
        try {
            response = dataZoneClientWrapper.readGroupProfile(Translator.translateToReadRequest(resourceModel));
        } catch (ResourceNotFoundException | CfnNotFoundException e) {
            logger.info("Group Profile for Domain %s and Group Identifier %s does not exist, proceeding with " +
                    "creation...", domainIdentifier, groupIdentifier);
            return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
        } catch (Exception e) {
            logger.error("Failed to check for pre-existence for Group Profile for Domain %s and Group Identifier " +
                    "%s, error %s", domainIdentifier, groupIdentifier, e.getMessage());
            throw e;
        }

        GroupProfileStatus currentStatus = response.status();
        logger.info("Group Profile for Domain %s and Group Identifier %s fetched, current status %s",
                domainIdentifier, groupIdentifier, currentStatus.toString());
        if (GroupProfileStatus.NOT_ASSIGNED.equals(currentStatus)) {
            if (GroupProfileStatus.ASSIGNED.toString().equals(resourceModel.getStatus())) {
                logger.info("Group Profile exists in NOT_ASSIGNED state for Domain %s and Group Identifier %s",
                        domainIdentifier, groupIdentifier);
                resourceModel.setId(response.id());
                resourceModel.setDomainId(response.domainId());
                return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
            } else {
                String errorMessage = String.format("Group Profile already exists for Domain %s and Group Identifier %s, " +
                                "current status is NOT_ASSIGNED, if you want to go ahead and assign use status as " +
                                "ASSIGNED",
                        domainIdentifier, groupIdentifier);
                throw new CfnAlreadyExistsException(new RuntimeException(errorMessage));
            }
        } else {
            String errorMessage = String.format("Group Profile already exists for Domain %s and Group Identifier %s",
                    domainIdentifier, groupIdentifier);
            throw new CfnAlreadyExistsException(new RuntimeException(errorMessage));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createGroupProfile(AmazonWebServicesClientProxy proxy,
                                                                             ProxyClient<DataZoneClient> proxyClient,
                                                                             ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                             DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-GroupProfile::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model, getNewClientToken()))
                // make service call
                .makeServiceCall((createGroupProfileRequest, client) -> dataZoneClientWrapper.createGroupProfile(createGroupProfileRequest))
                // and update the model fields.
                .done(this::updateModelFields);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFields(CreateGroupProfileRequest createGroupProfileRequest,
                                                                            CreateGroupProfileResponse createGroupProfileResponse,
                                                                            ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                            ResourceModel resourceModel,
                                                                            CallbackContext callbackContext) {
        logger.info("Successfully created GroupProfile for domain %s and group identifier %s",
                createGroupProfileRequest.domainIdentifier(), createGroupProfileRequest.groupIdentifier());
        resourceModel.setId(createGroupProfileResponse.id());
        resourceModel.setDomainId(createGroupProfileResponse.domainId());
        return ProgressEvent.progress(resourceModel, callbackContext);
    }
}
