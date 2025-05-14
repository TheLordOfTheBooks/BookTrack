package com.example.booktrack;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.Map;
import java.util.HashMap;

public class Signup extends AppCompatActivity {
    private EditText email_signup, password_signup;
    private Button signup_btn;
    private TextView passMust, signUp_TextVeiw;
    private View signup_view;

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
        passMust = findViewById(R.id.passMust);
        signUp_TextVeiw = findViewById(R.id.signUp_TextVeiw);
        signup_view = findViewById(R.id.main);

        passMust.setTextColor(Color.parseColor("#d9b99b"));
        signup_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        signup_btn.setTextColor(Color.BLACK);
        signUp_TextVeiw.setTextColor(Color.parseColor("#d9b99b"));
        signup_view.setBackgroundColor(Color.parseColor("#eed9c4"));
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

                        // ✅ FIRESTORE BOOK CREATION START
                        String uid = user.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Create a user document (optional)
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("createdAt", FieldValue.serverTimestamp());
                        db.collection("users")
                                .document(uid)
                                .set(userData, SetOptions.merge());

                        // Add a default book
                        Map<String, Object> defaultBook = new HashMap<>();
                        defaultBook.put("name", "Welcome to BookTrack");
                        defaultBook.put("author", "BookTrack AI");
                        defaultBook.put("genre", "Getting Started");
                        defaultBook.put("situation", "To Read");
                        defaultBook.put("page count", 1);
                        defaultBook.put("imageUrl", "https://m.media-amazon.com/images/I/616bdy4E+VL._SY522_.jpg");


                        db.collection("users")
                                .document(uid)
                                .collection("books")
                                .add(defaultBook)
                                .addOnSuccessListener(docRef -> Log.d("Firestore", "Default book added"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to add book", e));
                        // ✅ FIRESTORE BOOK CREATION END


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