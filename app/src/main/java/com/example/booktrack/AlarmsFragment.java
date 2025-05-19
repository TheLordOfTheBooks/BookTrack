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

public class AlarmsFragment extends Fragment {

    private RecyclerView alarmsRecyclerView;
    private AlarmsAdapter alarmsAdapter;
    private List<AlarmItem> alarmList = new ArrayList<>();
    private Button addAlarmButton;
    private ListenerRegistration alarmListener;

    public AlarmsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_alarms_fragment, container, false);

    }

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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (alarmListener != null) {
            alarmListener.remove();
        }
    }

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