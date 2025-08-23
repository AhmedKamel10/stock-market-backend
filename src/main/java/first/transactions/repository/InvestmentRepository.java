package first.transactions.repository;

import first.transactions.model.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
    List<Investment> findByUserId(Long userId);
    Optional<Investment> findByUserIdAndTickerSymbol(Long userId, String tickerSymbol);

}
