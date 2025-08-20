package first.transactions.dto;

public class TransferRequest
{
    private Long targetUserID;
    private double amount;

    //getters and setters
    public Long getTargetUserID() {
        return targetUserID;
    }
    public void setTargetUserID(Long targetUserID) {
        this.targetUserID = targetUserID;
    }
    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) {
        this.amount = amount;
    }
}
