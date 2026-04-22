package dto;

import java.util.List;

public class DeckRequestDTO {
	private long Id = 0;
    private String name;
    private List<CardItem> cards;

    public static class CardItem {
        public Long cardid;
        public Integer quantity;
		public Long getCardid() {
			// TODO Auto-generated method stub
			return cardid;
		}
		public Integer getQuantity() {
			// TODO Auto-generated method stub
			return quantity;
		}
    }
    // Getters và Setters
    
    public long getId() {
    	return Id;
    }

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