package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import model.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
	@Query(value = "SELECT AVG(win_rate) FROM card_stats WHERE card_name = :name", nativeQuery = true)
    Double getWinRateByName(@Param("name") String name);
}
