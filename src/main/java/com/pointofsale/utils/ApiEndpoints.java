
package com.pointofsale.utils;

public class ApiEndpoints {
    
    public static final String BASE_URL = "https://dev-eis-api.mra.mw/api/v1";

    // Onboarding
    public static final String ACTIVATE_TERMINAL = "/onboarding/activate-terminal";
    public static final String CONFIRM_TERMINAL_ACTIVATION = "/onboarding/terminal-activated-confirmation";
    
    //Terminal Site Products
    public static final String GET_TERMINAL_SITE_PRODUCTS = "/utilities/get-terminal-site-products";
    
    //Submit transactions
    public static final String SUBMIT_TRANSACTIONS = "/sales/submit-sales-transaction";
    
    //Terminal Blocking
    public static final String TERMINAL_BLOCKING_MESSAGE = "/utilities/get-terminal-blocking-message";
    public static final String CHECK_TERMINAL_UNBLOCK_STATUS = "/utilities/check-terminal-unblock-status";
    
    //Authorization code validation
    public static final String VALIDATE_AUTHORIZATION_CODE = "/utilities/validate-authorization-code";
    
    //Latest configurations
    public static final String GET_LATEST_CONFIG = "/configuration/get-latest-configs";
    
    //OfflineSignature url
    
    public static String OFFLINE_VALIDATION_BASE_URL = "https://dev-eis-portal.mra.mw/ReceiptValidation/Validate";
    
    //ping endpoint
    
    public static String SERVER_PING = "/utilities/ping";
    
    //Vat 5 endpoints
    public static String VALIDATE_VAT5_CERTIFICATE = "/utilities/validate-vat5-certificate";
    
    
    
    
}
