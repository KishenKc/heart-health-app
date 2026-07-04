package com.example.myapplication_java;

import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64; // ✅ use Android Base64 (more compatible on Android)

public class PasswordHasher {

    // Simple SHA-256 hash with salt
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));

            return Base64.encodeToString(salt, Base64.NO_WRAP) + ":" +
                    Base64.encodeToString(hashedPassword, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // ✅ Add this method — needed for Login.java
    public static boolean verifyPassword(String enteredPassword, String storedValue) {
        try {
            String[] parts = storedValue.split(":");
            if (parts.length != 2) return false;

            byte[] salt = Base64.decode(parts[0], Base64.NO_WRAP);
            byte[] storedHash = Base64.decode(parts[1], Base64.NO_WRAP);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] newHash = md.digest(enteredPassword.getBytes("UTF-8"));

            if (newHash.length != storedHash.length) return false;
            int diff = 0;
            for (int i = 0; i < newHash.length; i++) diff |= (newHash[i] ^ storedHash[i]);
            return diff == 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
