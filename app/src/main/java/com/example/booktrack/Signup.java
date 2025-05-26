package com.example.booktrack;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity class that handles user registration/signup functionality for the BookTrack application.
 * This activity provides a user interface for creating new user accounts using Firebase Authentication.
 *
 * <p>The activity includes email and password validation, custom UI styling with a warm color scheme,
 * and proper error handling for registration failures.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class Signup extends AppCompatActivity {

    /** Input field for user's email address */
    EditText emailInput;

    /** Input field for user's password */
    EditText passwordInput;

    /** Button to trigger the signup process */
    Button signupBtn;

    /** TextView displaying password requirements to the user */
    TextView passMust;

    /** TextView for additional signup-related text display */
    TextView signUpTextView;

    /** Floating action button for navigation (back arrow) */
    FloatingActionButton arrow;

    /** Firebase Authentication instance for handling user registration */
    private FirebaseAuth FBAuth;

    /**
     * Called when the activity is first created. Initializes the user interface,
     * sets up Firebase Authentication, applies custom styling, and configures event listeners.
     *
     * <p>This method enables edge-to-edge display, applies window insets for proper layout,
     * initializes all UI components, sets custom colors for the BookTrack theme,
     * and sets up click listeners for the signup button and back navigation.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        emailInput = findViewById(R.id.email_signup);
        passwordInput = findViewById(R.id.password_signup);
        signupBtn = findViewById(R.id.signup_btn);
        passMust = findViewById(R.id.passMust);
        signUpTextView = findViewById(R.id.signUp_TextVeiw);
        arrow = findViewById(R.id.arrow);
        FBAuth = FirebaseAuth.getInstance();

        findViewById(R.id.main).setBackgroundColor(Color.parseColor("#eed9c4"));
        signupBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signupBtn.setTextColor(Color.BLACK);
        signUpTextView.setTextColor(Color.parseColor("#d9b99b"));
        passMust.setTextColor(Color.parseColor("#d9b99b"));

        signupBtn.setOnClickListener(v -> registerUser(emailInput, passwordInput));
        arrow.setOnClickListener(v -> finish());
    }

    /**
     * Handles the user registration process using Firebase Authentication.
     *
     * <p>This method validates the provided email and password, then attempts to create
     * a new user account with Firebase. It provides appropriate feedback to the user
     * through toast messages and input field error indicators.</p>
     *
     * <p>Validation includes:
     * <ul>
     *   <li>Email format validation using Android's built-in email pattern matcher</li>
     *   <li>Password length validation (minimum 6 characters)</li>
     *   <li>Empty field validation</li>
     * </ul></p>
     *
     * @param emailInput    The EditText containing the user's email address
     * @param passwordInput The EditText containing the user's desired password
     */
    private void registerUser(EditText emailInput, EditText passwordInput) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (!isValidEmail(email)) {
            emailInput.setError("Invalid Email");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return;
        }

        FBAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Signup.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = (task.getException() != null)
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(Signup.this, "Registration failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Validates whether the provided email address follows a valid email format.
     *
     * <p>This method uses Android's built-in email pattern matcher to ensure
     * the email address conforms to standard email formatting rules. It also
     * checks that the email string is not empty or null.</p>
     *
     * @param email The email address string to validate
     * @return {@code true} if the email is valid and properly formatted, {@code false} otherwise
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}