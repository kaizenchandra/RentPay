package in.lmt.commercial;

import java.time.Instant;
import java.util.*;
import in.lmt.identity.IdentityConfig;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Request-scoped UI projection. Never an authorization token or a command cache. */
@Service @Transactional(propagation=Propagation.MANDATORY)
public class EntitlementSnapshots {
 private final RentableCapacity capacity;private final JdbcClient db;private final Entitlements engine;private final SubscriptionUpgrades upgrades;private final IdentityConfig config;
 public EntitlementSnapshots(JdbcClient db,Entitlements engine,SubscriptionUpgrades upgrades,IdentityConfig config,RentableCapacity capacity){this.capacity=capacity;this.db=db;this.engine=engine;this.upgrades=upgrades;this.config=config;}
 public record Feature(String code,String label,boolean eligible,String reason,String limitation,List<String> requiredPlans,boolean upgradeAvailable){}
 public record Quota(String resource,int allowance,int used,int reserved,Integer remaining,String status,String definition,Integer countingPolicyVersion){}
 public record Snapshot(UUID workspaceId,boolean canManageBilling,List<Feature> features,List<Quota> quotas,Instant asOf,int refreshAfterSeconds){}
 public Snapshot read(UUID w,UUID actor){
  boolean owner=db.sql("SELECT role='OWNER' FROM workspace_member WHERE workspace_id=:w AND account_id=:a AND active").param("w",w).param("a",actor).query(Boolean.class).single();
  boolean sandbox=config.sandbox&&db.sql("SELECT mode='sandbox' FROM workspace WHERE id=:w").param("w",w).query(Boolean.class).single();
  var decisions=engine.evaluate(w);
  var offers=sandbox?upgrades.offers(w):List.<SubscriptionUpgrades.Offer>of();
  Map<UUID,List<Entitlements.Decision>> targets=new HashMap<>();
  for(var offer:offers)targets.computeIfAbsent(offer.targetVersionId(),p->engine.preview(w,p));
  var features=decisions.stream().map(d->{
   boolean planLock=d.reason().equals("CAPABILITY_NOT_CONFIGURED");
   var names=planLock?offers.stream().filter(o->targets.get(o.targetVersionId()).stream().anyMatch(t->t.code().equals(d.code())&&t.eligible())).map(SubscriptionUpgrades.Offer::name).distinct().toList():List.<String>of();
   String message=d.eligible()?"Available, subject to resource permissions and capacity.":switch(d.reason()){
    case "FEATURE_UNAVAILABLE" -> "This feature is not operationally available. An upgrade will not enable it.";
    case "CAPABILITY_NOT_CONFIGURED" -> names.isEmpty()?"Your configuration does not include this capability. No qualifying upgrade offer is currently configured.":"This capability requires one of the configured plans listed below. Offers are local payment simulations; no money is collected.";
    case "SECURITY_SUSPENDED" -> "Workspace security access is suspended. Contact your workspace administrator.";
    default -> "This action is restricted by current workspace eligibility. Contact your workspace administrator; an upgrade does not resolve this restriction.";
   };
   return new Feature(d.code(),d.label(),d.eligible(),d.reason(),message,names,owner&&!names.isEmpty());
  }).toList();
  var quotas=db.sql("SELECT resource,allowance,used,reserved FROM workspace_grant WHERE workspace_id=:w ORDER BY resource").param("w",w).query((r,n)->new Quota(r.getString("resource"),r.getInt("allowance"),r.getInt("used"),r.getInt("reserved"),null,"NOT_ASSESSED","See the scoped usage report for counting and reconciliation.",null)).list();
  if(owner){var unit=capacity.status(w);quotas=quotas.stream().map(q->q.resource().equals("rentable_items")?new Quota(q.resource(),unit.allowance(),unit.used(),unit.reserved(),unit.remaining(),unit.status(),unit.definition(),unit.countingPolicyVersion()):q).toList();}
  return new Snapshot(w,owner,features,quotas,Instant.now(),15);
 }
}
