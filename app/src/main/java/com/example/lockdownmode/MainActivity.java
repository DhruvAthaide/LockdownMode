package com.example.lockdownmode;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.di.Injector;
import com.example.lockdownmode.util.SecurityUtils;

public class MainActivity extends AppCompatActivity {

    private LockdownViewModel viewModel;
    private SecurePrefs prefs;
    private ActivityResultLauncher<String> requestSmsPermissionLauncher;
    private ActivityResultLauncher<String[]> requestLocationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = Injector.providePrefs(this);

        // Check if lockdown is active
        if (prefs.getLockdownActive()) {
            startActivity(new Intent(this, LockdownLauncherActivity.class));
            finish();
            return;
        }

        // Force PIN setup if not set
        if (prefs.getPinHash() == null || prefs.getPinSalt() == null) {
            promptForNewPin(prefs);
        }

        viewModel = new ViewModelProvider(this, Injector.provideVMMFactory(this))
                .get(LockdownViewModel.class);

        // Initialize UI components
        Button btnActivate = findViewById(R.id.lockdown_button);
        ImageButton gearButton = findViewById(R.id.gear_button);
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navShield = findViewById(R.id.nav_shield);

        // Update status texts
        updateStatusTexts();

        // Set up permission launchers
        requestSmsPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(this, "SMS permission needed for emergency contact", Toast.LENGTH_SHORT).show();
            } else {
                activateLockdownWithDelay();
            }
        });

        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) == Boolean.TRUE &&
                    result.get(Manifest.permission.ACCESS_COARSE_LOCATION) == Boolean.TRUE) {
                activateLockdownWithDelay();
            } else {
                Toast.makeText(this, "Location permissions needed for GPS disabling", Toast.LENGTH_SHORT).show();
            }
        });

        // Navigation listeners
        btnActivate.setOnClickListener(view -> {
            if (prefs.getEmergencyContact() != null && ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
            } else if (prefs.getDisableGps() && (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                requestLocationPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            } else {
                activateLockdownWithDelay();
            }
        });

        gearButton.setOnClickListener(view -> {
            startActivity(new Intent(this, LockdownSettingsActivity.class));
            finish();
        });

        navHome.setOnClickListener(view -> {
            // Already on MainActivity, do nothing or refresh
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        navShield.setOnClickListener(view -> {
            startActivity(new Intent(this, LockdownSettingsActivity.class));
            finish();
        });

        viewModel.getIsLockdownActive().observe(this, isActive -> {
            if (isActive) {
                startActivity(new Intent(this, LockdownLauncherActivity.class));
                finish();
            }
        });
    }

    private void activateLockdownWithDelay() {
        int delaySeconds = prefs.getDelaySeconds();
        new Handler(Looper.getMainLooper()).postDelayed(() -> viewModel.activateLockdown(this), delaySeconds * 1000L);
    }

    private void updateStatusTexts() {
        TextView cameraStatus = findViewById(R.id.camera_status);
        TextView microphoneStatus = findViewById(R.id.microphone_status);
        TextView gpsStatus = findViewById(R.id.gps_status);
        TextView backgroundAppsStatus = findViewById(R.id.background_apps_status);
        TextView launcherModeStatus = findViewById(R.id.launcher_mode_status);

        cameraStatus.setText(prefs.getDisableCamera() ? "Disabled" : "Enabled");
        microphoneStatus.setText(prefs.getDisableMicrophone() ? "Disabled" : "Enabled");
        gpsStatus.setText(prefs.getDisableGps() ? "Disabled" : "Enabled");
        backgroundAppsStatus.setText(prefs.getKillBackgroundProcesses() ? "Disabled" : "Enabled");
        launcherModeStatus.setText(prefs.getMinimalLauncher() ? "Enabled" : "Disabled");
    }

    private void promptForNewPin(SecurePrefs prefs) {
        final EditText etPin1 = new EditText(this);
        etPin1.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etPin1.setHint("Enter new PIN");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Set PIN")
                .setMessage("Choose a PIN for exiting lockdown mode (at least 4 digits):")
                .setCancelable(false)
                .setView(etPin1)
                .setPositiveButton("Next", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin1 = etPin1.getText().toString().trim();
            if (pin1.length() < 4) {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.dismiss();
            confirmPin(prefs, pin1);
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> finish());
    }

    private void confirmPin(SecurePrefs prefs, String pin1) {
        final EditText etPin2 = new EditText(this);
        etPin2.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        etPin2.setHint("Confirm PIN");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm PIN")
                .setMessage("Re-enter your new PIN for confirmation:")
                .setCancelable(false)
                .setView(etPin2)
                .setPositiveButton("Set PIN", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String pin2 = etPin2.getText().toString().trim();
            if (!pin1.equals(pin2)) {
                Toast.makeText(this, "PINs do not match, try again.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                promptForNewPin(prefs);
                return;
            }
            String salt = SecurityUtils.generateRandomSalt();
            String hash = SecurityUtils.hashPin(pin1, salt);
            prefs.storePinHash(hash, salt);
            Toast.makeText(this, "PIN set successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            dialog.dismiss();
            promptForNewPin(prefs);
        });
    }
}