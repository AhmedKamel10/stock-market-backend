package first.transactions.dto;

import first.transactions.model.UserRole;

public class UserResponseDto {
    
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private Double balance;
    
    // Constructors
    public UserResponseDto() {}
    
    public UserResponseDto(Long id, String username, String email, UserRole role, Double balance) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance;
    }
}

