package com.pointofsale.model;

import java.time.LocalDateTime;

public class InvoiceDetails {
    private String invoiceNumber;
    private LocalDateTime invoiceDateTime;
    private String buyerTin;
    private int itemCount;
    private double invoiceTotal;
    private double totalVAT;
    private boolean transmitted;
    private String validationUrl;
    
    public InvoiceDetails() {
    // No-argument constructor for JavaFX or frameworks
    }

    // Constructor
    public InvoiceDetails(String invoiceNumber, LocalDateTime invoiceDateTime, String buyerTin,
                          int itemCount, double invoiceTotal, double totalVAT, boolean transmitted, String validationUrl) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDateTime = invoiceDateTime;
        this.buyerTin = buyerTin;
        this.itemCount = itemCount;
        this.invoiceTotal = invoiceTotal;
        this.totalVAT = totalVAT;
        this.transmitted = transmitted;
        this.validationUrl = validationUrl;
    }

    // Getters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDateTime getInvoiceDateTime() {
        return invoiceDateTime;
    }

    public String getBuyerTin() {
        return buyerTin;
    }

    public int getItemCount() {
        return itemCount;
    }

    public double getInvoiceTotal() {
        return invoiceTotal;
    }

    public double getTotalVAT() {
        return totalVAT;
    }

    public boolean isTransmitted() {
        return transmitted;
    }
    
    public String getValidationUrl() {
    return validationUrl;
    }

    // Setters
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setInvoiceDateTime(LocalDateTime invoiceDateTime) {
        this.invoiceDateTime = invoiceDateTime;
    }

    public void setBuyerTin(String buyerTin) {
        this.buyerTin = buyerTin;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public void setInvoiceTotal(double invoiceTotal) {
        this.invoiceTotal = invoiceTotal;
    }

    public void setTotalVAT(double totalVAT) {
        this.totalVAT = totalVAT;
    }

    public void setTransmitted(boolean transmitted) {
        this.transmitted = transmitted;
    }
    
    public void setValidationUrl(String validationUrl) {
    this.validationUrl = validationUrl;
    }

}
