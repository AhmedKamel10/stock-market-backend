package first.transactions.controller;

import first.transactions.model.Portfolio;
import first.transactions.model.User;
import first.transactions.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/portfolio")
@CrossOrigin("*")
public class PortfolioController {
    
    private final PortfolioService portfolioService;
    
    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }
    
    @GetMapping("/my-portfolio")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<Portfolio> getMyPortfolio(Authentication authentication) {
        String username = authentication.getName();
        Portfolio portfolio = portfolioService.getPortfolioByUsername(username);
        return ResponseEntity.ok(portfolio);
    }

    /**
     * Get portfolio summary for charts (simplified JSON)
     * Returns only the key metrics needed for frontend charts
     */
    @GetMapping("/my-portfolio/summary")
    @PreAuthorize("hasRole('INVESTOR') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<PortfolioSummaryDto> getMyPortfolioSummary(Authentication authentication) {
        String username = authentication.getName();
        Portfolio portfolio = portfolioService.getPortfolioByUsername(username);
        
        // Create simplified DTO for frontend charts
        PortfolioSummaryDto summary = new PortfolioSummaryDto();
        summary.setTotalValue(portfolio.getTotalValue());
        summary.setCashBalance(portfolio.getCashBalance());
        summary.setInvestmentsValue(portfolio.getInvestmentsValue());
        summary.setProfit(portfolio.getProfit());
        summary.setProfitPercentage(portfolio.getProfitPercentage());
        summary.setTotalHoldings(portfolio.getTotalHoldings());
        
        return ResponseEntity.ok(summary);
    }
    
    /**
     * Get any user's portfolio by ID (admin only)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Portfolio> getPortfolioByUserId(@PathVariable Long userId) {
        Portfolio portfolio = portfolioService.getPortfolioByUserId(userId);
        return ResponseEntity.ok(portfolio);
    }
    
    /**
     * Get all portfolios (admin only) 
     * Useful for admin dashboard showing all users' portfolio performance
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Portfolio>> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioService.getAllPortfolios();
        return ResponseEntity.ok(portfolios);
    }

    /**
     * Simple DTO for portfolio summary - perfect for frontend charts
     */
    public static class PortfolioSummaryDto {
        private Double totalValue;
        private Double cashBalance;
        private Double investmentsValue;
        private Double profit;                // Positive = gain, Negative = loss
        private Double profitPercentage;      // Percentage profit/loss
        private Integer totalHoldings;
        
        // Getters and setters
        public Double getTotalValue() { return totalValue; }
        public void setTotalValue(Double totalValue) { this.totalValue = totalValue; }
        
        public Double getCashBalance() { return cashBalance; }
        public void setCashBalance(Double cashBalance) { this.cashBalance = cashBalance; }
        
        public Double getInvestmentsValue() { return investmentsValue; }
        public void setInvestmentsValue(Double investmentsValue) { this.investmentsValue = investmentsValue; }
        
        public Double getProfit() { return profit; }
        public void setProfit(Double profit) { this.profit = profit; }
        
        public Double getProfitPercentage() { return profitPercentage; }
        public void setProfitPercentage(Double profitPercentage) { 
            this.profitPercentage = profitPercentage; 
        }
        
        public Integer getTotalHoldings() { return totalHoldings; }
        public void setTotalHoldings(Integer totalHoldings) { this.totalHoldings = totalHoldings; }
    }
}
