package dto;

import java.util.List;

public class TeamfightMatchRequest {
    public Long id;
    public Long competitionId;
    public Integer roundNumber;
    public Long team1Id;
    public Long team2Id;
    public Long tieBreakWinnerTeamId;
    public Boolean thirdPlace;
    public List<Duel> duels;

    public static class Duel {
        public Long player1Id;
        public Long deck1Id;
        public Long player2Id;
        public Long deck2Id;
        public String winnerSide;
    }
}
