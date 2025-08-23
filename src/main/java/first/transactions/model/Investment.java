package first.transactions.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "investments")
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String tickerSymbol;
    private Double amountUsd;  // <-- NEW field
    private Double SharesPurchased;
    private LocalDateTime purchasedAt = LocalDateTime.now();
    private Double profit;

    // ---- Getters and Setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public Double getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(Double amountUsd) {
        this.amountUsd = amountUsd;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }
    public Double getSharesPurchased() {
        return SharesPurchased;
    }
    public void setSharesPurchased(Double SharesPurchased) {
        this.SharesPurchased = SharesPurchased;
    }
    public Double getProfit() {
        return profit;
    }
    public void setProfit(Double profit) {
        this.profit = profit;
    }
}
