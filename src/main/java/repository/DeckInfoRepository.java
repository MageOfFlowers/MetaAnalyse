package repository;

import org.springframework.data.jpa.repository.JpaRepository;

import model.DeckInfo;

public interface DeckInfoRepository extends JpaRepository<DeckInfo, Long> {}
