package com.cimcorp.plc.logParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OnOffEntry {

    private String event;
    private List<OnOffTime> detail = new ArrayList<>();
    private long onTime = 0;
    private long offTime = 0;
    private boolean calculatedTime = false;

    public OnOffEntry() {
    }

    public OnOffEntry(LogRow lr, Calendar refTime) {
        if (lr.getHeader().equals("ON") || lr.getHeader().equals("OFF")) {
            this.event = lr.getDescription();
            detail.add(new OnOffTime(lr.getHeader(), lr.getDateTime()));

            if (lr.getHeader().equals("ON")){
                offTime = lr.getDateTime().getTimeInMillis() - refTime.getTimeInMillis();
                onTime = 0;
            }
            else {
                onTime = lr.getDateTime().getTimeInMillis() - refTime.getTimeInMillis();
                offTime = 0;
            }
        }
    }

    public LoggedTime getOnTime() {
        return new LoggedTime(onTime);
    }

    public LoggedTime getOffTime() {
        return new LoggedTime(offTime);
    }

    public String getEvent() {
        return event;
    }

    public OnOffEntry setEvent(String event) {
        this.event = event;
        return this;
    }

    public List<OnOffTime> getDetail() {
        return detail;
    }

    public OnOffEntry setDetail(List<OnOffTime> detail) {
        this.detail = detail;
        return this;
    }

    public void addEntry(OnOffTime oft) {

        this.detail.add(oft);
        if (oft.getCondition().equals("ON")) {
            offTime = offTime + (oft.getTime().getTimeInMillis() - detail.get(detail.size()-2).getTime().getTimeInMillis());
        }
        else {
            onTime = onTime + (oft.getTime().getTimeInMillis() - detail.get(detail.size()-2).getTime().getTimeInMillis());
        }
    }

    public void finalizeTime(Calendar refTime) {

        if (!calculatedTime) {
            if (detail.get(detail.size()-1).getCondition().equals("ON")) {
                onTime = onTime + (refTime.getTimeInMillis() - detail.get(detail.size()-1).getTime().getTimeInMillis());
            }
            else {
                offTime = offTime + (refTime.getTimeInMillis() - detail.get(detail.size() - 1).getTime().getTimeInMillis());
            }
            calculatedTime = true;

        }

    }

    public String toString(){
        return event + "\n"
                + "On Time: " + getOnTime().toString() + "\n"
                + "Off Time: " + getOffTime().toString();
    }

}
