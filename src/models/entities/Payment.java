package models.entities;

public class Payment {

    private String paymentId;
    private String appointmentId;
    private String amount;
    private String paymentMethod;
    private String paymentDate;
    private String receiptNo;
    private String staffId;
    private String additionalCharge;
    private String totalAmount;

    public Payment(String paymentId, String appointmentId, String amount,
                   String paymentMethod, String paymentDate, String receiptNo,
                   String staffId, String additionalCharge, String totalAmount) {
        this.paymentId        = paymentId;
        this.appointmentId    = appointmentId;
        this.amount           = amount;
        this.paymentMethod    = paymentMethod;
        this.paymentDate      = paymentDate;
        this.receiptNo        = receiptNo;
        this.staffId          = staffId;
        this.additionalCharge = additionalCharge;
        this.totalAmount      = totalAmount;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────
    public String getPaymentId()        { return paymentId; }
    public String getAppointmentId()    { return appointmentId; }
    public String getAmount()           { return amount; }
    public String getPaymentMethod()    { return paymentMethod; }
    public String getPaymentDate()      { return paymentDate; }
    public String getReceiptNo()        { return receiptNo; }
    public String getStaffId()          { return staffId; }
    public String getAdditionalCharge() { return additionalCharge; }
    public String getTotalAmount()      { return totalAmount; }

    // Convenience — parsed doubles
    public double getAmountDouble()           { return parseDouble(amount); }
    public double getAdditionalChargeDouble() { return parseDouble(additionalCharge); }
    public double getTotalAmountDouble()      { return parseDouble(totalAmount); }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return 0.0; }
    }

    // ─── Setters ──────────────────────────────────────────────────────────────
    public void setPaymentId(String paymentId)               { this.paymentId = paymentId; }
    public void setAppointmentId(String appointmentId)       { this.appointmentId = appointmentId; }
    public void setAmount(String amount)                     { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod)       { this.paymentMethod = paymentMethod; }
    public void setPaymentDate(String paymentDate)           { this.paymentDate = paymentDate; }
    public void setReceiptNo(String receiptNo)               { this.receiptNo = receiptNo; }
    public void setStaffId(String staffId)                   { this.staffId = staffId; }
    public void setAdditionalCharge(String additionalCharge) { this.additionalCharge = additionalCharge; }
    public void setTotalAmount(String totalAmount)           { this.totalAmount = totalAmount; }

    @Override
    public String toString() {
        return String.join("|", paymentId, appointmentId, amount, paymentMethod,
            paymentDate, receiptNo, staffId, additionalCharge, totalAmount);
    }
}