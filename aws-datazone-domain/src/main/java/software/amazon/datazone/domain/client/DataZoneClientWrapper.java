package software.amazon.datazone.domain.client;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.datazone.DataZoneClient;
import software.amazon.awssdk.services.datazone.model.AccessDeniedException;
import software.amazon.awssdk.services.datazone.model.ConflictException;
import software.amazon.awssdk.services.datazone.model.CreateDomainRequest;
import software.amazon.awssdk.services.datazone.model.CreateDomainResponse;
import software.amazon.awssdk.services.datazone.model.DeleteDomainRequest;
import software.amazon.awssdk.services.datazone.model.DeleteDomainResponse;
import software.amazon.awssdk.services.datazone.model.DomainStatus;
import software.amazon.awssdk.services.datazone.model.GetDomainRequest;
import software.amazon.awssdk.services.datazone.model.GetDomainResponse;
import software.amazon.awssdk.services.datazone.model.InternalServerException;
import software.amazon.awssdk.services.datazone.model.ListDomainsRequest;
import software.amazon.awssdk.services.datazone.model.ListDomainsResponse;
import software.amazon.awssdk.services.datazone.model.ResourceNotFoundException;
import software.amazon.awssdk.services.datazone.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.datazone.model.TagResourceRequest;
import software.amazon.awssdk.services.datazone.model.TagResourceResponse;
import software.amazon.awssdk.services.datazone.model.ThrottlingException;
import software.amazon.awssdk.services.datazone.model.UnauthorizedException;
import software.amazon.awssdk.services.datazone.model.UntagResourceRequest;
import software.amazon.awssdk.services.datazone.model.UntagResourceResponse;
import software.amazon.awssdk.services.datazone.model.UpdateDomainRequest;
import software.amazon.awssdk.services.datazone.model.UpdateDomainResponse;
import software.amazon.awssdk.services.datazone.model.ValidationException;
import software.amazon.cloudformation.exceptions.BaseHandlerException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.datazone.domain.helper.DomainOperation;
import software.amazon.datazone.domain.helper.LoggerWrapper;

import java.util.Set;
import java.util.function.Function;

@AllArgsConstructor
public class DataZoneClientWrapper {

    private final @NonNull ProxyClient<DataZoneClient> proxyClient;
    private final @NonNull LoggerWrapper logger;

    public static final Set<DomainStatus> TRANSIENT_DOMAIN_STATUS = Set.of(DomainStatus.CREATING, DomainStatus.DELETING);
    public static final Set<DomainStatus> FAILED_DOMAIN_STATUS = Set.of(DomainStatus.CREATION_FAILED, DomainStatus.DELETION_FAILED);
    public static final Set<DomainStatus> STABILIZED_DOMAIN_STATUS = Set.of(DomainStatus.AVAILABLE);
    public static final Set<DomainStatus> STABILIZED_DOMAIN_STATUS_FOR_DELETION = Set.of(DomainStatus.DELETED);

    public CreateDomainResponse createDomain(final @NonNull CreateDomainRequest createDomainRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.CREATE_DOMAIN, createDomainRequest, client::createDomain, createDomainRequest.name(), proxyClient);
        }
    }

    public GetDomainResponse getDomain(final @NonNull GetDomainRequest getDomainRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.GET_DOMAIN, getDomainRequest, client::getDomain, getDomainRequest.identifier(), proxyClient);
        }
    }

    public UpdateDomainResponse updateDomain(UpdateDomainRequest updateDomainRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.UPDATE_DOMAIN, updateDomainRequest, client::updateDomain, updateDomainRequest.identifier(), proxyClient);
        }
    }

    public ListDomainsResponse listDomains(ListDomainsRequest listDomainsRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.LIST_DOMAINS, listDomainsRequest, client::listDomains, null, proxyClient);
        }
    }

    public DeleteDomainResponse deleteDomain(final @NonNull DeleteDomainRequest deleteDomainRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.GET_DOMAIN, deleteDomainRequest, client::deleteDomain, deleteDomainRequest.identifier(), proxyClient);
        }
    }

    public UntagResourceResponse deleteTagsFromDomain(UntagResourceRequest untagResourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.DELETE_TAGS, untagResourceRequest, client::untagResource, untagResourceRequest.resourceArn(), proxyClient);
        }
    }

    public TagResourceResponse addTagsToDomain(TagResourceRequest tagResourceRequest) {
        try (final var client = proxyClient.client()) {
            return executeCall(DomainOperation.DELETE_TAGS, tagResourceRequest, client::tagResource, tagResourceRequest.resourceArn(), proxyClient);
        }
    }

    private <Request extends AwsRequest, Response extends AwsResponse> Response executeCall(
            final DomainOperation operation,
            final Request request,
            final Function<Request, Response> clientOperation,
            final String resourceIdentifier,
            final ProxyClient<DataZoneClient> dataZoneClientProxyClient) {
        try {
            return dataZoneClientProxyClient.injectCredentialsAndInvokeV2(request, clientOperation);
        } catch (final Exception e) {
            logger.error("Failed to perform %s on Domain with id %s due to error %s", operation, resourceIdentifier, e);
            throw translateAPIExceptionToCfnException(e, operation);
        }
    }

    public static BaseHandlerException translateAPIExceptionToCfnException(final Exception e,
                                                                           final DomainOperation operation) {
        if (e instanceof AccessDeniedException) {
            // Temporary workaround for CTs since non-existing domain throws Access Denied
            return new CfnNotFoundException(e);
        } else if (e instanceof ConflictException) {
            return new CfnAlreadyExistsException(e);
        } else if (e instanceof InternalServerException) {
            return new CfnInternalFailureException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        } else if (e instanceof ServiceQuotaExceededException) {
            return new CfnServiceLimitExceededException(e);
        } else if (e instanceof ThrottlingException) {
            return new CfnThrottlingException(e);
        } else if (e instanceof UnauthorizedException) {
            return new CfnAccessDeniedException(e);
        } else if (e instanceof ValidationException) {
            return new CfnInvalidRequestException(e);
        } else if (e instanceof ResourceNotFoundException) {
            return new CfnNotFoundException(e);
        }

        return new CfnGeneralServiceException(operation.getName(), e);
    }

}
