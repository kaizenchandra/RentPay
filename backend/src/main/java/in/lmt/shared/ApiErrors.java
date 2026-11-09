package in.lmt.shared;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@org.springframework.core.annotation.Order(-1)
@RestControllerAdvice(basePackages={"in.lmt.identity","in.lmt.workspace","in.lmt.orchestration","in.lmt.catalog.admin","in.lmt.documents","in.lmt.commercial"})
public class ApiErrors {
    public static ResponseEntity<ProblemDetail> response(int status,String code,String detail,List<Map<String,String>> fields) {
        var p=ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status),detail);
        p.setProperty("code",code);p.setProperty("fieldErrors",fields);p.setProperty("correlationId",UUID.randomUUID().toString());
        var h=new HttpHeaders();h.setCacheControl("no-store");
        if(status==429||status==503){h.set("Retry-After","60");p.setProperty("retryAfter",60);}
        return new ResponseEntity<>(p,h,HttpStatusCode.valueOf(status));
    }
    @ExceptionHandler(ApiProblem.class) ResponseEntity<ProblemDetail> problem(ApiProblem e){return response(e.status,e.code,e.getMessage(),e.field==null?List.of():List.of(Map.of("field",e.field,"message",e.getMessage())));}
    @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException e){return response(422,"VALIDATION_FAILED","Check the highlighted fields.",e.getBindingResult().getFieldErrors().stream().map(f->Map.of("field",f.getField(),"message",Objects.requireNonNullElse(f.getDefaultMessage(),"Invalid value"))).toList());}
    @ExceptionHandler({HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingRequestHeaderException.class}) ResponseEntity<ProblemDetail> malformed(Exception e){return response(400,"MALFORMED_REQUEST","The request format is invalid.",List.of());}
    @ExceptionHandler({org.springframework.dao.DataAccessException.class,org.springframework.transaction.TransactionException.class}) ResponseEntity<ProblemDetail> unavailable(Exception e){return response(503,"SERVICE_UNAVAILABLE","The service is temporarily unavailable. Retry the same operation, not a new submission.",List.of());}
}
