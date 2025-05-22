package com.pointofsale.model;

import javafx.beans.property.SimpleStringProperty;

public class Customer {
    
    private final SimpleStringProperty id;
    private final SimpleStringProperty name;
    private final SimpleStringProperty email;
    private final SimpleStringProperty phone;
    private final SimpleStringProperty tin;
    private final SimpleStringProperty status;
    private final SimpleStringProperty address;
    private final SimpleStringProperty registeredDate;
    private final SimpleStringProperty lastPurchaseDate;
    private final SimpleStringProperty notes;
   
    public Customer(String id, String name, String email, String phone) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.tin = new SimpleStringProperty("");
        this.status = new SimpleStringProperty("Active");
        this.address = new SimpleStringProperty("");
        this.registeredDate = new SimpleStringProperty("");
        this.lastPurchaseDate = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty("");
    }
    
    // Overloaded constructor with more parameters
    public Customer(String id, String name, String email, String phone,
                   String tin, String status) {
        this.id = new SimpleStringProperty(id);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.phone = new SimpleStringProperty(phone);
        this.tin = new SimpleStringProperty(tin != null ? tin : "");
        this.status = new SimpleStringProperty(status != null ? status : "Active");
        this.address = new SimpleStringProperty("");
        this.registeredDate = new SimpleStringProperty("");
        this.lastPurchaseDate = new SimpleStringProperty("");
        this.notes = new SimpleStringProperty("");
    }
    
    // Getters
    public String getId() { return id.get(); }
    public String getName() { return name.get(); }
    public String getEmail() { return email.get(); }
    public String getPhone() { return phone.get(); }
    public String getTin() { return tin.get(); }
    public String getStatus() { return status.get(); }
    public String getAddress() { return address.get(); }
    public String getRegisteredDate() { return registeredDate.get(); }
    public String getLastPurchaseDate() { return lastPurchaseDate.get(); }
    public String getNotes() { return notes.get(); }
    
    // Setters
    public void setName(String name) { this.name.set(name); }
    public void setEmail(String email) { this.email.set(email); }
    public void setPhone(String phone) { this.phone.set(phone); }
    public void setTin(String tin) { this.tin.set(tin); }
    public void setStatus(String status) { this.status.set(status); }
    public void setAddress(String address) { this.address.set(address); }
    public void setRegisteredDate(String date) { this.registeredDate.set(date); }
    public void setLastPurchaseDate(String date) { this.lastPurchaseDate.set(date); }
    public void setNotes(String notes) { this.notes.set(notes); }
    
    // JavaFX properties
    public SimpleStringProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleStringProperty emailProperty() { return email; }
    public SimpleStringProperty phoneProperty() { return phone; }
    public SimpleStringProperty tinProperty() { return tin; }
    public SimpleStringProperty statusProperty() { return status; }
    public SimpleStringProperty addressProperty() { return address; }
    public SimpleStringProperty registeredDateProperty() { return registeredDate; }
    public SimpleStringProperty lastPurchaseDateProperty() { return lastPurchaseDate; }
    public SimpleStringProperty notesProperty() { return notes; }
}