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

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private final List<GoalItem> goalList;
    private final Context context;

    public GoalAdapter(Context context, List<GoalItem> goalList) {
        this.context = context;
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_item, parent, false);
        return new GoalViewHolder(v);
    }

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

    private void deleteGoal(GoalItem goal) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("goals")
                .document(goal.getId())
                .delete();
    }

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

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView description, deadlineMillis, stateChange, deadlineWarning, bookTitle;
        ImageView bookCover;
        Button doneButton, failedButton, deleteButton;

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
