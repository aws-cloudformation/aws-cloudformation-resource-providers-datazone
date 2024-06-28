package software.amazon.datazone.projectmembership;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.projectmembership.client.DataZoneClientWrapper;
import software.amazon.datazone.projectmembership.helper.LoggerWrapper;

import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        this.logger = new LoggerWrapper(externalLogger);
        final DataZoneClientWrapper dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);

        this.validateInputs(request.getDesiredResourceState());
        logger.info("Received request for create for DomainId %s and MemberIdentifier %s ",
                request.getDesiredResourceState().getDomainIdentifier(), request.getDesiredResourceState().getMember());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                // Make create call
                .then(progress -> createProjectMembership(proxy, proxyClient, progress, dataZoneClientWrapper));
    }

    private void validateInputs(ResourceModel resourceModel) {
        if (Objects.isNull(resourceModel.getMember())) {
            logger.error("Received invalid request %s", resourceModel);
            throw new CfnInvalidRequestException(new Exception("Member is required for creating Project Membership."));
        }

        String userIdentifier = resourceModel.getMember().getUserIdentifier();
        String groupIdentifier = resourceModel.getMember().getGroupIdentifier();

        if (StringUtils.isNullOrEmpty(userIdentifier) && StringUtils.isNullOrEmpty(groupIdentifier)) {
            logger.error("Received invalid request %s", resourceModel);
            throw new CfnInvalidRequestException(new Exception("Either userIdentifier or groupIdentifier is required for creating Project Membership."));
        }

        if (!StringUtils.isNullOrEmpty(userIdentifier) && !StringUtils.isNullOrEmpty(groupIdentifier)) {
            logger.error("Received invalid request %s", resourceModel);
            throw new CfnInvalidRequestException(new Exception("Both userIdentifier and groupIdentifier can not be specified."));
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> createProjectMembership(AmazonWebServicesClientProxy proxy,
                                                                                  ProxyClient<DataZoneClient> proxyClient,
                                                                                  ProgressEvent<ResourceModel,
                                                                                          CallbackContext> progress,
                                                                                  DataZoneClientWrapper dataZoneClientWrapper) {
        return proxy.initiate("AWS-DataZone-ProjectMembership::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                // get Create Request
                .translateToServiceRequest(model -> Translator.translateToCreateRequest(model))
                // make service call
                .makeServiceCall((createProjectMembershipRequest, client) -> dataZoneClientWrapper.createProjectMembership(createProjectMembershipRequest))
                // and update the model fields.
                .done((createProjectMembershipRequest, createProjectMembershipResponse, dataZoneClientProxyClient, resourceModel, callbackContext) ->
                        updateModelFields(createProjectMembershipRequest, resourceModel, dataZoneClientWrapper));
    }
}
