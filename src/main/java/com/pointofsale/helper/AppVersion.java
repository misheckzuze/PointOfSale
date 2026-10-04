package com.pointofsale.helper;

import java.io.InputStream;
import java.util.Properties;

public final class AppVersion {
    public static String normalize(String version) {
        if (version == null || !version.matches("\\d+(\\.\\d+){0,2}(-[A-Za-z0-9.-]+)?")) return "0.0.0";
        String[] parts = version.split("-", 2)[0].split("\\.");
        return parts[0] + "." + (parts.length > 1 ? parts[1] : "0") + "." + (parts.length > 2 ? parts[2] : "0");
    }

    public static String get() {
        try (InputStream in = AppVersion.class.getResourceAsStream("/app-version.properties")) {
            if (in == null) return "0.0.0";
            Properties p = new Properties(); p.load(in);
            return normalize(p.getProperty("version"));
        } catch (java.io.IOException e) { return "0.0.0"; }
    }
}
