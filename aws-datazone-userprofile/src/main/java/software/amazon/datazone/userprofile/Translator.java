package software.amazon.datazone.userprofile;

import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.CreateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.GetUserProfileResponse;
import software.amazon.awssdk.services.datazone.model.SearchUserProfilesRequest;
import software.amazon.awssdk.services.datazone.model.UpdateUserProfileRequest;
import software.amazon.awssdk.services.datazone.model.UserProfileSummary;

import java.util.Objects;
import java.util.Optional;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Helper function to convert the Resource Model to GetDomainRequest which would be used for fetching the Domain.
     *
     * @param model Resource model for the Domain
     * @return The GetDomainRequest for the Domain.
     */
    public static GetDomainRequest translateToGetDomainRequest(final @NonNull ResourceModel model) {
        return GetDomainRequest.builder()
                .identifier(getDomain(model))
                .build();
    }

    /**
     * Helper function to convert the Resource Model to CreateUserProfileRequest which would be used for creating
     * the User Profiles.
     *
     * @param model              Resource model for the User Profile.
     * @param clientRequestToken The unique identifier for the request.
     * @return The CreateUserProfileRequest for the User Profile.
     */
    static CreateUserProfileRequest translateToCreateRequest(final @NonNull ResourceModel model,
                                                             final @NonNull String clientRequestToken) {
        return CreateUserProfileRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .clientToken(clientRequestToken)
                .userIdentifier(model.getUserIdentifier())
                .userType(model.getUserType())
                .build();
    }

    /**
     * Helper function to convert the Resource Model to GetUserProfileRequest which would be used for fetching
     * the User Profile.
     *
     * @param model Resource model for the User Profile.
     * @return The GetUserProfileRequest for the User Profile.
     */
    static GetUserProfileRequest translateToReadRequest(final ResourceModel model) {
        return GetUserProfileRequest.builder()
                .domainIdentifier(getDomain(model))
                .userIdentifier(getUserId(model))
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param getUserProfileResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetUserProfileResponse getUserProfileResponse) {
        UserProfileDetails details = Objects.isNull(getUserProfileResponse.details()) ? null : UserProfileDetails.builder()
                .iam(Objects.isNull(getUserProfileResponse.details().iam()) ? null : IamUserProfileDetails.builder()
                        .arn(getUserProfileResponse.details().iam().arn())
                        .build())
                .sso(Objects.isNull(getUserProfileResponse.details().sso()) ? null : SsoUserProfileDetails.builder()
                        .firstName(getUserProfileResponse.details().sso().firstName())
                        .lastName(getUserProfileResponse.details().sso().lastName())
                        .username(getUserProfileResponse.details().sso().username())
                        .build())
                .build();

        return ResourceModel.builder()
                .id(getUserProfileResponse.id())
                .domainId(getUserProfileResponse.domainId())
                .type(getUserProfileResponse.typeAsString())
                .status(getUserProfileResponse.statusAsString())
                .details(details)
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static UpdateUserProfileRequest translateToUpdateRequest(final ResourceModel model) {
        return translateToUpdateRequest(getDomain(model), getUserId(model), model.getStatus());
    }

    static UpdateUserProfileRequest translateToUpdateRequest(final @NonNull String domainId,
                                                             final @NonNull String userId,
                                                             final @NonNull String status) {
        return UpdateUserProfileRequest.builder()
                .domainIdentifier(domainId)
                .userIdentifier(userId)
                .status(status)
                .build();
    }

    /**
     * Request to search resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to search resources within aws account
     */
    public static SearchUserProfilesRequest translateToSearchRequest(ResourceModel model, String nextToken) {
        String userType = model.getUserType().contains("SSO") ? "DATAZONE_SSO_USER" : "DATAZONE_IAM_USER";
        return SearchUserProfilesRequest.builder()
                .domainIdentifier(getDomain(model))
                .nextToken(nextToken)
                .userType(userType)
                .build();
    }

    /**
     * Helper function to convert the userProfileSummary (received via the SearchUserProfiles Call) into
     * the resource model.
     *
     * @param userProfileSummary The userProfileSummary for the UserProfiles.
     * @param domainId           The domain for UserProfiles.
     * @return model Resource model.
     */
    static ResourceModel getResourceModelFromSummary(UserProfileSummary userProfileSummary, String domainId) {
        return ResourceModel.builder()
                .id(userProfileSummary.id())
                .domainId(domainId)
                .status(userProfileSummary.statusAsString())
                .details(UserProfileDetails.builder()
                        .iam(Objects.isNull(userProfileSummary.details().iam()) ? null : IamUserProfileDetails.builder()
                                .arn(userProfileSummary.details().iam().arn())
                                .build())
                        .sso(Objects.isNull(userProfileSummary.details().sso()) ? null : SsoUserProfileDetails.builder()
                                .firstName(userProfileSummary.details().sso().firstName())
                                .lastName(userProfileSummary.details().sso().lastName())
                                .username(userProfileSummary.details().sso().username())
                                .build())
                        .build())
                .type(Objects.nonNull(userProfileSummary.type()) ? userProfileSummary.type().toString() : null)
                .build();
    }

    public static String getDomain(ResourceModel model) {
        return Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
    }

    public static String getUserId(ResourceModel model) {
        return Optional.ofNullable(model.getId()).orElse(model.getUserIdentifier());
    }
}
