package com.cimcorp.plc.logParser;

import java.util.Calendar;

public class OnOffTime {

    String condition;
    Calendar time;

    public OnOffTime(String condition, Calendar time) {
        this.condition = condition;
        this.time = time;
    }

    public String getCondition() {
        return condition;
    }

    public OnOffTime setCondition(String condition) {
        this.condition = condition;
        return this;
    }

    public Calendar getTime() {
        return time;
    }

    public OnOffTime setTime(Calendar time) {
        this.time = time;
        return this;
    }
}
