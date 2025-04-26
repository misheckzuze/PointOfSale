
package com.pointofsale.model;


public class TaxRates {
    
    private String taxRateId;
    private Double rate;
    
    public TaxRates(String taxRateId, Double rate){
        this.taxRateId = taxRateId;
        this.rate = rate;
    }
    
    public String getTaxRateId(){
        return taxRateId;
    }
    public Double getRate(){
        return rate;
    }
    
}
