package first.transactions.repository;

import first.transactions.model.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

    // Find all stock history entries for a specific ticker
    List<StockHistory> findByTickerSymbol(String tickerSymbol);

    // ✅ correct JPA derived query using priceDate
    List<StockHistory> findByTickerSymbolAndPriceDateBetween(
            String tickerSymbol,
            LocalDateTime start,
            LocalDateTime end
    );

    // Find the most recent history entry for a ticker
    StockHistory findTopByTickerSymbolOrderByPriceDateDesc(String tickerSymbol);
}
