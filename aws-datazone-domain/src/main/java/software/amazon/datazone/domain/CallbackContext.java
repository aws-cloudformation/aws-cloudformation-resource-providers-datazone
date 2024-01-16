package software.amazon.datazone.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.datazone.model.DomainSummary;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private Integer stabilizationRetriesRemaining;
    private DomainSummary domainSummary;
}
