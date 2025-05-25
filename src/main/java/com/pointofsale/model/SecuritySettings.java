package com.pointofsale.model;

public class SecuritySettings {
    public boolean requireLogin;
    public int sessionTimeoutMinutes;
    public boolean enableAuditLog;

    public SecuritySettings(boolean requireLogin, int sessionTimeoutMinutes, boolean enableAuditLog) {
        this.requireLogin = requireLogin;
        this.sessionTimeoutMinutes = sessionTimeoutMinutes;
        this.enableAuditLog = enableAuditLog;
    }
}

