package model;

import jakarta.persistence.*;

@Entity
@Table(name = "CompetitionUse")
public class CompetitionUse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userid;
    private Long deckid;
    private Integer rank;
    private Float winrate;
    private Long competitionid;

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserid() { return userid; }
    public void setUserid(Long userid) { this.userid = userid; }
    public Long getDeckid() { return deckid; }
    public void setDeckid(Long deckid) { this.deckid = deckid; }
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    public Float getWinrate() { return winrate; }
    public void setWinrate(Float winrate) { this.winrate = winrate; }
    public Long getCompetitionid() { return competitionid; }
    public void setCompetitionid(Long competitionid) { this.competitionid = competitionid; }
}