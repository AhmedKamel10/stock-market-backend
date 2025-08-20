package first.transactions.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tickerSymbol; // e.g. "AAPL"

    private String name;

    private Double lastStockPrice; // cached value

    private Long totalShares;
    private Long availableShares;

    // Getters + Setters
    public Long getId() { return id; }
    public String getTickerSymbol() { return tickerSymbol; }
    public void setTickerSymbol(String tickerSymbol) { this.tickerSymbol = tickerSymbol; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLastStockPrice() { return lastStockPrice; }
    public void setLastStockPrice(Double lastStockPrice) { this.lastStockPrice = lastStockPrice; }
    public Long getTotalShares() { return totalShares; }
    public void setTotalShares(Long totalShares) { this.totalShares = totalShares; }
    public Long getAvailableShares() { return availableShares; }
    public void setAvailableShares(Long availableShares) { this.availableShares = availableShares; }
}
