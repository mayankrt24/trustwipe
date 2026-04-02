package com.example.trustwipe.dto;

/**
 * Response DTO for authentication operations.
 */
public class AuthResponse {
    private String message;
    private String token;
    private String email;
    private boolean success;

    public AuthResponse() {}

    public AuthResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public AuthResponse(String message, String token, String email, boolean success) {
        this.message = message;
        this.token = token;
        this.email = email;
        this.success = success;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}
