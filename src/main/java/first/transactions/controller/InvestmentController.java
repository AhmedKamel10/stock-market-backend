package first.transactions.controller;

import first.transactions.model.Company;
import first.transactions.model.Investment;
import first.transactions.model.User;
import first.transactions.repository.CompanyRepository;
import first.transactions.repository.InvestmentRepository;
import first.transactions.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import first.transactions.service.StockPriceService;
import first.transactions.service.PortfolioService;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/investments") // base path
@CrossOrigin("*")
@PreAuthorize("hasRole('INVESTOR') or hasRole('SUPER_ADMIN')")
public class InvestmentController {

    private final InvestmentRepository investmentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;
    private final PortfolioService portfolioService;

    public InvestmentController(InvestmentRepository investmentRepository,
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

    @PostMapping("/invest/buy")
    public ResponseEntity<?> invest(
            Authentication authentication,
            @RequestParam String ticker,
            @RequestParam Double amountUsd) {

        // Find the company by ticker
        Company company = companyRepository.findByTickerSymbol(ticker);
        if (company == null) {
            return ResponseEntity.badRequest().body("Ticker not found: " + ticker);
        }

        // Get logged-in user
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        // Check balance
        if (investor.getBalance() < amountUsd) {
            return ResponseEntity.badRequest().body("Not enough balance");
        }

        // Calculate shares
        stockPriceService.updateStockPrice(company, amountUsd);
        double latestPrice = company.getLastStockPrice();
        double sharesPurchased = amountUsd / latestPrice;
        long sharesToDeduct = (long) sharesPurchased;

        // Deduct balance from user
        investor.setBalance(investor.getBalance() - amountUsd);
        userRepository.save(investor);

        // Find existing investment for this user & ticker
        investmentRepository.findByUserIdAndTickerSymbol(investor.getId(), ticker)
                .ifPresentOrElse(investment -> {
                    // Update existing investment
                    investment.setAmountUsd(investment.getAmountUsd() + amountUsd);
                    investment.setSharesPurchased(investment.getSharesPurchased() + sharesPurchased);
                    investmentRepository.save(investment);
                }, () -> {
                    // Create new investment
                    Investment newInvestment = new Investment();
                    newInvestment.setUserId(investor.getId());
                    newInvestment.setTickerSymbol(ticker);
                    newInvestment.setAmountUsd(amountUsd);
                    newInvestment.setSharesPurchased(sharesPurchased);
                    investmentRepository.save(newInvestment);
                });

        // Update company shares
        company.setAvailableShares(company.getAvailableShares() - sharesToDeduct);
        companyRepository.save(company);

        // Update portfolio
        portfolioService.updatePortfolioAfterInvestment(authentication.getName());

        return ResponseEntity.ok("Investment successful in " + ticker);
    }
    @PostMapping("/invest/sell")
    public ResponseEntity<?> sell(
            Authentication authentication,
            @RequestParam String ticker,
            @RequestParam Double sharesToSell) {

        // Find the company by ticker
        Company company = companyRepository.findByTickerSymbol(ticker);
        if (company == null) {
            return ResponseEntity.badRequest().body("Ticker not found: " + ticker);
        }

        // Get logged-in user
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        // Find existing investment for this user & company
        Optional<Investment> investmentOpt =
                investmentRepository.findByUserIdAndTickerSymbol(investor.getId(), ticker);

        if (investmentOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("No investment found for this ticker");
        }

        Investment investment = investmentOpt.get();
        double totalShares = investment.getSharesPurchased();

        if (sharesToSell > totalShares) {
            return ResponseEntity.badRequest().body("You don't have enough shares to sell");
        }

        double amountUsd = sharesToSell * company.getLastStockPrice();

        // increase investor balance
        investor.setBalance(investor.getBalance() + amountUsd);
        userRepository.save(investor);

        // update stock price (down after sale)
        stockPriceService.updateStockPrice(company, -amountUsd);

        // update investment (partial or full sell)
        if (sharesToSell.equals(totalShares)) {
            // sold everything
            investmentRepository.delete(investment);
        } else {
            // partial sale
            double remainingShares = totalShares - sharesToSell;
            investment.setSharesPurchased(remainingShares);
            investment.setAmountUsd(remainingShares * company.getLastStockPrice());
            investmentRepository.save(investment);
        }

        // update company available shares
        company.setAvailableShares(company.getAvailableShares() + sharesToSell.longValue());
        companyRepository.save(company);

        // update portfolio
        portfolioService.updatePortfolioAfterInvestment(authentication.getName());

        return ResponseEntity.ok("Sold " + sharesToSell + " shares of " + ticker +
                ". New stock price: " + company.getLastStockPrice());
    }


    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(Authentication authentication) {
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        List<Investment> investments = investmentRepository.findByUserId(investor.getId());
        return ResponseEntity.ok(investments);
    }
}
