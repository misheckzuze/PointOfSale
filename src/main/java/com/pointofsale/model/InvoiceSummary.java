package com.pointofsale.model;

import java.util.List;

public class InvoiceSummary {
    private List<TaxBreakDown> taxBreakDown;
    private double totalVAT;
    private String offlineSignature;
    private double invoiceTotal;

    // Getters and Setters
    public List<TaxBreakDown> getTaxBreakDown() {
        return taxBreakDown;
    }

    public void setTaxBreakDown(List<TaxBreakDown> taxBreakDown) {
        this.taxBreakDown = taxBreakDown;
    }

    public double getTotalVAT() {
        return totalVAT;
    }

    public void setTotalVAT(double totalVAT) {
        this.totalVAT = totalVAT;
    }

    public String getOfflineSignature() {
        return offlineSignature;
    }

    public void setOfflineSignature(String offlineSignature) {
        this.offlineSignature = offlineSignature;
    }

    public double getInvoiceTotal() {
        return invoiceTotal;
    }

    public void setInvoiceTotal(double invoiceTotal) {
        this.invoiceTotal = invoiceTotal;
    }
}
