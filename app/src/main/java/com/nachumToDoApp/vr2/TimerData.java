package com.nachumToDoApp.vr2;

public class TimerData {
    private int position;
    private long duration;

    public TimerData(int position, long duration) {
        this.position = position;
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public long getDuration() {
        return duration;
    }
}