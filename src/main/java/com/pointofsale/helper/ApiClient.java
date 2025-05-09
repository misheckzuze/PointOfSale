package com.pointofsale.helper;

import javafx.application.Platform;

import javax.json.*;
import javax.net.ssl.*;
import java.io.StringReader;
import java.net.URI;
import java.net.http.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import com.pointofsale.utils.ApiEndpoints;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import com.google.gson.Gson;
import java.sql.Connection;
import java.util.List;
import java.time.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.function.BiConsumer;
import com.pointofsale.model.InvoiceSummary;
import com.pointofsale.model.TerminalBlockingInfo;
import com.pointofsale.model.TerminalBlockingResponse;
import com.pointofsale.model.LineItemDto;
import com.pointofsale.model.TerminalUnblockStatusResponse;
import com.pointofsale.model.CheckResult;
import com.pointofsale.model.InvoiceHeader;
import com.pointofsale.model.InvoicePayload;
import com.pointofsale.model.TaxBreakDown;
import com.pointofsale.data.Database;


public class ApiClient {

    private final HttpClient httpClient;

    public ApiClient() {
        HttpClient client = null;
        try {
            System.out.println("Initializing ApiClient with insecure SSL HttpClient...");
            client = createInsecureHttpClient();
            System.out.println("Insecure HttpClient created successfully.");
        } catch (Exception e) {
            System.err.println("Failed to create insecure HttpClient: " + e.getMessage());
            e.printStackTrace();
        }
        this.httpClient = client;
    }

