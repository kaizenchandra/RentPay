package in.lmt.commercial;
import in.lmt.identity.IdentityConfig;
import in.lmt.shared.ApiProblem;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
@Component @org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name="lmt.jobs.enabled",havingValue="true",matchIfMissing=true)
public class CommunicationReconciliationWorker {
 private final CommunicationReconciliation service;private final LocalCommunicationEvidence provider;private final IdentityConfig config;private final boolean enabled;
 public CommunicationReconciliationWorker(CommunicationReconciliation service,LocalCommunicationEvidence provider,IdentityConfig config,@org.springframework.beans.factory.annotation.Value("${lmt.communication.local-enabled:${lmt.identity.local-containers:false}}")boolean enabled){this.service=service;this.provider=provider;this.config=config;this.enabled=enabled;}
 @Scheduled(fixedDelayString="${lmt.communication.delay-ms:1000}",initialDelayString="${lmt.communication.initial-delay-ms:10000}")
 public void run(){if(!enabled||!config.sandbox)return;for(var claim:service.claim()){try{var evidence=service.prepare(claim);var result=provider.find(evidence);service.finish(claim,result.code(),result.reference());}catch(ApiProblem failure){service.finish(claim,failure.status==404?"AUTHORITY_REVOKED":failure.code,null);}catch(RuntimeException failure){service.finish(claim,"PROVIDER_UNAVAILABLE",null);}}}
}
