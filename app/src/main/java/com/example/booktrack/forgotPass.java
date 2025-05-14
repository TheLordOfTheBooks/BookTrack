package com.example.booktrack;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class forgotPass extends AppCompatActivity {

    private TextInputEditText emailForgotPass;
    private Button resetPasswordBtn;
    private FirebaseAuth FBAuth;
    private TextView Forgot_pass_textView;
    private View forgotPass_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pass);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailForgotPass = findViewById(R.id.email_forgot_pass);
        resetPasswordBtn = findViewById(R.id.change_pass_btn);
        forgotPass_view = findViewById(R.id.main);
        FBAuth = FirebaseAuth.getInstance();
        Forgot_pass_textView = findViewById(R.id.Forgot_pass_textView);


        resetPasswordBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        resetPasswordBtn.setTextColor(Color.BLACK);
        Forgot_pass_textView.setTextColor(Color.parseColor("#d9b99b"));
        forgotPass_view.setBackgroundColor(Color.parseColor("#eed9c4"));

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void resetPassword() {
        String email = emailForgotPass.getText().toString().trim();

        // Check if the email field is empty
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(forgotPass.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            return;
        }


        // Send password reset email
        FBAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, task -> {

                        if (task.isSuccessful()) {
                            FirebaseUser user = FBAuth.getCurrentUser();
                            Toast.makeText(forgotPass.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(forgotPass.this, "Failed to send reset email" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                });
    }
}