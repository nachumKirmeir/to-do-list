package com.nachumToDoApp.vr2;

public class TimerData {

    //the position of the item in the list
    private int position;
    //the amount of time until the timer stop
    private long duration;

    public TimerData(int position, long duration) {
        this.position = position;
        this.duration = duration;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getPosition() {
        return position;
    }

    public long getDuration() {
        return duration;
    }
}