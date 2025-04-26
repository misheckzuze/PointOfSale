package com.pointofsale.model;

public class LineItemDto {
    private long id = 0; // default to 0 as per the API specification
    private String productCode;
    private String description;
    private double unitPrice;
    private double quantity;
    private double discount;
    private double total;
    private double totalVAT;
    private String taxRateId;
    private boolean isProduct = true;
    
     public LineItemDto() {
    }
    // Constructor
    public LineItemDto(String productCode, String description, double unitPrice,
                       double quantity, double discount, double total, double totalVAT, String taxRateId) {
        this.productCode = productCode;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.discount = discount;
        this.total = total;
        this.totalVAT = totalVAT;
        this.taxRateId = taxRateId;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public double getTotalVAT() { return totalVAT; }
    public void setTotalVAT(double totalVAT) { this.totalVAT = totalVAT; }

    public String getTaxRateId() { return taxRateId; }
    public void setTaxRateId(String taxRateId) { this.taxRateId = taxRateId; }

    public boolean isProduct() { return isProduct; }
    public void setIsProduct(boolean isProduct) { this.isProduct = isProduct; }
}
