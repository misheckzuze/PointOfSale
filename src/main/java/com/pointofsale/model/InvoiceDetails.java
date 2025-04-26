package com.pointofsale.model;

import java.time.LocalDateTime;


public class InvoiceDetails {
    private String invoiceNumber;
    private LocalDateTime invoiceDateTime;

    // Getters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public LocalDateTime getInvoiceDateTime() {
        return invoiceDateTime;
    }

    // Setters
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public void setInvoiceDateTime(LocalDateTime invoiceDateTime) {
        this.invoiceDateTime = invoiceDateTime;
    }
}

