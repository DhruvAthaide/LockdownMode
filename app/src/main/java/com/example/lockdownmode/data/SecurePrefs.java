package com.example.lockdownmode.data;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.lockdownmode.util.SecurityUtils;

public class SecurePrefs {
    private final SharedPreferences prefs;

    public SecurePrefs(Context context) {
        try {
            MasterKey key = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    "lockdown_prefs",
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setLockdownActive(boolean val) {
        prefs.edit().putBoolean("lockdown_active", val).apply();
    }

    public boolean getLockdownActive() {
        return prefs.getBoolean("lockdown_active", false);
    }

    public void storePinHash(String hash, String salt) {
        prefs.edit().putString("pin_hash", hash).putString("pin_salt", salt).apply();
    }

    public String getPinHash() {
        return prefs.getString("pin_hash", null);
    }

    public String getPinSalt() {
        return prefs.getString("pin_salt", null);
    }

    public boolean isPinCorrect(String enteredPin) {
        String salt = getPinSalt();
        String hash = getPinHash();
        return salt != null && hash != null && SecurityUtils.verifyPin(enteredPin, salt, hash);
    }

    public void setDisableCamera(boolean val) {
        prefs.edit().putBoolean("disable_camera", val).apply();
    }

    public boolean getDisableCamera() {
        return prefs.getBoolean("disable_camera", true);
    }

    public void setDisableMicrophone(boolean val) {
        prefs.edit().putBoolean("disable_microphone", val).apply();
    }

    public boolean getDisableMicrophone() {
        return prefs.getBoolean("disable_microphone", true);
    }

    public void setDisableGps(boolean val) {
        prefs.edit().putBoolean("disable_gps", val).apply();
    }

    public boolean getDisableGps() {
        return prefs.getBoolean("disable_gps", true);
    }

    public void setClearRecentApps(boolean val) {
        prefs.edit().putBoolean("clear_recent_apps", val).apply();
    }

    public boolean getClearRecentApps() {
        return prefs.getBoolean("clear_recent_apps", true);
    }

    public void setKillBackgroundProcesses(boolean val) {
        prefs.edit().putBoolean("kill_background_processes", val).apply();
    }

    public boolean getKillBackgroundProcesses() {
        return prefs.getBoolean("kill_background_processes", true);
    }

    public void setMinimalLauncher(boolean val) {
        prefs.edit().putBoolean("minimal_launcher", val).apply();
    }

    public boolean getMinimalLauncher() {
        return prefs.getBoolean("minimal_launcher", true);
    }

    public void setDelaySeconds(int seconds) {
        prefs.edit().putInt("delay_seconds", seconds).apply();
    }

    public int getDelaySeconds() {
        return prefs.getInt("delay_seconds", 3); // Default 3 seconds
    }

    public void setEmergencyContact(String contact) {
        prefs.edit().putString("emergency_contact", contact).apply();
    }

    public String getEmergencyContact() {
        return prefs.getString("emergency_contact", null);
    }
}