    private HttpClient createInsecureHttpClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
        };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }

    public void activate(String activationCode, Consumer<Boolean> onResult) {
        System.out.println("Starting activation process...");

        new Thread(() -> {
            try {
                Map<String, String> info = getSystemInfo();
                String json = createJsonPayload(activationCode, info);
                String url = ApiEndpoints.BASE_URL + ApiEndpoints.ACTIVATE_TERMINAL;
                System.out.println("Sending request to: " + url);
                System.out.println("Request payload: " + json);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Accept", "text/plain")
                        .timeout(java.time.Duration.ofSeconds(30))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("Received response. Status code: " + response.statusCode());
                System.out.println("Response body: " + response.body());

                final boolean success = (response.statusCode() == 200) &&
                        isActivationSuccessful(response.body(), activationCode);

                Platform.runLater(() -> onResult.accept(success));

            } catch (Exception e) {
                System.err.println("Exception during activation: " + e.getMessage());
                e.printStackTrace();
                Platform.runLater(() -> onResult.accept(false));
            }
        }).start();
    }

   public void confirmActivation(String xSignature, String terminalId, String bearerToken, Consumer<Boolean> callback) {

    String url = ApiEndpoints.BASE_URL + ApiEndpoints.CONFIRM_TERMINAL_ACTIVATION;

    JsonObject bodyJson = Json.createObjectBuilder()
            .add("terminalId", terminalId)
            .build();

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("x-signature", xSignature)
            .header("Authorization", "Bearer " + bearerToken) // 🔐 Add bearer token here
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(bodyJson.toString()))
            .build();

    new Thread(() -> {
        boolean success = false;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonReader reader = Json.createReader(new StringReader(response.body()));
                JsonObject json = reader.readObject();

                int statusCode = json.getInt("statusCode", 0);
                String remark = json.getString("remark", "");

                if (statusCode == 1) {
                    System.out.println("✅ Terminal activation confirmed: " + remark);
                    
                    boolean isActive = json.getBoolean("data");
                     // Insert the 'isActive' value into the database
                    boolean insertResult = Helper.updateIsActiveInTerminalConfiguration(isActive);

                    // If insertion was successful, set success to true
                    success = insertResult;

                } else {
                    System.err.println("⚠ Terminal activation failed: " + remark);
                }
            } else {
                System.err.println("❌ Failed to confirm activation. HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error during terminal activation confirmation: " + e.getMessage());
            e.printStackTrace();
        }

        boolean finalSuccess = success;
        javafx.application.Platform.runLater(() -> callback.accept(finalSuccess));
    }).start();
}

    private Map<String, String> getSystemInfo() {
        System.out.println("Collecting system information...");
        Map<String, String> systemInfo = new HashMap<>();
        systemInfo.put("osName", Helper.getOSName());
        systemInfo.put("osVersion", Helper.getOSVersion());
        systemInfo.put("osArchitecture", Helper.getOSArchitecture());
        systemInfo.put("osBuild", Helper.getOSBuild());
        systemInfo.put("macAddress", Helper.getMacAddress());
        System.out.println("System info collected: " + systemInfo);
        return systemInfo;
    }

    private String createJsonPayload(String code, Map<String, String> info) {
        System.out.println("Creating JSON payload...");
        JsonObject payload = Json.createObjectBuilder()
                .add("terminalActivationCode", code)
                .add("environment", Json.createObjectBuilder()
                        .add("platform", Json.createObjectBuilder()
                                .add("osName", info.getOrDefault("osName", "Unknown"))
                                .add("osVersion", info.getOrDefault("osVersion", "Unknown"))
                                .add("osBuild", info.getOrDefault("osBuild", ""))
                                .add("macAddress", info.getOrDefault("macAddress", "Unknown"))
                        )
                        .add("pos", Json.createObjectBuilder()
                                .add("productID", "POS12345")
                                .add("productVersion", "1.0.0")
                        )
                )
                .build();
        String jsonString = payload.toString();
        System.out.println("JSON payload created: " + jsonString);
        return jsonString;
    }
    
    public void fetchBlockingMessage(String terminalId, String bearerToken,  Consumer<TerminalBlockingInfo> onResult) {
    System.out.println("Starting fetchBlockingMessage...");

    new Thread(() -> {
        try {
            String json = createTerminalIdPayload(terminalId);
            String url = ApiEndpoints.BASE_URL + ApiEndpoints.TERMINAL_BLOCKING_MESSAGE;

            System.out.println("Sending request to: " + url);
            System.out.println("Request payload: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Accept", "text/plain")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Received response. Status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            TerminalBlockingInfo result = null;

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                TerminalBlockingResponse blockingResponse = mapper.readValue(response.body(), TerminalBlockingResponse.class);
                result = blockingResponse.data;
            }

            TerminalBlockingInfo finalResult = result;
            Platform.runLater(() -> onResult.accept(finalResult));

        } catch (Exception e) {
            System.err.println("Exception during fetchBlockingMessage: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> onResult.accept(null));
        }
    }).start();
}
    
    public void checkIfTerminalIsBlocked(String terminalId, String bearerToken, Consumer<CheckResult> onResult) {
    System.out.println("Starting checkIfTerminalIsBlocked...");

    new Thread(() -> {
        try {
            String json = createTerminalIdPayload(terminalId);
            String url = ApiEndpoints.BASE_URL + ApiEndpoints.CHECK_TERMINAL_UNBLOCK_STATUS;

            System.out.println("Sending request to: " + url);
            System.out.println("Request payload: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + bearerToken)
                    .header("Accept", "text/plain")
                    .timeout(Duration.ofSeconds(30))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Received response. Status code: " + response.statusCode());
            System.out.println("Response body: " + response.body());

            CheckResult result = null;

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                TerminalUnblockStatusResponse statusResponse = 
                        mapper.readValue(response.body(), TerminalUnblockStatusResponse.class);
                result = statusResponse.data;  // or statusResponse.data if public field
            }

            CheckResult finalResult = result;
            Platform.runLater(() -> onResult.accept(finalResult));

        } catch (Exception e) {
            System.err.println("Exception during checkIfTerminalIsBlocked: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> onResult.accept(null));
        }
    }).start();
}


    
    private String createTerminalIdPayload(String terminalId) {
    JsonObject payload = Json.createObjectBuilder()
            .add("terminalId", terminalId)
            .build();
    return payload.toString();
}

    private boolean isActivationSuccessful(String responseBody, String code) {
        System.out.println("Checking if activation was successful...");
        try {
            if (responseBody == null || responseBody.isEmpty()) {
                System.err.println("Response body is null or empty");
                return false;
            }

            JsonReader jsonReader = Json.createReader(new StringReader(responseBody));
            JsonObject jsonResponse = jsonReader.readObject();

            System.out.println("Parsed JSON response: " + jsonResponse);

            if (jsonResponse.containsKey("statusCode")) {
                int statusCode = jsonResponse.getInt("statusCode");
                System.out.println("Found statusCode: " + statusCode);
                if (statusCode == 1) {
                    ActivationDataInserter.insertActivationData(responseBody);
                    ActivationDataInserter.insertActivationCode(code);
                    return true;
                } else {
                    System.err.println("Activation failed with statusCode: " + statusCode);
                }
            } else {
                System.out.println("Response does not contain expected statusCode indicator");
            }
            return false;
        } catch (Exception e) {
            System.err.println("Failed to parse response as JSON: " + e.getMessage());
            System.err.println("Raw response: " + responseBody);
            e.printStackTrace();
            return false;
        }
    }
    
    public void getTerminalSiteProducts(String tin, String siteId, String bearerToken, Consumer<Boolean> callback) {
        String url = ApiEndpoints.BASE_URL + ApiEndpoints.GET_TERMINAL_SITE_PRODUCTS;

        JsonObject requestBody = Json.createObjectBuilder()
            .add("tin", tin)
            .add("siteId", siteId)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + bearerToken)
            .header("Content-Type", "application/json")
            .header("Accept", "text/plain")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();

        new Thread(() -> {
            boolean success = false;

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                JsonReader reader = Json.createReader(new StringReader(response.body()));
                JsonObject responseJson = reader.readObject();

                int statusCode = responseJson.getInt("statusCode", 0);
                if (statusCode == 1) {
                    JsonArray dataArray = responseJson.getJsonArray("data");

                    for (JsonValue value : dataArray) {
                        if (value instanceof JsonObject) {
                            JsonObject product = (JsonObject) value;
                            Helper.insertOrUpdateProduct(product); // 🧠 Save to SQLite
                        }
                    }

                    System.out.println("✅ Products saved to database.");
                    success = true;
                } else {
                    System.err.println("⚠ API Error: " + responseJson.getString("remark", ""));
                }
            } else {
                System.err.println("❌ HTTP Error " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error fetching/saving products: " + e.getMessage());
            e.printStackTrace();
        }

          boolean finalSuccess = success;
          Platform.runLater(() -> callback.accept(finalSuccess));
      }).start();
    }
    
  public void submitTransactions(String payloadJson, String bearerToken, BiConsumer<Boolean, String> callback) {
    String url = ApiEndpoints.BASE_URL + ApiEndpoints.SUBMIT_TRANSACTIONS;

    HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + bearerToken)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payloadJson))
            .build();

    new Thread(() -> {
        boolean success = false;
        String validationUrl = "";

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📨 Submitting Transactions...");
            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                JsonReader reader = Json.createReader(new StringReader(response.body()));
                JsonObject json = reader.readObject();

                int statusCode = json.getInt("statusCode", 0);
                String remark = json.getString("remark", "");

                if (statusCode == 1) {
                    success = true;
                    if (json.containsKey("data")) {
                        JsonObject data = json.getJsonObject("data");
                        validationUrl = data.getString("validationURL", "");
                    }
                    System.out.println("✅ Transactions submitted successfully: " + remark);
                } else {
                    System.err.println("⚠️ Submission failed: " + remark);
                }
            } else {
                System.err.println("❌ HTTP error while submitting: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error during transaction submission: " + e.getMessage());
            e.printStackTrace();
        }

        String finalValidationUrl = validationUrl;
        boolean finalSuccess = success;
        Platform.runLater(() -> callback.accept(finalSuccess, finalValidationUrl));
    }).start();
}
  
  public void retryPendingTransactions() {
    String query = "SELECT * FROM Invoices WHERE State = 0";

    try (Connection connection = Database.createConnection();
         PreparedStatement stmt = connection.prepareStatement(query);
         var rs = stmt.executeQuery()) {

        while (rs.next()) {
           String invoiceNumber = rs.getString("InvoiceNumber");
           String paymentId = rs.getString("PaymentId");

           InvoiceHeader header = Helper.getInvoiceHeader(invoiceNumber, "", "", paymentId);
           List<LineItemDto> lineItems = Helper.getLineItems(invoiceNumber);
    
           InvoiceSummary invoiceSummary = new InvoiceSummary();
           List<TaxBreakDown> taxBreakdowns = Helper.generateTaxBreakdown(lineItems);
           invoiceSummary.setTaxBreakDown(taxBreakdowns);

           // ✅ Use values directly from Invoices table
           invoiceSummary.setTotalVAT(rs.getDouble("TotalVAT"));
           invoiceSummary.setInvoiceTotal(rs.getDouble("InvoiceTotal"));
           invoiceSummary.setOfflineSignature("");

           InvoicePayload payload = new InvoicePayload();
           payload.setInvoiceHeader(header);
           payload.setInvoiceLineItems(lineItems);
           payload.setInvoiceSummary(invoiceSummary);
    
           Gson gson = new Gson();
           String jsonPayload = gson.toJson(payload);
           String token = Helper.getToken();

           ApiClient apiClient = new ApiClient();
           apiClient.submitTransactions(jsonPayload, token, (success, returnedValidationUrl) -> {
           if (success) {
            Helper.updateValidationUrl(invoiceNumber, returnedValidationUrl);
            Helper.markAsTransmitted(invoiceNumber);
            System.out.println("✅ Auto-resend success for: " + invoiceNumber);
           } else {
              System.err.println("❌ Auto-resend failed for: " + invoiceNumber);
           }
        });
    }


    } catch (SQLException e) {
        System.err.println("❌ Error fetching pending transactions: " + e.getMessage());
    }
}
  
  public void validateAuthorizationCode(String authCode, String bearerToken, Consumer<Boolean> callback) {
     String url = ApiEndpoints.BASE_URL + ApiEndpoints.VALIDATE_AUTHORIZATION_CODE;

    JsonObject requestBody = Json.createObjectBuilder()
        .add("authorizationCode", authCode)
        .build();

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Authorization", "Bearer " + bearerToken)
        .header("Content-Type", "application/json")
        .header("Accept", "text/plain")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
        .build();

    new Thread(() -> {
        boolean isValid = false;

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status Code: " + response.statusCode());
            System.out.println("Response Body: " + response.body());

            if (response.statusCode() == 200) {
                JsonReader reader = Json.createReader(new StringReader(response.body()));
                JsonObject jsonResponse = reader.readObject();

                int statusCode = jsonResponse.getInt("statusCode", 0);
                if (statusCode == 1 && jsonResponse.containsKey("data")) {
                    JsonObject data = jsonResponse.getJsonObject("data");
                    isValid = data.getBoolean("isValidAuthorizationCode", false);

                    if (!isValid) {
                        System.err.println("⚠ Invalid authorization code.");
                    } else {
                        System.out.println("✅ Authorization code is valid.");
                    }
                } else {
                    System.err.println("⚠ Validation failed: " + jsonResponse.getString("remark", ""));
                }
            } else {
                System.err.println("❌ HTTP Error " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("❌ Error validating authorization code: " + e.getMessage());
            e.printStackTrace();
        }

        boolean finalIsValid = isValid;
        Platform.runLater(() -> callback.accept(finalIsValid));
    }).start();
}


}
