package software.amazon.datazone.project;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.datazone.model.CreateProjectRequest;
import software.amazon.awssdk.services.datazone.model.DeleteProjectRequest;
import software.amazon.awssdk.services.datazone.model.GetProjectRequest;
import software.amazon.awssdk.services.datazone.model.GetProjectResponse;
import software.amazon.awssdk.services.datazone.model.ListProjectsRequest;
import software.amazon.awssdk.services.datazone.model.ListProjectsResponse;
import software.amazon.awssdk.services.datazone.model.UpdateProjectRequest;
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
     * @return CreateProjectRequest the DataZone request to create a new project
     */
    static CreateProjectRequest translateToCreateRequest(final ResourceModel model) {
        validateRequiredInputs(model);
        return CreateProjectRequest.builder()
                .description(model.getDescription())
                .domainIdentifier(getDomainId(model))
                .glossaryTerms(model.getGlossaryTerms())
                .name(model.getName())
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return GetProjectRequest the DataZone request to describe a project
     */
    static GetProjectRequest translateToReadRequest(final ResourceModel model) {
        return GetProjectRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param GetProjectResponse the DataZone response for describing a project
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final GetProjectResponse getProjectResponse) {
        return ResourceModel.builder()
                .domainId(getProjectResponse.domainId())
                .id(getProjectResponse.id())
                .createdAt(getProjectResponse.createdAt().toString())
                .description(getProjectResponse.description())
                .createdBy(getProjectResponse.createdBy())
                .name(getProjectResponse.name())
                .lastUpdatedAt(getProjectResponse.lastUpdatedAt().toString())
                .glossaryTerms(getProjectResponse.glossaryTerms())
                .build();
    }

    /**
     * Request to delete a project resource
     *
     * @param model resource model
     * @return deleteProjectRequest the DataZone request to delete a project resource
     */
    static DeleteProjectRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteProjectRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param ResourceModel resource model
     * @return updateProjectRequest the Datazone request to modify a project resource
     */
    static UpdateProjectRequest translateToFirstUpdateRequest(final ResourceModel model) {
        return UpdateProjectRequest.builder()
                .domainIdentifier(getDomainId(model))
                .identifier(model.getId())
                .name(model.getName())
                .description(model.getDescription())
                .glossaryTerms(model.getGlossaryTerms())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param ResourceModel resource model
     * @param nextToken     token passed to the DataZone list project resources request
     * @return listProjectsRequest the DataZone request to list project resources within aws account
     */
    static ListProjectsRequest translateToListRequest(final ResourceModel model, final String nextToken) {
        return ListProjectsRequest.builder()
                .domainIdentifier(getDomainId(model))
                .nextToken(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param listProjectsResponse the DataZone list project resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final ListProjectsResponse listProjectsResponse) {

        return streamOfOrEmpty(listProjectsResponse.items())
                .map(projectSummary -> ResourceModel.builder()
                        .createdAt(projectSummary.createdAt().toString())
                        .createdBy(projectSummary.createdBy())
                        .description(projectSummary.description())
                        .domainId(projectSummary.domainId())
                        .id(projectSummary.id())
                        .name(projectSummary.name())
                        .lastUpdatedAt(projectSummary.updatedAt().toString())
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
        }
    }

}
