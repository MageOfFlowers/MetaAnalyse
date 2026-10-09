package model;

import jakarta.persistence.*;

@Entity
@Table(name = "teamfight_match")
public class TeamfightMatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competition_id", nullable = false)
    private Long competitionId;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "team1_id", nullable = false)
    private Long team1Id;

    @Column(name = "team2_id", nullable = false)
    private Long team2Id;

    @Column(name = "winner_team_id")
    private Long winnerTeamId;

    @Column(name = "is_third_place", nullable = false)
    private boolean thirdPlace;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompetitionId() { return competitionId; }
    public void setCompetitionId(Long competitionId) { this.competitionId = competitionId; }
    public Integer getRoundNumber() { return roundNumber; }
    public void setRoundNumber(Integer roundNumber) { this.roundNumber = roundNumber; }
    public Long getTeam1Id() { return team1Id; }
    public void setTeam1Id(Long team1Id) { this.team1Id = team1Id; }
    public Long getTeam2Id() { return team2Id; }
    public void setTeam2Id(Long team2Id) { this.team2Id = team2Id; }
    public Long getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Long winnerTeamId) { this.winnerTeamId = winnerTeamId; }
    public boolean isThirdPlace() { return thirdPlace; }
    public void setThirdPlace(boolean thirdPlace) { this.thirdPlace = thirdPlace; }
}
