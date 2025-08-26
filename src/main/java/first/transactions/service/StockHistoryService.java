package first.transactions.service;

import first.transactions.model.StockHistory;
import first.transactions.repository.StockHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class StockHistoryService {

    private final StockHistoryRepository stockHistoryRepository;

    public StockHistoryService(StockHistoryRepository stockHistoryRepository) {
        this.stockHistoryRepository = stockHistoryRepository;
    }

    /**
     * Get all stock history for a ticker symbol
     * Simple delegation to repository - keeping same interface as before
     * @param ticker Stock ticker symbol
     * @return List of stock history entries
     */
    public List<StockHistory> getHistoryByTicker(String ticker) {
        return stockHistoryRepository.findByTickerSymbol(ticker);
    }

    /**
     * Get stock history for a ticker between two dates
     * Simple delegation to repository - keeping same interface as before
     * @param ticker Stock ticker symbol
     * @param start Start date
     * @param end End date
     * @return List of stock history entries
     */
    public List<StockHistory> getHistoryBetweenDates(String ticker, LocalDateTime start, LocalDateTime end) {
        return stockHistoryRepository.findByTickerSymbolAndPriceDateBetween(ticker, start, end);
    }
}