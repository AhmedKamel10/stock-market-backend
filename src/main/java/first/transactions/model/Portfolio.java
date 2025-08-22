package first.transactions.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "portfolios")
public class Portfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // One-to-One relationship - each user has exactly one portfolio
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;
    
    @Column(nullable = false)
    private Double totalValue = 0.0; // cash + investments total
    
    @Column(nullable = false) 
    private Double cashBalance = 0.0;          // copied from user.balance
    
    @Column(nullable = false)
    private Double investmentsValue = 0.0;     // current value of all holdings
    
    @Column(nullable = false)
    private Double totalInvested = 0.0;        // original amount invested
    
    @Column(nullable = false)
    private Double profit = 0.0;               // investmentsValue - totalInvested (positive = gain, negative = loss)
    
    @Column(nullable = false)
    private Double profitPercentage = 0.0;         // (profit / totalInvested) * 100
    
    // Metadata
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "total_holdings")
    private Integer totalHoldings = 0;         // number of different stocks owned
    
    // Constructors
    public Portfolio() {
        this.lastUpdated = LocalDateTime.now();
    }
    
    public Portfolio(User user) {
        this.user = user;
        this.cashBalance = user.getBalance();
        this.lastUpdated = LocalDateTime.now();
        this.calculateTotals();
    }
    
    // Business Logic - Recalculate portfolio values
    public void calculateTotals() {
        this.totalValue = this.cashBalance + this.investmentsValue;
        
        if (this.totalInvested > 0) {
            this.profitPercentage = (this.profit / this.totalInvested) * 100;
        } else {
            this.profitPercentage = 0.0;
        }
        
        this.lastUpdated = LocalDateTime.now();
    }
    
    // Update method - called when investments change
    public void updateInvestmentData(Double newInvestmentsValue, Double newTotalInvested, Integer holdingsCount) {
        this.investmentsValue = newInvestmentsValue;
        this.totalInvested = newTotalInvested;
        this.profit = this.investmentsValue - this.totalInvested;  // Positive = gain, Negative = loss
        this.totalHoldings = holdingsCount;
        this.calculateTotals();
    }
    
    // Update cash balance (when user adds money or makes transfers)
    public void updateCashBalance(Double newCashBalance) {
        this.cashBalance = newCashBalance;
        this.calculateTotals();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Double getTotalValue() {
        return totalValue;
    }
    
    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }
    
    public Double getCashBalance() {
        return cashBalance;
    }
    
    public void setCashBalance(Double cashBalance) {
        this.cashBalance = cashBalance;
    }
    
    public Double getInvestmentsValue() {
        return investmentsValue;
    }
    
    public void setInvestmentsValue(Double investmentsValue) {
        this.investmentsValue = investmentsValue;
    }
    
    public Double getTotalInvested() {
        return totalInvested;
    }
    
    public void setTotalInvested(Double totalInvested) {
        this.totalInvested = totalInvested;
    }
    
    public Double getProfit() {
        return profit;
    }
    
    public void setProfit(Double profit) {
        this.profit = profit;
    }
    
    public Double getProfitPercentage() {
        return profitPercentage;
    }
    
    public void setProfitPercentage(Double profitPercentage) {
        this.profitPercentage = profitPercentage;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Integer getTotalHoldings() {
        return totalHoldings;
    }
    
    public void setTotalHoldings(Integer totalHoldings) {
        this.totalHoldings = totalHoldings;
    }
}
