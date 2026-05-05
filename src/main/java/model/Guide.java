package model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.util.Map;

@Entity
@Table(name = "guide")

public class Guide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type")
    private String type; // Ví dụ: 'counter' hoặc 'build'

    @Column(name = "name")
    private String name; // Tên bài viết/Meta Deck


    @Type(JsonBinaryType.class)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, String> details;
    
    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    // --- SETTERS ---

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}