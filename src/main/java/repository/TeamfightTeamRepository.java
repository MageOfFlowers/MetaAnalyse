package repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import model.TeamfightTeam;

public interface TeamfightTeamRepository extends JpaRepository<TeamfightTeam, Long> {
    List<TeamfightTeam> findByCompetitionId(Long competitionId);
    boolean existsByCompetitionIdAndNameIgnoreCase(Long competitionId, String name);
}
