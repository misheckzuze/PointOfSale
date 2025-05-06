package com.pointofsale.model;


public class TaxTrend {
    private String date;
    private double vatAmount;

    // Constructor
    public TaxTrend(String date, double vatAmount) {
        this.date = date;
        this.vatAmount = vatAmount;
    }

    // Getters
    public String getDate() {
        return date;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    // Setters (optional, depending on your use case)
    public void setDate(String date) {
        this.date = date;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    @Override
    public String toString() {
        return "TaxTrend{" +
               "date='" + date + '\'' +
               ", vatAmount=" + vatAmount +
               '}';
    }
}
