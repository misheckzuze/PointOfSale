package com.pointofsale.model;

public class TaxSummary {
    private String taxRate;
    private double totalSales;
    private double taxAmount;

    // Constructor
    public TaxSummary(String taxRate, double totalSales, double taxAmount) {
        this.taxRate = taxRate;
        this.totalSales = totalSales;
        this.taxAmount = taxAmount;
    }

    // Getters and Setters
    public String getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(String taxRate) {
        this.taxRate = taxRate;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

}
