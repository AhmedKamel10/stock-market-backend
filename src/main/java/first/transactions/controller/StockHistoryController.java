package first.transactions.controller;

import first.transactions.model.StockHistory;
import first.transactions.service.StockHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/stock-history")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    public StockHistoryController(StockHistoryService stockHistoryService) {
        this.stockHistoryService = stockHistoryService;
    }

    // Get all history for a ticker
    @GetMapping("/{ticker}")
    public List<StockHistory> getHistoryByTicker(@PathVariable String ticker) {
        return stockHistoryService.getHistoryByTicker(ticker);
    }

    // Get history for a ticker between two dates
    @GetMapping("/{ticker}/between")
    public List<StockHistory> getHistoryBetweenDates(
            @PathVariable String ticker,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        return stockHistoryService.getHistoryBetweenDates(ticker, start, end);
    }
}
