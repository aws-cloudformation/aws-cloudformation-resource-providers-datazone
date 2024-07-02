package software.amazon.datazone.projectmembership;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateProjectMembershipRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientBuilder;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

import static software.amazon.datazone.projectmembership.helper.Constants.GROUP_IDENTIFIER;
import static software.amazon.datazone.projectmembership.helper.Constants.USER_IDENTIFIER;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected LoggerWrapper logger;

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(DataZoneClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger logger);

    protected ProgressEvent<ResourceModel, CallbackContext> deleteProjectMembership(AmazonWebServicesClientProxy proxy,
                                                                                    ProxyClient<DataZoneClient> proxyClient,
                                                                                    ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                    DataZoneClientWrapper dataZoneClientWrapper) {
        ResourceModel resourceModel = progress.getResourceModel();
        CallbackContext callbackContext = progress.getCallbackContext();
        try {
            return proxy.initiate("AWS-DataZone-ProjectMembership::Delete", proxyClient, resourceModel, callbackContext)
                    .translateToServiceRequest(model -> Translator.translateToDeleteRequest(model))
                    .makeServiceCall((deleteProjectMembershipRequest, client) -> dataZoneClientWrapper.deleteProjectMembership(deleteProjectMembershipRequest))
                    .progress();
        } catch (ResourceNotFoundException | CfnNotFoundException exception) {
            logger.info("ProjectMembership with member %s in project %s, domain %s does not exist",
                    resourceModel.getMemberIdentifier(), resourceModel.getProjectIdentifier(), resourceModel.getDomainIdentifier());
            throw new CfnNotFoundException(exception);
        }
    }

    protected ProgressEvent<ResourceModel, CallbackContext> updateModelFields(CreateProjectMembershipRequest createProjectMembershipRequest,
                                                                              ResourceModel resourceModel,
                                                                              DataZoneClientWrapper dataZoneClientWrapper) {
        logger.info("Successfully created ProjectMembership in project %s domain %s member %s", createProjectMembershipRequest.projectIdentifier(),
                createProjectMembershipRequest.domainIdentifier(), createProjectMembershipRequest.member());
        String memberIdentifier = getMemberIdentifier(resourceModel, dataZoneClientWrapper);
        String memberIdentifierType = getMemberIdentifierType(resourceModel);

        resourceModel.setMemberIdentifier(memberIdentifier);
        resourceModel.setMemberIdentifierType(memberIdentifierType);

        return ProgressEvent.defaultSuccessHandler(resourceModel);
    }

    private String getMemberIdentifier(ResourceModel resourceModel, DataZoneClientWrapper dataZoneClientWrapper) {
        String userIdentifier = resourceModel.getMember().getUserIdentifier();
        String groupIdentifier = resourceModel.getMember().getGroupIdentifier();

        if (!StringUtils.isNullOrEmpty(userIdentifier)) {
            logger.info("Fetching user profile for %s", userIdentifier);
            GetUserProfileResponse getUserProfileResponse = dataZoneClientWrapper.getUserProfile(GetUserProfileRequest.builder()
                    .domainIdentifier(resourceModel.getDomainIdentifier())
                    .userIdentifier(userIdentifier)
                    .build());
            logger.info("Received id %s for user %s, type %s", getUserProfileResponse.id(), userIdentifier,
                    getUserProfileResponse.typeAsString());
            return getUserProfileResponse.id();
        } else {
            logger.info("Fetching group profile for %s", groupIdentifier);
            GetGroupProfileResponse getGroupProfileResponse = dataZoneClientWrapper.getGroupProfile(GetGroupProfileRequest.builder()
                    .domainIdentifier(resourceModel.getDomainIdentifier())
                    .groupIdentifier(groupIdentifier)
                    .build());
            logger.info("Received id %s for group %s, name %s", getGroupProfileResponse.id(), groupIdentifier,
                    getGroupProfileResponse.groupName());
            return getGroupProfileResponse.id();
        }
    }

    private String getMemberIdentifierType(ResourceModel resourceModel) {
        String userIdentifier = resourceModel.getMember().getUserIdentifier();
        if (!StringUtils.isNullOrEmpty(userIdentifier)) {
            return USER_IDENTIFIER;
        } else {
            return GROUP_IDENTIFIER;
        }
    }
}
