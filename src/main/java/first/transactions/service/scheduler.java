package first.transactions.service;

import first.transactions.model.Company;
import first.transactions.repository.CompanyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;
import first.transactions.service.StockPriceService;
@Component
public class scheduler {
    private final CompanyRepository companyRepository;
    private final StockPriceService stockPriceService;
    public scheduler(CompanyRepository companyRepository, StockPriceService stockPriceService) {
        this.companyRepository = companyRepository;
        this.stockPriceService = stockPriceService;

    }
    //run every 1 min
    @Scheduled(fixedRate = 60000)
    public void schedule() {
        var companies = companyRepository.findAll();
        for (Company company : companies){
            Double price = stockPriceService.getStockPrice(company.getTickerSymbol());
            company.setLastStockPrice(price);
            companyRepository.save(company);

        }

        System.out.println("stock price updated");

    }

    @Scheduled(fixedRate = 60000)
    public void CalculateRevenue() {}

}
