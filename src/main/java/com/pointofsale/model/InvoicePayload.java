package com.pointofsale.model;

import java.util.List;

public class InvoicePayload {
    private InvoiceHeader invoiceHeader;
    private List<LineItemDto> invoiceLineItems;
    private InvoiceSummary invoiceSummary;

    // Getters and Setters
    public InvoiceHeader getInvoiceHeader() {
        return invoiceHeader;
    }

    public void setInvoiceHeader(InvoiceHeader invoiceHeader) {
        this.invoiceHeader = invoiceHeader;
    }

    public List<LineItemDto> getInvoiceLineItems() {
        return invoiceLineItems;
    }

    public void setInvoiceLineItems(List<LineItemDto> invoiceLineItems) {
        this.invoiceLineItems = invoiceLineItems;
    }

    public InvoiceSummary getInvoiceSummary() {
        return invoiceSummary;
    }

    public void setInvoiceSummary(InvoiceSummary invoiceSummary) {
        this.invoiceSummary = invoiceSummary;
    }
}
