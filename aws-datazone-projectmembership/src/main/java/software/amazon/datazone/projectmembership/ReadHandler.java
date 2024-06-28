package software.amazon.datazone.projectmembership;

import com.amazonaws.util.CollectionUtils;
import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsResponse;
import software.amazon.awssdk.services.datazone.model.ProjectMember;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

import java.util.Objects;

import static software.amazon.datazone.projectmembership.helper.Constants.GROUP_IDENTIFIER;
import static software.amazon.datazone.projectmembership.helper.Constants.USER_IDENTIFIER;

public class ReadHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            Logger externalLogger) {

        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        logger.info("Received request for read for DomainId %s and MemberIdentifier %s ",
                request.getDesiredResourceState().getDomainIdentifier(), request.getDesiredResourceState().getMemberIdentifier());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> getProjectMembership(proxy, proxyClient, progress, dataZoneClientWrapper));
    }

    private ProgressEvent<ResourceModel, CallbackContext> getProjectMembership(AmazonWebServicesClientProxy proxy,
                                                                               ProxyClient<DataZoneClient> proxyClient,
                                                                               ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                               DataZoneClientWrapper dataZoneClientWrapper) {
        ResourceModel resourceModel = progress.getResourceModel();
        String memberIdentifier = resourceModel.getMemberIdentifier();
        String domainIdentifier = resourceModel.getDomainIdentifier();
        String projectIdentifier = resourceModel.getProjectIdentifier();

        String nextToken = null;

        while (true) {
            ListProjectMembershipsRequest listProjectMembershipsRequest = Translator.translateToListRequest(resourceModel, nextToken);

            ListProjectMembershipsResponse response = dataZoneClientWrapper.listProjectMemberships(listProjectMembershipsRequest);
            if (CollectionUtils.isNullOrEmpty(response.members())) {
                throw new CfnNotFoundException(new Exception("Project does not have any members."));
            }

            for (ProjectMember projectMember : response.members()) {
                if (!Objects.isNull(projectMember.memberDetails().user()) && memberIdentifier.equals(projectMember.memberDetails().user().userId())) {
                    logger.info("Found membership details for user member %s, in project %s, domain %s," +
                            " current designation %s", memberIdentifier, projectIdentifier, domainIdentifier, projectMember.designation());
                    return ProgressEvent.defaultSuccessHandler(getResourceModelForUser(resourceModel, projectMember));
                }

                if (!Objects.isNull(projectMember.memberDetails().group()) && memberIdentifier.equals(projectMember.memberDetails().group().groupId())) {
                    logger.info("Found membership details for group member %s, in project %s, domain %s," +
                            " current designation %s", memberIdentifier, projectIdentifier, domainIdentifier, projectMember.designation());
                    return ProgressEvent.defaultSuccessHandler(getResourceModelForGroup(resourceModel, projectMember));
                }
            }

            if (StringUtils.isNullOrEmpty(response.nextToken())) {
                break;
            } else {
                nextToken = response.nextToken();
            }
        }

        logger.info("Failed to find member %s, in project %s, domain %s", memberIdentifier, projectIdentifier,
                domainIdentifier);
        throw new CfnNotFoundException(new Exception(String.format("Failed to find member %s", memberIdentifier)));
    }

    private ResourceModel getResourceModelForGroup(ResourceModel resourceModel, ProjectMember projectMember) {
        // Update fields that can be updated
        resourceModel.setMemberIdentifierType(GROUP_IDENTIFIER);
        resourceModel.setDesignation(projectMember.designationAsString());
        return resourceModel;
    }

    private ResourceModel getResourceModelForUser(ResourceModel resourceModel, ProjectMember projectMember) {
        // Update fields that can be updated
        resourceModel.setMemberIdentifierType(USER_IDENTIFIER);
        resourceModel.setDesignation(projectMember.designationAsString());
        return resourceModel;
    }
}
