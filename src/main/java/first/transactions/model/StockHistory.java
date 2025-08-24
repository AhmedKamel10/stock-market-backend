package first.transactions.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stockhistory")
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "price_date") // optional, maps DB column
    private LocalDateTime priceDate;

    private double stockPrice;
    private String tickerSymbol;

    // Getters & Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getPriceDate() {
        return priceDate;
    }
    public void setPriceDate(LocalDateTime priceDate) {
        this.priceDate = priceDate;
    }

    public double getStockPrice() {
        return stockPrice;
    }
    public void setStockPrice(double stockPrice) {
        this.stockPrice = stockPrice;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }
    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }
}
