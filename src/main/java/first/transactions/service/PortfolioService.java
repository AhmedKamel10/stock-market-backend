package first.transactions.service;

import first.transactions.model.Investment;
import first.transactions.model.Portfolio;
import first.transactions.model.User;
import first.transactions.repository.CompanyRepository;
import first.transactions.repository.InvestmentRepository;
import first.transactions.repository.PortfolioRepository;
import first.transactions.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import first.transactions.service.PortfolioService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PortfolioService {
    
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;
    private final InvestmentRepository investmentRepository;
    private final CompanyRepository companyRepository;
    
    public PortfolioService(PortfolioRepository portfolioRepository,
                           UserRepository userRepository,
                           InvestmentRepository investmentRepository,
                           CompanyRepository companyRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
        this.investmentRepository = investmentRepository;
        this.companyRepository = companyRepository;
    }
    
    /**
     * Get or create portfolio for a user by username
     */
    public Portfolio getPortfolioByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return getOrCreatePortfolio(user);
    }
    
    /**
     * Get or create portfolio for a user by ID
     */
    public Portfolio getPortfolioByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        return getOrCreatePortfolio(user);
    }
    
    /**
     * Get or create portfolio - creates one if it doesn't exist
     */
    private Portfolio getOrCreatePortfolio(User user) {
        return portfolioRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Portfolio newPortfolio = new Portfolio(user);
                    recalculatePortfolio(newPortfolio);
                    return portfolioRepository.save(newPortfolio);
                });
    }
    
    /**
     * Recalculate portfolio from current investments and save
     */
    public Portfolio recalculatePortfolio(String username) {
        Portfolio portfolio = getPortfolioByUsername(username);
        recalculatePortfolio(portfolio);
        return portfolioRepository.save(portfolio);
    }
    
    /**
     * Internal method to recalculate portfolio values
     */
    private void recalculatePortfolio(Portfolio portfolio) {
        User user = portfolio.getUser();
        
        // Update cash balance from user
        portfolio.updateCashBalance(user.getBalance());
        
        // Get all investments for this user
        List<Investment> investments = investmentRepository.findByUserId(user.getId());
        
        if (investments.isEmpty()) {
            // No investments
            portfolio.updateInvestmentData(0.0, 0.0, 0);
        } else {
            // Group investments by ticker and sum them up
            Map<String, InvestmentSummary> groupedInvestments = investments.stream()
                    .collect(Collectors.groupingBy(
                        Investment::getTickerSymbol,
                        Collectors.reducing(
                            new InvestmentSummary(),
                            investment -> new InvestmentSummary(
                                investment.getSharesPurchased(),
                                investment.getAmountUsd()
                            ),
                            (s1, s2) -> new InvestmentSummary(
                                s1.totalShares + s2.totalShares,
                                s1.totalInvested + s2.totalInvested
                            )
                        )
                    ));
            
            // Calculate current value of all investments
            double currentInvestmentsValue = 0.0;
            double totalInvested = 0.0;
            
            for (Map.Entry<String, InvestmentSummary> entry : groupedInvestments.entrySet()) {
                String ticker = entry.getKey();
                InvestmentSummary summary = entry.getValue();
                
                // Get current stock price
                var company = companyRepository.findByTickerSymbol(ticker);
                double currentPrice = company != null ? company.getLastStockPrice() : 0.0;
                
                // Calculate current value for this holding
                double holdingCurrentValue = summary.totalShares * currentPrice;
                currentInvestmentsValue += holdingCurrentValue;
                totalInvested += summary.totalInvested;
            }
            
            // Update portfolio with calculated values
            portfolio.updateInvestmentData(
                currentInvestmentsValue,
                totalInvested,
                groupedInvestments.size()
            );
        }
    }

    public void recalculateAllPortfolios() {
        List<User> users = userRepository.findAll();  // assuming you have this
        List<Portfolio> updated = new ArrayList<>();

        for (User user : users) {
            String username = user.getUsername();
            Portfolio portfolio = getPortfolioByUsername(username);
            System.out.println("old profit of"+ username + "is" + portfolio.getProfit() );
            recalculatePortfolio(portfolio);
            System.out.println( "updated profit of " + username + "to" + portfolio.getProfit());

        }

    }
    
    /**
     * Update portfolio when user makes an investment
     */
    public void updatePortfolioAfterInvestment(String username) {
        Portfolio portfolio = getPortfolioByUsername(username);
        recalculatePortfolio(portfolio);
        portfolioRepository.save(portfolio);
    }
    
    /**
     * Update portfolio when user's balance changes
     */
    public void updatePortfolioBalance(String username, Double newBalance) {
        Portfolio portfolio = getPortfolioByUsername(username);
        portfolio.updateCashBalance(newBalance);
        portfolioRepository.save(portfolio);
    }
    
    /**
     * Get all portfolios (admin function)
     */
    public List<Portfolio> getAllPortfolios() {
        return portfolioRepository.findAll();
    }
    
    /**
     * Delete portfolio (cleanup when user is deleted)
     */
    public void deletePortfolioByUserId(Long userId) {
        portfolioRepository.deleteByUserId(userId);
    }
    
    /**
     * Helper class for investment calculations
     */
    private static class InvestmentSummary {
        double totalShares = 0.0;
        double totalInvested = 0.0;
        
        public InvestmentSummary() {}
        
        public InvestmentSummary(double totalShares, double totalInvested) {
            this.totalShares = totalShares;
            this.totalInvested = totalInvested;
        }
    }
}
