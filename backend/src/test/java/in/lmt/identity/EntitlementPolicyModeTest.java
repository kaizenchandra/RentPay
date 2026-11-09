package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import in.lmt.commercial.EntitlementOverrides;
import in.lmt.shared.ApiProblem;
class EntitlementPolicyModeTest {
 @Test void sandboxOverrideAdministrationIsUnavailableInOtherModes(){
  var overrides=new EntitlementOverrides(null,null,null,"unavailable");
  assertThatThrownBy(()->overrides.read(java.util.UUID.randomUUID(),java.util.UUID.randomUUID())).isInstanceOfSatisfying(ApiProblem.class,e->{assertThat(e.status).isEqualTo(503);assertThat(e.code).isEqualTo("ENTITLEMENT_POLICY_UNAVAILABLE");});
 }
 @Test void capacityRepairIsUnavailableOutsideSandbox(){
  var repairs=new in.lmt.commercial.CapacityRepairs(null,null,null,null,"unavailable");
  assertThatThrownBy(()->repairs.read(java.util.UUID.randomUUID(),java.util.UUID.randomUUID())).isInstanceOfSatisfying(ApiProblem.class,e->{assertThat(e.status).isEqualTo(503);assertThat(e.code).isEqualTo("CAPACITY_REPAIR_UNAVAILABLE");});
 }
}
