package first.transactions.controller;

import first.transactions.model.User;
import first.transactions.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<User> getUsers(){ return userRepository.findAll(); }

    @PostMapping("/add_balance")
    public ResponseEntity<String> addBalance(
            @RequestBody @Valid @NotNull(message = "Balance amount is required") 
            @Positive(message = "Balance amount must be positive")
            @DecimalMax(value = "1000000.0", message = "Maximum balance addition is $1,000,000")
            @DecimalMin(value = "0.01", message = "Minimum balance addition is $0.01")
            Double balance, 
            Authentication authentication){
        
        // Input validation passed, now process the request
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        
        double oldBalance = user.getBalance();
        double newBalance = oldBalance + balance;
        

        
        user.setBalance(newBalance);
        userRepository.save(user);
        
        return ResponseEntity.ok(String.format(
                "Balance updated successfully. Added: $%.2f, New Balance: $%.2f", 
                balance, newBalance));
    }

    @GetMapping("/user_profile")
    public User get_user_info(Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        return user;
    }

    @DeleteMapping("/delete_user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String delete(@PathVariable("id") int id) {
        try {
            userRepository.deleteUserById(id);
            return "User deleted without needing transaction cleanup";
        } catch (Exception e) {
            System.err.println("First delete attempt failed: "  );

            try {
                userRepository.deleteTransactionsByUserId(id);
                userRepository.deleteUserById(id);
                return "User deleted after removing related transactions";
            } catch (Exception innerEx) {
                System.err.println("Second delete attempt failed");
                return "Error deleting user and transactions ";
            }
        }
    }

    
}
