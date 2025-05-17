package com.example.booktrack;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
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

public class CreateAlarm extends AppCompatActivity {

    private Button pickDateTimeButton, addAlarmButton;
    private TextView selectedDateTimeText;
    private RadioGroup purposeGroup;
    private RadioButton finishBookRadio, otherRadio;
    private EditText otherMessageInput;
    private CheckBox addGoalCheckbox;
    private Spinner bookSpinner;
    private final List<Book> bookList = new ArrayList<>();
    private ArrayAdapter<Book> bookAdapter;
    private final Calendar selectedCalendar = Calendar.getInstance();

    private boolean shouldScheduleAfterPermission = false;
    private String pendingBookName, pendingMessage, pendingAlarmId;
    private long pendingTriggerMillis;

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
    }

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

        bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookList);
        bookAdapter.setDropDownViewResource(R.layout.spinner_items);
        bookSpinner.setAdapter(bookAdapter);
    }

    private void setupUIStyles() {
        View root = findViewById(R.id.main);
        root.setBackgroundColor(Color.parseColor("#eed9c4"));
        pickDateTimeButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        pickDateTimeButton.setTextColor(Color.BLACK);
        addAlarmButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        addAlarmButton.setTextColor(Color.BLACK);
    }

    private void setupListeners() {
        pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        purposeGroup.setOnCheckedChangeListener((group, checkedId) ->
                otherMessageInput.setVisibility(checkedId == R.id.other_radio ? View.VISIBLE : View.GONE));

        addAlarmButton.setOnClickListener(v -> createAlarm());
    }

    private void requestBatteryOptimizationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, day);
            showTimePicker();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        new TimePickerDialog(this, (view, hour, minute) -> {
            selectedCalendar.set(Calendar.HOUR_OF_DAY, hour);
            selectedCalendar.set(Calendar.MINUTE, minute);
            selectedCalendar.set(Calendar.SECOND, 0);

            String formatted = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(selectedCalendar.getTime());
            selectedDateTimeText.setText("Selected: " + formatted);
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();
    }

    private void createAlarm() {
        Book selectedBook = (Book) bookSpinner.getSelectedItem();
        if (selectedBook == null) {
            Toast.makeText(this, "Please select a book", Toast.LENGTH_SHORT).show();
            return;
        }

        long triggerMillis = selectedCalendar.getTimeInMillis();
        if (triggerMillis < System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = user.getUid();
        String alarmId = UUID.randomUUID().toString();
        AlarmItem alarm = new AlarmItem(alarmId, selectedBook.getDocId(), selectedBook.getName(),
                selectedBook.getImageUrl(), triggerMillis, purpose);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("alarms")
                .document(alarmId)
                .set(alarm)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
                    handlePostSave(selectedBook, triggerMillis, alarmId, purpose);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void handlePostSave(Book book, long triggerMillis, String alarmId, String message) {
        pendingBookName = book.getName();
        pendingMessage = message;
        pendingAlarmId = alarmId;
        pendingTriggerMillis = triggerMillis;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            shouldScheduleAfterPermission = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            return;
        }

        AlarmScheduler.schedule(this, new AlarmItem(alarmId, book.getDocId(), book.getName(),
                book.getImageUrl(), triggerMillis, message));

        if (addGoalCheckbox.isChecked()) {
            Intent goalIntent = new Intent(this, CreateGoal.class);
            goalIntent.putExtra("bookName", book.getName());
            goalIntent.putExtra("deadlineMillis", triggerMillis);
            goalIntent.putExtra("bookImageUrl", book.getImageUrl());
            goalIntent.putExtra("bookId", book.getDocId());
            startActivity(goalIntent);
        }

        finish();
    }

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
                        book.setDocId(doc.getId());
                        bookList.add(book);
                    }
                    bookAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show());
    }









}