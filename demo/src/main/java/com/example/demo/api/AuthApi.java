package com.example.demo.api;

import com.example.demo.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.http.HttpResponse;

public final class AuthApi {
    private AuthApi() {}

    private static final ObjectMapper M = new ObjectMapper();

    private static String path(String key, String def) {
        return ClientProps.getOr(key, def);
    }

    public static AuthResponse login(LoginRequest req) throws Exception {
        String body = M.writeValueAsString(req);
        HttpResponse<String> res = ApiClient.post(path("auth.login", "/api/auth/login"), body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return parseAuthResponse(res.body());
    }

    // Resend or send OTP code (backend-specific; we send just {"username":...})
    public static void requestOtp(String username) throws Exception {
        ObjectNode n = M.createObjectNode();
        n.put("username", username);
        HttpResponse<String> res = ApiClient.post(path("auth.otp.resend", "/api/auth/otp/request"),
                M.writeValueAsString(n));
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
    }

    public static AuthResponse verifyOtp(OtpVerifyRequest req) throws Exception {
        String body = M.writeValueAsString(req);
        HttpResponse<String> res = ApiClient.post(path("auth.otp", "/api/auth/otp/verify"), body);
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        return parseAuthResponse(res.body());
    }

    // AuthApi.java
    public static void register(RegisterRequest req) throws Exception {
        String body = M.writeValueAsString(req);
        HttpResponse<String> res = ApiClient.post(path("auth.register", "/api/auth/register"), body);

        int code = res.statusCode();
        if (code / 100 != 2) {
            String msg = extractMessage(res.body());

            // Friendly defaults for common cases
            if (code == 409) {
                if (msg == null || msg.isBlank()) msg = "Username or email already exists.";
                throw new RuntimeException(msg);
            } else if (code == 400) {
                if (msg == null || msg.isBlank()) msg = "Please check your inputs.";
                throw new RuntimeException(msg);
            }
            throw new RuntimeException("Error (" + code + "): " + (msg == null ? "" : msg));
        }
    }

    private static String extractMessage(String body) {
        try {
            if (body == null || body.isBlank()) return "";
            var n = M.readTree(body);
            if (n.has("message")) return n.get("message").asText();
            if (n.has("error"))   return n.get("error").asText();
            if (n.isTextual())    return n.asText();
            // sometimes servers return a list of validation messages:
            if (n.has("errors") && n.get("errors").isArray() && n.get("errors").size() > 0) {
                return n.get("errors").get(0).asText();
            }
        } catch (Exception ignore) {}
        return body == null ? "" : body;
    }


    public static void requestResetOtp(String email) throws Exception {
        ObjectNode n = M.createObjectNode();
        n.put("email", email == null ? "" : email.trim());

        HttpResponse<String> res = ApiClient.post(
                path("auth.password.forgot", "/api/auth/password/forgot"),
                M.writeValueAsString(n)
        );
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
    }


    public static void resetWithCode(String email, String code, String newPassword) throws Exception {
        ObjectNode n = M.createObjectNode();
        n.put("email", email == null ? "" : email.trim());
        n.put("code", code == null ? "" : code.trim());
        n.put("newPassword", newPassword == null ? "" : newPassword.trim());

        HttpResponse<String> res = ApiClient.post(
                path("auth.reset", "/api/auth/password/reset"),
                M.writeValueAsString(n)
        );
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
    }




    public static MeResponse me() throws Exception {
        HttpResponse<String> res = ApiClient.get(path("me", "/users/me"));
        if (res.statusCode() / 100 != 2) {
            throw new RuntimeException("HTTP " + res.statusCode() + " - " + safe(res.body()));
        }
        JsonNode n = M.readTree(res.body());
        if (n.hasNonNull("data")) n = n.get("data");     // handle wrapped responses
        return M.treeToValue(n, MeResponse.class);       // tolerant mapping
    }


    // ------- helpers -------

    // Accepts token under many names + accepts {"status":"OTP_REQUIRED"}
    private static AuthResponse parseAuthResponse(String json) throws Exception {
        JsonNode n = M.readTree(json);

        String token = pickString(n, "token", "accessToken", "jwt", "id_token", "access_token");
        boolean otpRequired = pickBoolean(n, "otpRequired", "otp_required", "requiresOtp", "otp");

        if (n.hasNonNull("status")) {
            String s = n.get("status").asText();
            if (s != null && s.equalsIgnoreCase("OTP_REQUIRED")) otpRequired = true;
        }

        if ((token == null || token.isBlank()) && n.hasNonNull("data")) {
            JsonNode d = n.get("data");
            if (d.hasNonNull("status")) {
                String s = d.get("status").asText();
                if (s != null && s.equalsIgnoreCase("OTP_REQUIRED")) otpRequired = true;
            }
            if (token == null) {
                token = pickString(d, "token", "accessToken", "jwt", "id_token", "access_token");
            }
        }

        // If OTP is required, token may legitimately be absent
        if (token == null && !otpRequired) {
            throw new RuntimeException("Login: could not find token in response: " + json);
        }
        return new AuthResponse( otpRequired , token);
    }

    private static String pickString(JsonNode n, String... keys) {
        for (String k : keys) if (n.hasNonNull(k)) return n.get(k).asText();
        return null;
    }

    private static boolean pickBoolean(JsonNode n, String... keys) {
        for (String k : keys) if (n.has(k)) return n.get(k).asBoolean();
        return false;
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? "<empty body>" : s;
    }
}
