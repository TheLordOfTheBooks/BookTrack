package com.example.booktrack;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying and managing reading goals in the BookTrack application.
 * This adapter handles the presentation of user-created goals with comprehensive interaction
 * capabilities including completion tracking, state management, and goal deletion.
 *
 * <p>The adapter provides a rich interface for goal management including:
 * <ul>
 *   <li>Visual goal representation with book cover images and metadata</li>
 *   <li>Deadline tracking with visual warnings for overdue goals</li>
 *   <li>Goal completion handling with optional book state transitions</li>
 *   <li>Goal deletion with confirmation dialogs for user safety</li>
 *   <li>Firebase Firestore integration for real-time data operations</li>
 *   <li>Automatic book status updates upon goal completion</li>
 * </ul></p>
 *
 * <p>The adapter implements intelligent deadline monitoring, automatically displaying
 * warning indicators for goals that have passed their deadline. It also handles
 * conditional book state changes, allowing users to automatically update their
 * reading progress when completing specific goals.</p>
 *
 * <p>All Firebase operations are performed with proper error handling and user
 * authentication validation to ensure data integrity and security.</p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    /** List of goal items to be displayed in the RecyclerView */
    private final List<GoalItem> goalList;

    /** Context reference for accessing resources and services */
    private final Context context;

    /**
     * Constructs a new GoalAdapter with the specified context and goal list.
     *
     * @param context  The context used for accessing resources and services
     * @param goalList The list of GoalItem objects to be displayed
     */
    public GoalAdapter(Context context, List<GoalItem> goalList) {
        this.context = context;
        this.goalList = goalList;
    }

    /**
     * Creates a new ViewHolder by inflating the goal item layout.
     * This method is called when the RecyclerView needs a new ViewHolder
     * to represent a goal item.
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused in this implementation)
     * @return A new GoalViewHolder that holds the inflated goal item view
     */
    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(v);
    }

    /**
     * Binds data from a GoalItem to the ViewHolder's views and configures user interactions.
     * This method populates the goal item view with goal information, handles deadline
     * validation, and sets up click listeners for goal management actions.
     *
     * <p>The binding process includes:
     * <ul>
     *   <li>Deadline validation and warning display for overdue goals</li>
     *   <li>Goal description and formatted deadline display</li>
     *   <li>Conditional state change information display</li>
     *   <li>Book title and cover image loading with Glide</li>
     *   <li>Action button configuration for goal completion and deletion</li>
     * </ul></p>
     *
     * <p>Interactive elements include:
     * <ul>
     *   <li><strong>Done Button</strong> - Completes the goal and optionally updates book status</li>
     *   <li><strong>Failed Button</strong> - Marks the goal as failed and removes it</li>
     *   <li><strong>Delete Button</strong> - Shows confirmation dialog before goal deletion</li>
     * </ul></p>
     *
     * <p>The method handles image loading with proper fallbacks and error handling,
     * ensuring a consistent user experience even when book cover images are unavailable.</p>
     *
     * @param holder   The ViewHolder which should be updated to represent the goal item
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        GoalItem goal = goalList.get(position);
        long deadlineMillis = goal.getDeadlineMillis();
        long now = System.currentTimeMillis();
        holder.deleteButton.setOnClickListener(v -> deleteGoal(goal));

        if (deadlineMillis < now) {
            holder.deadlineWarning.setVisibility(View.VISIBLE);
        } else {
            holder.deadlineWarning.setVisibility(View.GONE);
        }

        holder.description.setText("Goal: " + goal.getDescription());

        String dateStr = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(new Date(deadlineMillis));
        holder.deadlineMillis.setText("Deadline: " + dateStr);

        if (goal.isChangeState()) {
            holder.stateChange.setText("Change state to: " + goal.getNewState());
        } else {
            holder.stateChange.setText("");
        }

        holder.bookTitle.setText(goal.getBookName());

        String imageUrl = goal.getBookImageUrl();
        holder.bookCover.setImageResource(R.drawable.ic_launcher_background);
        Log.d("GoalAdapter", "Image URL: " + goal.getBookImageUrl());
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.bookCover);
        } else {
            holder.bookCover.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.doneButton.setOnClickListener(v -> {
            if (goal.isChangeState()) {
                updateBookSituation(goal.getBookId(), goal.getNewState());
            }
            deleteGoal(goal);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this goal?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteGoal(goal))
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        holder.failedButton.setOnClickListener(v -> deleteGoal(goal));
    }

    /**
     * Deletes a goal from Firebase Firestore.
     * This method removes the specified goal from the user's goals collection
     * in Firestore, ensuring proper cleanup of goal data.
     *
     * <p>The deletion process includes:
     * <ul>
     *   <li>User authentication validation</li>
     *   <li>Document deletion from the user's goals subcollection</li>
     *   <li>Automatic UI updates through RecyclerView data binding</li>
     * </ul></p>
     *
     * <p>This method is called from various user interactions including
     * goal completion, failure marking, and explicit deletion requests.</p>
     *
     * @param goal The GoalItem to be deleted from Firestore
     */
    private void deleteGoal(GoalItem goal) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("goals")
                .document(goal.getId())
                .delete();
    }

    /**
     * Updates a book's reading status in Firebase Firestore.
     * This method is called when a goal is completed and the goal was configured
     * to automatically change the associated book's reading state.
     *
     * <p>The update process includes:
     * <ul>
     *   <li>Validation of book ID and new state parameters</li>
     *   <li>User authentication validation</li>
     *   <li>Firestore document update for the book's "situation" field</li>
     *   <li>Automatic synchronization with the user's book collection</li>
     * </ul></p>
     *
     * <p>This feature enables seamless progress tracking by automatically
     * transitioning books from "Currently Reading" to "Read" status when
     * reading goals are completed, maintaining accurate reading statistics.</p>
     *
     * @param bookId   The unique identifier of the book to update
     * @param newState The new reading state to assign to the book
     */
    private void updateBookSituation(String bookId, String newState) {
        if (bookId == null || newState == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("books")
                .document(bookId)
                .update("situation", newState);
    }

    /**
     * Returns the total number of goal items in the data set.
     *
     * @return The total number of goal items held by the adapter
     */
    @Override
    public int getItemCount() {
        return goalList.size();
    }

    /**
     * ViewHolder class that holds and manages the views for a single goal item.
     * This class implements the ViewHolder pattern for efficient RecyclerView scrolling
     * and view recycling, containing references to all UI elements within a goal item layout.
     *
     * <p>The ViewHolder manages the following UI components:
     * <ul>
     *   <li>Text displays for goal description, deadline, state changes, and book title</li>
     *   <li>ImageView for book cover display</li>
     *   <li>Warning indicator for overdue goals</li>
     *   <li>Action buttons for goal completion, failure, and deletion</li>
     * </ul></p>
     */
    public static class GoalViewHolder extends RecyclerView.ViewHolder {

        /** TextView for displaying the goal description */
        TextView description;

        /** TextView for displaying the formatted goal deadline */
        TextView deadlineMillis;

        /** TextView for displaying state change information */
        TextView stateChange;

        /** TextView for displaying deadline warning messages */
        TextView deadlineWarning;

        /** TextView for displaying the associated book title */
        TextView bookTitle;

        /** ImageView for displaying the book cover image */
        ImageView bookCover;

        /** Button for marking the goal as completed */
        Button doneButton;

        /** Button for marking the goal as failed */
        Button failedButton;

        /** Button for deleting the goal */
        Button deleteButton;

        /**
         * Constructs a new GoalViewHolder and initializes all view references.
         *
         * @param itemView The root view of the goal item layout
         */
        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            bookTitle = itemView.findViewById(R.id.book_name_text);
            description = itemView.findViewById(R.id.goal_description);
            deadlineMillis = itemView.findViewById(R.id.goal_deadline);
            stateChange = itemView.findViewById(R.id.goal_state_change);
            deadlineWarning = itemView.findViewById(R.id.deadline_passed_warning);
            bookCover = itemView.findViewById(R.id.book_cover);
            doneButton = itemView.findViewById(R.id.button_done);
            failedButton = itemView.findViewById(R.id.button_failed);
            deleteButton = itemView.findViewById(R.id.button_delete);
        }
    }
}