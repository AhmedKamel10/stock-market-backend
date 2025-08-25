package first.transactions.service;

import first.transactions.model.Transfers;
import first.transactions.model.User;
import first.transactions.repository.TransferRepository;
import first.transactions.repository.UserRepository;
import first.transactions.dto.TransferRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransferService {

    private final TransferRepository transferRepository;
    private final UserRepository userRepository;

    public TransferService(TransferRepository transferRepository, UserRepository userRepository) {
        this.transferRepository = transferRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a basic transfer (outgoing expense)
     * @param transfers Transfer object with amount and recipient
     * @param username Username of the sender
     * @return Success message or error
     */
    public TransferResult createTransfer(Transfers transfers, String username) {
        // Get the user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Validate amount
        if (transfers.getAmount() <= 0) {
            return TransferResult.error("Transfer amount must be positive");
        }

        // Validate recipient
        if (transfers.getRecipient() == null || transfers.getRecipient().trim().isEmpty()) {
            return TransferResult.error("Recipient is required");
        }

        // Check sufficient balance
        if (user.getBalance() < transfers.getAmount()) {
            return TransferResult.error(String.format(
                "Insufficient balance. Available: $%.2f, Required: $%.2f", 
                user.getBalance(), transfers.getAmount()));
        }

        // Set user and deduct balance
        transfers.setUser(user);
        user.setBalance(user.getBalance() - transfers.getAmount());

        // Save transfer and updated user
        transferRepository.save(transfers);
        userRepository.save(user);

        return TransferResult.success(String.format(
            "Transfer created for %s with amount: $%.2f", 
            transfers.getRecipient(), transfers.getAmount()));
    }

    /**
     * Send money from one user to another user
     * @param transferRequest Transfer details (targetUserId, amount)
     * @param senderUsername Username of sender
     * @return Success message or error
     */
    public TransferResult sendToUser(TransferRequest transferRequest, String senderUsername) {
        // Get sender
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UsernameNotFoundException(senderUsername));

        // Get receiver
        User receiver = userRepository.findById(transferRequest.getTargetUserID())
                .orElseThrow(() -> new UsernameNotFoundException("Recipient user not found with ID: " + transferRequest.getTargetUserID()));

        double amount = transferRequest.getAmount();

        // Validate amount
        if (amount <= 0) {
            return TransferResult.error("Transfer amount must be positive");
        }

        // Check sufficient balance
        if (sender.getBalance() < amount) {
            return TransferResult.error(String.format(
                "Insufficient balance. Available: $%.2f, Required: $%.2f", 
                sender.getBalance(), amount));
        }

        // Prevent self-transfer
        if (sender.getId().equals(receiver.getId())) {
            return TransferResult.error("Cannot transfer money to yourself");
        }

        // Update balances
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);

        // Create transaction records
        Transfers senderTx = new Transfers(-amount, "Sent to " + receiver.getUsername(), sender);
        Transfers receiverTx = new Transfers(amount, "Received from " + sender.getUsername(), receiver);

        // Save everything
        transferRepository.save(senderTx);
        transferRepository.save(receiverTx);
        userRepository.save(sender);
        userRepository.save(receiver);

        return TransferResult.success(String.format(
            "Successfully transferred $%.2f to %s", amount, receiver.getUsername()));
    }

    /**
     * Get all transfers for a user
     * @param username Username
     * @return List of transfers
     */
    public List<Transfers> getUserTransfers(String username) {
        return transferRepository.findByUserUsername(username);
    }

    /**
     * Delete a transfer (if it belongs to the user)
     * @param transferId Transfer ID to delete
     * @param username Username of requester
     * @return Success message or error
     */
    public TransferResult deleteTransfer(Long transferId, String username) {
        // Validate ID
        if (transferId == null || transferId <= 0) {
            return TransferResult.error("Invalid transfer ID");
        }

        // Get user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        // Find the transfer
        Optional<Transfers> transferOpt = transferRepository.findById(transferId);
        if (transferOpt.isEmpty()) {
            return TransferResult.error("Transfer not found");
        }

        Transfers transfer = transferOpt.get();

        // Check ownership
        if (!transfer.getUser().getId().equals(user.getId())) {
            return TransferResult.error("You are not authorized to delete this transfer");
        }

        // Delete the transfer
        transferRepository.delete(transfer);

        return TransferResult.success("Transfer deleted successfully");
    }

    /**
     * Calculate total transfer amount for a user
     * @param username Username
     * @return Total amount
     */
    public double calculateTotalTransfers(String username) {
        // Verify user exists
        userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<Transfers> transfers = transferRepository.findByUserUsername(username);
        return transfers.stream()
                .mapToDouble(Transfers::getAmount)
                .sum();
    }

    /**
     * Result wrapper for transfer operations
     */
    public static class TransferResult {
        private final boolean success;
        private final String message;

        private TransferResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static TransferResult success(String message) {
            return new TransferResult(true, message);
        }

        public static TransferResult error(String message) {
            return new TransferResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}