package com.pointofsale.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ProductSale {
    private final SimpleStringProperty productName;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty revenue;
    private final SimpleDoubleProperty profit;

    public ProductSale(String productName, int quantity, double revenue, double profit) {
        this.productName = new SimpleStringProperty(productName);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.revenue = new SimpleDoubleProperty(revenue);
        this.profit = new SimpleDoubleProperty(profit);
    }

    public String getProductName() { return productName.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getRevenue() { return revenue.get(); }
    public double getProfit() { return profit.get(); }
}
