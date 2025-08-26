package first.transactions.controller;

import first.transactions.service.StockHistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/stock-history")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    public StockHistoryController(StockHistoryService stockHistoryService) {
        this.stockHistoryService = stockHistoryService;
    }

    // Get all history for a ticker
    @GetMapping("/{ticker}")
    public ResponseEntity<?> getHistoryByTicker(
            @PathVariable @NotBlank(message = "Ticker symbol is required")
            @Pattern(regexp = "^[A-Z]{1,5}$", message = "Ticker must be 1-5 uppercase letters") 
            String ticker) {
        
        // Delegate to service layer
        StockHistoryService.StockHistoryResult result = stockHistoryService.getHistoryByTicker(ticker);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.badRequest().body(result.getErrorMessage());
        }
    }

    // Get history for a ticker between two dates
    @GetMapping("/{ticker}/between")
    public ResponseEntity<?> getHistoryBetweenDates(
            @PathVariable @NotBlank(message = "Ticker symbol is required")
            @Pattern(regexp = "^[A-Z]{1,5}$", message = "Ticker must be 1-5 uppercase letters") 
            String ticker,
            @RequestParam("start") @NotNull(message = "Start date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @NotNull(message = "End date is required")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        // Delegate to service layer - handles all validation and business rules
        StockHistoryService.StockHistoryResult result = stockHistoryService.getHistoryBetweenDates(ticker, start, end);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getData());
        } else {
            return ResponseEntity.badRequest().body(result.getErrorMessage());
        }
    }
}
