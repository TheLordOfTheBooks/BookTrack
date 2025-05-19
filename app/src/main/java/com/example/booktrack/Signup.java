package com.example.booktrack;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button signupBtn;
    TextView passMust;
    TextView signUpTextView;
    FloatingActionButton arrow;

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


    }



    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}