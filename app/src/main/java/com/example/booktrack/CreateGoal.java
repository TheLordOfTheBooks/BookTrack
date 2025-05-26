package com.example.booktrack;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for creating reading goals with customizable deadlines and optional book state changes.
 * This activity provides a comprehensive interface for users to set specific reading objectives
 * tied to their book collection, with options for automatic book state transitions upon goal completion.
 *
 * <p>The CreateGoal activity supports two primary goal types:
 * <ul>
 *   <li><strong>Finish Book</strong> - A predefined goal to complete reading a specific book</li>
 *   <li><strong>Custom Goal</strong> - User-defined objectives with custom descriptions</li>
 * </ul></p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Dynamic UI that adapts based on goal type selection</li>
 *   <li>Integration with book data received via Intent extras</li>
 *   <li>Optional automatic book state changes upon goal completion</li>
 *   <li>Deadline display and validation from alarm scheduling context</li>
 *   <li>Firebase Firestore integration for persistent goal storage</li>
 *   <li>BookTrack's signature visual styling and user experience</li>
 * </ul></p>
 *
 * <p>The activity is typically launched from alarm creation workflows where users
 * can associate specific reading goals with their scheduled reminders, creating
 * a comprehensive reading habit management system.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class CreateGoal extends AppCompatActivity {

    /** TextView displaying the formatted deadline for the goal */
    private TextView deadlineText;

    /** RadioGroup for selecting between predefined and custom goal types */
    private RadioGroup goalTypeGroup;

    /** RadioButton for selecting the "Finish Book" goal type */
    private RadioButton radioFinishBook;

    /** RadioButton for selecting custom goal type */
    private RadioButton radioOther;

    /** EditText for entering custom goal descriptions */
    private EditText descriptionInput;

    /** Switch to enable/disable automatic book state changes */
    private Switch changeStateSwitch;

    /** Spinner for selecting the new book state when automatic changes are enabled */
    private Spinner stateSpinner;

    /** Button to save the configured goal to Firebase */
    private Button saveGoalButton;

    /** Floating action button for navigation (back arrow) */
    FloatingActionButton arrow;

    /** Calendar instance for handling deadline date/time operations */
    private Calendar deadlineCalendar;

    /** Array of available book states for the state transition spinner */
    private final String[] bookStates = {
            "Want to Read",
            "Currently Reading",
            "Stopped Reading",
            "Read"
    };

    /**
     * Initializes the goal creation activity, sets up the user interface,
     * configures dynamic UI behavior, and processes Intent data.
     *
     * <p>The initialization process includes:
     * <ul>
     *   <li>Edge-to-edge display configuration with proper window insets</li>
     *   <li>UI component initialization and styling with BookTrack colors</li>
     *   <li>Spinner adapter setup for book state selection</li>
     *   <li>Intent data processing for deadline, book ID, name, and image URL</li>
     *   <li>Dynamic UI behavior configuration for goal type and state change options</li>
     *   <li>Event listener setup for user interactions and navigation</li>
     * </ul></p>
     *
     * <p>Dynamic UI behavior includes:
     * <ul>
     *   <li>Custom description field visibility based on goal type selection</li>
     *   <li>Book state spinner visibility based on automatic state change toggle</li>
     *   <li>Deadline display from received Intent data with proper formatting</li>
     * </ul></p>
     *
     * <p>The activity processes Intent extras containing:
     * <ul>
     *   <li>deadlineMillis - Unix timestamp for goal deadline</li>
     *   <li>bookId - Unique identifier for the associated book</li>
     *   <li>bookName - Display name of the associated book</li>
     *   <li>bookImageUrl - URL for the book cover image</li>
     * </ul></p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
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

        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(v -> finish());

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

    /**
     * Validates user input and saves the configured goal to Firebase Firestore.
     * This method handles comprehensive validation, data collection, and database operations
     * for goal persistence.
     *
     * <p>The save process includes:
     * <ul>
     *   <li>User authentication validation</li>
     *   <li>Goal type selection validation and description handling</li>
     *   <li>Custom goal description validation for non-empty input</li>
     *   <li>Book state change configuration processing</li>
     *   <li>Intent data extraction for book and deadline information</li>
     *   <li>Goal object construction with all relevant metadata</li>
     *   <li>Firebase Firestore document creation in user's goals collection</li>
     *   <li>Success/failure feedback through toast messages</li>
     * </ul></p>
     *
     * <p>Validation includes:
     * <ul>
     *   <li>User authentication state verification</li>
     *   <li>Goal type selection requirement</li>
     *   <li>Non-empty custom description for "Other" goal type</li>
     *   <li>Proper Intent data availability</li>
     * </ul></p>
     *
     * <p>The goal object stored in Firestore contains:
     * <ul>
     *   <li>description - Goal description (predefined or custom)</li>
     *   <li>deadlineMillis - Unix timestamp for goal deadline</li>
     *   <li>changeState - Boolean indicating if book state should change</li>
     *   <li>newState - Target book state (if automatic change enabled)</li>
     *   <li>bookId - Associated book identifier</li>
     *   <li>bookName - Associated book display name</li>
     *   <li>bookImageUrl - Associated book cover image URL</li>
     * </ul></p>
     *
     * <p>Upon successful save, the activity automatically closes and returns to
     * the previous screen. If saving fails, an error message is displayed and
     * the user can retry the operation.</p>
     */
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
        goal.put("deadlineMillis", deadlineMillis);
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