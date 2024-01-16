package software.amazon.datazone.subscriptiontarget;

import com.amazonaws.util.StringUtils;
import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.CreateSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.DeleteSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.GetSubscriptionTargetRequest;
import software.amazon.awssdk.services.datazone.model.GetSubscriptionTargetResponse;
import software.amazon.awssdk.services.datazone.model.ListSubscriptionTargetsRequest;
import software.amazon.awssdk.services.datazone.model.SubscriptionTargetSummary;
import software.amazon.awssdk.services.datazone.model.UpdateSubscriptionTargetRequest;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {

    /**
     * Helper function to convert the Resource Model to CreateSubscriptionTargetRequest which would be used for creating
     * the SubscriptionTarget.
     *
     * @param model              Resource model for the SubscriptionTarget.
     * @param clientRequestToken The unique identifier for the request.
     * @return The CreateSubscriptionTargetRequest for the SubscriptionTarget.
     */
    static CreateSubscriptionTargetRequest translateToCreateRequest(final @NonNull ResourceModel model,
                                                                    final @NonNull String clientRequestToken) {
        return CreateSubscriptionTargetRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .environmentIdentifier(model.getEnvironmentIdentifier())
                .applicableAssetTypes(model.getApplicableAssetTypes())
                .authorizedPrincipals(model.getAuthorizedPrincipals())
                .clientToken(clientRequestToken)
                .manageAccessRole(model.getManageAccessRole())
                .name(model.getName())
                .provider(model.getProvider())
                .subscriptionTargetConfig(getNullSafeStream(model.getSubscriptionTargetConfig())
                        .map(subscriptionTargetForm -> software.amazon.awssdk.services.datazone.model.SubscriptionTargetForm.builder()
                                .content(subscriptionTargetForm.getContent())
                                .formName(subscriptionTargetForm.getFormName())
                                .build())
                        .collect(Collectors.toList())
                )
                .type(model.getType())
                .build();
    }

    /**
     * Helper function to convert the Resource Model to GetSubscriptionTargetRequest which would be used for fetching
     * the SubscriptionTarget.
     *
     * @param model Resource model for the SubscriptionTarget.
     * @return The GetSubscriptionTargetRequest for the SubscriptionTarget.
     */
    static GetSubscriptionTargetRequest translateToReadRequest(final ResourceModel model) {
        String domainId = Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
        String environmentId = Optional.ofNullable(model.getEnvironmentIdentifier()).orElse(model.getEnvironmentId());
        return GetSubscriptionTargetRequest.builder()
                .identifier(model.getId())
                .domainIdentifier(domainId)
                .environmentIdentifier(environmentId)
                .build();
    }

    /**
     * Helper function to convert the GetSubscriptionTargetResponse (received via the GetSubscriptionTarget Call) into
     * the resource model.
     *
     * @param response The GetSubscriptionTargetResponse for the SubscriptionTarget.
     * @return model Resource model.
     */
    static ResourceModel translateFromReadResponse(final GetSubscriptionTargetResponse response) {
        return ResourceModel.builder()
                .id(response.id())
                .domainId(response.domainId())
                .environmentId(response.environmentId())
                .applicableAssetTypes(response.applicableAssetTypes())
                .authorizedPrincipals(response.authorizedPrincipals())
                .createdAt(Objects.isNull(response.createdAt()) ? null : response.createdAt().toString())
                .createdBy(StringUtils.isNullOrEmpty(response.createdBy()) ? "" : response.createdBy())
                .manageAccessRole(response.manageAccessRole())
                .name(response.name())
                .projectId(response.projectId())
                .provider(response.provider())
                .subscriptionTargetConfig(getNullSafeStream(response.subscriptionTargetConfig())
                        .map(subscriptionTargetForm -> SubscriptionTargetForm.builder()
                                .content(subscriptionTargetForm.content())
                                .formName(subscriptionTargetForm.formName())
                                .build())
                        .collect(Collectors.toList())
                )
                .type(response.type())
                .updatedAt(Objects.isNull(response.updatedAt()) ? null : response.updatedAt().toString())
                .updatedBy(StringUtils.isNullOrEmpty(response.updatedBy()) ? "" : response.updatedBy())
                .build();
    }

    /**
     * Helper function to convert the Resource Model to DeleteSubscriptionTargetRequest which would be used for
     * deleting the Domain.
     *
     * @param model              Resource model for the Domain
     * @param clientRequestToken The unique identifier for the request.
     * @return The DeleteSubscriptionTargetRequest for the Domain.
     */
    static DeleteSubscriptionTargetRequest translateToDeleteRequest(final @NonNull ResourceModel model) {
        String domainId = Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
        String environmentId = Optional.ofNullable(model.getEnvironmentIdentifier()).orElse(model.getEnvironmentId());
        return DeleteSubscriptionTargetRequest.builder()
                .identifier(model.getId())
                .domainIdentifier(domainId)
                .environmentIdentifier(environmentId)
                .build();
    }

    /**
     * Helper function to convert the Resource Model to UpdateSubscriptionTargetRequest which would be used for
     * updating the SubscriptionTarget.
     *
     * @param model Resource model for the SubscriptionTarget
     * @return The UpdateSubscriptionTargetRequest for the SubscriptionTarget.
     */
    static UpdateSubscriptionTargetRequest translateToUpdateRequest(final @NonNull ResourceModel model) {
        String domainId = Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
        String environmentId = Optional.ofNullable(model.getEnvironmentIdentifier()).orElse(model.getEnvironmentId());
        return UpdateSubscriptionTargetRequest.builder()
                .domainIdentifier(domainId)
                .environmentIdentifier(environmentId)
                .identifier(model.getId())
                .applicableAssetTypes(model.getApplicableAssetTypes())
                .authorizedPrincipals(model.getAuthorizedPrincipals())
                .manageAccessRole(model.getManageAccessRole())
                .name(model.getName())
                .provider(model.getProvider())
                .subscriptionTargetConfig(getNullSafeStream(model.getSubscriptionTargetConfig())
                        .map(subscriptionTargetForm -> software.amazon.awssdk.services.datazone.model.SubscriptionTargetForm.builder()
                                .content(subscriptionTargetForm.getContent())
                                .formName(subscriptionTargetForm.getFormName())
                                .build())
                        .collect(Collectors.toList())
                )
                .build();
    }

    /**
     * Helper function to get ListSubscriptionTargetsRequest.
     *
     * @return ListSubscriptionTargetsRequest with the next token.
     */
    static ListSubscriptionTargetsRequest translateToListRequest(final @NonNull ResourceModel model,
                                                                 final String nextToken) {
        String domainId = Optional.ofNullable(model.getDomainIdentifier()).orElse(model.getDomainId());
        String environmentId = Optional.ofNullable(model.getEnvironmentIdentifier()).orElse(model.getEnvironmentId());
        return ListSubscriptionTargetsRequest.builder()
                .domainIdentifier(domainId)
                .environmentIdentifier(environmentId)
                .nextToken(nextToken)
                .build();
    }


    /**
     * Helper function to convert the SubscriptionTargetSummary (received via the ListSubscriptionTargets Call) into
     * the resource model.
     *
     * @param subscriptionTargetSummary The SubscriptionTargetSummary for the SubscriptionTarget.
     * @return model Resource model.
     */
    static ResourceModel getResourceModelFromSummary(SubscriptionTargetSummary subscriptionTargetSummary) {
        return ResourceModel.builder()
                .id(subscriptionTargetSummary.id())
                .domainId(subscriptionTargetSummary.domainId())
                .environmentId(subscriptionTargetSummary.environmentId())
                .applicableAssetTypes(subscriptionTargetSummary.applicableAssetTypes())
                .authorizedPrincipals(subscriptionTargetSummary.authorizedPrincipals())
                .createdAt(Objects.isNull(subscriptionTargetSummary.createdAt()) ? null : subscriptionTargetSummary.createdAt().toString())
                .createdBy(subscriptionTargetSummary.createdBy())
                .manageAccessRole(subscriptionTargetSummary.manageAccessRole())
                .name(subscriptionTargetSummary.name())
                .projectId(subscriptionTargetSummary.projectId())
                .provider(subscriptionTargetSummary.provider())
                .subscriptionTargetConfig(getNullSafeStream(subscriptionTargetSummary.subscriptionTargetConfig())
                        .map(subscriptionTargetForm -> SubscriptionTargetForm.builder()
                                .content(subscriptionTargetForm.content())
                                .formName(subscriptionTargetForm.formName())
                                .build())
                        .collect(Collectors.toList())
                )
                .type(subscriptionTargetSummary.type())
                .updatedAt(Objects.isNull(subscriptionTargetSummary.updatedAt()) ? null : subscriptionTargetSummary.updatedAt().toString())
                .updatedBy(subscriptionTargetSummary.updatedBy())
                .build();
    }

    private static <T> Stream<T> getNullSafeStream(Collection<T> collection) {
        return Stream.ofNullable(collection)
                .flatMap(Collection::stream);
    }
}
