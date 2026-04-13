package dto;

public class CompetitionRegistrationDTO {
    private Long deckid;
    private Long competitionid;
    private Long userid;
    private Float winrate;
    private Integer rank;
    // Getters và Setters
	public Long getDeckid() {
		return deckid;
	}
	public void setDeckid(Long deckid) {
		this.deckid = deckid;
	}
	public Long getCompetitionid() {
		return competitionid;
	}
	public void setCompetitionid(Long competitionid) {
		this.competitionid = competitionid;
	}
	public Long getUserid() {
		return userid;
	}
	public void setUserid(Long userid) {
		this.userid = userid;
	}
	public Float getWinrate() {
		return winrate;
	}
	public void setWinrate(Float winrate) {
		this.winrate = winrate;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
}