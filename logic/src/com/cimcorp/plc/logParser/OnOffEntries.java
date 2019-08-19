package com.cimcorp.plc.logParser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OnOffEntries {

    private Calendar refTimeStart;
    private Calendar refTimeEnd;
    private List<OnOffEntry> entries = new ArrayList<OnOffEntry>();

    public OnOffEntries(Calendar refTimeStart, Calendar refTimeEnd) {
        this.refTimeStart = refTimeStart;
        this.refTimeEnd = refTimeEnd;
    }

    public Calendar getRefTimeStart() {
        return refTimeStart;
    }

    public Calendar getRefTimeEnd() {
        return refTimeEnd;
    }

    public OnOffEntries setRefTimeEnd(Calendar refTimeEnd) {
        this.refTimeEnd = refTimeEnd;
        return this;
    }

    public OnOffEntries setRefTimeStart(Calendar refTimeStart) {
        this.refTimeStart = refTimeStart;
        return this;
    }

    public List<OnOffEntry> getEntries() {
        return entries;
    }

    public OnOffEntries setEntries(List<OnOffEntry> entries) {
        this.entries = entries;
        return this;
    }

    public static void checkAndAddEvent(OnOffEntries ofe, List<LogRow> llr, Calendar refTimeStart, Calendar refTimeEnd){

        ofe.setRefTimeStart(refTimeStart);
        ofe.setRefTimeEnd(refTimeEnd);

        /*for (LogRow lr : llr) {
            if (lr.getHeader().equals("ON") || lr.getHeader().equals("OFF")) {

                long count = ofe.getEntries().stream()
                        .filter(p -> p.getEvent().equals(lr.getDescription()))
                        .count();

                if (count == 0) {
                    ofe.getEntries().add(new OnOffEntry(lr, refTimeStart));
                }
                else {
                    int index = 0;
                    for (int i = 0; i < ofe.getEntries().size(); i++) {
                        if (ofe.getEntries().get(i).getEvent().equals(lr.getDescription())){
                            index = i;
                        }
                    }
                    ofe.getEntries().get(index).addEntry(new OnOffTime(lr.getHeader(),lr.getDateTime()));
                }
            }
        }*/

        for (OnOffEntry e : ofe.getEntries()) {
            e.finalizeTime(refTimeEnd);
        }

    }
}
