package dto;

import java.util.List;

public class DeckRequestDTO {
    private String name;
    private List<CardItem> cards;

    public static class CardItem {
        public Long cardid;
        public Integer quantity;
    }
    // Getters và Setters

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CardItem> getCards() {
		return cards;
	}

	public void setCards(List<CardItem> cards) {
		this.cards = cards;
	}
}