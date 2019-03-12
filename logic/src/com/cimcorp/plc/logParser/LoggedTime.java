package com.cimcorp.plc.logParser;

public class LoggedTime {

    private int days = 0;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private int milliSeconds = 0;

    public LoggedTime(long lt) {

        double dayInMs = 1000*60*60*24;
        double hourInMs = 1000*60*60;
        double minuteInMs = 1000*60;
        double secondInMs = 1000;
        double remainder = 0;

        days = (int) (lt / dayInMs);
        remainder = lt - (days*dayInMs);
        hours = (int) (remainder / hourInMs);
        remainder = remainder - (hours*hourInMs);
        minutes = (int) (remainder / minuteInMs);
        remainder = remainder - (minutes*minuteInMs);
        seconds = (int) (remainder / secondInMs);
        remainder = remainder - (seconds*secondInMs);
        milliSeconds = (int) remainder;
        System.out.println();

    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getMilliSeconds() {
        return milliSeconds;
    }

    public String toString(){
        return "Days: " + Integer.toString(days) + " "
                + "Hours: " + Integer.toString(hours) + " "
                + "Minutes: " + Integer.toString(minutes) + " "
                + "Seconds: " + Integer.toString(seconds) + " "
                + "MilliSeconds: " + Integer.toString(milliSeconds);

    }
}
