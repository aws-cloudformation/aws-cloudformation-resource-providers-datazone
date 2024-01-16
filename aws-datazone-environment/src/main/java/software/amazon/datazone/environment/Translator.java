package software.amazon.datazone.environment;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.EnvironmentParameter;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentsRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentsResponse;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return CreateEnvironmentRequest the aws service request to create a dataZone Environment
     */
    static CreateEnvironmentRequest translateToCreateRequest(final ResourceModel model) {
        validateRequiredInputs(model);
        List<EnvironmentParameter> userParameters = getEnvironmentParameters(model);

        return CreateEnvironmentRequest.builder()
                .name(model.getName())
                .projectIdentifier(model.getProjectIdentifier())
                .domainIdentifier(model.getDomainIdentifier())
                .description(model.getDescription())
                .environmentProfileIdentifier(model.getEnvironmentProfileIdentifier())
                .glossaryTerms(model.getGlossaryTerms())
                .userParameters(userParameters)
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    public static GetEnvironmentRequest translateToReadRequest(final ResourceModel model) {
        return GetEnvironmentRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param getEnvironmentResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetEnvironmentResponse getEnvironmentResponse) {
        return ResourceModel.builder()
                .awsAccountId(getEnvironmentResponse.awsAccountId())
                .awsAccountRegion(getEnvironmentResponse.awsAccountRegion())
                .createdAt(String.valueOf(getEnvironmentResponse.createdAt()))
                .createdBy(getEnvironmentResponse.createdBy())
                .description(getEnvironmentResponse.description())
                .domainId(getEnvironmentResponse.domainId())
                .environmentBlueprintId(getEnvironmentResponse.environmentBlueprintId())
                .environmentProfileId(getEnvironmentResponse.environmentProfileId())
                .glossaryTerms(List.copyOf(getEnvironmentResponse.glossaryTerms()))
                .id(getEnvironmentResponse.id())
                .name(getEnvironmentResponse.name())
                .projectId(getEnvironmentResponse.projectId())
                .provider(getEnvironmentResponse.provider())
                .status(getEnvironmentResponse.statusAsString())
                .updatedAt(String.valueOf(getEnvironmentResponse.updatedAt()))
                .userParameters(
                        getEnvironmentResponse.userParameters().stream().map(userParameter ->
                                software.amazon.datazone.environment.EnvironmentParameter.builder()
                                        .value(Objects.isNull(userParameter.defaultValue()) ? "" : userParameter.defaultValue())
                                        .name(userParameter.keyName())
                                        .build()
                        ).collect(Collectors.toList())
                )
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteEnvironmentRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteEnvironmentRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return updateEnvironmentRequest the aws service request to modify a resource
     */
    static UpdateEnvironmentRequest translateToFirstUpdateRequest(final ResourceModel model) {
        return UpdateEnvironmentRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .description(model.getDescription())
                .glossaryTerms(model.getGlossaryTerms())
                .name(model.getName())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param model     resource model
     * @param nextToken token passed to the aws service list resources request
     * @return ListEnvironmentsRequest the aws DataZone request to list resources within aws account
     */
    static ListEnvironmentsRequest translateToListRequest(final ResourceModel model, final String nextToken) {
        return ListEnvironmentsRequest.builder()
                .domainIdentifier(getDomainId(model))
                .projectIdentifier(model.getProjectIdentifier())
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param listEnvironmentsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final ListEnvironmentsResponse listEnvironmentsResponse) {
        return streamOfOrEmpty(listEnvironmentsResponse.items())
                .map(environmentSummary -> ResourceModel.builder()
                        .awsAccountId(environmentSummary.awsAccountId())
                        .awsAccountRegion(environmentSummary.awsAccountRegion())
                        .createdAt(environmentSummary.createdAt().toString())
                        .createdBy(environmentSummary.createdBy())
                        .description(environmentSummary.description())
                        .domainId(environmentSummary.domainId())
                        .environmentProfileId(environmentSummary.environmentProfileId())
                        .id(environmentSummary.id())
                        .name(environmentSummary.name())
                        .projectId(environmentSummary.projectId())
                        .provider(environmentSummary.provider())
                        .status(environmentSummary.statusAsString())
                        .updatedAt(environmentSummary.updatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private static String getDomainId(ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }

    private static void validateRequiredInputs(ResourceModel desiredResourceState) {

        if (StringUtils.isNullOrEmpty(desiredResourceState.getDomainIdentifier())) {
            throw new CfnInvalidRequestException("DomainIdentifier can not be empty.");
        } else if (StringUtils.isNullOrEmpty(desiredResourceState.getEnvironmentProfileIdentifier())) {
            throw new CfnInvalidRequestException("EnvironmentProfileIdentifier can not be empty.");
        } else if (StringUtils.isNullOrEmpty(desiredResourceState.getProjectIdentifier())) {
            throw new CfnInvalidRequestException("ProjectIdentifier can not be empty.");
        }
    }

    private static List<EnvironmentParameter> getEnvironmentParameters(ResourceModel model) {
        List<EnvironmentParameter> userParams = streamOfOrEmpty(model.getUserParameters())
                .map(userParam -> EnvironmentParameter.builder()
                        .name(userParam.getName())
                        .value(userParam.getValue())
                        .build())
                .collect(Collectors.toList());
        return userParams;
    }
}
