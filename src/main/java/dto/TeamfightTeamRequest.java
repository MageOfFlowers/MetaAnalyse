package dto;

import java.util.List;

public class TeamfightTeamRequest {
    public Long competitionId;
    public String name;
    public List<Member> members;

    public static class Member {
        public Long userId;
        public Long deckId;
    }
}
