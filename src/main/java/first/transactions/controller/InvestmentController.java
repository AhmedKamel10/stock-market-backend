package first.transactions.controller;

import first.transactions.model.Investment;
import first.transactions.repository.InvestmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/investments") // base path
@CrossOrigin("*")
public class InvestmentController {

    private final InvestmentRepository investmentRepository;

    public InvestmentController(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @PostMapping("/invest")
    public ResponseEntity<?> invest(
            @RequestParam Long userId,
            @RequestParam String ticker,
            @RequestParam Double amountUsd) {

        Investment investment = new Investment();
        investment.setUserId(userId);
        investment.setTickerSymbol(ticker);
        investment.setAmountUsd(amountUsd);

        investmentRepository.save(investment);

        return ResponseEntity.ok(investment);
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<?> getPortfolio(@PathVariable Long userId) {
        List<Investment> investments = investmentRepository.findByUserId(userId);
        return ResponseEntity.ok(investments);
    }
}
