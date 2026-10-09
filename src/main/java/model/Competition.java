package model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

@Entity
@Table(name = "Competition")
public class Competition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // Đổi tên thành competition_date để tránh lỗi xung đột từ khóa và kiểu dữ liệu
    @Column(name = "competition_date")
    private LocalDate date; 

    private String region;

    @Column(name = "tournament_type", nullable = false)
    private String tournamentType = "SINGLE";

    @Column(name = "tournament_format", nullable = false)
    private String tournamentFormat = "SINGLE_ELIMINATION";

    // Getters và Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDate() { return date; }
    @JsonProperty("competition_date")
    public LocalDate getCompetitionDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getTournamentType() { return tournamentType; }
    public void setTournamentType(String tournamentType) { this.tournamentType = tournamentType; }
    public String getTournamentFormat() { return tournamentFormat; }
    public void setTournamentFormat(String tournamentFormat) { this.tournamentFormat = tournamentFormat; }
}