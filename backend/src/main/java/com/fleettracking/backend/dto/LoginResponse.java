package com.fleettracking.backend.dto;

public class LoginResponse {
    private boolean success;
    private String role;       // "admin" or "chauffeur"
    private String userId;
    private String name;

    public LoginResponse() {}

    public LoginResponse(boolean success, String role, String userId, String name) {
        this.success = success;
        this.role = role;
        this.userId = userId;
        this.name = name;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
