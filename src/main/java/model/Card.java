package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Card") // Chú ý: viết hoa đúng như tên bảng trong DB của bạn
public class Card {

    @Id
    private Long id; // int8 trong DB

    private String name;
    
    private String color;
    
    private String rarity;
    
    private String url;

    // --- Constructors ---
    public Card() {
    }

    public Card(Long id, String name, String color, String rarity, String url) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.rarity = rarity;
        this.url = url;
    }

    // --- Getters và Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRarity() {
        return rarity;
    }

    public void setRarity(String rarity) {
        this.rarity = rarity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}