package com.example.booktrack;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * CreateAlarm is an activity that allows users to create and schedule reading alarms
 * for their books in the book tracking application. This activity provides a comprehensive
 * interface for setting up reminders with customizable messages, dates, times, and
 * optional goal creation.
 *
 * <p>Key features include:</p>
 * <ul>
 *   <li>Book selection from user's Firebase Firestore collection</li>
 *   <li>Date and time picker for alarm scheduling</li>
 *   <li>Customizable alarm messages and purposes</li>
 *   <li>Battery optimization permission handling</li>
 *   <li>Notification channel creation and management</li>
 *   <li>Integration with AlarmScheduler for system-level alarm scheduling</li>
 *   <li>Optional goal creation linked to the alarm</li>
 * </ul>
 *
 * <p>The activity handles Android runtime permissions for notifications (API 33+)
 * and provides user guidance for battery optimization settings to ensure reliable
 * alarm functionality.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class CreateAlarm extends AppCompatActivity {

    /**
     * Button for opening date and time picker dialogs.
     */
    private Button pickDateTimeButton;

    /**
     * Button for creating and saving the alarm.
     */
    private Button addAlarmButton;

    /**
     * TextView displaying the currently selected date and time.
     */
    private TextView selectedDateTimeText;

    /**
     * Radio group for selecting the alarm purpose/message type.
     */
    private RadioGroup purposeGroup;

    /**
     * Radio button for selecting "finish book" as the alarm purpose.
     */
    private RadioButton finishBookRadio;

    /**
     * Radio button for selecting custom message as the alarm purpose.
     */
    private RadioButton otherRadio;

    /**
     * EditText for entering custom alarm messages when "other" purpose is selected.
     */
    private EditText otherMessageInput;

    /**
     * Checkbox for optionally creating a goal alongside the alarm.
     */
    private CheckBox addGoalCheckbox;

    /**
     * Spinner for selecting a book from the user's collection.
     */
    private Spinner bookSpinner;

    /**
     * List containing all books loaded from Firebase Firestore.
     */
    private final List<Book> bookList = new ArrayList<>();

    /**
     * ArrayAdapter for populating the book spinner with book data.
     */
    private ArrayAdapter<Book> bookAdapter;

    /**
     * Calendar instance holding the user's selected alarm date and time.
     */
    private final Calendar selectedCalendar = Calendar.getInstance();

    /**
     * Flag indicating whether alarm scheduling should occur after permission is granted.
     */
    private boolean shouldScheduleAfterPermission = false;

    /**
     * Temporarily stored book name for post-permission alarm scheduling.
     */
    private String pendingBookName;

    /**
     * Temporarily stored alarm message for post-permission alarm scheduling.
     */
    private String pendingMessage;

    /**
     * Temporarily stored alarm ID for post-permission alarm scheduling.
     */
    private String pendingAlarmId;

    /**
     * Temporarily stored trigger time for post-permission alarm scheduling.
     */
    private long pendingTriggerMillis;

    /**
     * Floating action button serving as a back/close button.
     */
    FloatingActionButton arrow;

    /**
     * Called when the activity is first created. Initializes the user interface,
     * sets up event listeners, requests necessary permissions, creates notification
     * channels, and loads books from Firebase Firestore.
     *
     * <p>The method performs the following setup operations:</p>
     * <ul>
     *   <li>Initializes all UI components</li>
     *   <li>Requests battery optimization permissions for reliable alarms</li>
     *   <li>Sets up custom UI styling</li>
     *   <li>Configures event listeners for user interactions</li>
     *   <li>Creates notification channels for Android O+</li>
     *   <li>Loads user's books from Firebase Firestore</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-created from a previous saved state,
     *                          this is the state data. If null, the activity is being created fresh.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_alarm);

        initViews();
        requestBatteryOptimizationPermission();
        setupUIStyles();
        setupListeners();
        createNotificationChannel();
        loadBooksFromFirestore();

        // Setup back button
        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(v -> {
            finish();
        });
    }

    /**
     * Initializes all UI components and sets up the book spinner adapter.
     * This method finds all views by their IDs and configures the spinner
     * with a custom adapter for displaying book information.
     */
    private void initViews() {
        pickDateTimeButton = findViewById(R.id.pick_date_time_button);
        selectedDateTimeText = findViewById(R.id.selected_date_time);
        purposeGroup = findViewById(R.id.purpose_radio_group);
        finishBookRadio = findViewById(R.id.finish_book_radio);
        otherRadio = findViewById(R.id.other_radio);
        otherMessageInput = findViewById(R.id.other_message_input);
        addGoalCheckbox = findViewById(R.id.add_goal_checkbox);
        addAlarmButton = findViewById(R.id.add_alarm_button);
        bookSpinner = findViewById(R.id.book_spinner);

        // Setup book spinner adapter
        bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookList);
        bookAdapter.setDropDownViewResource(R.layout.spinner_items);
        bookSpinner.setAdapter(bookAdapter);
    }

    /**
     * Configures custom styling for UI components.
     * Sets background colors and text colors to match the app's design theme.
     */
    private void setupUIStyles() {
        View root = findViewById(R.id.main);
        root.setBackgroundColor(Color.parseColor("#eed9c4"));
        pickDateTimeButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        pickDateTimeButton.setTextColor(Color.BLACK);
        addAlarmButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        addAlarmButton.setTextColor(Color.BLACK);
    }

    /**
     * Sets up event listeners for user interface interactions.
     * Configures click listeners for buttons and change listeners for radio groups.
     */
    private void setupListeners() {
        pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        // Show/hide custom message input based on radio button selection
        purposeGroup.setOnCheckedChangeListener((group, checkedId) ->
                otherMessageInput.setVisibility(checkedId == R.id.other_radio ? View.VISIBLE : View.GONE));

        addAlarmButton.setOnClickListener(v -> createAlarm());
    }

    /**
     * Requests battery optimization permission to ensure alarms work reliably
     * when the device is in idle mode. Shows a dialog explaining why the permission
     * is needed and guides the user to the system settings if they choose to allow it.
     */
    private void requestBatteryOptimizationPermission() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            new AlertDialog.Builder(this)
                    .setTitle("Allow Background Alarms")
                    .setMessage("To ensure alarms work when the phone is idle, allow this app to ignore battery optimizations.")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    /**
     * Displays a date picker dialog for selecting the alarm date.
     * After date selection, automatically shows the time picker dialog.
     */
    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
            showTimePicker();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Displays a time picker dialog for selecting the alarm time.
     * Updates the selected date/time display with the formatted date and time.
     */
    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedCalendar.set(Calendar.MINUTE, minute);
            selectedCalendar.set(Calendar.SECOND, 0);

            // Format and display the selected date/time
            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(selectedCalendar.getTime());
            selectedDateTimeText.setText("Selected: " + formatted);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    /**
     * Creates and saves a new alarm based on user input.
     * Validates all required fields, creates an AlarmItem object, saves it to
     * Firebase Firestore, and handles the scheduling process.
     *
     * <p>Validation includes:</p>
     * <ul>
     *   <li>Book selection verification</li>
     *   <li>Future date/time validation</li>
     *   <li>Purpose selection and custom message validation</li>
     *   <li>User authentication verification</li>
     * </ul>
     */
    private void createAlarm() {
        // Validate book selection
        Book selectedBook = (Book) bookSpinner.getSelectedItem();
        if (selectedBook == null) {
            Toast.makeText(this, "Please select a book", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate selected time is in the future
        long deadlineMillis = selectedCalendar.getTimeInMillis();
        if (deadlineMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine alarm purpose/message
        String purpose;
        int checkedId = purposeGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.finish_book_radio) {
            purpose = "Finish book until that date";
        } else if (checkedId == R.id.other_radio) {
            purpose = otherMessageInput.getText().toString().trim();
            if (purpose.isEmpty()) {
                Toast.makeText(this, "Please enter your message", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            Toast.makeText(this, "Please choose a purpose", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate user authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create and save alarm to Firestore
        String uid = user.getUid();
        String alarmId = UUID.randomUUID().toString();
        AlarmItem alarm = new AlarmItem(alarmId, selectedBook.getDocId(), selectedBook.getName(),
                selectedBook.getImageUrl(), deadlineMillis, purpose);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("alarms")
                .document(alarmId)
                .set(alarm)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
                    handlePostSave(selectedBook, deadlineMillis, alarmId, purpose);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    /**
     * Handles post-save operations after successfully saving an alarm to Firestore.
     * Manages notification permissions, schedules the system alarm, and optionally
     * navigates to goal creation if the user requested it.
     *
     * @param book The selected book for the alarm
     * @param deadlineMillis The alarm trigger time in milliseconds
     * @param alarmId The unique identifier for the created alarm
     * @param message The alarm message/purpose
     */
    private void handlePostSave(Book book, long deadlineMillis, String alarmId, String message) {
        // Store pending data for permission callback
        pendingBookName = book.getName();
        pendingMessage = message;
        pendingAlarmId = alarmId;
        pendingTriggerMillis = deadlineMillis;

        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            shouldScheduleAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        // Schedule the alarm
        AlarmScheduler.schedule(this, new AlarmItem(alarmId, book.getDocId(), book.getName(),
                book.getImageUrl(), deadlineMillis, message));

        // Navigate to goal creation if requested
        if (addGoalCheckbox.isChecked()) {
            Intent goalIntent = new Intent(this, CreateGoal.class);
            goalIntent.putExtra("bookName", book.getName());
            goalIntent.putExtra("deadlineMillis", deadlineMillis);
            goalIntent.putExtra("bookImageUrl", book.getImageUrl());
            goalIntent.putExtra("bookId", book.getDocId());
            startActivity(goalIntent);
        }

        finish();
    }

    /**
     * Creates a notification channel for alarm notifications on Android O and above.
     * This is required for displaying notifications on modern Android versions.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "timer_channel_id",
                    "Timer Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when alarm time arrives");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    /**
     * Handles the result of permission requests, specifically for notification permissions.
     * If notification permission is granted and there's a pending alarm, schedules the alarm.
     *
     * @param requestCode The request code passed to requestPermissions()
     * @param permissions The requested permissions (never null)
     * @param grantResults The grant results for the corresponding permissions (never null)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (shouldScheduleAfterPermission) {
                AlarmScheduler.schedule(this, new AlarmItem(pendingAlarmId, "", pendingBookName, "",
                        pendingTriggerMillis, pendingMessage));
                shouldScheduleAfterPermission = false;
            }
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the user's book collection from Firebase Firestore and populates the book spinner.
     * Only loads books for the currently authenticated user and handles both success
     * and failure scenarios with appropriate user feedback.
     */
    private void loadBooksFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("books")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Book book = doc.toObject(Book.class);
                        book.setDocId(doc.getId()); // Preserve Firestore document ID
                        bookList.add(book);
                    }
                    bookAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show());
    }
}