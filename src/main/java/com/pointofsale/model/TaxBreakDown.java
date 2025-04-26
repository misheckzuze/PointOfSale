package com.pointofsale.model;

public class TaxBreakDown {
    private String rateId;
    private double taxableAmount;
    private double taxAmount;

    // Getters and Setters
    public String getRateId() { return rateId; }
    public void setRateId(String rateId) { this.rateId = rateId; }

    public double getTaxableAmount() { return taxableAmount; }
    public void setTaxableAmount(double taxableAmount) { this.taxableAmount = taxableAmount; }

    public double getTaxAmount() { return taxAmount; }
    public void setTaxAmount(double taxAmount) { this.taxAmount = taxAmount; }
}

