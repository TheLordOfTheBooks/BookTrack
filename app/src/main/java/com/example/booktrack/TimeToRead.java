package com.example.booktrack;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Activity that provides a comprehensive time and schedule management interface for reading activities.
 * This activity serves as a centralized hub for all time-related features in the BookTrack application,
 * including alarms, goals, and timer functionality through a tabbed navigation interface.
 *
 * <p>The TimeToRead activity implements a fragment-based architecture with bottom navigation,
 * allowing users to seamlessly switch between different time management tools while maintaining
 * a consistent user experience and efficient resource utilization.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Bottom navigation with three primary time management sections</li>
 *   <li>Fragment-based architecture for efficient memory management</li>
 *   <li>Edge-to-edge display with proper window insets handling</li>
 *   <li>Seamless navigation between alarms, goals, and timer functionality</li>
 *   <li>Consistent UI experience across all time management features</li>
 *   <li>Back navigation support through floating action button</li>
 * </ul></p>
 *
 * <p>The three main sections accessible through bottom navigation are:
 * <ul>
 *   <li><strong>Alarms</strong> - Reading reminder scheduling and management</li>
 *   <li><strong>Goals</strong> - Reading objective tracking and deadline management</li>
 *   <li><strong>Timer</strong> - Countdown timer for reading sessions</li>
 * </ul></p>
 *
 * <p>The activity provides a unified interface for all reading time management needs,
 * helping users maintain consistent reading habits through comprehensive scheduling
 * and tracking capabilities.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class TimeToRead extends AppCompatActivity {

    /** Floating action button for navigation (back arrow) */
    FloatingActionButton arrow;

    /**
     * Initializes the time management activity, sets up the navigation interface,
     * and configures fragment switching functionality.
     *
     * <p>The initialization process includes:
     * <ul>
     *   <li>Edge-to-edge display configuration with proper window insets</li>
     *   <li>Bottom navigation view setup and event listener configuration</li>
     *   <li>Fragment switching logic for seamless navigation between sections</li>
     *   <li>Default fragment selection (AlarmsFragment) for initial user experience</li>
     *   <li>Back navigation button setup for returning to previous activity</li>
     * </ul></p>
     *
     * <p>Navigation configuration includes:
     * <ul>
     *   <li><strong>nav_alarm</strong> → AlarmsFragment for reading reminder management</li>
     *   <li><strong>nav_goal</strong> → GoalsFragment for reading objective tracking</li>
     *   <li><strong>nav_timer</strong> → TimerFragment for reading session timing</li>
     * </ul></p>
     *
     * <p>The fragment switching mechanism uses replace transactions to ensure
     * proper memory management and smooth transitions between different
     * time management interfaces.</p>
     *
     * <p>The activity defaults to showing the AlarmsFragment, providing immediate
     * access to the most commonly used time management feature while allowing
     * easy navigation to other sections as needed.</p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                          this Bundle contains the data it most recently supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_time_to_read);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView navView = findViewById(R.id.bottom_nav);
        navView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_alarm) {
                selectedFragment = new AlarmsFragment();
            } else if (item.getItemId() == R.id.nav_goal) {
                selectedFragment = new GoalsFragment();
            } else if (item.getItemId() == R.id.nav_timer) {
                selectedFragment = new TimerFragment();
            } else {
                return false;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            } else {
                return false;
            }

        });

        navView.setSelectedItemId(R.id.nav_alarm);
        arrow = findViewById(R.id.arrow);
        arrow.setOnClickListener(v ->finish());
    }
}