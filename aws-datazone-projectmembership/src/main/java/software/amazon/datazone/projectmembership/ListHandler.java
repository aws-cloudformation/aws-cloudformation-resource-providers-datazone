package software.amazon.datazone.projectmembership;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsResponse;
import software.amazon.awssdk.services.datazone.model.MemberDetails;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static software.amazon.datazone.projectmembership.Translator.streamOfOrEmpty;
import static software.amazon.datazone.projectmembership.helper.Constants.GROUP_IDENTIFIER;
import static software.amazon.datazone.projectmembership.helper.Constants.USER_IDENTIFIER;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(AmazonWebServicesClientProxy proxy,
                                                                          ResourceHandlerRequest<ResourceModel> request,
                                                                          CallbackContext callbackContext,
                                                                          ProxyClient<DataZoneClient> proxyClient,
                                                                          Logger externalLogger) {
        // Initialise
        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        final ResourceModel resourceModel = request.getDesiredResourceState();
        final ListProjectMembershipsRequest listRequest = Translator.translateToListRequest(resourceModel, request.getNextToken());
        logger.info("Received request for list for DomainId %s and MemberIdentifier %s",
                request.getDesiredResourceState().getDomainIdentifier(), request.getDesiredResourceState().getMember());

        final ListProjectMembershipsResponse response = dataZoneClientWrapper.listProjectMemberships(listRequest);
        final String nextToken = Objects.isNull(response) ? null : response.nextToken();

        final List<ResourceModel> models = streamOfOrEmpty(response.members())
                .map(projectMember -> ResourceModel.builder()
                        .domainIdentifier(resourceModel.getDomainIdentifier())
                        .projectIdentifier(resourceModel.getProjectIdentifier())
                        .memberIdentifier(resourceModel.getMemberIdentifier())
                        .designation(projectMember.designationAsString())
                        .memberIdentifier(getMemberIdentifier(projectMember.memberDetails()))
                        .memberIdentifierType(getMemberIdentifierType(projectMember.memberDetails()))
                        .build()
                )
                .collect(Collectors.toList());

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(nextToken)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private String getMemberIdentifierType(MemberDetails memberDetails) {
        if (!Objects.isNull(memberDetails.user()) && !StringUtils.isNullOrEmpty(memberDetails.user().userId())) {
            return USER_IDENTIFIER;
        }

        if (!Objects.isNull(memberDetails.group()) && !StringUtils.isNullOrEmpty(memberDetails.group().groupId())) {
            return GROUP_IDENTIFIER;
        }

        throw new RuntimeException(String.format("Invalid memberDetails %s", memberDetails));
    }

    private String getMemberIdentifier(MemberDetails memberDetails) {
        if (!Objects.isNull(memberDetails.user()) && !StringUtils.isNullOrEmpty(memberDetails.user().userId())) {
            return memberDetails.user().userId();
        }

        if (!Objects.isNull(memberDetails.group()) && !StringUtils.isNullOrEmpty(memberDetails.group().groupId())) {
            return memberDetails.group().groupId();
        }

        throw new RuntimeException(String.format("Invalid memberDetails %s", memberDetails));
    }
}
