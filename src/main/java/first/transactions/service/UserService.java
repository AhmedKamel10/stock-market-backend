package first.transactions.service;

import first.transactions.model.User;
import first.transactions.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioService portfolioService;

    public UserService(UserRepository userRepository, PortfolioService portfolioService) {
        this.userRepository = userRepository;
        this.portfolioService = portfolioService;
    }

    /**
     * Get all users (admin only)
     * @return List of all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Get user profile by username
     * @param username Username
     * @return User profile
     */
    public User getUserProfile(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Add balance to user account
     * @param amount Amount to add (already validated by controller)
     * @param username Username
     * @return Result with success/error message
     */
    public UserResult addBalance(Double amount, String username) {
        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        double oldBalance = user.getBalance();
        double newBalance = oldBalance + amount;

        // Business rule: prevent extremely high balances  
        if (newBalance > 10000000.0) { // $10 million max total balance
            return UserResult.error("Total balance cannot exceed $10,000,000");
        }

        // Update balance
        user.setBalance(newBalance);
        userRepository.save(user);

        // Update portfolio to reflect new cash balance
        try {
            portfolioService.updatePortfolioBalance(username, newBalance);
        } catch (Exception e) {
            // Log error but don't fail the transaction
            System.err.println("Failed to update portfolio after balance addition: " + e.getMessage());
        }

        return UserResult.success(String.format(
                "Balance updated successfully. Added: $%.2f, New Balance: $%.2f", 
                amount, newBalance));
    }

    /**
     * Delete user with proper cleanup
     * @param userId User ID to delete
     * @return Result with success/error message
     */
    public UserResult deleteUser(int userId) {
        // Validate user ID
        if (userId <= 0) {
            return UserResult.error("Invalid user ID");
        }

        try {
            // Attempt clean deletion first
            userRepository.deleteUserById(userId);
            
            // Also cleanup portfolio if exists
            try {
                portfolioService.deletePortfolioByUserId((long) userId);
            } catch (Exception e) {
                // Portfolio cleanup failed, but user is deleted
                System.err.println("Portfolio cleanup failed for user " + userId + ": " + e.getMessage());
            }
            
            return UserResult.success("User deleted successfully");
                    
        } catch (Exception e) {
            System.err.println("First delete attempt failed for user " + userId + ": " + e.getMessage());

            // Fallback: cleanup related data first
            try {
                userRepository.deleteTransactionsByUserId(userId);
                
                // Also try to cleanup portfolio
                try {
                    portfolioService.deletePortfolioByUserId((long) userId);
                } catch (Exception portfolioEx) {
                    System.err.println("Portfolio cleanup failed during fallback: " + portfolioEx.getMessage());
                }
                
                userRepository.deleteUserById(userId);
                
                return UserResult.success("User deleted after removing related transactions");
                
            } catch (Exception innerEx) {
                System.err.println("Second delete attempt failed for user " + userId + ": " + innerEx.getMessage());
                return UserResult.error("Failed to delete user. Please contact administrator.");
            }
        }
    }

    /**
     * Check if a username exists
     * @param username Username to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Get user by username
     * @param username Username to find
     * @return User object
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    /**
     * Result wrapper for user operations
     */
    public static class UserResult {
        private final boolean success;
        private final String message;

        private UserResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static UserResult success(String message) {
            return new UserResult(true, message);
        }

        public static UserResult error(String message) {
            return new UserResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}