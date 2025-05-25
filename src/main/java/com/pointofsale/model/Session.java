package com.pointofsale.model;

import java.time.LocalDateTime;

public class Session {
    public static String currentUsername;
    public static String role;
    public static String firstName;
    public static String lastName;
    public static String currentPassword;

    public static LocalDateTime lastActivityTime;

    public static void updateActivity() {
        lastActivityTime = LocalDateTime.now();
    }

    public static void clear() {
        currentUsername = null;
        currentPassword = null;
        role = null;
        firstName = null;
        lastName = null;
        lastActivityTime = null;
    }
}
