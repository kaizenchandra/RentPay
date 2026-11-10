package in.lmt.identity;

import in.lmt.shared.ApiProblem;
import in.lmt.shared.Audit;
import java.time.Instant;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class AccountControls {
    private final JdbcClient db;
    private final Audit audit;
    private final ObjectMapper json;
    private final long reauthenticationSeconds;

    public AccountControls(JdbcClient db, Audit audit, ObjectMapper json,
            @Value("${lmt.security.reauthentication-seconds:300}") long reauthenticationSeconds) {
        this.db = db; this.audit = audit; this.json = json;
        if (reauthenticationSeconds < 1 || reauthenticationSeconds > 1800) throw new IllegalArgumentException("Invalid reauthentication interval");
        this.reauthenticationSeconds = reauthenticationSeconds;
    }

    @Transactional(readOnly=true) public boolean identityModeAllowed(UUID actor,String mode){return db.sql("SELECT EXISTS(SELECT 1 FROM user_account WHERE id=:a AND identity_mode=:mode)").param("a",actor).param("mode",mode).query(Boolean.class).single();}

    public record Profile(UUID id, String displayName, String mode, String maskedMobile, String maskedEmail, int version, String etag) {}
    public record SessionView(UUID id, String clientLabel, Instant signedInAt, Instant lastActiveAt, boolean current, int version, String etag) {}
    public record Page(int limit, String nextCursor) {}
    public record Sessions(List<SessionView> items, Page page, int totalMatching, Instant asOf) {}
    public record Revoked(UUID id, String status) {}
    public record Notice(UUID id, String message, Instant occurredAt, Instant readAt, int version, String etag, String category) { public Notice { if(category==null)category="security"; } }
    public record Notices(List<Notice> items, Page page, int totalMatching, int unreadCount, Instant asOf) {}

    private void scope(UUID actor) {
        db.sql("SELECT set_config('lmt.account_id',:id,true)").param("id", actor.toString()).query().singleRow();
    }
    private static String etag(String kind, UUID id, int version) { return "\"" + kind + "-" + id + "-v" + version + "\""; }
    private static void precondition(String expected) {
        if (expected == null || expected.isBlank()) throw new ApiProblem(428,"PRECONDITION_REQUIRED","Reload the record before confirming this change.");
    }
    private void match(String expected, String actual) {
        if (!actual.equals(expected)) throw new ApiProblem(412,"VERSION_STALE","This record changed. Reload and review it before trying again.");
    }
    private Optional<String> replay(UUID actor, String operation, UUID key, String hash) {
        scope(actor);
        db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))")
            .param("key", actor + ":" + operation + ":" + key).query().singleRow();
        var prior = db.sql("SELECT request_hash,result::text FROM identity_command WHERE actor_id=:a AND operation=:o AND request_key=:k")
            .param("a",actor).param("o",operation).param("k",key).query().listOfRows().stream().findFirst();
        if (prior.isEmpty()) return Optional.empty();
        if (!hash.equals(prior.get().get("request_hash"))) throw new ApiProblem(409,"IDEMPOTENCY_CONFLICT","This request key already belongs to a different confirmation.");
        return Optional.of((String)prior.get().get("result"));
    }
    private void record(UUID actor, String operation, UUID key, String hash, Object result, UUID resource, String message) {
        db.sql("INSERT INTO identity_command(actor_id,operation,request_key,request_hash,result) VALUES(:a,:o,:k,:h,CAST(:r AS jsonb))")
            .param("a",actor).param("o",operation).param("k",key).param("h",hash).param("r",json.writeValueAsString(result)).update();
        audit.record(actor,null,operation,resource);
        db.sql("INSERT INTO account_notification(id,account_id,message) VALUES(:id,:a,:m)")
            .param("id",UUID.randomUUID()).param("a",actor).param("m",message).update();
    }

    @Transactional(readOnly=true)
    public Profile profile(UUID actor) {
        return db.sql("SELECT id,display_name,identity_mode,mobile,email,version FROM user_account WHERE id=:a")
            .param("a",actor).query((rs,n) -> {
                String mobile = rs.getString("mobile"); int version = rs.getInt("version");
                return new Profile(actor,rs.getString("display_name"),rs.getString("identity_mode"),
                    mobile==null?null:"+91••••••" + mobile.substring(mobile.length()-4),rs.getString("email")==null?null:ChallengeService.maskEmail(rs.getString("email")),version,etag("profile",actor,version));
            }).optional().orElseThrow(() -> new ApiProblem(401,"UNAUTHENTICATED","Sign in again."));
    }

    @Transactional
    public Profile updateProfile(UUID actor, UUID key, String expected, String displayName) {
        precondition(expected);
        String name = displayName.strip();
        String hash = ChallengeService.hash(expected.length()+":"+expected+":"+name);
        var prior = replay(actor,"identity.profile_updated",key,hash);
        if (prior.isPresent()) return json.readValue(prior.get(),Profile.class);
        db.sql("SELECT id FROM user_account WHERE id=:a FOR UPDATE").param("a",actor).query().singleRow();
        match(expected,profile(actor).etag());
        db.sql("UPDATE user_account SET display_name=:n,version=version+1 WHERE id=:a").param("n",name).param("a",actor).update();
        var result = profile(actor);
        record(actor,"identity.profile_updated",key,hash,result,actor,"Your profile display name was changed.");
        return result;
    }

    @Transactional
    public void registerSession(UUID actor, String token, String userAgent, int expectedGeneration) {
        db.sql("SELECT pg_advisory_xact_lock(hashtextextended(:key,0))").param("key","account-session-generation:"+actor).query().singleRow();
        int generation=db.sql("SELECT auth_generation FROM user_account WHERE id=:a").param("a",actor).query(Integer.class).single();
        if(generation!=expectedGeneration)throw new ApiProblem(409,"AUTHENTICATION_STALE","Account recovery changed this sign-in. Request a new code.");
        String hint = userAgent == null ? "" : userAgent;
        String browser = hint.contains("Edg/") ? "Edge" : hint.contains("Firefox/") ? "Firefox" : hint.contains("Chrome/") ? "Chrome" : hint.contains("Safari/") ? "Safari" : "Browser not identified";
        String platform = hint.contains("Android") ? "Android" : hint.contains("iPhone") || hint.contains("iPad") ? "iOS" : hint.contains("Windows") ? "Windows" : hint.contains("Mac") ? "macOS" : hint.contains("Linux") ? "Linux" : "platform unknown";
        UUID id = UUID.randomUUID();
        db.sql("INSERT INTO account_session(id,account_id,token_hash,client_label) VALUES(:id,:a,:h,:label)")
            .param("id",id).param("a",actor).param("h",ChallengeService.hash(token)).param("label",browser+" · "+platform).update();
        audit.record(actor,null,"identity.session_registered",id);
    }

    @Transactional
    public void logout(UUID actor, String token) {
        db.sql("UPDATE account_session SET revoked_at=CURRENT_TIMESTAMP,version=version+1 WHERE account_id=:a AND token_hash=:h AND revoked_at IS NULL")
            .param("a",actor).param("h",ChallengeService.hash(token)).update();
        db.sql("DELETE FROM spring_session WHERE principal_name=:a AND session_id=:token")
            .param("a",actor.toString()).param("token",token).update();
        audit.record(actor,null,"identity.logout",actor);
    }

    @Transactional(readOnly=true)
    public boolean sessionAllowed(UUID actor, String token) {
        return db.sql("SELECT EXISTS(SELECT 1 FROM account_session WHERE account_id=:a AND token_hash=:h AND revoked_at IS NULL AND authenticated_at>CURRENT_TIMESTAMP-interval '12 hours')")
            .param("a",actor).param("h",ChallengeService.hash(token)).query(Boolean.class).single();
    }

    private UUID cursor(UUID actor, String cursor) {
        if (cursor == null) return new UUID(0,0);
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), java.nio.charset.StandardCharsets.UTF_8).split(":");
            if(parts.length != 2 || !parts[0].equals(actor.toString())) throw new IllegalArgumentException();
            return UUID.fromString(parts[1]);
        } catch (IllegalArgumentException e) { throw new ApiProblem(422,"INVALID_CURSOR","Reload this list.","cursor"); }
    }
    private String next(UUID actor, UUID last) { return Base64.getUrlEncoder().withoutPadding().encodeToString((actor+":"+last).getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
    private static final String ACTIVE = """
        FROM account_session a JOIN spring_session s ON a.token_hash=encode(sha256(convert_to(s.session_id,'UTF8')),'hex')
        WHERE a.account_id=:actor AND s.principal_name=:principal AND a.revoked_at IS NULL
        AND a.authenticated_at>CURRENT_TIMESTAMP-interval '12 hours'
        AND s.last_access_time + s.max_inactive_interval::bigint*1000 > extract(epoch FROM CURRENT_TIMESTAMP)*1000
        """;

    @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Sessions sessions(UUID actor, String currentToken, String cursor) {
        var rows = db.sql("SELECT a.*,s.last_access_time " + ACTIVE + " AND a.id>:after ORDER BY a.id LIMIT 26")
            .param("actor",actor).param("principal",actor.toString()).param("after",cursor(actor,cursor))
            .query((rs,n) -> new SessionView(rs.getObject("id",UUID.class),rs.getString("client_label"),
                rs.getTimestamp("authenticated_at").toInstant(),Instant.ofEpochMilli(rs.getLong("last_access_time")),
                rs.getString("token_hash").equals(ChallengeService.hash(currentToken)),rs.getInt("version"),etag("session",rs.getObject("id",UUID.class),rs.getInt("version")))).list();
        boolean more = rows.size()>25; var items = more ? rows.subList(0,25) : rows;
        int total = db.sql("SELECT count(*) " + ACTIVE).param("actor",actor).param("principal",actor.toString()).query(Integer.class).single();
        return new Sessions(items,new Page(25,more?next(actor,items.getLast().id()):null),total,Instant.now());
    }

    @Transactional
    public Revoked revoke(UUID actor, String currentToken, UUID target, UUID key, String expected) {
        precondition(expected);
        String hash = ChallengeService.hash(target+":"+expected);
        var prior = replay(actor,"identity.session_revoked",key,hash);
        if (prior.isPresent()) return json.readValue(prior.get(),Revoked.class);
        var session = db.sql("SELECT * FROM account_session WHERE id=:id AND account_id=:a AND revoked_at IS NULL FOR UPDATE")
            .param("id",target).param("a",actor).query().listOfRows().stream().findFirst()
            .orElseThrow(() -> new ApiProblem(404,"SESSION_NOT_FOUND","Session is unavailable."));
        String tokenHash = (String)session.get("token_hash");
        if(tokenHash.equals(ChallengeService.hash(currentToken))) throw new ApiProblem(409,"USE_SIGN_OUT","Use Sign out to end your current session.");
        boolean recent = db.sql("SELECT EXISTS(SELECT 1 FROM account_session WHERE account_id=:a AND token_hash=:h AND revoked_at IS NULL AND authenticated_at>CURRENT_TIMESTAMP-(:seconds * interval '1 second'))")
            .param("a",actor).param("h",ChallengeService.hash(currentToken)).param("seconds",reauthenticationSeconds).query(Boolean.class).single();
        if(!recent) throw new ApiProblem(403,"REAUTHENTICATION_REQUIRED","Sign in again before ending another session.");
        match(expected,etag("session",target,((Number)session.get("version")).intValue()));
        db.sql("UPDATE account_session SET revoked_at=CURRENT_TIMESTAMP,version=version+1 WHERE id=:id").param("id",target).update();
        // Same transaction as the tombstone/audit: never rely only on framework save/delete timing.
        db.sql("DELETE FROM spring_session WHERE principal_name=:a AND encode(sha256(convert_to(session_id,'UTF8')),'hex')=:h")
            .param("a",actor.toString()).param("h",tokenHash).update();
        var result = new Revoked(target,"revoked");
        record(actor,"identity.session_revoked",key,hash,result,target,"Another browser session was signed out from account security.");
        return result;
    }

    @Transactional(readOnly=true, isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public Notices notices(UUID actor, String cursor, String filter) {
        scope(actor);
        if(!Set.of("all","unread","read").contains(filter))throw new ApiProblem(422,"INVALID_FILTER","Choose all, unread or read notifications.","status");
        Instant before=Instant.parse("9999-12-31T23:59:59Z");UUID last=new UUID(-1L,-1L);
        if(cursor!=null)try{
            if(cursor.length()>512)throw new IllegalArgumentException();
            String[] parts=new String(Base64.getUrlDecoder().decode(cursor),java.nio.charset.StandardCharsets.UTF_8).split("/",-1);
            if(parts.length!=5||!parts[0].equals("notices-v2")||!parts[1].equals(actor.toString())||!parts[2].equals(filter))throw new IllegalArgumentException();
            before=Instant.parse(parts[3]);last=UUID.fromString(parts[4]);
        }catch(RuntimeException invalid){throw new ApiProblem(422,"INVALID_CURSOR","Reload notifications for this account and filter.","cursor");}
        String predicate="account_id=:a AND (:filter='all' OR (:filter='unread' AND read_at IS NULL) OR (:filter='read' AND read_at IS NOT NULL))";
        var rows=db.sql("SELECT id,message,occurred_at,read_at,version,category FROM account_notification WHERE "+predicate+" AND (occurred_at,id)<(:before,:last) ORDER BY occurred_at DESC,id DESC LIMIT 26")
            .param("a",actor).param("filter",filter).param("before",java.sql.Timestamp.from(before)).param("last",last)
            .query((rs,n)->new Notice(rs.getObject(1,UUID.class),rs.getString(2),rs.getTimestamp(3).toInstant(),rs.getTimestamp(4)==null?null:rs.getTimestamp(4).toInstant(),rs.getInt(5),etag("notice",rs.getObject(1,UUID.class),rs.getInt(5)),rs.getString(6))).list();
        boolean more=rows.size()>25;var items=more?rows.subList(0,25):rows;
        int total=db.sql("SELECT count(*) FROM account_notification WHERE "+predicate).param("a",actor).param("filter",filter).query(Integer.class).single();
        int unread=db.sql("SELECT count(*) FROM account_notification WHERE account_id=:a AND read_at IS NULL").param("a",actor).query(Integer.class).single();
        String nextCursor=more?Base64.getUrlEncoder().withoutPadding().encodeToString(("notices-v2/"+actor+"/"+filter+"/"+items.getLast().occurredAt()+"/"+items.getLast().id()).getBytes(java.nio.charset.StandardCharsets.UTF_8)):null;
        return new Notices(items,new Page(25,nextCursor),total,unread,Instant.now());
    }
    @Transactional
    public Notice readNotice(UUID actor, UUID id, UUID key, String expected) {
        precondition(expected);
        String operation="identity.notification_read", hash=ChallengeService.hash(id+":"+expected);
        var prior=replay(actor,operation,key,hash);
        if(prior.isPresent())return json.readValue(prior.get(),Notice.class);
        var row=db.sql("SELECT * FROM account_notification WHERE id=:id AND account_id=:a FOR UPDATE")
            .param("id",id).param("a",actor).query().listOfRows().stream().findFirst()
            .orElseThrow(()->new ApiProblem(404,"NOTIFICATION_NOT_FOUND","Notification is unavailable."));
        int version=((Number)row.get("version")).intValue();
        match(expected,etag("notice",id,version));
        if(row.get("read_at")==null){
            db.sql("UPDATE account_notification SET read_at=clock_timestamp(),version=version+1 WHERE id=:id AND account_id=:a")
                .param("id",id).param("a",actor).update();
            audit.record(actor,null,operation,id);
        }
        Notice result=db.sql("SELECT id,message,occurred_at,read_at,version,category FROM account_notification WHERE id=:id AND account_id=:a")
            .param("id",id).param("a",actor).query((rs,n)->new Notice(id,rs.getString(2),rs.getTimestamp(3).toInstant(),rs.getTimestamp(4).toInstant(),rs.getInt(5),etag("notice",id,rs.getInt(5)),rs.getString(6))).single();
        db.sql("INSERT INTO identity_command(actor_id,operation,request_key,request_hash,result) VALUES(:a,:o,:k,:h,CAST(:r AS jsonb))")
            .param("a",actor).param("o",operation).param("k",key).param("h",hash).param("r",json.writeValueAsString(result)).update();
        return result;
    }

    public record SessionSelection(UUID id,String clientLabel,Instant authenticatedAt,int version) {}
    public record RevocationPreview(List<SessionSelection> sessions,String etag) {}
    public record BatchRevoked(List<UUID> sessionIds,int endedCount,String status) {}
    private List<UUID> targets(List<UUID> ids){
        if(ids==null||ids.isEmpty()||ids.size()>25||ids.stream().anyMatch(Objects::isNull)||new HashSet<>(ids).size()!=ids.size())
            throw new ApiProblem(422,"INVALID_SELECTION","Select between one and 25 distinct other sessions.","sessionIds");
        return ids.stream().sorted().toList();
    }
    private RevocationPreview selection(UUID actor,String token,List<UUID> ids,boolean lock){
        String tokenHash=ChallengeService.hash(token);
        var rows=db.sql("SELECT a.*,a.authenticated_at>clock_timestamp()-(:seconds * interval '1 second') AS recent,EXISTS(SELECT 1 FROM spring_session s WHERE s.principal_name=:principal AND a.token_hash=encode(sha256(convert_to(s.session_id,'UTF8')),'hex') AND s.last_access_time+s.max_inactive_interval::bigint*1000>extract(epoch FROM clock_timestamp())*1000) AS active FROM account_session a WHERE a.account_id=:a AND (a.id IN (:ids) OR a.token_hash=:h) ORDER BY a.id"+(lock?" FOR UPDATE OF a":""))
            .param("seconds",reauthenticationSeconds).param("principal",actor.toString()).param("a",actor).param("ids",ids).param("h",tokenHash).query().listOfRows();
        var current=rows.stream().filter(r->tokenHash.equals(r.get("token_hash"))).findFirst().orElseThrow(()->new ApiProblem(401,"UNAUTHENTICATED","Sign in again."));
        if(current.get("revoked_at")!=null||!(Boolean)current.get("active"))throw new ApiProblem(401,"UNAUTHENTICATED","Sign in again.");
        if(ids.contains(current.get("id")))throw new ApiProblem(409,"USE_SIGN_OUT","Use Sign out to end your current session.");
        if(lock&&!(Boolean)current.get("recent"))throw new ApiProblem(403,"REAUTHENTICATION_REQUIRED","Verify your current session before ending other sessions.");
        var selected=rows.stream().filter(r->ids.contains(r.get("id"))).toList();
        if(selected.size()!=ids.size())throw new ApiProblem(404,"SESSION_NOT_FOUND","One or more selected sessions are unavailable.");
        if(selected.stream().anyMatch(r->r.get("revoked_at")!=null||!(Boolean)r.get("active")))throw new ApiProblem(412,"VERSION_STALE","A selected session ended or expired. Refresh and review your selection.");
        var result=selected.stream().map(r->new SessionSelection((UUID)r.get("id"),(String)r.get("client_label"),((java.sql.Timestamp)r.get("authenticated_at")).toInstant(),((Number)r.get("version")).intValue())).toList();
        String versions=result.stream().map(r->r.id()+":"+r.version()).collect(java.util.stream.Collectors.joining(","));
        return new RevocationPreview(result,"\"session-selection-"+ChallengeService.hash(actor+":"+tokenHash+":"+versions)+"\"");
    }
    @Transactional(readOnly=true,isolation=org.springframework.transaction.annotation.Isolation.REPEATABLE_READ)
    public RevocationPreview previewRevocations(UUID actor,String token,List<UUID> ids){return selection(actor,token,targets(ids),false);}
    @Transactional
    public BatchRevoked revokeSelected(UUID actor,String token,List<UUID> requested,UUID key,String expected){
        precondition(expected);var ids=targets(requested);String operation="identity.sessions_revoked",hash=ChallengeService.hash(expected+":"+ids);
        var prior=replay(actor,operation,key,hash);if(prior.isPresent())return json.readValue(prior.get(),BatchRevoked.class);
        var preview=selection(actor,token,ids,true);match(expected,preview.etag());
        db.sql("DELETE FROM spring_session s USING account_session a WHERE a.account_id=:a AND a.id IN (:ids) AND s.principal_name=:principal AND a.token_hash=encode(sha256(convert_to(s.session_id,'UTF8')),'hex')")
            .param("a",actor).param("ids",ids).param("principal",actor.toString()).update();
        db.sql("UPDATE account_session SET revoked_at=clock_timestamp(),version=version+1 WHERE account_id=:a AND id IN (:ids)").param("a",actor).param("ids",ids).update();
        for(UUID id:ids)audit.record(actor,null,"identity.session_revoked",id);
        var result=new BatchRevoked(ids,ids.size(),"revoked");
        record(actor,operation,key,hash,result,actor,ids.size()+" selected browser sessions were signed out from account security.");
        return result;
    }

}
