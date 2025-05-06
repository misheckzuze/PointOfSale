package com.pointofsale.model;

import java.util.List;

public class TerminalBlockingResponse {
    
    public int statusCode;
    public String remark;
    public TerminalBlockingInfo data;
     public List<ApiError> errors;
}
