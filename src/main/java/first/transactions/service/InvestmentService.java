package first.transactions.service;

import first.transactions.model.Company;
import first.transactions.model.Investment;
import first.transactions.model.User;
import first.transactions.repository.CompanyRepository;
import first.transactions.repository.InvestmentRepository;
import first.transactions.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;
    private final PortfolioService portfolioService;

    public InvestmentService(InvestmentRepository investmentRepository,
                           CompanyRepository companyRepository,
                           UserRepository userRepository,
                           StockPriceService stockPriceService,
                           PortfolioService portfolioService) {
        this.investmentRepository = investmentRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.stockPriceService = stockPriceService;
        this.portfolioService = portfolioService;
    }

    /**
     * Buy stocks for a user
     * @param ticker Stock symbol (already validated)
     * @param amountUsd Amount to invest (already validated)
     * @param username Username of investor
     * @return Success message or error
     */
    public InvestmentResult buyStock(String ticker, Double amountUsd, String username) {
        // Find the company by ticker
        Company company = companyRepository.findByTickerSymbol(ticker.toUpperCase());
        if (company == null) {
            return InvestmentResult.error("Ticker not found: " + ticker);
        }

        // Get logged-in user
        User investor = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Check if user has sufficient balance
        if (investor.getBalance() < amountUsd) {
            return InvestmentResult.error(String.format(
                "Insufficient balance. Available: $%.2f, Required: $%.2f", 
                investor.getBalance(), amountUsd));
        }

        // Check if company has available shares
        if (company.getAvailableShares() <= 0) {
            return InvestmentResult.error("No shares available for this company");
        }

        // Calculate shares to purchase
        stockPriceService.updateStockPrice(company, amountUsd);
        double latestPrice = company.getLastStockPrice();
        double sharesPurchased = amountUsd / latestPrice;
        long sharesToDeduct = (long) sharesPurchased;

        // Deduct balance from user
        investor.setBalance(investor.getBalance() - amountUsd);
        userRepository.save(investor);

        // Find existing investment for this user & ticker or create new one
        try {
            investmentRepository.findByUserIdAndTickerSymbol(investor.getId(), ticker.toUpperCase())
                    .ifPresentOrElse(investment -> {
                        // Update existing investment
                        investment.setAmountUsd(investment.getAmountUsd() + amountUsd);
                        investment.setSharesPurchased(investment.getSharesPurchased() + sharesPurchased);
                        investmentRepository.save(investment);
                    }, () -> {
                        // Create new investment
                        Investment newInvestment = new Investment();
                        newInvestment.setUserId(investor.getId());
                        newInvestment.setTickerSymbol(ticker.toUpperCase());
                        newInvestment.setAmountUsd(amountUsd);
                        newInvestment.setSharesPurchased(sharesPurchased);
                        investmentRepository.save(newInvestment);
                    });
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException ex) {
            // Handle duplicate records - consolidate them
            Investment consolidatedInvestment = consolidateDuplicateInvestments(investor.getId(), ticker.toUpperCase());
            // Update consolidated investment
            consolidatedInvestment.setAmountUsd(consolidatedInvestment.getAmountUsd() + amountUsd);
            consolidatedInvestment.setSharesPurchased(consolidatedInvestment.getSharesPurchased() + sharesPurchased);
            investmentRepository.save(consolidatedInvestment);
        }

        // Update company shares
        company.setAvailableShares(company.getAvailableShares() - sharesToDeduct);
        companyRepository.save(company);

        // Update portfolio
        portfolioService.updatePortfolioAfterInvestment(username);

        return InvestmentResult.success("Investment successful in " + ticker);
    }

    /**
     * Sell stocks for a user
     * @param ticker Stock symbol (already validated)
     * @param sharesToSell Shares to sell (already validated)
     * @param username Username of investor
     * @return Success message or error
     */
    public InvestmentResult sellStock(String ticker, Double sharesToSell, String username) {
        // Find the company by ticker
        Company company = companyRepository.findByTickerSymbol(ticker.toUpperCase());
        if (company == null) {
            return InvestmentResult.error("Ticker not found: " + ticker);
        }

        // Get logged-in user
        User investor = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Find existing investment for this user & company
        Optional<Investment> investmentOpt;
        try {
            investmentOpt = investmentRepository.findByUserIdAndTickerSymbol(investor.getId(), ticker.toUpperCase());
        } catch (org.springframework.dao.IncorrectResultSizeDataAccessException ex) {
            // Handle duplicate records - consolidate them
            Investment consolidatedInvestment = consolidateDuplicateInvestments(investor.getId(), ticker.toUpperCase());
            investmentOpt = Optional.of(consolidatedInvestment);
        }

        if (investmentOpt.isEmpty()) {
            return InvestmentResult.error(
                "No investment found for ticker: " + ticker + ". You don't own any shares of this company.");
        }

        Investment investment = investmentOpt.get();
        double totalShares = investment.getSharesPurchased();

        // Check if user has enough shares
        if (sharesToSell > totalShares) {
            return InvestmentResult.error(String.format(
                "Insufficient shares to sell. You own %.3f shares, but trying to sell %.3f shares of %s",
                totalShares, sharesToSell, ticker));
        }

        double amountUsd = sharesToSell * company.getLastStockPrice();

        // Increase investor balance
        investor.setBalance(investor.getBalance() + amountUsd);
        userRepository.save(investor);

        // Update stock price (down after sale)
        stockPriceService.updateStockPrice(company, -amountUsd);

        // Update investment (partial or full sell)
        if (sharesToSell.equals(totalShares)) {
            // Sold everything
            investmentRepository.delete(investment);
        } else {
            // Partial sale
            double remainingShares = totalShares - sharesToSell;
            investment.setSharesPurchased(remainingShares);
            investment.setAmountUsd(remainingShares * company.getLastStockPrice());
            investmentRepository.save(investment);
        }

        // Update company available shares
        company.setAvailableShares(company.getAvailableShares() + sharesToSell.longValue());
        companyRepository.save(company);

        // Update portfolio
        portfolioService.updatePortfolioAfterInvestment(username);

        return InvestmentResult.success(String.format(
                "Sale successful! Sold %.3f shares of %s for $%.2f. New stock price: $%.2f",
                sharesToSell, ticker, amountUsd, company.getLastStockPrice()));
    }

    /**
     * Get user's investment portfolio
     * @param username Username
     * @return List of investments
     */
    public List<Investment> getUserPortfolio(String username) {
        User investor = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        
        return investmentRepository.findByUserId(investor.getId());
    }

    /**
     * Consolidates duplicate investment records for the same user and ticker symbol
     * @param userId User ID
     * @param tickerSymbol Ticker symbol
     * @return Consolidated investment record
     */
    private Investment consolidateDuplicateInvestments(Long userId, String tickerSymbol) {
        // Get all investments for this user and ticker
        List<Investment> allInvestments = investmentRepository.findByUserId(userId)
                .stream()
                .filter(inv -> inv.getTickerSymbol().equalsIgnoreCase(tickerSymbol))
                .toList();
        
        if (allInvestments.isEmpty()) {
            // This shouldn't happen, but handle gracefully
            Investment newInvestment = new Investment();
            newInvestment.setUserId(userId);
            newInvestment.setTickerSymbol(tickerSymbol.toUpperCase());
            newInvestment.setAmountUsd(0.0);
            newInvestment.setSharesPurchased(0.0);
            return investmentRepository.save(newInvestment);
        }
        
        if (allInvestments.size() == 1) {
            // Only one record, return it
            return allInvestments.get(0);
        }
        
        // Multiple records found - consolidate them
        Investment primaryInvestment = allInvestments.get(0);
        double totalAmountUsd = 0.0;
        double totalSharesPurchased = 0.0;
        
        // Sum up all investments
        for (Investment inv : allInvestments) {
            totalAmountUsd += inv.getAmountUsd() != null ? inv.getAmountUsd() : 0.0;
            totalSharesPurchased += inv.getSharesPurchased() != null ? inv.getSharesPurchased() : 0.0;
        }
        
        // Update primary investment with consolidated values
        primaryInvestment.setAmountUsd(totalAmountUsd);
        primaryInvestment.setSharesPurchased(totalSharesPurchased);
        investmentRepository.save(primaryInvestment);
        
        // Delete duplicate records (keep the first one)
        for (int i = 1; i < allInvestments.size(); i++) {
            investmentRepository.delete(allInvestments.get(i));
        }
        
        return primaryInvestment;
    }

    /**
     * Result wrapper for investment operations
     */
    public static class InvestmentResult {
        private final boolean success;
        private final String message;

        private InvestmentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static InvestmentResult success(String message) {
            return new InvestmentResult(true, message);
        }

        public static InvestmentResult error(String message) {
            return new InvestmentResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
