package com.example.booktrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment that manages and displays reading alarms and reminders in the BookTrack application.
 * This fragment provides a comprehensive interface for viewing, managing, and interacting with
 * user-created reading alarms.
 *
 * <p>The fragment handles real-time synchronization with Firebase Firestore to display current alarms,
 * automatic cleanup of expired alarms, and provides detailed alarm management through popup dialogs.
 * It integrates with the Android AlarmManager to ensure proper scheduling and cancellation of
 * system-level alarms.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Real-time alarm list display with automatic updates from Firestore</li>
 *   <li>Automatic cleanup of expired alarms to maintain data hygiene</li>
 *   <li>Interactive alarm details popup with book information and management options</li>
 *   <li>Integration with system AlarmManager for proper alarm scheduling/cancellation</li>
 *   <li>Navigation to alarm creation interface</li>
 *   <li>Responsive UI with BookTrack's signature visual styling</li>
 * </ul></p>
 *
 * <p>The fragment implements proper lifecycle management to ensure Firebase listeners are
 * cleaned up appropriately and system resources are released when the fragment is destroyed.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AlarmsFragment extends Fragment {

    /** RecyclerView for displaying the list of active alarms */
    private RecyclerView alarmsRecyclerView;

    /** Adapter for managing alarm item display in the RecyclerView */
    private AlarmsAdapter alarmsAdapter;

    /** List containing all active alarm items */
    private List<AlarmItem> alarmList = new ArrayList<>();

    /** Button for navigating to the alarm creation interface */
    private Button addAlarmButton;

    /** Firebase Firestore listener registration for real-time alarm updates */
    private ListenerRegistration alarmListener;

    /**
     * Default constructor for AlarmsFragment.
     * Required for proper fragment instantiation by the Android framework.
     */
    public AlarmsFragment() {}

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_alarms_fragment, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, providing access to the created view hierarchy.
     * This method initializes all UI components, sets up the RecyclerView adapter, configures event listeners,
     * and initiates data loading operations.
     *
     * <p>The initialization process includes:
     * <ul>
     *   <li>Cleaning up expired alarms from the database</li>
     *   <li>Setting up the RecyclerView with LinearLayoutManager and AlarmsAdapter</li>
     *   <li>Configuring long-click listener for alarm management</li>
     *   <li>Setting up navigation to alarm creation activity</li>
     *   <li>Applying BookTrack's visual styling to UI components</li>
     *   <li>Initiating real-time alarm data loading from Firestore</li>
     * </ul></p>
     *
     * @param view               The View returned by onCreateView()
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cleanUpExpiredAlarms();
        alarmsRecyclerView = view.findViewById(R.id.alarms_recycler_view);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        alarmsAdapter = new AlarmsAdapter(getContext(), alarmList);
        alarmsAdapter.setOnAlarmLongClickListener(alarm -> showAlarmPopup(alarm));

        alarmsRecyclerView.setAdapter(alarmsAdapter);
        addAlarmButton = view.findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateAlarm.class);
            startActivity(intent);
        });

        addAlarmButton.setBackgroundColor(Color.parseColor("#FAF0E6"));
        addAlarmButton.setTextColor(Color.BLACK);

        loadAlarmsFromDatabase();
    }

    /**
     * Loads and maintains real-time synchronization of alarm data from Firebase Firestore.
     * This method sets up a snapshot listener that automatically updates the local alarm list
     * whenever changes occur in the user's alarm collection.
     *
     * <p>The loading process includes:
     * <ul>
     *   <li>Authenticating the current user</li>
     *   <li>Setting up a real-time Firestore listener on the user's alarm collection</li>
     *   <li>Converting Firestore documents to AlarmItem objects</li>
     *   <li>Sorting alarms by deadline for chronological display</li>
     *   <li>Notifying the adapter of data changes for UI updates</li>
     *   <li>Handling listener errors with appropriate user feedback</li>
     * </ul></p>
     *
     * <p>The listener automatically handles additions, deletions, and modifications
     * to alarms, ensuring the UI remains synchronized with the database state.</p>
     */
    private void loadAlarmsFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        alarmListener = db.collection("users")
                .document(user.getUid())
                .collection("alarms")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to listen for alarms", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (querySnapshot != null) {
                        alarmList.clear();
                        for (QueryDocumentSnapshot doc : querySnapshot) {
                            AlarmItem alarm = doc.toObject(AlarmItem.class);
                            alarmList.add(alarm);
                        }

                        alarmList.sort((a1, a2) -> Long.compare(a1.getDeadlineMillis(), a2.getDeadlineMillis()));
                        alarmsAdapter.notifyDataSetChanged();
                    }
                });
    }

    /**
     * Called when the view previously created by onCreateView() has been detached from the fragment.
     * This method performs essential cleanup operations to prevent memory leaks and ensure
     * proper resource management.
     *
     * <p>Cleanup operations include:
     * <ul>
     *   <li>Removing the Firebase Firestore snapshot listener</li>
     *   <li>Releasing database connection resources</li>
     *   <li>Preventing memory leaks from active listeners</li>
     * </ul></p>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alarmListener != null) {
            alarmListener.remove();
        }
    }

    /**
     * Performs automatic cleanup of expired alarms from the user's Firestore collection.
     * This method runs during fragment initialization to maintain database hygiene by
     * removing alarms that have passed their scheduled time.
     *
     * <p>The cleanup process includes:
     * <ul>
     *   <li>Retrieving all user alarms from Firestore</li>
     *   <li>Comparing each alarm's deadline with the current system time</li>
     *   <li>Deleting expired alarms from the database</li>
     *   <li>Logging successful deletions and failures for debugging</li>
     * </ul></p>
     *
     * <p>This automatic cleanup ensures that users don't see outdated alarms and
     * helps maintain optimal database performance by reducing unnecessary data.</p>
     */
    private void cleanUpExpiredAlarms() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("alarms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long now = System.currentTimeMillis();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AlarmItem alarm = doc.toObject(AlarmItem.class);
                        if (alarm.getDeadlineMillis() < now) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid ->
                                            Log.d("AlarmsFragment", "Deleted expired alarm: " + alarm.getAlarmId()))
                                    .addOnFailureListener(e ->
                                            Log.e("AlarmsFragment", "Failed to delete alarm", e));
                        }
                    }
                });
    }

    /**
     * Displays a detailed popup dialog for alarm management and information viewing.
     * This method creates and shows an AlertDialog containing comprehensive alarm details
     * and management options including deletion functionality.
     *
     * <p>The popup dialog includes:
     * <ul>
     *   <li>Book cover image loaded with Glide</li>
     *   <li>Book name and alarm message display</li>
     *   <li>Formatted date and time information</li>
     *   <li>Cancel button for dismissing the dialog</li>
     *   <li>Delete button for removing the alarm</li>
     * </ul></p>
     *
     * <p>The delete functionality performs comprehensive cleanup including:
     * <ul>
     *   <li>Canceling the corresponding system alarm via AlarmManager</li>
     *   <li>Removing the alarm data from Firestore</li>
     *   <li>Providing user feedback on operation success/failure</li>
     *   <li>Automatic dialog dismissal after successful deletion</li>
     * </ul></p>
     *
     * @param alarm The AlarmItem object containing details to display and manage
     */
    private void showAlarmPopup(AlarmItem alarm) {
        View popupView = LayoutInflater.from(requireContext()).inflate(R.layout.alarm_details, null);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(popupView)
                .create();

        ImageView image = popupView.findViewById(R.id.popup_book_image);
        TextView name = popupView.findViewById(R.id.popup_book_name);
        TextView msg = popupView.findViewById(R.id.popup_message);
        TextView datetime = popupView.findViewById(R.id.popup_datetime);

        Button cancel = popupView.findViewById(R.id.popup_cancel_button);
        Button delete = popupView.findViewById(R.id.popup_delete_button);

        name.setText("Book: " + alarm.getBookName());
        msg.setText("Message: " + alarm.getMessage());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        datetime.setText("When: " + sdf.format(new Date(alarm.getDeadlineMillis())));

        Glide.with(requireContext())
                .load(alarm.getBookImageUrl())
                .into(image);

        cancel.setOnClickListener(v -> dialog.dismiss());

        delete.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(requireContext(), AlarmReceiver.class);
            intent.putExtra("alarm_id", alarm.getAlarmId());
            intent.putExtra("alarm_message", alarm.getBookName() + ": " + alarm.getMessage());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    (int) alarm.getDeadlineMillis(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            if (user != null) {
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.getUid())
                        .collection("alarms")
                        .document(alarm.getAlarmId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Alarm deleted", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        dialog.show();
    }
}