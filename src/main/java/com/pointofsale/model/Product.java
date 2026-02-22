package com.pointofsale.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product implements Cloneable {
    private final SimpleStringProperty barcode;
    private final SimpleStringProperty name;
    private final SimpleStringProperty description;
    private final SimpleDoubleProperty price;
    private final SimpleDoubleProperty quantity;
    private final SimpleStringProperty unitOfMeasure;
    private final SimpleStringProperty taxRate;
    private final SimpleDoubleProperty total;

    private long id = 0;
    private final SimpleDoubleProperty discount = new SimpleDoubleProperty(0.0);
    private final SimpleDoubleProperty totalVAT = new SimpleDoubleProperty(0.0);
    private boolean isProduct = true;

    private double originalPrice;
    private double discountPercent;
    private double discountAmount;

    public Product(String barcode, String name, String description, double price,
                   String taxRate, double quantity, String unitOfMeasure, boolean isProduct) {
        this.barcode = new SimpleStringProperty(barcode);
        this.name = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.price = new SimpleDoubleProperty(price);
        this.taxRate = new SimpleStringProperty(taxRate);
        this.quantity = new SimpleDoubleProperty(quantity);
        this.unitOfMeasure = new SimpleStringProperty(unitOfMeasure);
        this.total = new SimpleDoubleProperty(price * quantity);
        this.isProduct = isProduct;

        this.originalPrice = price;
        this.totalVAT.set(calculateVAT(this.total.get(), taxRate));
    }

    private double calculateVAT(double amount, String taxRateId) {
        if ("VAT16".equals(taxRateId)) {
            return amount * 0.16;
        } else if ("VAT12".equals(taxRateId)) {
            return amount * 0.12;
        }
        return 0.0;
    }

    public void updateTotal() {
        this.total.set(this.price.get() * this.quantity.get());
        this.totalVAT.set(calculateVAT(this.total.get(), getTaxRate()));
    }

    // Getters
    public long getId() { return id; }
    public String getBarcode() { return barcode.get(); }
    public String getName() { return name.get(); }
    public String getDescription() { return description.get(); }
    public double getPrice() { return price.get(); }
    public double getQuantity() { return quantity.get(); }
    public String getUnitOfMeasure() { return unitOfMeasure.get(); }
    public String getTaxRate() { return taxRate.get(); }
    public double getTotal() { return total.get(); }
    public double getDiscount() { return discount.get(); }
    public double getTotalVAT() { return totalVAT.get(); }
    public boolean isProduct() { return isProduct; }

    // Setters
    public void setId(long id) { this.id = id; }

    public void setDiscount(double discount) {
        this.discount.set(discount);
        updateTotal();
    }

    public void setPrice(double price) {
        this.price.set(price);
        updateTotal();
    }

    public void setIsProduct(boolean isProduct) {
        this.isProduct = isProduct;
    }

    public void setQuantity(double quantity) {
        this.quantity.set(quantity);
        updateTotal();
    }

    public void increaseQuantity() {
        this.quantity.set(this.quantity.get() + 1);
    }

    // Discount tracking
    public double getOriginalPrice() {
        return originalPrice > 0 ? originalPrice : price.get();
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(double discountPercent) {
        this.discountPercent = discountPercent;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    // JavaFX properties
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleDoubleProperty priceProperty() { return price; }
    public SimpleDoubleProperty quantityProperty() { return quantity; }
    public SimpleDoubleProperty discountProperty() { return discount; }
    public SimpleDoubleProperty totalVATProperty() { return totalVAT; }
    public SimpleDoubleProperty totalProperty() {
    return total;
}

    // Clone method
    @Override
    public Product clone() {
        Product cloned = new Product(
            this.getBarcode(),
            this.getName(),
            this.getDescription(),
            this.getPrice(),
            this.getTaxRate(),
            this.getQuantity(),
            this.getUnitOfMeasure(),
            this.isProduct()
        );
        cloned.setId(this.getId());
        cloned.setDiscount(this.getDiscount());
        cloned.setOriginalPrice(this.getOriginalPrice());
        cloned.setDiscountPercent(this.getDiscountPercent());
        cloned.setDiscountAmount(this.getDiscountAmount());
        return cloned;
    }
}
