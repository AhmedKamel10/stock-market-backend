package first.transactions.repository;

import first.transactions.model.Transfers;
import first.transactions.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM transactions WHERE user_id = :id", nativeQuery = true)
    void deleteTransactionsByUserId(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
    void deleteUserById(@Param("userId") int userId);
}
