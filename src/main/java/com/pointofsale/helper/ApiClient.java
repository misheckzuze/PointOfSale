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
}
