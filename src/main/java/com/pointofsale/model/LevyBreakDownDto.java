
package com.pointofsale.model;


public class LevyBreakDownDto {
    private String levyTypeId;
    private double levyRate;
    private double levyAmount;

    // Default constructor
    public LevyBreakDownDto() {
    }

    // Constructor with parameters
    public LevyBreakDownDto(String levyTypeId, double levyRate, double levyAmount) {
        this.levyTypeId = levyTypeId;
        this.levyRate = levyRate;
        this.levyAmount = levyAmount;
    }

    // Getter and Setter for levyTypeId
    public String getLevyTypeId() {
        return levyTypeId;
    }

    public void setLevyTypeId(String levyTypeId) {
        this.levyTypeId = levyTypeId;
    }

    // Getter and Setter for levyRate
    public double getLevyRate() {
        return levyRate;
    }

    public void setLevyRate(double levyRate) {
        this.levyRate = levyRate;
    }

    // Getter and Setter for levyAmount
    public double getLevyAmount() {
        return levyAmount;
    }

    public void setLevyAmount(double levyAmount) {
        this.levyAmount = levyAmount;
    }

    @Override
    public String toString() {
        return "LevyBreakDownDto{" +
                "levyTypeId='" + levyTypeId + '\'' +
                ", levyRate=" + levyRate +
                ", levyAmount=" + levyAmount +
                '}';
    }
}

