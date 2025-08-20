package first.transactions.controller;

import first.transactions.model.User;
import first.transactions.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserRepository userRepository;
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @GetMapping
    public List<User> getUsers(){ return userRepository.findAll(); }

    @PostMapping("/add_balance")
    public String addBalance(@RequestBody Double balance, Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        double old_balance = user.getBalance();
        user.setBalance(balance+ old_balance  );
        userRepository.save(user);
        return "user balance  updated";

    }

    @GetMapping("/user_profile")
    public User get_user_info(Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(()->new UsernameNotFoundException(username));
        return user;
    }

    @DeleteMapping("/delete_user/{id}")
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
