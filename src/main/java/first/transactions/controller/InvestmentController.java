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

import java.util.List;

@RestController
@RequestMapping("/investments") // base path
@CrossOrigin("*")
public class InvestmentController {

    private final InvestmentRepository investmentRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public InvestmentController(InvestmentRepository investmentRepository,
                                CompanyRepository companyRepository,
                                UserRepository userRepository) {
        this.investmentRepository = investmentRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/invest")
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
        double latestPrice = company.getLastStockPrice();
        double sharesPurchased = amountUsd / latestPrice;

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

        return ResponseEntity.ok(investment);
    }}

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(Authentication authentication) {
        User investor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        List<Investment> investments = investmentRepository.findByUserId(investor.getId());
        return ResponseEntity.ok(investments);
    }
}
