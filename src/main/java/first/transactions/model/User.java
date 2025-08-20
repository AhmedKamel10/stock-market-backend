package first.transactions.model;
import first.transactions.model.Transfers;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private Double balance;
    public User(){}
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private  List<Transfers> transactions = new ArrayList<>();
    public List<Transfers> getTransactions() {
        return transactions;
    }


    public void setTransactions(List<Transfers> transactions) {
        this.transactions = transactions;
    }



    //getters and setters
    public Long getId() {return  id;}
    public void setId(Long id) {this.id = id;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public Double getBalance() {return balance;}
    public void setBalance(Double balance) {this.balance = balance;}

}
