package com.example.booktrack;

import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.view.View;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.*;

public class CreateGoal extends AppCompatActivity {

    private TextView deadlineText;
    private RadioGroup goalTypeGroup;
    private RadioButton radioFinishBook, radioOther;
    private EditText descriptionInput;
    private Switch changeStateSwitch;
    private Spinner stateSpinner;
    private Button saveGoalButton;

    private Calendar deadlineCalendar;
    private final String[] bookStates = {
            "Want to Read",
            "Currently Reading",
            "Stopped Reading",
            "Read"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_goal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        deadlineText = findViewById(R.id.goal_deadline);
        goalTypeGroup = findViewById(R.id.goal_type_group);
        radioFinishBook = findViewById(R.id.radio_finish_book);
        radioOther = findViewById(R.id.radio_other);
        descriptionInput = findViewById(R.id.goal_description);
        changeStateSwitch = findViewById(R.id.change_state_switch);
        stateSpinner = findViewById(R.id.book_state_spinner);
        saveGoalButton = findViewById(R.id.save_goal_button);

        View root = findViewById(R.id.main);
        root.setBackgroundColor(Color.parseColor("#eed9c4")); // Soft beige background

        saveGoalButton.setBackgroundColor(Color.parseColor("#FAF0E6")); // Light cream buttons
        saveGoalButton.setTextColor(Color.BLACK);



        stateSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookStates));

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
                this,
                R.layout.spinner_items, // Custom style
                bookStates
        );
        stateAdapter.setDropDownViewResource(R.layout.spinner_items); // same style for dropdown

        stateSpinner.setAdapter(stateAdapter);

        // Get date/time from alarm intent
        long millis = getIntent().getLongExtra("deadlineMillis", -1);
        if (millis != -1) {
            deadlineCalendar = Calendar.getInstance();
            deadlineCalendar.setTimeInMillis(millis);
            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(deadlineCalendar.getTime());
            deadlineText.setText("Deadline: " + formatted);
        } else {
            deadlineText.setText("Deadline: Unknown");
        }

        // Show/hide custom goal field
        goalTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_other) {
                descriptionInput.setVisibility(View.VISIBLE);
            } else {
                descriptionInput.setVisibility(View.GONE);
            }
        });

        // Show/hide state spinner
        changeStateSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            stateSpinner.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        saveGoalButton.setOnClickListener(v -> saveGoal());
    }

    private void saveGoal() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String description;
        int checkedId = goalTypeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_finish_book) {
            description = "Finish book";
        } else if (checkedId == R.id.radio_other) {
            description = descriptionInput.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter your custom goal", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Please select a goal type", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean changeState = changeStateSwitch.isChecked();
        String bookId = getIntent().getStringExtra("bookId");
        String selectedBookName = getIntent().getStringExtra("bookName");
        String selectedBookImageUrl = getIntent().getStringExtra("bookImageUrl");
        Long deadlineMillis = getIntent().getLongExtra("deadlineMillis", 0);
        String newState = null;
        if (changeState) {
            newState = stateSpinner.getSelectedItem().toString();
        }

        Map<String, Object> goal = new HashMap<>();
        goal.put("description", description);
        goal.put("triggerMillis", deadlineMillis);
        goal.put("changeState", changeState);
        goal.put("newState", changeState ? newState : null);
        goal.put("bookId", bookId);
        goal.put("bookName", selectedBookName);
        goal.put("bookImageUrl", selectedBookImageUrl);


        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("goals")
                .add(goal)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Goal saved", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save goal", Toast.LENGTH_SHORT).show();
                });
    }
}