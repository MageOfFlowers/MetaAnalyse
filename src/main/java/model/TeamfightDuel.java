package model;

import jakarta.persistence.*;

@Entity
@Table(name = "teamfight_duel")
public class TeamfightDuel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_id", nullable = false)
    private Long matchId;

    @Column(name = "player1_id", nullable = false)
    private Long player1Id;

    @Column(name = "deck1_id", nullable = false)
    private Long deck1Id;

    @Column(name = "player2_id", nullable = false)
    private Long player2Id;

    @Column(name = "deck2_id", nullable = false)
    private Long deck2Id;

    @Column(name = "winner_side", nullable = false)
    private String winnerSide;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Long getPlayer1Id() { return player1Id; }
    public void setPlayer1Id(Long player1Id) { this.player1Id = player1Id; }
    public Long getDeck1Id() { return deck1Id; }
    public void setDeck1Id(Long deck1Id) { this.deck1Id = deck1Id; }
    public Long getPlayer2Id() { return player2Id; }
    public void setPlayer2Id(Long player2Id) { this.player2Id = player2Id; }
    public Long getDeck2Id() { return deck2Id; }
    public void setDeck2Id(Long deck2Id) { this.deck2Id = deck2Id; }
    public String getWinnerSide() { return winnerSide; }
    public void setWinnerSide(String winnerSide) { this.winnerSide = winnerSide; }
}
