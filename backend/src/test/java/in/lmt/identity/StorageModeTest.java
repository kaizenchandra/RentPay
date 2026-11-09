package in.lmt.identity;
import static org.assertj.core.api.Assertions.*;
import in.lmt.documents.LocalQuarantineStore;
import in.lmt.shared.ApiProblem;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
class StorageModeTest {
 @Test void localAdapterCannotEnableProductionStorage(){
  var config=new IdentityConfig(new MockEnvironment(),"unavailable","","","https://localhost","");
  var store=new LocalQuarantineStore(config);
  assertThat(store.available()).isFalse();
  assertThat(store.inspect(java.util.UUID.randomUUID(),1,"0".repeat(64))).isEqualTo(in.lmt.documents.PrivateObjectStore.Integrity.unavailable);
  assertThatThrownBy(()->store.put(java.util.UUID.randomUUID(),new byte[]{1})).isInstanceOfSatisfying(ApiProblem.class,e->{assertThat(e.status).isEqualTo(503);assertThat(e.code).isEqualTo("STORAGE_UNAVAILABLE");});
 }
}
