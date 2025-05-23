
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
    
}
