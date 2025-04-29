package com.pointofsale.model;

import java.util.List;


public class TerminalUnblockStatusResponse {
    
    public TerminalUnblockStatusResponse() {} 
    
    public int statusCode;
    public String remark;
    public CheckResult data;
    public List<ApiError> errors;
}

