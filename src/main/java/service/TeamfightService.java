package service;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import dto.TeamfightMatchRequest;
import dto.TeamfightTeamRequest;
import model.*;
import repository.*;

@Service
public class TeamfightService {
    private final CompetitionRepository competitionRepo;
    private final TeamfightTeamRepository teamRepo;
    private final TeamfightMemberRepository memberRepo;
    private final TeamfightMatchRepository matchRepo;
    private final TeamfightDuelRepository duelRepo;
    private final UserRepository userRepo;
    private final DeckRepository deckRepo;

    public TeamfightService(CompetitionRepository competitionRepo, TeamfightTeamRepository teamRepo,
            TeamfightMemberRepository memberRepo, TeamfightMatchRepository matchRepo,
            TeamfightDuelRepository duelRepo, UserRepository userRepo, DeckRepository deckRepo) {
        this.competitionRepo = competitionRepo;
        this.teamRepo = teamRepo;
        this.memberRepo = memberRepo;
        this.matchRepo = matchRepo;
        this.duelRepo = duelRepo;
        this.userRepo = userRepo;
        this.deckRepo = deckRepo;
    }

    @Transactional
    public TeamfightTeam createTeam(TeamfightTeamRequest request) {
        if (request.competitionId == null || request.name == null || request.name.isBlank()) {
            throw badRequest("Select a tournament and enter a team name.");
        }
        Competition competition = competitionRepo.findById(request.competitionId)
                .orElseThrow(() -> badRequest("Tournament not found."));
        if (!"TEAMFIGHT".equalsIgnoreCase(competition.getTournamentType())) {
            throw badRequest("Teams can only be added to teamfight tournaments.");
        }
        if (teamRepo.existsByCompetitionIdAndNameIgnoreCase(request.competitionId, request.name.trim())) {
            throw badRequest("A team with this name already exists in the tournament.");
        }
        if (request.members == null || request.members.size() < 3 || request.members.size() > 4) {
            throw badRequest("Each team must have 3 or 4 members.");
        }
        Set<Long> users = new HashSet<>();
        Set<Long> decks = new HashSet<>();
        List<Long> existingTeamIds = teamRepo.findByCompetitionId(request.competitionId).stream()
                .map(TeamfightTeam::getId).collect(Collectors.toList());
        List<TeamfightMember> existingMembers = existingTeamIds.isEmpty()
                ? List.of() : memberRepo.findByTeamIdIn(existingTeamIds);
        for (TeamfightTeamRequest.Member member : request.members) {
            if (member.userId == null || member.deckId == null || !users.add(member.userId) || !decks.add(member.deckId)) {
                throw badRequest("Each member must have one distinct player and deck.");
            }
            if (!userRepo.existsById(member.userId) || !deckRepo.existsById(member.deckId)) {
                throw badRequest("Every selected player and deck must exist.");
            }
            if (existingMembers.stream().anyMatch(existing -> existing.getUserId().equals(member.userId)
                    || existing.getDeckId().equals(member.deckId))) {
                throw badRequest("A player and deck can only belong to one team in this tournament.");
            }
        }

        TeamfightTeam team = new TeamfightTeam();
        team.setCompetitionId(request.competitionId);
        team.setName(request.name.trim());
        team = teamRepo.save(team);
        for (TeamfightTeamRequest.Member member : request.members) {
            TeamfightMember rosterMember = new TeamfightMember();
            rosterMember.setTeamId(team.getId());
            rosterMember.setUserId(member.userId);
            rosterMember.setDeckId(member.deckId);
            memberRepo.save(rosterMember);
        }
        return team;
    }

