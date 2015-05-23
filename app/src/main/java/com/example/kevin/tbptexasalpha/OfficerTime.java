package com.example.kevin.tbptexasalpha;

/**
 * Created by kevinrosen1 on 5/23/15.
 */
public class OfficerTime {
    private int hours;
    private String startTime;

    public OfficerTime(String time, int hours){
        startTime = time;
        this.hours = hours;
    }

    public String getStartTime(){
        return startTime;
    }

    public int getHours(){
        return hours;
    }

    @Override
    public String toString() {
        return "Start Time: " + startTime + ", Blocks: " + hours;
    }
}
