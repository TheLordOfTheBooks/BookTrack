package com.example.booktrack;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    EditText emailInput;
    EditText passwordInput;
    Button signupBtn;
    TextView passMust;
    TextView signUpTextView;

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
        findViewById(R.id.main).setBackgroundColor(Color.parseColor("#eed9c4"));


        signupBtn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signupBtn.setTextColor(Color.BLACK);
        signUpTextView.setTextColor(Color.parseColor("#d9b99b"));
        passMust.setTextColor(Color.parseColor("#d9b99b"));

        FBAuth = FirebaseAuth.getInstance();

        signupBtn.setOnClickListener(v -> registerUser(emailInput, passwordInput));
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

        FBAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FBAuth.getCurrentUser();

                        if (user != null) {
                            String uid = user.getUid();
                            getSharedPreferences("BookTrackPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("uid", uid)
                                    .apply();

                            FirebaseFirestore db = FirebaseFirestore.getInstance();


                            Map<String, Object> userData = new HashMap<>();
                            userData.put("createdAt", FieldValue.serverTimestamp());
                            db.collection("users").document(uid).set(userData, SetOptions.merge());
                        }

                        Toast.makeText(Signup.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = (task.getException() != null) ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(Signup.this, "Registration Failed: " + errorMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}