    @Transactional
    public TeamfightMatch saveMatch(TeamfightMatchRequest request) {
        if (request.competitionId == null || request.roundNumber == null || request.roundNumber < 1
                || request.team1Id == null || request.team2Id == null || request.team1Id.equals(request.team2Id)
                || request.duels == null || request.duels.isEmpty()) {
            throw badRequest("Provide a round, two different teams, and at least one duel.");
        }
        Competition competition = competitionRepo.findById(request.competitionId)
                .orElseThrow(() -> badRequest("Tournament not found."));
        if (!"TEAMFIGHT".equalsIgnoreCase(competition.getTournamentType())) {
            throw badRequest("Matches can only be added to teamfight tournaments.");
        }
        TeamfightTeam team1 = getTeam(request.team1Id, request.competitionId);
        TeamfightTeam team2 = getTeam(request.team2Id, request.competitionId);
        if (request.tieBreakWinnerTeamId != null
                && !Set.of(team1.getId(), team2.getId()).contains(request.tieBreakWinnerTeamId)) {
            throw badRequest("The tie-break winner must be one of the teams in this match.");
        }

        List<TeamfightMember> roster = memberRepo.findByTeamIdIn(List.of(team1.getId(), team2.getId()));
        Map<Long, TeamfightMember> memberByUser = new HashMap<>();
        Map<Long, Set<Long>> decksByTeam = new HashMap<>();
        for (TeamfightMember member : roster) {
            if (memberByUser.putIfAbsent(member.getUserId(), member) != null) {
                throw badRequest("A player cannot belong to both teams in a match.");
            }
            decksByTeam.computeIfAbsent(member.getTeamId(), ignored -> new HashSet<>()).add(member.getDeckId());
        }
        Set<Long> playersUsed = new HashSet<>();
        for (TeamfightMatchRequest.Duel duel : request.duels) {
            TeamfightMember p1 = memberByUser.get(duel.player1Id);
            TeamfightMember p2 = memberByUser.get(duel.player2Id);
            if (p1 == null || p2 == null || !playersUsed.add(duel.player1Id) || !playersUsed.add(duel.player2Id)
                    || !team1.getId().equals(p1.getTeamId()) || !team2.getId().equals(p2.getTeamId())
                    || !decksByTeam.get(team1.getId()).contains(duel.deck1Id)
                    || !decksByTeam.get(team2.getId()).contains(duel.deck2Id)) {
                throw badRequest("Each duel must use distinct roster players and a deck owned by that player's team.");
            }
            if (duel.winnerSide == null || !Set.of("TEAM1", "TEAM2", "DRAW").contains(duel.winnerSide)) {
                throw badRequest("Duel result must be TEAM1, TEAM2, or DRAW.");
            }
        }

        TeamfightMatch match = request.id == null ? new TeamfightMatch()
                : matchRepo.findById(request.id).orElseThrow(() -> badRequest("Match not found."));
        if (match.getId() != null && !Objects.equals(match.getCompetitionId(), request.competitionId)) {
            throw badRequest("Match does not belong to this tournament.");
        }
        match.setCompetitionId(request.competitionId);
        match.setRoundNumber(request.roundNumber);
        match.setTeam1Id(team1.getId());
        match.setTeam2Id(team2.getId());

        int team1Wins = (int) request.duels.stream().filter(d -> "TEAM1".equals(d.winnerSide)).count();
        int team2Wins = (int) request.duels.stream().filter(d -> "TEAM2".equals(d.winnerSide)).count();
        Long winnerTeamId = team1Wins == team2Wins
                ? request.tieBreakWinnerTeamId
                : team1Wins > team2Wins ? team1.getId() : team2.getId();
        if (winnerTeamId == null && "SINGLE_ELIMINATION".equals(competition.getTournamentFormat())) {
            throw badRequest("A tied single-elimination match requires a tie-break winner.");
        }
        match.setWinnerTeamId(winnerTeamId);
        match = matchRepo.save(match);
        duelRepo.deleteByMatchId(match.getId());
        for (TeamfightMatchRequest.Duel duel : request.duels) {
            TeamfightDuel row = new TeamfightDuel();
            row.setMatchId(match.getId());
            row.setPlayer1Id(duel.player1Id);
            row.setDeck1Id(duel.deck1Id);
            row.setPlayer2Id(duel.player2Id);
            row.setDeck2Id(duel.deck2Id);
            row.setWinnerSide(duel.winnerSide);
            duelRepo.save(row);
        }
        return match;
    }

    @Transactional
    public void deleteMatch(Long id) {
        if (!matchRepo.existsById(id)) throw badRequest("Match not found.");
        duelRepo.deleteByMatchId(id);
        matchRepo.deleteById(id);
    }

    public List<TeamfightTeam> getTeams(Long competitionId) {
        return teamRepo.findByCompetitionId(competitionId);
    }

    public List<TeamfightMember> getMembers(Long competitionId) {
        List<Long> ids = getTeams(competitionId).stream().map(TeamfightTeam::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of() : memberRepo.findByTeamIdIn(ids);
    }

    public List<TeamfightMatch> getMatches(Long competitionId) {
        return matchRepo.findByCompetitionIdOrderByRoundNumberAscIdAsc(competitionId);
    }

    public List<TeamfightDuel> getDuels(Long competitionId) {
        List<Long> ids = getMatches(competitionId).stream().map(TeamfightMatch::getId).collect(Collectors.toList());
        return ids.isEmpty() ? List.of() : duelRepo.findByMatchIdIn(ids);
    }

    public Map<String, List<?>> getAnalysisSnapshot() {
        Map<String, List<?>> snapshot = new HashMap<>();
        snapshot.put("teams", teamRepo.findAll());
        snapshot.put("members", memberRepo.findAll());
        snapshot.put("matches", matchRepo.findAll());
        snapshot.put("duels", duelRepo.findAll());
        return snapshot;
    }

    private TeamfightTeam getTeam(Long id, Long competitionId) {
        TeamfightTeam team = teamRepo.findById(id).orElseThrow(() -> badRequest("Team not found."));
        if (!competitionId.equals(team.getCompetitionId())) throw badRequest("Team belongs to another tournament.");
        return team;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
