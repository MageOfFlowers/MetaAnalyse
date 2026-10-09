package repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import model.TeamfightDuel;

public interface TeamfightDuelRepository extends JpaRepository<TeamfightDuel, Long> {
    List<TeamfightDuel> findByMatchIdIn(List<Long> matchIds);
    void deleteByMatchId(Long matchId);
}
