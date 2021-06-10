package com.cimcorp.plc.logParser;

public class PivotRowDataPoint {

    private String key;
    private String value;
    private String columnHeader;

    public PivotRowDataPoint() {}

    public PivotRowDataPoint(String s) {

       if (s.indexOf(':') >= 0) {
           String[] sArray = s.split(":");
           this.key = sArray[0];
           this.key = this.key.replace("{","");
           this.key = this.key.replace("}","");
           this.value = sArray[1];
           this.value = this.value.replace("}","");
           try {
               Integer.parseInt(value);
               this.columnHeader = this.key;
           } catch (NumberFormatException e) {
               this.columnHeader = this.key + ":" + this.value;
               this.value = "1";
           }
       } else {
           this.key = s;
           this.columnHeader = s;
           this.value = "1";
       }

    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getColumnHeader() {
        return columnHeader;
    }
}
