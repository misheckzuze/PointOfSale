package com.pointofsale.model;

import java.util.List;

public class PingResponse {
    public int statusCode;
    public String remark;
    public Data data;
    public List<ApiError> errors;

    public static class Data {
        public String serverDate;
    }

    public static class ApiError {
        public int errorCode;
        public String fieldName;
        public String errorMessage;
    }
}

