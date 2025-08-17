package com.example.lockdownmode.util;

import java.security.MessageDigest;
import java.util.UUID;

public class SecurityUtils {

    public static String hashPin(String pin, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (salt + pin).getBytes();
            byte[] digest = md.digest(bytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean verifyPin(String pin, String salt, String expectedHash) {
        String hash = hashPin(pin, salt);
        return hash != null && hash.equals(expectedHash);
    }
    public static String generateRandomSalt() {
        return UUID.randomUUID().toString();
    }
}