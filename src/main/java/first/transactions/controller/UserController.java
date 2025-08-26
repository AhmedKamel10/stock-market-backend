package first.transactions.controller;

import first.transactions.model.User;
import first.transactions.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import jakarta.validation.constraints.*;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getUsers(){
        // Delegate to service layer
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/add_balance")
    public ResponseEntity<String> addBalance(
            @RequestBody @NotNull(message = "Balance amount is required") 
            @Positive(message = "Balance amount must be positive")
            @DecimalMax(value = "1000000.0", message = "Maximum balance addition is $1,000,000")
            @DecimalMin(value = "0.01", message = "Minimum balance addition is $0.01")
            Double balance, 
            Authentication authentication){
        
        // Delegate to service layer - validation already handled by annotations
        UserService.UserResult result = userService.addBalance(balance, authentication.getName());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
    }

    @GetMapping("/user_profile")
    public ResponseEntity<User> getUserProfile(Authentication authentication){
        // Delegate to service layer
        User user = userService.getUserProfile(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/delete_user/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable("id") int id) {
        // Delegate to service layer - handles all deletion logic and cleanup
        UserService.UserResult result = userService.deleteUser(id);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
    }

}
