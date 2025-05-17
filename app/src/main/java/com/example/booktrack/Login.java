package com.example.booktrack;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private FirebaseAuth FBAuth;
    EditText emailInput, passwordInput;
    Button loginBtn, signupBtn, forgotPassBtn;
    TextView loginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        View mainView = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        emailInput = findViewById(R.id.email_login);
        passwordInput = findViewById(R.id.password_login);
        loginBtn = findViewById(R.id.login_btn);
        signupBtn = findViewById(R.id.signup_btn);
        forgotPassBtn = findViewById(R.id.forgotpass_btn);
        loginText = findViewById(R.id.login_text);

        // Colors & Styling
        loginBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signupBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        forgotPassBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        loginBtn.setTextColor(Color.BLACK);
        signupBtn.setTextColor(Color.BLACK);
        forgotPassBtn.setTextColor(Color.BLACK);
        loginText.setTextColor(Color.parseColor("#d9b99b"));
        mainView.setBackgroundColor(Color.parseColor("#eed9c4"));

        FBAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> loginUser(emailInput, passwordInput));
        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Signup.class);
            startActivity(intent);
        });
        forgotPassBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, forgotPass.class);
            startActivity(intent);


        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                new AlertDialog.Builder(this)
                        .setTitle("Allow Background Alarms")
                        .setMessage("To ensure alarms work when the phone is idle, allow this app to ignore battery optimizations.")
                        .setPositiveButton("Allow", (dialog, which) -> {
                            Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }

        }


    }

    private void loginUser(EditText emailInput, EditText passwordInput) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        FBAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FBAuth.getCurrentUser();
                        if (user != null) {
                            getSharedPreferences("BookTrackPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("uid", user.getUid())
                                    .apply();
                        }

                        Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    } else {
                        String errorMsg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(Login.this, "Authentication Failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied. You may miss alarm reminders.", Toast.LENGTH_LONG).show();
                }
            });


}