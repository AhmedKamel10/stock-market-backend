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
     * @param ticker Stock ticker symbol (already validated by controller)
     * @return List of stock history entries
     */
    public StockHistoryResult getHistoryByTicker(String ticker) {
        // Validate ticker format (business rule)
        if (!isValidTicker(ticker)) {
            return StockHistoryResult.error("Invalid ticker format: " + ticker);
        }

        try {
            List<StockHistory> history = stockHistoryRepository.findByTickerSymbol(ticker.toUpperCase());
            
            if (history.isEmpty()) {
                return StockHistoryResult.error("No price history found for ticker: " + ticker);
            }
            
            return StockHistoryResult.success(history);
            
        } catch (Exception e) {
            return StockHistoryResult.error("Error retrieving stock history: " + e.getMessage());
        }
    }

    /**
     * Get stock history for a ticker between two dates
     * @param ticker Stock ticker symbol
     * @param start Start date
     * @param end End date
     * @return Result with stock history or error message
     */
    public StockHistoryResult getHistoryBetweenDates(String ticker, LocalDateTime start, LocalDateTime end) {
        // Validate ticker
        if (!isValidTicker(ticker)) {
            return StockHistoryResult.error("Invalid ticker format: " + ticker);
        }

        // Validate date range
        DateRangeValidation validation = validateDateRange(start, end);
        if (!validation.isValid()) {
            return StockHistoryResult.error(validation.getErrorMessage());
        }

        try {
            List<StockHistory> history = stockHistoryRepository
                    .findByTickerSymbolAndPriceDateBetween(ticker.toUpperCase(), start, end);
            
            if (history.isEmpty()) {
                return StockHistoryResult.error(String.format(
                    "No price history found for ticker %s between %s and %s", 
                    ticker, start.toLocalDate(), end.toLocalDate()));
            }
            
            return StockHistoryResult.success(history);
            
        } catch (Exception e) {
            return StockHistoryResult.error("Error retrieving stock history: " + e.getMessage());
        }
    }


    /**
     * Validate ticker format
     */
    private boolean isValidTicker(String ticker) {
        return ticker != null && 
               !ticker.trim().isEmpty() && 
               ticker.matches("^[A-Z]{1,5}$");
    }

    /**
     * Validate date range for business rules
     */
    private DateRangeValidation validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return DateRangeValidation.invalid("Start date cannot be null");
        }
        
        if (end == null) {
            return DateRangeValidation.invalid("End date cannot be null");
        }
        
        if (start.isAfter(end)) {
            return DateRangeValidation.invalid("Start date must be before end date");
        }
        
        // Business rule: prevent querying future dates
        LocalDateTime now = LocalDateTime.now();
        if (start.isAfter(now)) {
            return DateRangeValidation.invalid("Start date cannot be in the future");
        }
        
        if (end.isAfter(now)) {
            return DateRangeValidation.invalid("End date cannot be in the future");
        }

        
        return DateRangeValidation.valid();
    }

    /**
     * Result wrapper for stock history operations
     */
    public static class StockHistoryResult {
        private final boolean success;
        private final List<StockHistory> data;
        private final String errorMessage;

        private StockHistoryResult(boolean success, List<StockHistory> data, String errorMessage) {
            this.success = success;
            this.data = data;
            this.errorMessage = errorMessage;
        }

        public static StockHistoryResult success(List<StockHistory> data) {
            return new StockHistoryResult(true, data, null);
        }

        public static StockHistoryResult error(String errorMessage) {
            return new StockHistoryResult(false, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public List<StockHistory> getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
    }


    /**
     * Date range validation result
     */
    private static class DateRangeValidation {
        private final boolean valid;
        private final String errorMessage;

        private DateRangeValidation(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static DateRangeValidation valid() {
            return new DateRangeValidation(true, null);
        }

        public static DateRangeValidation invalid(String errorMessage) {
            return new DateRangeValidation(false, errorMessage);
        }

        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
    }
}
