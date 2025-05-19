package com.example.booktrack;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private Button BookList_btn, TimeToRead_btn;
    private View main_view;
    FloatingActionButton plus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BookList_btn = findViewById(R.id.book_list_button);
        TimeToRead_btn = findViewById(R.id.time_to_read_button);
        main_view = findViewById(R.id.main);
        plus = findViewById(R.id.plus);

        plus.setOnClickListener(v -> {
            startActivity(new Intent(this, AddMyBook.class));
        });
        BookList_btn.setOnClickListener(v -> {
            startActivity(new Intent(this, BookList.class));
        });
        TimeToRead_btn.setOnClickListener(v -> {
            startActivity(new Intent(this, TimeToRead.class));
        });

        BookList_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        BookList_btn.setTextColor(Color.BLACK);
        TimeToRead_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        TimeToRead_btn.setTextColor(Color.BLACK);
        main_view.setBackgroundColor(Color.parseColor("#eed9c4"));
    }






}