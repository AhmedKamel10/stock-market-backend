package first.transactions.dto;

public class DeleteTransactionRequest {
    private Long id;

    public DeleteTransactionRequest() {}

    public DeleteTransactionRequest(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}


