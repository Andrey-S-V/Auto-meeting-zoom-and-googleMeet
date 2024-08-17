package com.example.auto_meet.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auto_meet.Meeting;
import com.example.auto_meet_zoom.R;
import com.example.auto_meet.activity.MainActivity;
import com.google.gson.Gson;

import java.util.List;
import java.util.Locale;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

    private List<Meeting> meetings;
    private SharedPreferences preferences;

    public MeetingAdapter(List<Meeting> meetings, SharedPreferences preferences) {
        this.meetings = meetings;
        this.preferences = preferences;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetings.get(position);
        holder.bind(meeting);
    }

    @Override
    public int getItemCount() {
        return meetings.size();
    }

    public void deleteItem(int position) {
        meetings.remove(position);
        notifyItemRemoved(position);
        saveMeetingsToSharedPreferences(meetings);
    }

    private void saveMeetingsToSharedPreferences(List<Meeting> meetings) {
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String meetingsJson = gson.toJson(meetings);
        editor.putString(MainActivity.MEETINGS_KEY, meetingsJson);
        editor.apply();
    }

    public static class MeetingViewHolder extends RecyclerView.ViewHolder {

        private TextView meetingNumberTextView;
        private TextView meetingPasswordTextView;
        private TextView dateTimeTextView;
        private View icon;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            meetingNumberTextView = itemView.findViewById(R.id.meetingNumberTextView);
            meetingPasswordTextView = itemView.findViewById(R.id.meetingPasswordTextView);
            dateTimeTextView = itemView.findViewById(R.id.dateTimeTextView);
            icon = itemView.findViewById(R.id.icon);
        }

        public void bind(Meeting meeting) {
            Context context = itemView.getContext();
            Drawable drawable = ContextCompat.getDrawable(context, meeting.isZoom() ? R.drawable.icon_zoom : R.drawable.icon_google_meet);
            icon.setBackground(drawable);
            meetingNumberTextView.setText(meeting.getMeetingNumber());
            meetingNumberTextView.setTextColor(ContextCompat.getColor(context,meeting.isZoom() ? R.color.blue : R.color.green));
            meetingPasswordTextView.setVisibility(meeting.getMeetingPassword() == null ? View.GONE : View.VISIBLE);
            meetingPasswordTextView.setText(meeting.getMeetingPassword());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());
            dateTimeTextView.setText(sdf.format(meeting.getDateTime()));
            dateTimeTextView.setTextColor(ContextCompat.getColor(context,meeting.isZoom() ? R.color.blue : R.color.green));

        }
    }

}


