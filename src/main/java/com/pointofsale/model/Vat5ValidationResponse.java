package com.pointofsale.model;

// Vat5ValidationResponse.java

import java.util.List;

public class Vat5ValidationResponse {
    public int statusCode;
    public String remark;
    public Vat5Data data;
    public List<ApiError> errors;
}
