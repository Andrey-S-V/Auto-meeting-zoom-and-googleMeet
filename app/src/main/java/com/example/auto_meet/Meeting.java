package com.example.auto_meet;

import java.util.Date;

public class Meeting {
    private final String meetingNumber;
    private final String meetingPassword;
    private final Date dateTime;
    private final boolean isZoom;

    public Meeting(String meetingNumber, String meetingPassword, Date dateTime, boolean isZoom) {
        this.meetingNumber = meetingNumber;
        this.meetingPassword = meetingPassword;
        this.dateTime = dateTime;
        this.isZoom = isZoom;
    }

    public String getMeetingNumber() {
        return meetingNumber;
    }

    public String getMeetingPassword() {
        return meetingPassword;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public boolean isZoom() {
        return isZoom;
    }
}

