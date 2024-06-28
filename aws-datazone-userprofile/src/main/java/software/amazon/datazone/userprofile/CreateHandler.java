package software.amazon.datazone.userprofile;

import com.amazonaws.util.StringUtils;
import com.google.common.base.Objects;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.UserAssignment;
import software.amazon.awssdk.services.datazone.model.UserProfileStatus;
import software.amazon.awssdk.services.datazone.model.UserType;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.userprofile.client.DataZoneClientWrapper;
import software.amazon.datazone.userprofile.helper.LoggerWrapper;

import java.util.Map;
import java.util.Set;

public class CreateHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        validateRequest(request.getDesiredResourceState(), dataZoneClientWrapper);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                // Make create call
                .then(progress -> checkForPreExistence(progress, dataZoneClientWrapper))
                // Checking for pre-existence using null ID
                .then(progress -> StringUtils.isNullOrEmpty(progress.getResourceModel().getId()) ? createUserProfile(proxy, proxyClient, progress, dataZoneClientWrapper) :
                        transitionToActive(proxy, proxyClient, progress, dataZoneClientWrapper))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(),
                        proxyClient, externalLogger));
    }

    private void validateRequest(ResourceModel resourceModel, DataZoneClientWrapper dataZoneClientWrapper) {
        GetDomainResponse domainResponse = dataZoneClientWrapper.getDomain(Translator.translateToGetDomainRequest(resourceModel));
        if (Objects.equal(domainResponse.singleSignOn().userAssignment(), UserAssignment.AUTOMATIC) && resourceModel.getUserType().equals(UserType.SSO_USER.toString())) {
            String errorMessage = String.format("Cannot create User Profile for SSO Users in Domains with implicit User assignment.",
                    resourceModel.getDomainId(), resourceModel.getId());
            throw new CfnInvalidRequestException(new RuntimeException(errorMessage));
        }
        if (UserProfileStatus.DEACTIVATED.toString().equals(resourceModel.getStatus())) {
            String errorMessage = String.format("Cannot create User Profile with status DEACTIVATED for Domain %s and" +
                    " User Identifier %s", resourceModel.getDomainId(), resourceModel.getId());
            throw new CfnInvalidRequestException(new RuntimeException(errorMessage));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> transitionToActive(AmazonWebServicesClientProxy proxy,
                                                                             ProxyClient<DataZoneClient> proxyClient,
                                                                             ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                             DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-UserProfile::TransitionToActive", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(Translator::translateToUpdateRequest)
                // make service call
                .makeServiceCall((updateUserProfileRequest, client) -> dataZoneClientWrapper.updateUserProfile(updateUserProfileRequest))
                // and update the model fields.
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkForPreExistence(ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                               DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to get the resource.
        ResourceModel resourceModel = progress.getResourceModel();
        String domainIdentifier = resourceModel.getDomainIdentifier();
        String userIdentifier = resourceModel.getUserIdentifier();
        logger.info("Checking for pre-existence of User Profile for Domain %s and User Identifier %s",
                domainIdentifier, userIdentifier);
        GetUserProfileResponse response;
        try {
            response = dataZoneClientWrapper.readUserProfile(Translator.translateToReadRequest(resourceModel));
        } catch (ResourceNotFoundException | CfnNotFoundException e) {
            logger.info("User Profile for Domain %s and User Identifier %s does not exist, proceeding with " +
                    "creation...", domainIdentifier, userIdentifier);
            return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
        } catch (Exception e) {
            logger.error("Failed to check for pre-existence for User Profile for Domain %s and User Identifier " +
                    "%s, error %s", domainIdentifier, userIdentifier, e.getMessage());
            throw e;
        }

        UserProfileStatus currentStatus = response.status();
        logger.info("User Profile for Domain %s and User Identifier %s fetched, current status %s",
                domainIdentifier, userIdentifier, currentStatus.toString());

        Map<UserProfileStatus, UserProfileStatus> validTransitions = Map.of(
                UserProfileStatus.DEACTIVATED, UserProfileStatus.ACTIVATED,
                UserProfileStatus.NOT_ASSIGNED, UserProfileStatus.ASSIGNED);
        Set<UserProfileStatus> terminalUserProfileStatus = Set.of(UserProfileStatus.DEACTIVATED,
                UserProfileStatus.NOT_ASSIGNED);

        if (terminalUserProfileStatus.contains(currentStatus)) {
            if (validTransitions.get(currentStatus).toString().equals(resourceModel.getStatus())) {
                resourceModel.setId(response.id());
                resourceModel.setDomainId(response.domainId());
                return ProgressEvent.progress(resourceModel, progress.getCallbackContext());
            } else {
                String errorMessage = String.format("User Profile already exists for Domain %s and User Identifier %s, " +
                                "current status is %s, if you want to go ahead and update status, use status as " +
                                "%s and retry creation.",
                        domainIdentifier, userIdentifier, currentStatus, validTransitions.get(currentStatus));
                throw new CfnAlreadyExistsException(new RuntimeException(errorMessage));
            }
        } else {
            String errorMessage = String.format("User Profile already exists for Domain %s and User Identifier %s",
                    domainIdentifier, userIdentifier);
            throw new CfnAlreadyExistsException(new RuntimeException(errorMessage));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createUserProfile(AmazonWebServicesClientProxy proxy,
                                                                            ProxyClient<DataZoneClient> proxyClient,
                                                                            ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                            DataZoneClientWrapper dataZoneClientWrapper) {
        // Call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-UserProfile::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model, getNewClientToken()))
                // make service call
                .makeServiceCall((createUserProfileRequest, client) -> dataZoneClientWrapper.createUserProfile(createUserProfileRequest))
                // and update the model fields.
                .done(this::updateModelFields);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFields(CreateUserProfileRequest createUserProfileRequest,
                                                                            CreateUserProfileResponse createUserProfileResponse,
                                                                            ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                            ResourceModel resourceModel,
                                                                            CallbackContext callbackContext) {
        logger.info("Successfully created UserProfile for domain %s and user identifier %s",
                createUserProfileRequest.domainIdentifier(), createUserProfileRequest.userIdentifier());
        resourceModel.setId(createUserProfileResponse.id());
        resourceModel.setDomainId(createUserProfileResponse.domainId());
        return ProgressEvent.progress(resourceModel, callbackContext);
    }
}
