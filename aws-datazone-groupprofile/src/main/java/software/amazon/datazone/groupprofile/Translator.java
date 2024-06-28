package software.amazon.datazone.groupprofile;

import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.CreateGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetGroupProfileResponse;
import software.amazon.awssdk.services.datazone.model.GroupProfileSummary;
import software.amazon.awssdk.services.datazone.model.SearchGroupProfilesRequest;
import software.amazon.awssdk.services.datazone.model.UpdateGroupProfileRequest;

import java.util.Optional;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {
    /**
     * Helper function to convert the Resource Model to CreateGroupProfileRequest which would be used for creating
     * the Group Profiles.
     *
     * @param model              Resource model for the Group Profile.
     * @param clientRequestToken The unique identifier for the request.
     * @return The CreateGroupProfileRequest for the Group Profile.
     */
    static CreateGroupProfileRequest translateToCreateRequest(final @NonNull ResourceModel model,
                                                              final @NonNull String clientRequestToken) {
        return CreateGroupProfileRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .clientToken(clientRequestToken)
                .groupIdentifier(model.getGroupIdentifier())
                .build();
    }

    /**
     * Helper function to convert the Resource Model to GetGroupProfileRequest which would be used for fetching
     * the Group Profile.
     *
     * @param model Resource model for the Group Profile.
     * @return The GetGroupProfileRequest for the Group Profile.
     */
    static GetGroupProfileRequest translateToReadRequest(final ResourceModel model) {
        return GetGroupProfileRequest.builder()
                .domainIdentifier(getDomain(model))
                .groupIdentifier(getGroupId(model))
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param getGroupProfileResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetGroupProfileResponse getGroupProfileResponse) {
        return ResourceModel.builder()
                .id(getGroupProfileResponse.id())
                .domainId(getGroupProfileResponse.domainId())
                .status(getGroupProfileResponse.statusAsString())
                .groupName(getGroupProfileResponse.groupName())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static UpdateGroupProfileRequest translateToUpdateRequest(final ResourceModel model) {
        return translateToUpdateRequest(getDomain(model), getGroupId(model), model.getStatus());
    }

    static UpdateGroupProfileRequest translateToUpdateRequest(final String domainId,
                                                              final String groupId,
                                                              final String status) {
        return UpdateGroupProfileRequest.builder()
                .domainIdentifier(domainId)
                .groupIdentifier(groupId)
                .status(status)
                .build();
    }

    /**
     * Request to search resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to search resources within aws account
     */
    public static SearchGroupProfilesRequest translateToSearchRequest(ResourceModel model, String nextToken) {
        final String groupType = "DATAZONE_SSO_GROUP";
        return SearchGroupProfilesRequest.builder()
                .domainIdentifier(getDomain(model))
                .nextToken(nextToken)
                .groupType(groupType)
                .build();
    }

    /**
     * Helper function to convert the groupProfileSummary (received via the SearchGroupProfiles Call) into
     * the resource model.
     *
     * @param groupProfileSummary The groupProfileSummary for the GroupProfiles.
     * @param domainId            The domainId for the GroupProfiles.
     * @return model Resource model.
     */
    static ResourceModel getResourceModelFromSummary(GroupProfileSummary groupProfileSummary, String domainId) {
        return ResourceModel.builder()
                .id(groupProfileSummary.id())
                .domainId(domainId)
                .status(groupProfileSummary.statusAsString())
                .build();
    }

    public static String getDomain(ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }

    public static String getGroupId(ResourceModel model) {
        return Optional.ofNullable(model.getId()).orElse(model.getGroupIdentifier());
    }
}
