package com.cimcorp.plc.logParser;

import java.util.Calendar;
import java.util.TimeZone;

public class LogRow {

    private Calendar dateTime;
    private double excelDateTime;
    private long id;
    private long level;
    private long colour;
    private String header;
    private String description;

    public LogRow(){}

    public LogRow(String[] str){
        parseExcelDateTime(str[0]);
        this.id = Integer.parseInt(str[1]);
        this.level = Integer.parseInt(str[2]);
        this.colour = Integer.parseInt(str[3]);
        this.header = str[4];
        this.description = str[5];
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public LogRow setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
        return this;
    }

    public double getExcelDateTime() {
        return excelDateTime;
    }

    public LogRow setExcelDateTime(double excelDateTime) {
        this.excelDateTime = excelDateTime;
        return this;
    }

    public long getId() {
        return id;
    }

    public LogRow setId(long id) {
        this.id = id;
        return this;
    }

    public long getLevel() {
        return level;
    }

    public LogRow setLevel(long level) {
        this.level = level;
        return this;
    }

    public long getColour() {
        return colour;
    }

    public LogRow setColour(long colour) {
        this.colour = colour;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public LogRow setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getDescription() {
        return description;
    }

    private void parseExcelDateTime(String s) {
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        c.clear();
        c.setLenient(false);

        Calendar excelRef = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        excelRef.clear();
        excelRef.setLenient(false);
        excelRef.set(1900,01,01);
        long excelRefDays = (excelRef.getTimeInMillis() * -1) / 1000 / 60 / 60 / 24;

        int i = s.indexOf("/");
        int month = Integer.parseInt(s.substring(0,i));
        int j = s.indexOf("/",i+1);
        int day = Integer.parseInt(s.substring(i+1,j));
        int year = Integer.parseInt(s.substring(j+1,j+5));

        i = s.indexOf(" ");
        j = s.indexOf(":");
        int hour = Integer.parseInt(s.substring(i+1,j));
        i = s.indexOf(":",j+1);
        int minute = Integer.parseInt(s.substring(j+1,i));
        j = s.indexOf(".",i);
        int second = Integer.parseInt(s.substring(i+1,j));
        int milliSecond = Integer.parseInt(s.substring(j+1,s.length())) / 1000;

        c.set(year,month,day,hour,minute,second);
        long k = c.getTimeInMillis() + (long)milliSecond;
        c.setTimeInMillis(k);
        this.setDateTime(c);

        double excelTime = (long) k;
        excelTime = (excelTime / 1000 / 60 / 60 / 24) + excelRefDays + 2;

        double timeInMillis = ((hour*1000*60*60)+(minute*1000*60)+(second*1000)+milliSecond) / (1000*60*60*24);
        excelTime = excelTime + timeInMillis;
        this.setExcelDateTime(excelTime);
    }

    /*private String[] buildCsvLine(LogRow lr, String[] header) {

        String[] s = new String[Array.getLength(header)];

        s[0] = Double.toString(lr.getExcelDateTime());
        s[1] = Integer.toString(lr.getDateTime().get(Calendar.HOUR_OF_DAY));
        s[2] = Integer.toString(lr.getDateTime().get(Calendar.MINUTE));

        for (int i = 3; i < Array.getLength(header); i++){
            if (lr.getColumnLabel().get(i-3).equals(lr.getDescription())) {
                // s[i] = Integer.toString(lr.getCrates());
            }
            else {
                s[i] = "0";
            }
        }

        return s;
    }*/

    /*@Override
    public List<String[]> toCSV(List<?> l) {

        List<LogRow> lr = (List<LogRow>) l;
        List<String[]> ls = new ArrayList<>();

        String[] header = new String[3 + lr.get(0).getColumnLabel().size()];
        header[0] = "DateTime";
        header[1] = "Hour";
        header[2] = "Minute";
        for (int i = 3; i < Array.getLength(header); i++){
            header[i] = lr.get(0).getColumnLabel().get(i-3);
        }

        ls.add(header);
        for (LogRow p : lr) {
            ls.add(buildCsvLine(p, header));
        }

        return ls;
    }*/


}