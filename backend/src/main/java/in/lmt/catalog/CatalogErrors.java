package in.lmt.catalog;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = CatalogController.class)
public class CatalogErrors {
    @ExceptionHandler(CatalogProblem.class)
    ResponseEntity<ProblemDetail> domain(CatalogProblem e) {
        var d = e.data;
        return problem(d.status(), d.code(), d.detail(), d.field());
    }
    @ExceptionHandler({DataAccessException.class, org.springframework.transaction.TransactionException.class})
    ResponseEntity<ProblemDetail> unavailable(RuntimeException e) {
        return problem(503, "CATALOG_UNAVAILABLE", "The catalogue is temporarily unavailable. Please try again.", null);
    }
    private ResponseEntity<ProblemDetail> problem(int status, String code, String detail, String field) {
        var body = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail);
        body.setProperty("code", code);
        body.setProperty("correlationId", UUID.randomUUID().toString());
        body.setProperty("fieldErrors", field == null ? List.of() : List.of(Map.of("field", field, "message", detail)));
        var headers = new HttpHeaders();
        headers.setCacheControl("no-store");
        if (status == 503) { headers.set("Retry-After", "5"); body.setProperty("retryAfter", 5); }
        return new ResponseEntity<>(body, headers, HttpStatusCode.valueOf(status));
    }
}
