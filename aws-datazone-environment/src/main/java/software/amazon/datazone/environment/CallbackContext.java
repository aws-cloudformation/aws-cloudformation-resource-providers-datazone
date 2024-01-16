package software.amazon.datazone.environment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.datazone.model.EnvironmentSummary;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private Integer stabilizationRetriesRemaining;
    private Integer timeOutRetriesRemaining;
    private EnvironmentSummary environmentSummary;
}
