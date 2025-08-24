package first.transactions.service;

import first.transactions.model.Company;
import first.transactions.model.Investment;
import first.transactions.repository.CompanyRepository;
import first.transactions.repository.InvestmentRepository;
import first.transactions.repository.UserRepository;
import org.springframework.stereotype.Service;
import first.transactions.repository.StockHistoryRepository;
import first.transactions.model.StockHistory;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockPriceService {

    private final PortfolioService portfolioService;
    private final InvestmentRepository investmentRepository;
    private final StockHistoryRepository stockHistoryRepository;
    public StockPriceService(PortfolioService portfolioService, InvestmentRepository investmentRepository, StockHistoryRepository stockHistoryRepository, CompanyRepository companyRepository, UserRepository userRepository) {
        this.portfolioService = portfolioService;
        this.investmentRepository = investmentRepository;
        this.stockHistoryRepository = stockHistoryRepository;
    }

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
        double k = 5;

        // Price change proportional to money relative to company market value
        double priceChange = currentPrice * k * (money / companyMarketValue);

        // Calculate new price
        double newPrice = currentPrice + priceChange;

        // Clamp minimum price
        newPrice = Math.max(newPrice, 0.01);

        company.setLastStockPrice(newPrice);

        // add the new price to the history (3ashan el chart)
        StockHistory stockHistory = new StockHistory();
        stockHistory.setPriceDate(LocalDateTime.now());
        stockHistory.setStockPrice(newPrice);
        stockHistory.setTickerSymbol(company.getTickerSymbol());
        stockHistoryRepository.save(stockHistory);


        //e7seb el profit beta3 kol investment
        List<Investment> investments = investmentRepository.findBytickerSymbol(company.getTickerSymbol());
        for (Investment investment : investments) {
            double avgBuyPrice = investment.getAmountUsd() / investment.getSharesPurchased();
            double profit = (newPrice - avgBuyPrice) * investment.getSharesPurchased();
            investment.setProfit(profit);
            investmentRepository.save(investment);
        }


        //e7seb kol el profit beta3 el user
        portfolioService.recalculateAllPortfolios();

    }
}
