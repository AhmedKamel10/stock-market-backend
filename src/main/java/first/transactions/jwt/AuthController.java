package first.transactions.jwt;

import first.transactions.model.User;
import first.transactions.repository.UserRepository;
import first.transactions.dto.UserRegistrationDto;
import first.transactions.dto.UserResponseDto;
import first.transactions.dto.LoginRequestDto;
import first.transactions.dto.LoginResponseDto;
import first.transactions.jwt.JwtUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        // Use BCrypt to verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        
        // Generate token
        String token = jwtUtil.generateToken(loginRequest.getUsername());
        
        // Return structured response
        return new LoginResponseDto(
            token,
            user.getId(),
            user.getUsername(),
            user.getRole(),
            "Login successful"
        );
    }

    @PostMapping("/register")
    public UserResponseDto createUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        // Check if username already exists
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists  
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Create new user from DTO
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setBalance(0.0); // Default balance
        user.setRole(registrationDto.getRole() != null ? registrationDto.getRole() : first.transactions.model.UserRole.INVESTOR);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Return response DTO without password
        return new UserResponseDto(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getBalance()
        );
    }
}