package com.pointofsale.model;

import java.util.ArrayList;
import java.util.List;

/** Local working copy, deliberately separate from fiscal invoices. */
public class CartDraft {
    public List<Line> lines = new ArrayList<>();
    public String customer = "", tin = "", authorization = "", tendered = "", note = "";
    public String payment = "Cash", invoiceNumber;
    public double discountAmount, discountPercent;
    public boolean vat5, checkoutStarted;
    public Vat5Data vat5Data;

    public static class Line {
        public String barcode, name, description, unit, tax;
        public long id;
        public double price, originalPrice, quantity, discount, discountAmount, discountPercent, total, vat;
        public boolean product;

        public Line(Product p) {
            barcode = p.getBarcode(); name = p.getName(); description = p.getDescription();
            unit = p.getUnitOfMeasure(); tax = p.getTaxRate(); id = p.getId();
            price = p.getPrice(); originalPrice = p.getOriginalPrice(); quantity = p.getQuantity();
            discount = p.getDiscount(); discountAmount = p.getDiscountAmount();
            discountPercent = p.getDiscountPercent(); total = p.getTotal(); vat = p.getTotalVAT();
            product = p.isProduct();
        }

        public Product toProduct() {
            Product p = new Product(barcode, name, description, price, tax, quantity, unit, product);
            p.setId(id); p.setOriginalPrice(originalPrice); p.setDiscount(discount);
            p.setDiscountAmount(discountAmount); p.setDiscountPercent(discountPercent);
            p.totalProperty().set(total); p.totalVATProperty().set(vat);
            return p;
        }
    }
}
