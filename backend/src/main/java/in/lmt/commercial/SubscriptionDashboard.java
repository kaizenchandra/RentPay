package in.lmt.commercial;

import in.lmt.catalog.CatalogService;
import in.lmt.workspace.WorkspaceCommands;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

/** Read-only workspace commerce projection. Rental and personal payments are never joined. */
@Service
public class SubscriptionDashboard {
 private final JdbcClient db;private final WorkspaceCommands scope;private final UsageQueries usage;private final CatalogService catalog;
 public SubscriptionDashboard(JdbcClient db,WorkspaceCommands scope,UsageQueries usage,CatalogService catalog){this.db=db;this.scope=scope;this.usage=usage;this.catalog=catalog;}
 public record Coverage(String status,String explanation){}
 public record Period(String status,Instant start,Instant end,String explanation){}
 public record Renewal(String status,Instant at,String explanation){}
 public record Estimate(String status,String amount,String currency,String explanation){}
 public record Communication(String channel,String status,Long used,Long allowance,Instant periodStart,Instant periodEnd,String explanation){}
 public record Upgrade(UUID versionId,String code,String name,Integer rentableLimit,Integer managerLimit,boolean available,String explanation){}
 public record Dashboard(UUID subscriptionId,UUID workspaceId,String workspaceName,String mode,String workspaceStatus,UUID planVersionId,String planCode,String planName,int planVersion,String subscriptionStatus,Instant startedAt,Period billingPeriod,Renewal nextRenewal,Estimate nextInvoiceEstimate,Coverage activeAddOns,List<UsageQueries.Quota> quotas,List<Communication> communicationUsage,List<Upgrade> upgrades,Coverage upgradeAvailability,Coverage paymentHistory,List<Entitlements.Decision> entitlements,Instant asOf){}
 @Transactional(readOnly=true,isolation=Isolation.REPEATABLE_READ)
 public Dashboard get(UUID actor,UUID w){
  scope.requireOwner(w,actor);
  var row=db.sql("SELECT s.id,s.started_at,s.status,s.period_start,s.period_end,w.mode,p.code,p.version_number FROM workspace_subscription s JOIN workspace w ON w.id=s.workspace_id JOIN plan_version p ON p.version_id=s.plan_version_id WHERE s.workspace_id=:w").param("w",w).query().singleRow();
  var state=usage.snapshot(w);Instant started=((java.sql.Timestamp)row.get("started_at")).toInstant();
  // Paid terms are recorded only after verified sandbox settlement; live billing remains unavailable.
  boolean free="active_free".equals(row.get("status"));
  var period=new Period(free?"not_applicable":"available",row.get("period_start")==null?null:((java.sql.Timestamp)row.get("period_start")).toInstant(),row.get("period_end")==null?null:((java.sql.Timestamp)row.get("period_end")).toInstant(),free?"This Free subscription has no recurring billing period. Subscription start is shown separately.":"Recorded sandbox subscription period. No automatic renewal collection is configured.");
  var renewal=new Renewal(free?"not_applicable":"unavailable",null,free?"No paid renewal is scheduled for this Free subscription.":"Renewal scheduling has not been integrated.");
  var estimate=new Estimate(free?"not_applicable":"unavailable",null,"INR",free?"No recurring SaaS invoice is scheduled for this Free subscription. This is not a zero-value invoice or a rental balance.":"An invoice estimate cannot be determined from the available contract data.");
  var communications=List.of(new Communication("email","unavailable",null,null,null,null,"Workspace email metering and allowances are not configured. Sign-in codes are not workspace consumption."),new Communication("whatsapp","unavailable",null,null,null,null,"Workspace WhatsApp metering and allowances are not configured."));
  var upgrades=catalog.list("workspace","monthly").items().stream().filter(p->!p.code().equals(row.get("code"))).map(p->new Upgrade(p.versionId(),p.code(),p.name(),p.rentableLimit(),p.managerLimit(),false,"Published catalogue baseline only. Live checkout is not configured. Explicitly configured local test offers are shown in the upgrade review.")).toList();
  return new Dashboard((UUID)row.get("id"),w,state.workspaceName(),(String)row.get("mode"),state.workspaceStatus(),state.planVersionId(),(String)row.get("code"),state.planName(),((Number)row.get("version_number")).intValue(),state.subscriptionStatus(),started,period,renewal,estimate,
   new Coverage("unavailable","Paid add-on subscriptions are not implemented. Local entitlement inputs are test configuration, not purchased add-ons; this is not a confirmed empty paid subscription list."),state.quotas(),communications,upgrades,
   new Coverage("unavailable","No live purchasable upgrade is configured. Use upgrade review for explicitly configured sandbox offers. Published baselines below are comparison information, not active entitlements."),
   new Coverage("unavailable","Workspace SaaS payment history is not implemented. No payment records are fabricated; rental transactions and personal membership payments belong to separate ledgers."),state.capabilities(),db.sql("SELECT CURRENT_TIMESTAMP").query(java.time.OffsetDateTime.class).single().toInstant());
 }
}
