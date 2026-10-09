package repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import model.TeamfightMember;

public interface TeamfightMemberRepository extends JpaRepository<TeamfightMember, Long> {
    List<TeamfightMember> findByTeamIdIn(List<Long> teamIds);
}
