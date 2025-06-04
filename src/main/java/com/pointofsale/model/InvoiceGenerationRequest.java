package com.pointofsale.model;
import java.time.LocalDateTime;

public class InvoiceGenerationRequest {
    public int numItems;
    public LocalDateTime transactiondate;
    public long transactionCount;
    public double invoiceTotal;
    public double vatAmount;
    public long businessId;
    public int terminalPosition;
}
