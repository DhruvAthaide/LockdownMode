package com.example.lockdownmode;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.di.Injector;

public class LockdownSettingsActivity extends AppCompatActivity {

    private SecurePrefs prefs;
    private TextView contactTextView;
    private ActivityResultLauncher<String> requestContactPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdownsettings);

        prefs = Injector.providePrefs(this);

        // Check if lockdown is active
        if (prefs.getLockdownActive()) {
            startActivity(new Intent(this, LockdownLauncherActivity.class));
            finish();
            return;
        }

        // Initialize UI components
        SwitchMaterial cameraSwitch = findViewById(R.id.camera_switch);
        SwitchMaterial microphoneSwitch = findViewById(R.id.microphone_switch);
        SwitchMaterial gpsSwitch = findViewById(R.id.gps_switch);
        SwitchMaterial recentAppsSwitch = findViewById(R.id.recent_apps_switch);
        SwitchMaterial backgroundProcessesSwitch = findViewById(R.id.background_processes_switch);
        SwitchMaterial launcherSwitch = findViewById(R.id.launcher_switch);
        LinearLayout navHome = findViewById(R.id.nav_home);
        LinearLayout navShield = findViewById(R.id.nav_shield);
        ImageButton backButton = findViewById(R.id.back_button);
        LinearLayout contactItem = findViewById(R.id.contact_item);
        Spinner delaySpinner = findViewById(R.id.delay_spinner);
        contactTextView = findViewById(R.id.contact_text);

        // Set up switches
        cameraSwitch.setChecked(prefs.getDisableCamera());
        cameraSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setDisableCamera(isChecked));

        microphoneSwitch.setChecked(prefs.getDisableMicrophone());
        microphoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setDisableMicrophone(isChecked));

        gpsSwitch.setChecked(prefs.getDisableGps());
        gpsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setDisableGps(isChecked));

        recentAppsSwitch.setChecked(prefs.getClearRecentApps());
        recentAppsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setClearRecentApps(isChecked));

        backgroundProcessesSwitch.setChecked(prefs.getKillBackgroundProcesses());
        backgroundProcessesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setKillBackgroundProcesses(isChecked));

        launcherSwitch.setChecked(prefs.getMinimalLauncher());
        launcherSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> prefs.setMinimalLauncher(isChecked));

        // Set up delayed activation spinner
        String[] delayOptions = {"0 seconds", "3 seconds", "5 seconds", "10 seconds"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, delayOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        delaySpinner.setAdapter(adapter);

        // Load saved delay
        int savedDelay = prefs.getDelaySeconds();
        int position = 0;
        switch (savedDelay) {
            case 0: position = 0; break;
            case 3: position = 1; break;
            case 5: position = 2; break;
            case 10: position = 3; break;
        }
        delaySpinner.setSelection(position);

        // Save selected delay
        delaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                int delay = Integer.parseInt(selected.split(" ")[0]);
                prefs.setDelaySeconds(delay);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Update contact text
        String emergencyContact = prefs.getEmergencyContact();
        if (emergencyContact != null && !emergencyContact.isEmpty()) {
            contactTextView.setText("Selected: " + emergencyContact);
        } else {
            contactTextView.setText("None selected");
        }

        // Set up contact picker with permission handling
        requestContactPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContact, REQUEST_CONTACT);
            } else {
                Toast.makeText(this, "Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        });

        contactItem.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(pickContact, REQUEST_CONTACT);
            } else {
                requestContactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS);
            }
        });

        // Navigation listeners
        navHome.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navShield.setOnClickListener(view -> {
            // Already on LockdownSettingsActivity, do nothing or refresh
            Intent intent = new Intent(this, LockdownSettingsActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        backButton.setOnClickListener(view -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private static final int REQUEST_CONTACT = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                if (idColumnIndex == -1) {
                    Toast.makeText(this, "Error retrieving contact ID", Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }
                String id = cursor.getString(idColumnIndex);
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );
                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    int phoneColumnIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    if (phoneColumnIndex == -1) {
                        Toast.makeText(this, "Error retrieving phone number", Toast.LENGTH_SHORT).show();
                        phoneCursor.close();
                        cursor.close();
                        return;
                    }
                    String phoneNumber = phoneCursor.getString(phoneColumnIndex);
                    prefs.setEmergencyContact(phoneNumber);
                    contactTextView.setText("Selected: " + phoneNumber);
                    phoneCursor.close();
                } else {
                    Toast.makeText(this, "No phone number found for contact", Toast.LENGTH_SHORT).show();
                }
                cursor.close();
            } else {
                Toast.makeText(this, "Error retrieving contact", Toast.LENGTH_SHORT).show();
            }
        }
    }
}