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

public class forgotPass extends AppCompatActivity {

    private FirebaseAuth FBAuth;
    TextInputEditText emailInput;
    FloatingActionButton arrow;

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