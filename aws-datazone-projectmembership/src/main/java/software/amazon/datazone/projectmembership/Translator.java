package software.amazon.datazone.projectmembership;

import com.amazonaws.util.StringUtils;
import com.google.common.collect.Lists;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.model.CreateProjectMembershipRequest;
import software.amazon.awssdk.services.datazone.model.DeleteProjectMembershipRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectMembershipsRequest;
import software.amazon.awssdk.services.datazone.model.Member;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static software.amazon.datazone.projectmembership.helper.Constants.GROUP_IDENTIFIER;
import static software.amazon.datazone.projectmembership.helper.Constants.USER_IDENTIFIER;

public class Translator {

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateProjectMembershipRequest translateToCreateRequest(final @NonNull ResourceModel model) {
        return CreateProjectMembershipRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .projectIdentifier(model.getProjectIdentifier())
                .designation(model.getDesignation())
                .member(Member.builder()
                        .groupIdentifier(StringUtils.isNullOrEmpty(model.getMember().getGroupIdentifier())
                                ? null : model.getMember().getGroupIdentifier())
                        .userIdentifier(StringUtils.isNullOrEmpty(model.getMember().getUserIdentifier())
                                ? null : model.getMember().getUserIdentifier())
                        .build())
                .build();
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteProjectMembershipRequest translateToDeleteRequest(final @NonNull ResourceModel model) {
        return DeleteProjectMembershipRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .projectIdentifier(model.getProjectIdentifier())
                .member(Member.builder()
                        .userIdentifier(USER_IDENTIFIER.equals(model.getMemberIdentifierType()) ?
                                model.getMemberIdentifier() : null)
                        .groupIdentifier(GROUP_IDENTIFIER.equals(model.getMemberIdentifierType()) ?
                                model.getMemberIdentifier() : null)
                        .build())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static ListProjectMembershipsRequest translateToListRequest(final @NonNull ResourceModel model,
                                                                final String nextToken) {
        return ListProjectMembershipsRequest.builder()
                .domainIdentifier(model.getDomainIdentifier())
                .projectIdentifier(model.getProjectIdentifier())
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final AwsResponse awsResponse) {
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L75-L82
        return streamOfOrEmpty(Lists.newArrayList())
                .map(resource -> ResourceModel.builder()
                        // include only primary identifier
                        .build())
                .collect(Collectors.toList());
    }

    public static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest tagResourceRequest(final ResourceModel model, final Map<String, String> addedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
    }

    /**
     * Request to add tags to a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static AwsRequest untagResourceRequest(final ResourceModel model, final Set<String> removedTags) {
        final AwsRequest awsRequest = null;
        // TODO: construct a request
        // e.g. https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-logs/blob/2077c92299aeb9a68ae8f4418b5e932b12a8b186/aws-logs-loggroup/src/main/java/com/aws/logs/loggroup/Translator.java#L39-L43
        return awsRequest;
    }
}
