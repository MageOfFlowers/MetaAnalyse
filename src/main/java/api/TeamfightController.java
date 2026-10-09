package api;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import dto.TeamfightMatchRequest;
import dto.TeamfightTeamRequest;
import model.*;
import service.TeamfightService;

@RestController
@RequestMapping("/api/teamfight")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST})
public class TeamfightController {
    private final TeamfightService teamfightService;

    public TeamfightController(TeamfightService teamfightService) {
        this.teamfightService = teamfightService;
    }

    @GetMapping("/teams")
    public List<TeamfightTeam> getTeams(@RequestParam Long competitionId) {
        return teamfightService.getTeams(competitionId);
    }

    @GetMapping("/members")
    public List<TeamfightMember> getMembers(@RequestParam Long competitionId) {
        return teamfightService.getMembers(competitionId);
    }

    @GetMapping("/matches")
    public List<TeamfightMatch> getMatches(@RequestParam Long competitionId) {
        return teamfightService.getMatches(competitionId);
    }

    @GetMapping("/duels")
    public List<TeamfightDuel> getDuels(@RequestParam Long competitionId) {
        return teamfightService.getDuels(competitionId);
    }

    @GetMapping("/analysis")
    public Map<String, List<?>> getAnalysisSnapshot() {
        return teamfightService.getAnalysisSnapshot();
    }

    @PostMapping("/teams")
    public TeamfightTeam createTeam(@RequestBody TeamfightTeamRequest request) {
        return teamfightService.createTeam(request);
    }

    @PostMapping("/matches")
    public TeamfightMatch saveMatch(@RequestBody TeamfightMatchRequest request) {
        return teamfightService.saveMatch(request);
    }

    @PostMapping("/match-delete")
    public Map<String, String> deleteMatch(@RequestBody Long id) {
        teamfightService.deleteMatch(id);
        return Map.of("status", "deleted");
    }
}
