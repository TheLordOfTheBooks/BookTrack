package com.example.booktrack;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying reading alarms and reminders in the BookTrack application.
 * This adapter manages a list of alarm items, each representing a scheduled reading reminder
 * associated with a specific book.
 *
 * <p>The adapter provides a visual representation of each alarm including the book cover image,
 * title, and scheduled date/time. It supports long-click interactions for alarm management
 * operations such as editing or deletion.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Display of book cover images using Glide for efficient image loading</li>
 *   <li>Formatted date and time display for alarm schedules</li>
 *   <li>Long-click listener support for alarm management operations</li>
 *   <li>Efficient ViewHolder pattern implementation for smooth scrolling</li>
 *   <li>Integration with AlarmItem data model</li>
 * </ul></p>
 *
 * @author BookTrack Development Team
 * @version 1.0
 * @since 1.0
 */
public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>{

    /** Context reference for accessing resources and services */
    private final Context context;

    /** List of alarm items to be displayed in the RecyclerView */
    private final List<AlarmItem> alarmList;

    /** Listener for handling long-click events on alarm items */
    private OnAlarmLongClickListener longClickListener;

    /**
     * Constructs a new AlarmsAdapter with the specified context and alarm list.
     *
     * @param context   The context used for accessing resources and services
     * @param alarmList The list of AlarmItem objects to be displayed
     */
    public AlarmsAdapter(Context context, List<AlarmItem> alarmList) {
        this.context = context;
        this.alarmList = alarmList;
    }

    /**
     * Sets the listener for handling long-click events on alarm items.
     * This allows the parent component to respond to user interactions
     * such as editing or deleting alarms.
     *
     * @param listener The OnAlarmLongClickListener to handle long-click events
     */
    public void setOnAlarmLongClickListener(OnAlarmLongClickListener listener) {
        this.longClickListener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the alarm item layout.
     * This method is called when the RecyclerView needs a new ViewHolder
     * to represent an alarm item.
     *
     * @param parent   The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View (unused in this implementation)
     * @return A new AlarmViewHolder that holds the inflated alarm item view
     */
    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    /**
     * Binds data from an AlarmItem to the ViewHolder's views.
     * This method populates the alarm item view with book information,
     * formatted date/time, and sets up the long-click listener.
     *
     * <p>The binding process includes:
     * <ul>
     *   <li>Setting the book title text</li>
     *   <li>Formatting and displaying the alarm date (yyyy-MM-dd format)</li>
     *   <li>Formatting and displaying the alarm time (HH:mm format)</li>
     *   <li>Loading the book cover image using Glide</li>
     *   <li>Setting up the long-click listener for alarm management</li>
     * </ul></p>
     *
     * @param holder   The ViewHolder which should be updated to represent the alarm item
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem alarm = alarmList.get(position);

        holder.bookTitle.setText(alarm.getBookName());

        Date date = new Date(alarm.getDeadlineMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        holder.dateText.setText(dateFormat.format(date));
        holder.timeText.setText(timeFormat.format(date));

        Glide.with(context)
                .load(alarm.getBookImageUrl())
                .into(holder.bookCover);

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onAlarmLongClick(alarm);
            }
            return true;
        });
    }

    /**
     * Returns the total number of alarm items in the data set.
     *
     * @return The total number of alarm items held by the adapter
     */
    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    /**
     * ViewHolder class that holds and manages the views for a single alarm item.
     * This class implements the ViewHolder pattern for efficient RecyclerView scrolling
     * and view recycling.
     *
     * <p>The ViewHolder contains references to all the views within an alarm item layout,
     * including the book cover image, title, date, and time display elements.</p>
     */
    public static class AlarmViewHolder extends RecyclerView.ViewHolder {

        /** ImageView for displaying the book cover image */
        ImageView bookCover;

        /** TextView for displaying the book title */
        TextView bookTitle;

        /** TextView for displaying the alarm date */
        TextView dateText;

        /** TextView for displaying the alarm time */
        TextView timeText;

        /**
         * Constructs a new AlarmViewHolder and initializes all view references.
         *
         * @param itemView The root view of the alarm item layout
         */
        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            dateText = itemView.findViewById(R.id.alarm_date);
            timeText = itemView.findViewById(R.id.alarm_time);
        }
    }

    /**
     * Interface for handling long-click events on alarm items.
     * Implementing classes can use this interface to respond to user interactions
     * such as editing, deleting, or viewing details of specific alarms.
     */
    public interface OnAlarmLongClickListener {
        /**
         * Called when an alarm item is long-clicked by the user.
         *
         * @param alarm The AlarmItem that was long-clicked
         */
        void onAlarmLongClick(AlarmItem alarm);
    }
}