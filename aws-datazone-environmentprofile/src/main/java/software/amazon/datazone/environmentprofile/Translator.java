package software.amazon.datazone.environmentprofile;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.model.CreateEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.DeleteEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.EnvironmentParameter;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetEnvironmentProfileResponse;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesRequest;
import software.amazon.awssdk.services.datazone.model.ListEnvironmentProfilesResponse;
import software.amazon.awssdk.services.datazone.model.UpdateEnvironmentProfileRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;

import java.util.Collection;
import java.util.List;
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
     * @return awsRequest the aws service request to create a resource
     */
    static CreateEnvironmentProfileRequest translateToCreateRequest(final ResourceModel model) {

        validateRequiredInputs(model);

        List<EnvironmentParameter> userParams = getEnvironmentParameters(model);

        return CreateEnvironmentProfileRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .environmentBlueprintIdentifier(model.getEnvironmentBlueprintIdentifier())
                .name(model.getName())
                .projectIdentifier(model.getProjectIdentifier())
                .awsAccountId(model.getAwsAccountId())
                .awsAccountRegion(model.getAwsAccountRegion())
                .description(model.getDescription())
                .userParameters(userParams)
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static GetEnvironmentProfileRequest translateToReadRequest(final ResourceModel model) {
        return GetEnvironmentProfileRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param getEnvironmentProfileResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetEnvironmentProfileResponse getEnvironmentProfileResponse) {

        List<software.amazon.datazone.environmentprofile.EnvironmentParameter> userParams = streamOfOrEmpty(getEnvironmentProfileResponse.userParameters())
                .map(userParam -> software.amazon.datazone.environmentprofile.EnvironmentParameter.builder()
                        .value(userParam.defaultValue())
                        .name(userParam.keyName())
                        .build()
                )
                .collect(Collectors.toList());

        return ResourceModel.builder()
                .awsAccountId(getEnvironmentProfileResponse.awsAccountId())
                .awsAccountRegion(getEnvironmentProfileResponse.awsAccountRegion())
                .createdAt(getEnvironmentProfileResponse.createdAt().toString())
                .createdBy(getEnvironmentProfileResponse.createdBy())
                .description(getEnvironmentProfileResponse.description())
                .domainId(getEnvironmentProfileResponse.domainId())
                .environmentBlueprintId(getEnvironmentProfileResponse.environmentBlueprintId())
                .id(getEnvironmentProfileResponse.id())
                .name(getEnvironmentProfileResponse.name())
                .projectId(getEnvironmentProfileResponse.projectId())
                .updatedAt(getEnvironmentProfileResponse.updatedAt().toString())
                .userParameters(userParams)
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteEnvironmentProfileRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteEnvironmentProfileRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return UpdateEnvironmentProfileRequest the aws service request to modify a resource
     */

    static UpdateEnvironmentProfileRequest translateToFirstUpdateRequest(final ResourceModel model) {

        List<EnvironmentParameter> userParams = getEnvironmentParameters(model);
        return UpdateEnvironmentProfileRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .awsAccountId(model.getAwsAccountId())
                .awsAccountRegion(model.getAwsAccountRegion())
                .description(model.getDescription())
                .name(model.getName())
                .userParameters(userParams)
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListEnvironmentProfilesRequest translateToListRequest(final ResourceModel model, final String nextToken) {
        return ListEnvironmentProfilesRequest.builder()
                .domainIdentifier(getDomainId(model))
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param listEnvironmentProfilesResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final ListEnvironmentProfilesResponse listEnvironmentProfilesResponse) {
        return streamOfOrEmpty(listEnvironmentProfilesResponse.items())
                .map(environmentProfileSummary -> ResourceModel.builder()
                        .awsAccountId(environmentProfileSummary.awsAccountId())
                        .awsAccountRegion(environmentProfileSummary.awsAccountRegion())
                        .createdAt(environmentProfileSummary.createdAt().toString())
                        .createdBy(environmentProfileSummary.createdBy())
                        .description(environmentProfileSummary.description())
                        .domainId(environmentProfileSummary.domainId())
                        .environmentBlueprintId(environmentProfileSummary.environmentBlueprintId())
                        .id(environmentProfileSummary.id())
                        .name(environmentProfileSummary.name())
                        .projectId(environmentProfileSummary.projectId())
                        .updatedAt(environmentProfileSummary.updatedAt().toString())
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
        } else if (StringUtils.isNullOrEmpty(desiredResourceState.getEnvironmentBlueprintIdentifier())) {
            throw new CfnInvalidRequestException("EnvironmentBlueprintIdentifier can not be empty.");
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
