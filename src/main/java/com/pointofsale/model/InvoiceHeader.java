package com.pointofsale.model;

public class InvoiceHeader {
    private String invoiceNumber;
    private String invoiceDateTime;
    private String sellerTIN;
    private String buyerTIN;
    private String buyerAuthorizationCode;
    private String siteId;
    private int globalConfigVersion;
    private int taxpayerConfigVersion;
    private int terminalConfigVersion;
    private boolean isReliefSupply;
    private Vat5CertificateDetails vat5CertificateDetails;
    private String paymentMethod;

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceDateTime() {
        return invoiceDateTime;
    }

    public void setInvoiceDateTime(String invoiceDateTime) {
        this.invoiceDateTime = invoiceDateTime;
    }

    public String getSellerTIN() {
        return sellerTIN;
    }

    public void setSellerTIN(String sellerTIN) {
        this.sellerTIN = sellerTIN;
    }

    public String getBuyerTIN() {
        return buyerTIN;
    }

    public void setBuyerTIN(String buyerTIN) {
        this.buyerTIN = buyerTIN;
    }

    public String getBuyerAuthorizationCode() {
        return buyerAuthorizationCode;
    }

    public void setBuyerAuthorizationCode(String buyerAuthorizationCode) {
        this.buyerAuthorizationCode = buyerAuthorizationCode;
    }

    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public int getGlobalConfigVersion() {
        return globalConfigVersion;
    }

    public void setGlobalConfigVersion(int globalConfigVersion) {
        this.globalConfigVersion = globalConfigVersion;
    }

    public int getTaxpayerConfigVersion() {
        return taxpayerConfigVersion;
    }

    public void setTaxpayerConfigVersion(int taxpayerConfigVersion) {
        this.taxpayerConfigVersion = taxpayerConfigVersion;
    }

    public int getTerminalConfigVersion() {
        return terminalConfigVersion;
    }

    public void setTerminalConfigVersion(int terminalConfigVersion) {
        this.terminalConfigVersion = terminalConfigVersion;
    }

    public boolean isReliefSupply() {
        return isReliefSupply;
    }

    public void setReliefSupply(boolean reliefSupply) {
        isReliefSupply = reliefSupply;
    }

    public Vat5CertificateDetails getVat5CertificateDetails() {
        return vat5CertificateDetails;
    }

    public void setVat5CertificateDetails(Vat5CertificateDetails vat5CertificateDetails) {
        this.vat5CertificateDetails = vat5CertificateDetails;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
