package first.transactions.controller;

import first.transactions.model.Investment;
import first.transactions.service.InvestmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import jakarta.validation.constraints.*;
import java.util.List;
@RestController
@RequestMapping("/investments") // base path
@CrossOrigin("*")
@PreAuthorize("hasRole('INVESTOR') or hasRole('SUPER_ADMIN')")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    @PostMapping("/invest/buy")
    public ResponseEntity<?> invest(
            Authentication authentication,
            @RequestParam @NotBlank(message = "Ticker symbol is required")
            @Pattern(regexp = "^[A-Z]{1,5}$", message = "Ticker must be 1-5 uppercase letters")
            String ticker,
            @RequestParam @NotNull(message = "Investment amount is required")
            @Positive(message = "Investment amount must be positive")
            @DecimalMin(value = "1.0", message = "Minimum investment is $1.00")
            @DecimalMax(value = "1000000.0", message = "Maximum investment is $1,000,000")
            Double amountUsd) {

        // Delegate to service layer - clean separation of concerns
        InvestmentService.InvestmentResult result = investmentService.buyStock(ticker, amountUsd, authentication.getName());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
    }
    @PostMapping("/invest/sell")
    public ResponseEntity<?> sell(
            Authentication authentication,
            @RequestParam @NotBlank(message = "Ticker symbol is required")
            @Pattern(regexp = "^[A-Z]{1,5}$", message = "Ticker must be 1-5 uppercase letters")
            String ticker,
            @RequestParam @NotNull(message = "Shares to sell is required")
            @Positive(message = "Shares to sell must be positive")
            @DecimalMin(value = "0.001", message = "Minimum shares to sell is 0.001")
            @DecimalMax(value = "1000000.0", message = "Maximum shares to sell is 1,000,000")
            Double sharesToSell) {

        // Delegate to service layer - clean separation of concerns
        InvestmentService.InvestmentResult result = investmentService.sellStock(ticker, sharesToSell, authentication.getName());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
    }


    @GetMapping("/portfolio")
    public ResponseEntity<List<Investment>> getPortfolio(Authentication authentication) {
        // Delegate to service layer
        List<Investment> investments = investmentService.getUserPortfolio(authentication.getName());
        return ResponseEntity.ok(investments);
    }
}
