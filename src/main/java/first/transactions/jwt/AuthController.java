package first.transactions.jwt;

import first.transactions.model.User;
import first.transactions.repository.UserRepository;
import first.transactions.jwt.JwtUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }
    @PostMapping
    public String login(@RequestBody User loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() -> new UsernameNotFoundException("username not found"));

        if (!user.getPassword().equals(loginRequest.getPassword())){
            throw new RuntimeException("Wrong password");
        }
        return jwtUtil.generateToken(loginRequest.getUsername());
    }
    @PostMapping("/register")
    public User createUser(@RequestBody User user){
        return userRepository.save(user);
    }
}
