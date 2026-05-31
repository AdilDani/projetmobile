package com.fleettracking.app.data;

/** Body returned by POST /api/auth/login. */
public class LoginResponse {
    public boolean success;
    public String role;     // "admin" or "chauffeur"
    public String userId;
    public String name;
}
