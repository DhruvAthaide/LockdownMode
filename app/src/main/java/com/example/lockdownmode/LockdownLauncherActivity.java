package com.example.lockdownmode;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.lockdownmode.data.SecurePrefs;
import com.example.lockdownmode.di.Injector;

public class LockdownLauncherActivity extends AppCompatActivity {

    private LockdownViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lockdownactive);

        viewModel = new ViewModelProvider(this, Injector.provideVMMFactory(this))
                .get(LockdownViewModel.class);

        Button btnExit = findViewById(R.id.exit_button);
        ImageButton closeButton = findViewById(R.id.close_button);

        btnExit.setOnClickListener(view -> showExitDialog(this));

        closeButton.setOnClickListener(view -> showExitDialog(this));
    }

    @Override
    public void onBackPressed() {
        if (Injector.providePrefs(this).getMinimalLauncher()) {
            showExitDialog(this); // Require PIN to exit
        } else {
            super.onBackPressed();
        }
    }

    private void showExitDialog(Context context) {
        SecurePrefs prefs = Injector.providePrefs(context);
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        new AlertDialog.Builder(context)
                .setTitle("Exit Lockdown")
                .setMessage("Enter your PIN to exit lockdown:")
                .setView(input)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    String pin = input.getText().toString().trim();
                    if (prefs.isPinCorrect(pin)) {
                        viewModel.exitLockdown(context, pin);
                        finish();
                    } else {
                        Toast.makeText(context, "Wrong PIN", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .setCancelable(false) // Prevent dialog dismissal
                .show();
    }
}