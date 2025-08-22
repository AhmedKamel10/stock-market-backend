package first.transactions.repository;

import first.transactions.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    
    Optional<Portfolio> findByUserId(Long userId);
    
    @Query("SELECT p FROM Portfolio p JOIN p.user u WHERE u.username = :username")
    Optional<Portfolio> findByUserUsername(@Param("username") String username);
    
    boolean existsByUserId(Long userId);
    
    void deleteByUserId(Long userId);
    
    // Custom query to get portfolio with user data in one query
    @Query("SELECT p FROM Portfolio p JOIN FETCH p.user WHERE p.user.id = :userId")
    Optional<Portfolio> findByUserIdWithUser(@Param("userId") Long userId);
}
