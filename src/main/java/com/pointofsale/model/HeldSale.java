package com.pointofsale.model;


import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;



public class HeldSale {
    private String holdId;
    private String customerName;
    private String customerTIN;
    private List<Product> items;
    private double cartDiscountAmount;
    private double cartDiscountPercent;
    private LocalDateTime holdTime;
    
    public HeldSale(String holdId, String customerName, String customerTIN, 
                   List<Product> items, double cartDiscountAmount, 
                   double cartDiscountPercent) {
        this.holdId = holdId;
        this.customerName = customerName;
        this.customerTIN = customerTIN;
        this.items = new ArrayList<>(items);
        this.cartDiscountAmount = cartDiscountAmount;
        this.cartDiscountPercent = cartDiscountPercent;
        this.holdTime = LocalDateTime.now();
    }
    
    public String getHoldId() { return holdId; }
    public String getCustomerName() { return customerName; }
    public String getCustomerTIN() { return customerTIN; }
    public List<Product> getItems() { return items; }
    public double getCartDiscountAmount() { return cartDiscountAmount; }
    public double getCartDiscountPercent() { return cartDiscountPercent; }
    public LocalDateTime getHoldTime() { return holdTime; }
    
    public String getFormattedHoldTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss");
        return holdTime.format(formatter);
    }
}


