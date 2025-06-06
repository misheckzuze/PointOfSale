package com.pointofsale.model;


public class TerminalContactInfo {
    public String email;
    public String phone;
    public String addressLine;

    public TerminalContactInfo(String email, String phone, String addressLine) {
        this.email = email;
        this.phone = phone;
        this.addressLine = addressLine;
    }
}

