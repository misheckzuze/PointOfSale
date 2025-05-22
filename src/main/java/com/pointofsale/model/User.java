package com.pointofsale.model;

public class User {
    private int userID;
    private String firstName;
    private String lastName;
    private String userName;
    private String gender;
    private String phoneNumber;
    private String emailAddress;
    private String address;
    private String role;
    private String password;
    
    // Default constructor
    public User() {}
    
    // Constructor with all fields
    public User(int userID, String firstName, String lastName, String userName, 
                String gender, String phoneNumber, String emailAddress, 
                String address, String role, String password) {
        this.userID = userID;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.address = address;
        this.role = role;
        this.password = password;
    }
    
    // Getters and Setters
    public int getUserID() { return userID; }
    public void setUserID(int userID) { this.userID = userID; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    // Helper method to get full name
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    @Override
    public String toString() {
        return userName + " (" + getFullName() + ")";
    }
}
