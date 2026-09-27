package com.pointofsale.model;

public class SubmitResult {
    public boolean success;
    public String validationUrl = "";
    public boolean networkFailure;   // true only for real connectivity/exception failures
    public int statusCode;
    public String remark = "";
}
