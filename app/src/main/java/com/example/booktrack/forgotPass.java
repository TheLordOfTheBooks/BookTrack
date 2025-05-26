package com.example.booktrack;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity that handles password reset functionality for the BookTrack application.
 * This activity provides a simple interface for users to request password reset emails
 * through Firebase Authentication.
 *
 * <p>The activity features a clean, user-friendly interface with the signature BookTrack
 * color scheme, email input validation, and comprehensive error handling. Users can
 * enter their registered email address to receive a password reset link.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Email validation to ensure proper input format</li>
 *   <li>Firebase Authentication integration for secure password reset</li>
 *   <li>Custom UI styling consistent with BookTrack's design language</li>
 *   <li>Clear user feedback through toast messages</li>
 *   <li>Simple navigation with back button functionality</li>
 * </ul></p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class forgotPass extends AppCompatActivity {

    /** Firebase Authentication instance for handling password reset operations */
    private FirebaseAuth FBAuth;

    /** Text input field for user's email address */
    TextInputEditText emailInput;

    /** Floating action button for navigation (back arrow) */
    FloatingActionButton arrow;

    /**
     * Initializes the forgot password activity, sets up the user interface,
     * and configures Firebase Authentication for password reset functionality.
     *
     * <p>This method performs the following setup operations:
     * <ul>
     *   <li>Configures window insets for proper edge-to-edge display</li>
     *   <li>Initializes Firebase Authentication instance</li>
     *   <li>Binds UI components from the layout</li>
     *   <li>Applies BookTrack's signature color scheme to UI elements</li>
     *   <li>Sets up click listeners for password reset and navigation</li>
     *   <li>Configures transparent backgrounds for proper visual hierarchy</li>
     * </ul></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).left,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).top,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).right,
                    insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            );
            return insets;
        });

        FBAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.email_forgot_pass);
        arrow = findViewById(R.id.arrow);
        findViewById(R.id.change_pass_btn).setOnClickListener(v -> resetPassword(emailInput));
        findViewById(R.id.main).setBackgroundColor(Color.parseColor("#eed9c4"));
        findViewById(R.id.change_pass_btn).setBackgroundColor(Color.parseColor("#FAF0E6"));
        findViewById(R.id.change_pass_btn).setForeground(null);
        findViewById(R.id.Forgot_pass_textView).setBackgroundColor(Color.TRANSPARENT);
        findViewById(R.id.Forgot_pass_textView).setForeground(null);

        arrow.setOnClickListener(v -> finish());
    }

    /**
     * Handles the password reset process by sending a reset email through Firebase Authentication.
     *
     * <p>This method validates the user's email input and initiates the password reset process
     * through Firebase Auth. It provides comprehensive user feedback through toast messages
     * for both successful operations and error conditions.</p>
     *
     * <p>The reset process includes:
     * <ul>
     *   <li>Email input validation to ensure the field is not empty</li>
     *   <li>Firebase password reset email request</li>
     *   <li>Success confirmation with automatic activity closure</li>
     *   <li>Detailed error handling with specific error messages</li>
     * </ul></p>
     *
     * <p>Upon successful email dispatch, the user is notified and the activity closes
     * automatically, returning them to the login screen. If an error occurs, a detailed
     * error message is displayed to help the user understand what went wrong.</p>
     *
     * @param emailInput The TextInputEditText containing the user's email address for password reset
     */
    private void resetPassword(TextInputEditText emailInput) {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }

        FBAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String error = (task.getException() != null && task.getException().getMessage() != null)
                                ? task.getException().getMessage()
                                : "Unknown error";
                        Toast.makeText(this, "Failed to send reset email: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}