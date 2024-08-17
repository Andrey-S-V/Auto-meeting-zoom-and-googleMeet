package com.example.auto_meet.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.auto_meet.Meeting;
import com.example.auto_meet.SwipeToDeleteCallback;
import com.example.auto_meet.adapter.MeetingAdapter;
import com.example.auto_meet_zoom.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private MeetingAdapter meetingAdapter;
    private List<Meeting> meetings;
    private final Handler handler = new Handler();

    @BindView(R.id.textMeetingIdAndPassword) TextView mTextMeetingIdAndPassword;
    @BindView(R.id.buttonMeetingIdAndPassword) TextView mButtonMeetingIdAndPassword;
    @BindView(R.id.textMeetingLink) TextView mTextMeetingLink;
    @BindView(R.id.buttonMeetingLink) TextView mButtonMeetingLink;
    @BindView(R.id.textMeetingReference) TextView mTextMeetingReference;
    @BindView(R.id.buttonMeetingReference) TextView mButtonMeetingReference;

    @BindView(R.id.layout) LinearLayout mLayout;
    @BindView(R.id.TitleName) TextView mTitleText;

    private static final String PREFS_NAME = "MyPrefs";
    public static final String MEETINGS_KEY = "Meetings";
    private static final String TAG = "MainActivity";

    private final SimpleDateFormat internalDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        RecyclerView recyclerView = findViewById(R.id.listTimeMeeting);

        meetings = loadMeetingsFromSharedPreferences();
        Log.d(TAG, "Loaded meetings: " + meetings);

        if (meetings == null) {
            meetings = new ArrayList<>();
        }

        meetingAdapter = new MeetingAdapter(meetings, getSharedPreferences(PREFS_NAME, MODE_PRIVATE));

        recyclerView.setAdapter(meetingAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(meetingAdapter, this));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        startBackgroundTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMeetingsToSharedPreferences(meetings);
        Log.d(TAG, "Saved meetings on destroy: " + meetings);
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBackgroundTask();
    }

    @OnClick(R.id.googleMeet)
    public void onGoogleMeetClick(){
        updateUI(false);
    }

    @OnClick(R.id.zoom)
    public void onZoomClick(){
        updateUI(true);
    }

    public void updateUI(boolean isZoom) {
        mTitleText.setText(isZoom ? R.string.title_zoom_name : R.string.title_google_meet_name);
        mTitleText.setTextColor(ContextCompat.getColor(this, isZoom ? R.color.white : R.color.green));
        mTextMeetingReference.setTextColor(ContextCompat.getColor(this, isZoom ? R.color.white : R.color.green));

        mLayout.setBackgroundColor(ContextCompat.getColor(this,isZoom ? R.color.blue : R.color.lightGreen));

        mTextMeetingIdAndPassword.setVisibility(isZoom ? View.VISIBLE : View.GONE);
        mButtonMeetingIdAndPassword.setVisibility(isZoom ? View.VISIBLE : View.GONE);
        mTextMeetingLink.setVisibility(isZoom ? View.VISIBLE : View.GONE);
        mButtonMeetingLink.setVisibility(isZoom ? View.VISIBLE : View.GONE);
        mTextMeetingReference.setVisibility(isZoom ? View.GONE : View.VISIBLE);
        mButtonMeetingReference.setVisibility(isZoom ? View.GONE : View.VISIBLE);
    }

    public void ConnectToGoogleMeetWithReference(View view) {
        createMeetWithReference();
    }

    public void ConnectToMeetWithCode(View view) {
        createJoinMeetingDialog();
    }

    public void ConnectToMeetWithLink(View view) {
        connectToMeetingWithLink();
    }

    private void startBackgroundTask() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkAndConnectToMeeting();
                handler.postDelayed(this, TimeUnit.MINUTES.toMillis(1));
            }
        }, TimeUnit.MINUTES.toMillis(1));
    }

    private void checkAndConnectToMeeting() {
        Calendar currentCalendar = Calendar.getInstance();

        for (Meeting meeting : meetings) {
            Calendar meetingCalendar = Calendar.getInstance();
            meetingCalendar.setTime(meeting.getDateTime());

            if (internalDateFormat.format(currentCalendar.getTime()).equals(internalDateFormat.format(meetingCalendar.getTime()))) {
                if (meeting.isZoom()) {
                    connectToMeeting(meeting.getMeetingNumber(), meeting.getMeetingPassword());
                } else {
                    connectToGoogleMeet(meeting.getMeetingNumber()); // Используем meetingNumber для хранения ссылки на Google Meet
                }
                break;
            }
        }
    }

    public void createMeetWithReference() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_link_google_meet, null);
        builder.setView(view);

        final EditText linkMeeting = view.findViewById(R.id.link);
        final Button btnConnect = view.findViewById(R.id.btnConnect);

        final AlertDialog dialog = builder.create();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDateTimePicker(linkMeeting.getText().toString(), null, false);
            }
        });

        dialog.show();
    }

    private void createJoinMeetingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_join_zoom_meeting, null);
        builder.setView(view);

        final EditText numberInput = view.findViewById(R.id.meeting_no_input);
        final EditText passwordInput = view.findViewById(R.id.password_input);
        final Button btnConnect = view.findViewById(R.id.btnConnect);

        final AlertDialog dialog = builder.create();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDateTimePicker(numberInput.getText().toString(), passwordInput.getText().toString(), true);
            }
        });

        dialog.show();
    }

    private void connectToMeetingWithLink() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_link_zoom, null);
        builder.setView(view);

        final EditText linkMeeting = view.findViewById(R.id.link);
        final Button btnConnect = view.findViewById(R.id.btnConnect);

        final AlertDialog dialog = builder.create();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showDateTimePicker(linkMeeting.getText().toString(), null, true);
            }
        });

        dialog.show();
    }

    private void connectToMeeting(String meetingNumber, String meetingPassword) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("zoomus://zoom.us/join?confno=" + meetingNumber + "&pwd=" + meetingPassword));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void connectToGoogleMeet(String meetingLink) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(meetingLink));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showDateTimePicker(final String meetingNumber, final String password, final boolean isZoom) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar selectedCalendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        selectedCalendar.set(Calendar.YEAR, year);
                        selectedCalendar.set(Calendar.MONTH, monthOfYear);
                        selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        if (selectedCalendar.before(currentCalendar)) {
                            showErrorDialog(R.string.incorrect_date_entered, isZoom);
                        } else {
                            showTimePicker(selectedCalendar, meetingNumber, password, isZoom);
                        }
                    }
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void showTimePicker(final Calendar calendar, final String meetingNumber, final String password, final boolean isZoom) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        Calendar currentCalendar = Calendar.getInstance();
                        if (calendar.before(currentCalendar)) {
                            showErrorDialog(R.string.incorrect_time_entered, isZoom);
                        } else {
                            Meeting meeting = new Meeting(meetingNumber, password, calendar.getTime(), isZoom);
                            meetings.add(meeting);

                            meetingAdapter.notifyItemInserted(meetings.size() - 1);
                            saveMeetingsToSharedPreferences(meetings);
                            Log.d(TAG, "Added meeting: " + meeting);
                        }
                    }
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        );

        timePickerDialog.show();
    }

    private void showErrorDialog(int messageId, boolean isZoom) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_error, null);

        LinearLayout layoutView = view.findViewById(R.id.error_layout);
        TextView titleTextView = view.findViewById(R.id.error_title);
        TextView messageTextView = view.findViewById(R.id.error_message);
        Button okButton = view.findViewById(R.id.ok_button);

        layoutView.setBackground(ContextCompat.getDrawable(this, isZoom ? R.drawable.bg_button_blue : R.drawable.bg_button_green));
        titleTextView.setTextColor(ContextCompat.getColor(this,isZoom ? R.color.white : R.color.green));
        messageTextView.setTextColor(ContextCompat.getColor(this,isZoom ? R.color.white : R.color.green));
        okButton.setTextColor(ContextCompat.getColor(this,isZoom ? R.color.blue : R.color.green));

        messageTextView.setText(messageId);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void saveMeetingsToSharedPreferences(List<Meeting> meetings) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        Gson gson = new Gson();
        String meetingsJson = gson.toJson(meetings);

        editor.putString(MEETINGS_KEY, meetingsJson);
        editor.apply();
        Log.d(TAG, "Saved meetings to SharedPreferences: " + meetingsJson);
    }

    private List<Meeting> loadMeetingsFromSharedPreferences() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        Gson gson = new Gson();
        String meetingsJson = preferences.getString(MEETINGS_KEY, "");
        Log.d(TAG, "Loaded meetings JSON from SharedPreferences: " + meetingsJson);

        if (!TextUtils.isEmpty(meetingsJson)) {
            Type type = new TypeToken<List<Meeting>>(){}.getType();
            return gson.fromJson(meetingsJson, type);
        } else {
            return new ArrayList<>();
        }
    }
}
