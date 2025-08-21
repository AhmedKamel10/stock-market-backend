package first.transactions.controller;

import first.transactions.model.Company;
import first.transactions.model.Investment;
import first.transactions.model.User;
import first.transactions.repository.CompanyRepository;
import first.transactions.repository.InvestmentRepository;
import first.transactions.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import first.transactions.service.StockPriceService;
import java.util.List;

@RestController
@RequestMapping("/investments") // base path
@CrossOrigin("*")
public class InvestmentController {

    private final InvestmentRepository investmentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;

    public InvestmentController(InvestmentRepository investmentRepository,
                                CompanyRepository companyRepository,
                                UserRepository userRepository, StockPriceService stockPriceService) {
        this.investmentRepository = investmentRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.stockPriceService = stockPriceService;
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

        // Calculate shares

        stockPriceService.updateStockPrice(company, amountUsd);
        double latestPrice = company.getLastStockPrice();
        double sharesPurchased = amountUsd / latestPrice;
        long sharespurchased = (long) sharesPurchased;
        // Get logged-in user
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        // Create investment
        if(investor.getBalance() < amountUsd){
            return ResponseEntity.badRequest().body("Not enough balance");
        }
        else{
        Investment investment = new Investment();
        investment.setUserId(investor.getId());
        investment.setTickerSymbol(ticker);
        investment.setAmountUsd(amountUsd);
        investment.setSharesPurchased(sharesPurchased);
        investmentRepository.save(investment);
        company.setAvailableShares(company.getAvailableShares() - sharespurchased);
        companyRepository.save(company);
        return ResponseEntity.ok(investment);
    }}

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

        // Find all investments for this user & company
        List<Investment> investments = investmentRepository.findByUserIdAndTickerSymbol(investor.getId(), ticker);
        double totalShares = investments.stream().mapToDouble(Investment::getSharesPurchased).sum();

        if (totalShares == 0.0) {
            return ResponseEntity.badRequest().body("No investment found for this ticker");
        }

        if (sharesToSell > totalShares) {
            return ResponseEntity.badRequest().body("You don't have enough shares to sell");
        }

        double amountUsd = sharesToSell * company.getLastStockPrice();
        //zawed el balance beta3 el investor 7asab el sale
        investor.setBalance(investor.getBalance() + amountUsd);
        userRepository.save(investor);

        // update el stock price ba3d el sale (el mafrood yenzel fa bel negative)
        stockPriceService.updateStockPrice(company, -amountUsd);


        //(FIFO)
        double remainingSharesToSell = sharesToSell;
        for (Investment inv : investments) {
            if (remainingSharesToSell <= 0) break;

            double sharesInThisInvestment = inv.getSharesPurchased();

            if (sharesInThisInvestment <= remainingSharesToSell) {
                // Sell all shares from this investment
                remainingSharesToSell -= sharesInThisInvestment;
                investmentRepository.delete(inv);
            } else {
                // Partial sale
                inv.setSharesPurchased(sharesInThisInvestment - remainingSharesToSell);
                inv.setAmountUsd(inv.getSharesPurchased() * company.getLastStockPrice());
                investmentRepository.save(inv);
                remainingSharesToSell = 0;
            }
        }

        // Update company available shares (hanzawed 3ashan 3amalna sale)
        company.setAvailableShares(company.getAvailableShares() + sharesToSell.longValue());


        return ResponseEntity.ok("Sold " + sharesToSell + " shares of " + ticker + ". New stock price: " + company.getLastStockPrice());
    }


    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(Authentication authentication) {
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        List<Investment> investments = investmentRepository.findByUserId(investor.getId());
        return ResponseEntity.ok(investments);
    }
}
