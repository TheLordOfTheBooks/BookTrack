
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

/**
 * Main login activity for the BookTrack application that handles user authentication
 * and initial app setup including permissions and battery optimization settings.
 *
 * <p>This activity provides a comprehensive login interface with Firebase Authentication,
 * handles notification permissions for Android 13+, manages battery optimization settings
 * for reliable alarm functionality, and includes navigation to signup and password reset flows.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Firebase Authentication integration for secure user login</li>
 *   <li>Automatic notification permission request for devices running Android 13+</li>
 *   <li>Battery optimization bypass request for uninterrupted alarm functionality</li>
 *   <li>Custom UI styling with BookTrack's signature warm color palette</li>
 *   <li>Navigation to registration and password recovery activities</li>
 * </ul></p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class Login extends AppCompatActivity {

    /** Firebase Authentication instance for handling user login operations */
    private FirebaseAuth FBAuth;

    /** Input field for user's email address */
    EditText emailInput;

    /** Input field for user's password */
    EditText passwordInput;

    /** Button to trigger the login authentication process */
    Button loginBtn;

    /** Button to navigate to the signup/registration activity */
    Button signupBtn;

    /** Button to navigate to the forgot password activity */
    Button forgotPassBtn;

    /** TextView displaying the main login header text */
    TextView loginText;

    /**
     * Initializes the login activity, sets up the user interface, handles permissions,
     * and configures authentication components.
     *
     * <p>This method performs several critical setup operations:
     * <ul>
     *   <li>Requests notification permissions on Android 13+ devices</li>
     *   <li>Configures window insets for edge-to-edge display</li>
     *   <li>Initializes all UI components and Firebase Authentication</li>
     *   <li>Applies custom styling with BookTrack's color theme</li>
     *   <li>Sets up click listeners for login, signup, and forgot password actions</li>
     *   <li>Prompts user to disable battery optimization for reliable alarms</li>
     * </ul></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
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
        FBAuth = FirebaseAuth.getInstance();

        loginBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signupBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        forgotPassBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        loginBtn.setTextColor(Color.BLACK);
        signupBtn.setTextColor(Color.BLACK);
        forgotPassBtn.setTextColor(Color.BLACK);
        loginText.setTextColor(Color.parseColor("#d9b99b"));
        mainView.setBackgroundColor(Color.parseColor("#eed9c4"));

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

    /**
     * Handles user authentication using Firebase Authentication with email and password.
     *
     * <p>This method validates user input, attempts authentication with Firebase,
     * stores user session data upon successful login, and provides appropriate feedback
     * for both successful and failed authentication attempts.</p>
     *
     * <p>The authentication process includes:
     * <ul>
     *   <li>Input validation for empty email and password fields</li>
     *   <li>Firebase authentication attempt with provided credentials</li>
     *   <li>User ID storage in SharedPreferences for session management</li>
     *   <li>Navigation to MainActivity upon successful authentication</li>
     *   <li>Error message display for authentication failures</li>
     * </ul></p>
     *
     * @param emailInput    The EditText containing the user's email address
     * @param passwordInput The EditText containing the user's password
     */
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

    /**
     * ActivityResultLauncher for handling notification permission requests on Android 13+ devices.
     *
     * <p>This launcher is triggered when the app needs to request POST_NOTIFICATIONS permission
     * on devices running Android API level 33 (TIRAMISU) or higher. It handles the user's response
     * to the permission request and provides feedback if the permission is denied.</p>
     *
     * <p>If permission is denied, the user receives a toast notification explaining that they
     * may miss alarm reminders, but the app continues to function normally.</p>
     */
    private final ActivityResultLauncher<String> requestNotificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notification permission denied. You may miss alarm reminders.", Toast.LENGTH_LONG).show();
                }
            });
}