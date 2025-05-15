package com.example.booktrack;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private List<Book> bookList = new ArrayList<>();
    private ArrayAdapter<Book> bookAdapter;
    private Calendar selectedCalendar;
    private boolean shouldStartTimerAfterPermission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_alarm);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pickDateTimeButton = findViewById(R.id.pick_date_time_button);
        selectedDateTimeText = findViewById(R.id.selected_date_time);
        purposeGroup = findViewById(R.id.purpose_radio_group);
        finishBookRadio = findViewById(R.id.finish_book_radio);
        otherRadio = findViewById(R.id.other_radio);
        otherMessageInput = findViewById(R.id.other_message_input);
        addGoalCheckbox = findViewById(R.id.add_goal_checkbox);
        addAlarmButton = findViewById(R.id.add_alarm_button);
        bookSpinner = findViewById(R.id.book_spinner);
        selectedCalendar = Calendar.getInstance();

        bookAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, bookList);
        bookSpinner.setAdapter(bookAdapter);

        loadBooksFromFirestore();

        pickDateTimeButton.setOnClickListener(v -> showDatePicker());

        purposeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            otherMessageInput.setVisibility(checkedId == R.id.other_radio ? View.VISIBLE : View.GONE);
        });

        addAlarmButton.setOnClickListener(v -> createAlarm());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedCalendar.set(Calendar.MINUTE, minute);
                    selectedCalendar.set(Calendar.SECOND, 0);

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    String formatted = formatter.format(selectedCalendar.getTime());
                    selectedDateTimeText.setText("Selected: " + formatted);
                },
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                true);
        timePicker.show();
    }

    private void createAlarm() {
        Book selectedBook = (Book) bookSpinner.getSelectedItem();
        if (selectedBook == null) {
            Toast.makeText(this, "Please select a book", Toast.LENGTH_SHORT).show();
            return;
        }

        String bookId = selectedBook.getDocId();
        String bookName = selectedBook.getName();
        String bookImageUrl = selectedBook.getImageUrl();
        long triggerMillis = selectedCalendar.getTimeInMillis();

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

        AlarmItem alarm = new AlarmItem(alarmId, bookId, bookName, bookImageUrl, triggerMillis, purpose);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("alarms")
                .document(alarmId)
                .set(alarm)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Alarm saved", Toast.LENGTH_SHORT).show();
                    scheduleAlarm(bookName, purpose, triggerMillis);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save alarm", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
    }

    private void scheduleAlarm(String bookName, String message, long triggerMillis) {
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("alarm_message", bookName + ": " + message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) triggerMillis,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                    Toast.makeText(this, "Please enable 'Schedule exact alarms' permission in system settings", Toast.LENGTH_LONG).show();

                    // Optionally open settings screen for the user:
                    Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent);
                    return;
                }
            }

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerMillis,
                    pendingIntent
            );
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
                        book.setDocId(doc.getId());  // 🔥 Set Firestore doc ID manually
                        bookList.add(book);
                    }
                    bookAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load books", Toast.LENGTH_SHORT).show());
    }





}