package com.example.booktrack;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

/**
 * BookList is the main activity that manages and displays different categories of books
 * in a book tracking application. It provides a tabbed interface using bottom navigation
 * to switch between different reading states (Read, Currently Reading, Stopped Reading, Want to Read).
 *
 * <p>This activity uses fragments to display book lists for each category and provides
 * navigation controls through a bottom navigation view and a floating action button for going back.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class BookList extends AppCompatActivity {

    /**
     * RecyclerView component for displaying the list of books.
     * Currently declared but not actively used in the current implementation.
     */
    private RecyclerView recyclerView;

    /**
     * List containing Book objects to be displayed.
     * Currently declared but not actively used in the current implementation.
     */
    private List<Book> bookList;

    /**
     * Adapter for binding book data to the RecyclerView.
     * Currently declared but not actively used in the current implementation.
     */
    private BookAdapter adapter;

    /**
     * Floating action button that serves as a back/arrow button to close the activity.
     */
    FloatingActionButton arrow;

    /**
     * Called when the activity is first created. This method sets up the user interface,
     * enables edge-to-edge display, configures window insets, initializes the bottom navigation,
     * and sets up the floating action button.
     *
     * <p>The method performs the following operations:</p>
     * <ul>
     *   <li>Sets the content view to the book list layout</li>
     *   <li>Enables edge-to-edge display for modern Android UI</li>
     *   <li>Configures window insets for proper padding</li>
     *   <li>Loads the default "Read" fragment if no saved state exists</li>
     *   <li>Sets up bottom navigation with four categories</li>
     *   <li>Configures the floating action button as a back button</li>
     * </ul>
     *
     * @param savedInstanceState If the activity is being re-created from a previous saved state,
     *                          this is the state data. If null, the activity is being created fresh.
     * @see AppCompatActivity#onCreate(Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_book_list);

        // Configure window insets for edge-to-edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load default fragment if this is a fresh activity creation
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, BookListFragment.newInstance("Read"))
                    .commit();
        }

        // Setup bottom navigation with category switching
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int id = item.getItemId();

            // Determine which fragment to display based on selected navigation item
            if (id == R.id.nav_read) {
                selectedFragment = BookListFragment.newInstance("Read");
            } else if (id == R.id.nav_current) {
                selectedFragment = BookListFragment.newInstance("Currently Reading");
            } else if (id == R.id.nav_stopped) {
                selectedFragment = BookListFragment.newInstance("Stopped Reading");
            } else if (id == R.id.nav_want) {
                selectedFragment = BookListFragment.newInstance("Want to Read");
            }

            // Replace current fragment with selected one
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set default selected item to "Read" category
        bottomNav.setSelectedItemId(R.id.nav_read);

        // Setup floating action button as back/close button
        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(v -> finish());
    }
}