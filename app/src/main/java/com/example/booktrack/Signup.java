package com.example.booktrack;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Signup extends AppCompatActivity {
    private EditText email_signup, password_signup;
    private Button signup_btn;

    private FirebaseAuth FBAuth;

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
        email_signup = findViewById(R.id.email_signup);
        password_signup = findViewById(R.id.password_signup);
        signup_btn = findViewById(R.id.signup_btn);
        FBAuth = FirebaseAuth.getInstance();
        signup_btn.setOnClickListener(v -> registerUser());
    }
    private void registerUser(){
        String email = email_signup.getText().toString().trim();
        String password = password_signup.getText().toString().trim();

        if (!isValidEmail(email)) {
            email_signup.setError("Invalid Email");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            password_signup.setError("Password must be at least 6 characters");
            return;
        }
        FBAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FBAuth.getCurrentUser();
                        Toast.makeText(Signup.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(Signup.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}