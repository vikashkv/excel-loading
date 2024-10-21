import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class RuleMigrationController {

    @Autowired
    private RuleMigrationService ruleMigrationService;

    @PostMapping("/migrate")
    public ResponseEntity<String> migrateRules() {
        String result = ruleMigrationService.migrateRules();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/latest-migration")
    public ResponseEntity<RuleMigrationEntry> getLatestMigration() {
        return ruleMigrationService.getLatestMigration()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
