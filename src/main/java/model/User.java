package model;

import jakarta.persistence.*;

@Entity
@Table(name = "\"User\"") // Dùng dấu ngoặc kép vì User là từ khóa nhạy cảm trong SQL
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}