package com.fleettracking.app.data;

/** Body sent to POST /api/auth/login. */
public class LoginRequest {
    public String login;
    public String password;

    public LoginRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
