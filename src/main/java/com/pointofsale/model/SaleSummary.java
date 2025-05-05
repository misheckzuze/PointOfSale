package com.pointofsale.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SaleSummary {
    private final SimpleStringProperty date;
    private final SimpleIntegerProperty transactions;
    private final SimpleDoubleProperty revenue;
    private final SimpleDoubleProperty tax;

    public SaleSummary(String date, int transactions, double revenue, double tax) {
        this.date = new SimpleStringProperty(date);
        this.transactions = new SimpleIntegerProperty(transactions);
        this.revenue = new SimpleDoubleProperty(revenue);
        this.tax = new SimpleDoubleProperty(tax);
    }

    public String getDate() { return date.get(); }
    public int getTransactions() { return transactions.get(); }
    public double getRevenue() { return revenue.get(); }
    public double getTax() { return tax.get(); }
}
