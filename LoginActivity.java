package com.example.hsbcapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button submitButton, biometricLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        submitButton = findViewById(R.id.submit);
        biometricLoginButton = findViewById(R.id.biometric_login);

        // Regular login button listener
        submitButton.setOnClickListener(v -> {
            String user = username.getText().toString();
            String pass = password.getText().toString();
            if (validateCredentials(user, pass)) {
                // Move to next screen
                Intent intent = new Intent(LoginActivity.this, ImageUploadActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        // Biometric login button listener
        biometricLoginButton.setOnClickListener(v -> showBiometricPrompt());
    }

    private boolean validateCredentials(String user, String pass) {
        // For now, using dummy validation
        return user.equals("admin") && pass.equals("1234");
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        // Move to Image Upload screen on success
                        Intent intent = new Intent(LoginActivity.this, ImageUploadActivity.class);
                        startActivity(intent);
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Login")
                .setDescription("Use your fingerprint to log in")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}