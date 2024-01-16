package software.amazon.datazone.environment;

import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.EnvironmentSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.datazone.environment.client.DataZoneClientWrapper;
import software.amazon.datazone.environment.helper.LoggerWrapper;
import software.amazon.datazone.environment.helper.ResourceStabilizer;

import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<DataZoneClient> proxyClient,
            final Logger externalLogger) {

        // Initialize
        this.logger = new LoggerWrapper(externalLogger);
        this.dataZoneClientWrapper = new DataZoneClientWrapper(proxyClient, logger);
        this.stabilizer = new ResourceStabilizer(dataZoneClientWrapper, logger);

        final CallbackContext currentContext = getCallbackContext(callbackContext);

        return ProgressEvent.progress(request.getDesiredResourceState(), currentContext)
                .then(progress -> createEnvironment(proxy, proxyClient, progress))
                .then(progress -> stabilizer.stabilizeResource(progress.getResourceModel(), progress.getCallbackContext()))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, externalLogger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createEnvironment(AmazonWebServicesClientProxy proxy,
                                                                            ProxyClient<DataZoneClient> proxyClient,
                                                                            ProgressEvent<ResourceModel, CallbackContext> progress) {
        final EnvironmentSummary environmentSummary = progress.getCallbackContext().getEnvironmentSummary();
        // If the environmentSummary is not null then this implies that we created the environment in the previous stabilization
        // attempt and this attempt we just need to wait till environment gets stabilized.
        if (!Objects.isNull(environmentSummary)) {
            return ProgressEvent.progress(progress.getResourceModel(), progress.getCallbackContext());
        }

        // Else we need to call DataZone Control Plane to create the resource.
        return proxy.initiate("AWS-DataZone-Environment::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::translateToCreateRequest)
                .makeServiceCall((createEnvironmentRequest, client) -> dataZoneClientWrapper.createEnvironment(createEnvironmentRequest))
                // and update the model fields and context
                .done(this::updateModelFieldsAndContext);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateModelFieldsAndContext(CreateEnvironmentRequest createEnvironmentRequest,
                                                                                      CreateEnvironmentResponse createEnvironmentResponse,
                                                                                      ProxyClient<DataZoneClient> dataZoneClientProxyClient,
                                                                                      ResourceModel resourceModel,
                                                                                      CallbackContext callbackContext) {
        logger.info("Successfully created Environment with name %s and id %s with domain id %s",
                createEnvironmentResponse.name(), createEnvironmentResponse.id(), createEnvironmentRequest.domainIdentifier());
        resourceModel.setId(createEnvironmentResponse.id());
        resourceModel.setDomainId(createEnvironmentRequest.domainIdentifier());
        EnvironmentSummary environmentSummary = EnvironmentSummary.builder()
                .id(createEnvironmentResponse.id())
                .domainId(createEnvironmentResponse.domainId())
                .name(createEnvironmentResponse.name())
                .projectId(createEnvironmentResponse.projectId())
                .build();

        CallbackContext updatedContext = CallbackContext.builder()
                .environmentSummary(environmentSummary)
                .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining())
                .build();

        return ProgressEvent.progress(resourceModel, updatedContext);
    }

}
