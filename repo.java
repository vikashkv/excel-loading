import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RuleMigrationRepository extends JpaRepository<RuleMigrationEntry, Long> {
    Optional<RuleMigrationEntry> findTopByOrderByProcessedAtDesc();
}
