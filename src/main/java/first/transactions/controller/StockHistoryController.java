package first.transactions.controller;

import first.transactions.model.StockHistory;
import first.transactions.repository.StockHistoryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stock-history")
public class StockHistoryController {

    private final StockHistoryRepository stockHistoryRepository;

    public StockHistoryController(StockHistoryRepository stockHistoryRepository) {
        this.stockHistoryRepository = stockHistoryRepository;
    }

    // Get all history for a ticker
    @GetMapping("/{ticker}")
    public List<StockHistory> getHistoryByTicker(@PathVariable String ticker) {
        return stockHistoryRepository.findByTickerSymbol(ticker);
    }

    // Get history for a ticker between two dates
    @GetMapping("/{ticker}/between")
    public List<StockHistory> getHistoryBetweenDates(
            @PathVariable String ticker,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return stockHistoryRepository.findByTickerSymbolAndPriceDateBetween(ticker, start, end);
    }
}
