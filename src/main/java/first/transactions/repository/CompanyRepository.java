package first.transactions.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import first.transactions.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}