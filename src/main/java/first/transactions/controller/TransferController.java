package first.transactions.controller;

import first.transactions.model.Transfers;
import first.transactions.model.User;
import first.transactions.repository.TransferRepository;
import first.transactions.repository.UserRepository;
import first.transactions.dto.TransferRequest;
import jakarta.transaction.Transactional;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final TransferRepository transferRepository;
    private final UserRepository userRepository;
    public TransferController(TransferRepository transferRepository, UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
    }

    @PostMapping
    public String create(@RequestBody Transfers transfers, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException(username));
        transfers.setUser(user);
        double old_balance = user.getBalance();
        user.setBalance(old_balance - transfers.getAmount());
        transferRepository.save(transfers);
        userRepository.save(user);
        return "transfer created for " + transfers.getRecipient() + " with ammount : " + transfers.getAmount();


    }
    @GetMapping("/my_transactions")
    public List<Transfers> getUserTransfers(Authentication authentication) {
        String username = authentication.getName();
        return transferRepository.findByUserUsername(username);
    }

    @PostMapping("/delete_transaction")
    public ResponseEntity<String> delete(@RequestBody Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Find the transaction by ID
        Transfers transaction = transferRepository.findById(id)
                .orElse(null);

        if (transaction == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Transaction not found");
        }

        // Check that the transaction belongs to this user
        if (!transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to delete this transaction");
        }

        // Delete the transaction
        transferRepository.delete(transaction);

        return ResponseEntity.ok("Transaction deleted successfully");
    }

    @GetMapping("/calculate_total")
    public double calculate_total(Authentication authentication){
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        List<Transfers> transfers = transferRepository.findByUserUsername(username);
        double total_amount = 0;
        for(Transfers transfer : transfers){
            total_amount += transfer.getAmount();

        }
        return total_amount;

    }
    @Transactional
    @PostMapping("/send_to_user")
    public String sendToUser(Authentication authentication, @RequestBody TransferRequest transferRequest) {
        User sender = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException(authentication.getName()));

        User receiver = userRepository.findById(transferRequest.getTargetUserID())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        double amount = transferRequest.getAmount();

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (sender.getBalance() < amount) {
            throw new IllegalStateException("Insufficient balance");
        }
        if(sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Sender and receiver are the same");
        }

        // Update balances
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        // Log transactions
        Transfers senderTx = new Transfers(-amount, "Sent to " + receiver.getUsername(), sender);
        Transfers receiverTx = new Transfers(amount, "Received from " + sender.getUsername(), receiver);

        transferRepository.save(senderTx);
        transferRepository.save(receiverTx);

        userRepository.save(sender);
        userRepository.save(receiver);

        return "Transfer sent successfully to " + receiver.getUsername();
    }




}
