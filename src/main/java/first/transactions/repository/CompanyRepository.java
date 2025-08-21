package first.transactions.repository;

import first.transactions.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query(
            value = "SELECT * FROM COMPANIES c WHERE UPPER(c.TICKER_SYMBOL) = UPPER(:tickerSymbol)",
            nativeQuery = true
    )
    Company findByTickerSymbol(@Param("tickerSymbol") String tickerSymbol);
}
