package in.lmt.catalog;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/public/plans")
public class CatalogController {
    private final CatalogService service;
    public CatalogController(CatalogService service) { this.service = service; }
    @GetMapping
    public ResponseEntity<CatalogService.Catalog> list(
            @RequestParam(defaultValue = "workspace") String audience,
            @RequestParam(defaultValue = "monthly") String interval) {
        validate(audience, interval);
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.list(audience, interval));
    }
    @GetMapping("/{code}")
    public ResponseEntity<CatalogService.Plan> detail(@PathVariable String code,
            @RequestParam(defaultValue = "workspace") String audience,
            @RequestParam(defaultValue = "monthly") String interval) {
        validate(audience, interval);
        var plan = service.list(audience, interval).items().stream().filter(p -> p.code().equals(code))
                .findFirst().orElseThrow(() -> new CatalogProblem(404, "PLAN_NOT_FOUND", "Plan is not available.", null));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(plan);
    }
    private void validate(String audience, String interval) {
        if (!audience.equals("workspace") && !audience.equals("consumer"))
            throw new CatalogProblem(422, "VALIDATION_FAILED", "Choose workspace or consumer.", "audience");
        if (!interval.equals("monthly") && !interval.equals("annual"))
            throw new CatalogProblem(422, "VALIDATION_FAILED", "Choose monthly or annual.", "interval");
    }
}
