import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RuleMigrationEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String version;
    private LocalDateTime processedAt;

    // Constructor
    public RuleMigrationEntry(String version, LocalDateTime processedAt) {
        this.version = version;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public RuleMigrationEntry() {}
}
