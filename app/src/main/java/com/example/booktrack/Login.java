package com.example.booktrack;

import android.annotation.SuppressLint;
import android.content.Intent;
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
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    private EditText email_login,password_login;
    private Button login_btn, signup_btn, forgotpass_btn;
    private TextView login_text;
    private View login_view;

    private FirebaseAuth FBAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FBAuth = FirebaseAuth.getInstance();


        email_login = findViewById(R.id.email_login);
        password_login = findViewById(R.id.password_login);
        login_btn = findViewById(R.id.login_btn);
        signup_btn= findViewById(R.id.signup_btn);
        forgotpass_btn = findViewById(R.id.forgotpass_btn);
        login_text = findViewById(R.id.login_text);
        login_view = findViewById(R.id.main);


        login_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        login_btn.setTextColor(Color.BLACK);
        signup_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signup_btn.setTextColor(Color.BLACK);
        forgotpass_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        forgotpass_btn.setTextColor(Color.BLACK);
        login_text.setTextColor(Color.parseColor("#d9b99b"));
        login_view.setBackgroundColor(Color.parseColor("#eed9c4"));

        login_btn.setOnClickListener(v -> loginUser());
    }

    public void go(View view){
        Intent si = new Intent(this, Signup.class);
        startActivity(si);
    }

    public void go2(View view){
        Intent si = new Intent(this, forgotPass.class);
        startActivity(si);
    }

    private void loginUser(){
        String email = email_login.getText().toString().trim();
        String password = password_login.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            email_login.setError("Email is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            password_login.setError("Password is required");
            return;
        }
        FBAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FBAuth.getCurrentUser();
                        Toast.makeText(Login.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}