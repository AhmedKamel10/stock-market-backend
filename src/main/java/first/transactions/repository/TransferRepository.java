package first.transactions.repository;

import first.transactions.model.Transfers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransferRepository extends JpaRepository<Transfers, Long> {
    List<Transfers> findByUserUsername(String username);
    @Query(value = "select * from transactions where amount > :sent", nativeQuery = true)
    List<Transfers> findAllByAmountGreaterThanZero(@Param("sent") double sent);

}
