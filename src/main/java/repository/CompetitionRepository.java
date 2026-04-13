package repository;

import org.springframework.data.jpa.repository.JpaRepository;

import model.Competition;

public interface CompetitionRepository extends JpaRepository<Competition, Long> {}
