package model;

import jakarta.persistence.*;

@Entity
@Table(name = "DeckInfo")
public class DeckInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long deckid;
    private Long cardid;
    private Integer quantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDeckid() { return deckid; }
    public void setDeckid(Long deckid) { this.deckid = deckid; }
    public Long getCardid() { return cardid; }
    public void setCardid(Long cardid) { this.cardid = cardid; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}