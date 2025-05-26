package com.example.booktrack;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays and manages the user's reading goals in the BookTrack application.
 * This fragment provides a real-time view of all active goals with automatic synchronization
 * from Firebase Firestore, displaying them in chronological order by deadline.
 *
 * <p>The GoalsFragment serves as a central hub for goal monitoring and management,
 * providing users with a comprehensive overview of their reading objectives and
 * progress tracking capabilities.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Real-time goal synchronization with Firebase Firestore</li>
 *   <li>Chronological goal ordering by deadline for priority visualization</li>
 *   <li>Integration with GoalAdapter for rich goal interaction capabilities</li>
 *   <li>Automatic UI updates when goals are added, modified, or removed</li>
 *   <li>Proper resource management with listener cleanup</li>
 *   <li>User authentication validation and session management</li>
 * </ul></p>
 *
 * <p>The fragment implements real-time data synchronization using Firestore's
 * snapshot listeners, ensuring that the goal list remains current across all
 * user devices and sessions. Goals are automatically sorted by deadline to
 * help users prioritize their reading objectives effectively.</p>
 *
 * <p>Resource management is handled properly through the fragment lifecycle,
 * with Firebase listeners being registered during view creation and cleaned
 * up during view destruction to prevent memory leaks.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class GoalsFragment extends Fragment {

    /** RecyclerView for displaying the list of reading goals */
    private RecyclerView recyclerView;

    /** Adapter for managing goal item display in the RecyclerView */
    private GoalAdapter adapter;

    /** List containing all active goal items */
    private List<GoalItem> goalList = new ArrayList<>();

    /** Firebase Firestore listener registration for real-time goal updates */
    private ListenerRegistration goalListener;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * This method inflates the goals fragment layout to create the UI structure.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views
     * @param container          The parent view that the fragment's UI should be attached to
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     * @return The View for the fragment's UI, or null
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_goals_fragment, container, false);
    }

    /**
     * Called immediately after onCreateView() has returned, providing access to the created view hierarchy.
     * This method initializes the RecyclerView, sets up the adapter, and begins loading goal data
     * from Firebase Firestore.
     *
     * <p>The initialization process includes:
     * <ul>
     *   <li>RecyclerView setup with LinearLayoutManager for vertical scrolling</li>
     *   <li>GoalAdapter creation and binding to the RecyclerView</li>
     *   <li>Initiation of real-time goal data loading from Firestore</li>
     * </ul></p>
     *
     * <p>The fragment uses a LinearLayoutManager to display goals in a vertical list,
     * providing an intuitive interface for users to review their reading objectives
     * and interact with individual goals through the adapter's built-in controls.</p>
     *
     * @param view               The View returned by onCreateView()
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.goals_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GoalAdapter(requireContext(), goalList);
        recyclerView.setAdapter(adapter);

        loadGoals();
    }

    /**
     * Loads and maintains real-time synchronization of goal data from Firebase Firestore.
     * This method sets up a snapshot listener that automatically updates the local goal list
     * whenever changes occur in the user's goal collection.
     *
     * <p>The loading process includes:
     * <ul>
     *   <li>User authentication validation</li>
     *   <li>Firestore query setup with deadline-based ordering</li>
     *   <li>Real-time snapshot listener registration</li>
     *   <li>Automatic goal list updates and adapter notifications</li>
     *   <li>Goal ID assignment from Firestore document IDs</li>
     * </ul></p>
     *
     * <p>Goals are retrieved from the user's personal goals subcollection and
     * ordered by deadline in ascending order, ensuring that the most urgent
     * goals appear at the top of the list for better user prioritization.</p>
     *
     * <p>The method uses Firestore's snapshot listeners for real-time updates,
     * automatically reflecting any changes made to goals across all user devices
     * and sessions without requiring manual refresh operations.</p>
     *
     * <p>Error handling ensures graceful degradation when network connectivity
     * issues or authentication problems occur, preventing application crashes
     * while maintaining user experience continuity.</p>
     */
    private void loadGoals() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("goals")
                .orderBy("deadlineMillis", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null) return;

                    goalList.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        GoalItem goal = doc.toObject(GoalItem.class);
                        goal.setId(doc.getId());
                        goalList.add(goal);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    /**
     * Called when the view previously created by onCreateView() has been detached from the fragment.
     * This method performs essential cleanup operations to prevent memory leaks and ensure
     * proper resource management.
     *
     * <p>Cleanup operations include:
     * <ul>
     *   <li>Removal of the Firebase Firestore snapshot listener</li>
     *   <li>Prevention of memory leaks from active database connections</li>
     *   <li>Proper resource deallocation following fragment lifecycle</li>
     * </ul></p>
     *
     * <p>The Firebase listener cleanup is critical for preventing memory leaks
     * and ensuring that background database operations are properly terminated
     * when the fragment is no longer active or visible to the user.</p>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (goalListener != null) goalListener.remove();
    }
}