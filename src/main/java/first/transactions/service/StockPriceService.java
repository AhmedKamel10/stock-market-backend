package first.transactions.service;

import first.transactions.model.Company;
import org.springframework.stereotype.Service;

@Service
public class StockPriceService {

    /**
     * Update stock price based on a trade (buy or sell) using invested money
     * @param company Company whose stock price is updated
     * @param money Positive for investment (buy), negative for sell
     */
    public void updateStockPrice(Company company, double money) {
        double currentPrice = company.getLastStockPrice() != null ? company.getLastStockPrice() : 100.0;
        long totalShares = company.getTotalShares() != null ? company.getTotalShares() : 1000L;

        // Convert total company value to money
        double companyMarketValue = totalShares * currentPrice;

        // Sensitivity factor: larger k → bigger price impact
        double k = 0.05; // max 5% impact per large investment

        // Price change proportional to money relative to company market value
        double priceChange = currentPrice * k * (money / companyMarketValue);

        // Calculate new price
        double newPrice = currentPrice + priceChange;

        // Clamp minimum price
        newPrice = Math.max(newPrice, 0.01);

        company.setLastStockPrice(newPrice);
    }
}
