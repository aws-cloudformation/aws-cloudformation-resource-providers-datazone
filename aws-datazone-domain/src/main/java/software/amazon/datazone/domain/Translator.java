package software.amazon.datazone.domain;

import lombok.NonNull;
import software.amazon.awssdk.services.datazone.model.CreateDomainRequest;
import software.amazon.awssdk.services.datazone.model.DeleteDomainRequest;
import software.amazon.awssdk.services.datazone.model.DomainSummary;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.ListDomainsRequest;
import software.amazon.awssdk.services.datazone.model.SingleSignOn;
import software.amazon.awssdk.services.datazone.model.TagResourceRequest;
import software.amazon.awssdk.services.datazone.model.UntagResourceRequest;
import software.amazon.awssdk.services.datazone.model.UpdateDomainRequest;
import software.amazon.datazone.domain.helper.TagHelper;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {

    /**
     * Helper function to convert the Resource Model to CreateDomainRequest which would be used for creating the Domain.
     *
     * @param model              Resource model for the Domain
     * @param clientRequestToken The unique identifier for the request.
     * @return The CreateDomainRequest for the Domain.
     */
    public static CreateDomainRequest translateToCreateRequest(final @NonNull ResourceModel model,
                                                               final @NonNull String clientRequestToken) {
        // If SingleSignOn is not null then we need to add it to the CreateDomainRequest.
        final SingleSignOn signOn = Objects.isNull(model.getSingleSignOn()) ? null : SingleSignOn.builder()
                .type(model.getSingleSignOn().getType())
                .userAssignment(model.getSingleSignOn().getUserAssignment())
                .build();

        return CreateDomainRequest.builder()
                .description(model.getDescription())
                .domainExecutionRole(model.getDomainExecutionRole())
                .kmsKeyIdentifier(model.getKmsKeyIdentifier())
                .name(model.getName())
                .singleSignOn(signOn)
                .tags(TagHelper.convertToMap(model.getTags()))
                .clientToken(clientRequestToken)
                .build();
    }

    /**
     * Helper function to convert the Resource Model to GetDomainRequest which would be used for fetching the Domain.
     *
     * @param model Resource model for the Domain
     * @return The GetDomainRequest for the Domain.
     */
    public static GetDomainRequest translateToReadRequest(final @NonNull ResourceModel model) {
        return GetDomainRequest.builder()
                .identifier(model.getId())
                .build();
    }

    /**
     * Helper function to convert the GetDomainResponse (received via the GetDomainCall) into the resource model.
     *
     * @param getDomainResponse The GetDomainResponse for the domain.
     * @return model Resource model
     */
    public static ResourceModel translateFromReadResponse(final @NonNull GetDomainResponse getDomainResponse) {
        final software.amazon.datazone.domain.SingleSignOn signOn = Objects.isNull(getDomainResponse.singleSignOn()) ?
                null : software.amazon.datazone.domain.SingleSignOn.builder()
                .type(getDomainResponse.singleSignOn().typeAsString())
                .userAssignment(getDomainResponse.singleSignOn().userAssignmentAsString())
                .build();

        return ResourceModel.builder()
                .arn(getDomainResponse.arn())
                .createdAt(getDomainResponse.createdAt().toString())
                .description(getDomainResponse.description())
                .domainExecutionRole(getDomainResponse.domainExecutionRole())
                .id(getDomainResponse.id())
                .kmsKeyIdentifier(getDomainResponse.kmsKeyIdentifier())
                .lastUpdatedAt(getDomainResponse.lastUpdatedAt().toString())
                .name(getDomainResponse.name())
                .portalUrl(getDomainResponse.portalUrl())
                .singleSignOn(signOn)
                .status(getDomainResponse.statusAsString())
                .tags(TagHelper.convertToSet(getDomainResponse.tags()))
                .managedAccountId("") // Get doesn't return managedAccountId but the same is required for Contract Tests
                .build();
    }

    /**
     * Helper function to convert the Resource Model to DeleteDomainRequest which would be used for deleting the Domain.
     *
     * @param model              Resource model for the Domain
     * @param clientRequestToken The unique identifier for the request.
     * @return The DeleteDomainRequest for the Domain.
     */
    public static DeleteDomainRequest translateToDeleteRequest(final @NonNull ResourceModel model,
                                                               final @NonNull String clientRequestToken) {
        return DeleteDomainRequest.builder()
                .identifier(model.getId())
                .clientToken(clientRequestToken)
                .build();
    }

    /**
     * Helper function to convert the Resource Model to UpdateDomainRequest which would be used for updating the Domain.
     *
     * @param model                        Resource model for the Domain
     * @param isSingleSignOnUpdateRequired Whether signOn needs to be updated.
     * @return The UpdateDomainRequest for the Domain.
     */
    public static UpdateDomainRequest translateToUpdateRequest(final @NonNull ResourceModel model,
                                                               final @NonNull Boolean isSingleSignOnUpdateRequired) {
        // If SingleSignOn is not null then we need to add it to the UpdateDomainRequest.
        final SingleSignOn signOn = isSingleSignOnUpdateRequired ? SingleSignOn.builder()
                .userAssignment(model.getSingleSignOn().getUserAssignment())
                .type(model.getSingleSignOn().getType())
                .build() : null;

        return UpdateDomainRequest.builder()
                .identifier(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .domainExecutionRole(model.getDomainExecutionRole())
                .singleSignOn(signOn)
                .build();
    }

    /**
     * Helper function to get ListDomainRequest.
     *
     * @param nextToken token passed to the DataZone Control Plane to fetch the next set of results.
     * @return ListDomainsRequest with the next token.
     */
    public static ListDomainsRequest translateToListRequest(final String nextToken) {
        return ListDomainsRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    /**
     * Helper function to convert DomainSummary to ResourceModel.
     *
     * @param domainSummary DomainSummary for the domain.
     * @return model Resource model
     */
    public static ResourceModel getResourceModelFromDomainSummary(DomainSummary domainSummary) {
        return ResourceModel.builder()
                .arn(domainSummary.arn())
                .createdAt(String.valueOf(domainSummary.createdAt()))
                .description(domainSummary.description())
                .id(domainSummary.id())
                .lastUpdatedAt(String.valueOf(domainSummary.lastUpdatedAt()))
                .managedAccountId(domainSummary.managedAccountId())
                .name(domainSummary.name())
                .portalUrl(domainSummary.portalUrl())
                .status(domainSummary.status().toString())
                .build();
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    public static TagResourceRequest tagResourceRequest(final @NonNull ResourceModel model,
                                                        final @NonNull Map<String, String> addedTags) {
        return TagResourceRequest.builder()
                .resourceArn(model.getArn())
                .tags(addedTags)
                .build();
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    public static UntagResourceRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
        return UntagResourceRequest.builder()
                .resourceArn(model.getArn())
                .tagKeys(removedTags)
                .build();
    }
}
