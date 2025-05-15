package com.example.booktrack;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import android.util.Log;
import com.bumptech.glide.Glide;
import androidx.activity.result.ActivityResultLauncher;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.Manifest;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;


import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResult;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.booktrack.Book;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AlarmsFragment extends Fragment {

    private RecyclerView alarmsRecyclerView;
    private AlarmsAdapter alarmsAdapter;
    private List<AlarmItem> alarmList = new ArrayList<>();
    private Button addAlarmButton;

    public AlarmsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_alarms_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        alarmsRecyclerView = view.findViewById(R.id.alarms_recycler_view);
        alarmsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        alarmsAdapter = new AlarmsAdapter(getContext(), alarmList);
        alarmsRecyclerView.setAdapter(alarmsAdapter);
        addAlarmButton = view.findViewById(R.id.add_alarm_button);
        addAlarmButton.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), CreateAlarm.class);
            startActivity(intent);
        });

        loadAlarmsFromDatabase(); // You’ll implement this to read from Firestore/Room
    }

    private void loadAlarmsFromDatabase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("alarms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    alarmList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        AlarmItem alarm = doc.toObject(AlarmItem.class);
                        alarmList.add(alarm);
                    }
                    alarmsAdapter.notifyDataSetChanged(); // ✅ refresh RecyclerView
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load alarms", Toast.LENGTH_SHORT).show());
    }



    /*private void loadAlarmsFromDatabase() {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUserUid)
                .collection("alarms")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    alarmList.clear();
                    for (DocumentSnapshot doc : querySnapshot) {
                        AlarmItem alarm = doc.toObject(AlarmItem.class);
                        alarmList.add(alarm);
                    }
                    alarmsAdapter.notifyDataSetChanged();
                });
    }*/
}