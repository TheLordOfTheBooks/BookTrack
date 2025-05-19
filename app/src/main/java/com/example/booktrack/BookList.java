package com.example.booktrack;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BookList extends AppCompatActivity {


    private RecyclerView recyclerView;
    private List<Book> bookList;
    private BookAdapter adapter;
    FloatingActionButton arrow;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list); // make sure this matches your layout file



        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, BookListFragment.newInstance("Read"))
                    .commit();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            if (id == R.id.nav_read) {
                selectedFragment = BookListFragment.newInstance("Read");
            } else if (id == R.id.nav_current) {
                selectedFragment = BookListFragment.newInstance("Currently Reading");
            } else if (id == R.id.nav_stopped) {
                selectedFragment = BookListFragment.newInstance("Stopped Reading");
            } else if (id == R.id.nav_want) {
                selectedFragment = BookListFragment.newInstance("Want to Read");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        bottomNav.setSelectedItemId(R.id.nav_read);
        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(v -> finish());
    }

}