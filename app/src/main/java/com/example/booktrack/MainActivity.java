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

public class MainActivity extends AppCompatActivity {

    private Button AddBook_btn, BookList_btn, TimeToRead_btn;
    private View main_view;

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
        AddBook_btn = findViewById(R.id.add_book_button);
        BookList_btn = findViewById(R.id.book_list_button);
        TimeToRead_btn = findViewById(R.id.add_book);
        main_view = findViewById(R.id.main);

        AddBook_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        AddBook_btn.setTextColor(Color.BLACK);
        BookList_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        BookList_btn.setTextColor(Color.BLACK);
        TimeToRead_btn.setBackgroundColor(Color.parseColor("#FAF0E6"));
        TimeToRead_btn.setTextColor(Color.BLACK);
        main_view.setBackgroundColor(Color.parseColor("#eed9c4"));


    }

    public void AddMyBook(View view){
        Intent si = new Intent(this, AddMyBook.class);
        startActivity(si);
    }

    public void BookList(View view){
        Intent si = new Intent(this, BookList.class);
        startActivity(si);
    }

    public void TimeToRead(View view){
        Intent si = new Intent(this, TimeToRead.class);
        startActivity(si);
    }



}