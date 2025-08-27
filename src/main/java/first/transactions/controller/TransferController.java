package first.transactions.controller;

import first.transactions.model.Transfers;
import first.transactions.dto.TransferRequest;
import first.transactions.dto.DeleteTransactionRequest;
import first.transactions.service.TransferService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/transfers")
@PreAuthorize("hasRole('INVESTOR') or hasRole('SUPER_ADMIN')")
public class TransferController {
    
    private final TransferService transferService;
    
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody Transfers transfers, Authentication authentication) {
        // Delegate to service layer
        TransferService.TransferResult result = transferService.createTransfer(transfers, authentication.getName());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            return ResponseEntity.badRequest().body(result.getMessage());
        }
    }
    @GetMapping("/my_transactions")
    public ResponseEntity<List<Transfers>> getUserTransfers(Authentication authentication) {
        // Delegate to service layer
        List<Transfers> transfers = transferService.getUserTransfers(authentication.getName());
        return ResponseEntity.ok(transfers);
    }

    @PostMapping("/delete_transaction")
    public ResponseEntity<String> delete(@RequestBody DeleteTransactionRequest request, Authentication authentication) {
        // Delegate to service layer
        TransferService.TransferResult result = transferService.deleteTransfer(request.getId(), authentication.getName());
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result.getMessage());
        } else {
            // Determine appropriate HTTP status based on error message
            String message = result.getMessage();
            if (message.contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            } else if (message.contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
            } else {
                return ResponseEntity.badRequest().body(message);
            }
        }
    }

    @GetMapping("/calculate_total")
    public ResponseEntity<Double> calculateTotal(Authentication authentication){
        // Delegate to service layer
        double total = transferService.calculateTotalTransfers(authentication.getName());
        return ResponseEntity.ok(total);
    }
//    @PostMapping("/send_to_user")
//    public ResponseEntity<String> sendToUser(Authentication authentication, @RequestBody TransferRequest transferRequest) {
//        // Delegate to service layer
//        TransferService.TransferResult result = transferService.sendToUser(transferRequest, authentication.getName());
//
//        if (result.isSuccess()) {
//            return ResponseEntity.ok(result.getMessage());
//        } else {
//            return ResponseEntity.badRequest().body(result.getMessage());
//        }
//    }




}
