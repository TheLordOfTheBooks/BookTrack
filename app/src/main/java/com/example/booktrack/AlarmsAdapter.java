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


public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.AlarmViewHolder>{
    private final Context context;
    private final List<AlarmItem> alarmList;

    public AlarmsAdapter(Context context, List<AlarmItem> alarmList) {
        this.context = context;
        this.alarmList = alarmList;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmItem alarm = alarmList.get(position);
        holder.bookTitle.setText(alarm.getBookName());

        long millis = alarm.getTriggerMillis();
        Date date = new Date(millis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        holder.dateText.setText(dateFormat.format(date));
        holder.timeText.setText(timeFormat.format(date));
        Glide.with(context).load(alarm.getBookImageUrl()).into(holder.bookCover);
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        ImageView bookCover;
        TextView bookTitle, dateText, timeText;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            bookCover = itemView.findViewById(R.id.book_cover);
            bookTitle = itemView.findViewById(R.id.book_title);
            dateText = itemView.findViewById(R.id.alarm_date);
            timeText = itemView.findViewById(R.id.alarm_time);
        }
    }

}
