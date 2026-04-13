package repository;

import org.springframework.data.jpa.repository.JpaRepository;

import model.Deck;

public interface DeckRepository extends JpaRepository<Deck, Long> {}