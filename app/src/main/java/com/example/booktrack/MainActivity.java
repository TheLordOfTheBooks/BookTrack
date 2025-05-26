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

/**
 * MainActivity serves as the primary entry point for the BookTrack application.
 * This activity provides the main navigation interface with buttons to access
 * different sections of the app including the book list and time-to-read features.
 * It also includes a floating action button for adding new books.
 *
 * The activity implements edge-to-edge display and applies custom styling
 * with a warm color scheme using beige and cream colors.
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Button for navigating to the book list screen.
     * Displays all books in the user's collection.
     */
    private Button BookList_btn;

    /**
     * Button for navigating to the time-to-read calculation screen.
     * Provides reading time estimates and tracking functionality.
     */
    private Button TimeToRead_btn;

    /**
     * Main view container that serves as the root layout for the activity.
     * Used for applying background styling and window insets.
     */
    private View main_view;

    /**
     * Floating action button for adding new books to the collection.
     * Provides quick access to the add book functionality.
     */
    FloatingActionButton plus;

    /**
     * Called when the activity is first created. This method initializes the UI components,
     * sets up the edge-to-edge display, configures window insets, and establishes
     * click listeners for navigation buttons.
     *
     * The method also applies custom styling to buttons and the main view with
     * a consistent color scheme using beige (#FAF0E6) for buttons and cream (#eed9c4)
     * for the background.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                          previously being shut down, this Bundle contains the data
     *                          it most recently supplied in onSaveInstanceState(Bundle).
     *                          Note: Otherwise it is null.
     *
     * @see androidx.appcompat.app.AppCompatActivity#onCreate(Bundle)
     */
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