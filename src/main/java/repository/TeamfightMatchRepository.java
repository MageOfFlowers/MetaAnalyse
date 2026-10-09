package repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import model.TeamfightMatch;

public interface TeamfightMatchRepository extends JpaRepository<TeamfightMatch, Long> {
    List<TeamfightMatch> findByCompetitionIdOrderByRoundNumberAscIdAsc(Long competitionId);
}
