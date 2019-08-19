package com.cimcorp.plc.logParser;

import csvUtils.CSVWriter;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class PivotRow implements CSVWriter {

    private static List<String> headers = new ArrayList<>();

    private Map<String,String> myColumnValues = new HashMap<>();

    public static List<String> getHeaders() {
        return headers;
    }

    public static void setHeaders(List<String> headers) {
        PivotRow.headers = headers;
    }

    public Map<String, String> getMyColumnValues() {
        return myColumnValues;
    }

    public PivotRow setMyColumnValues(Map<String, String> myColumnValues) {
        this.myColumnValues = myColumnValues;
        return this;
    }

    public PivotRow() {
        if (getHeaders().size() == 0) {
            getHeaders().add("Time");
            getHeaders().add("Hour");
            getHeaders().add("Minute");
        }
    }

    public PivotRow(LogRow lr, List<String> exclusions, List<String> columnWithValue) {
        if (getHeaders().size() == 0) {
            getHeaders().add("Time");
            getHeaders().add("Hour");
            getHeaders().add("Minute");
        }

        myColumnValues.put("Time", Double.toString(lr.getExcelDateTime()));
        myColumnValues.put("Hour", Integer.toString(lr.getDateTime().get(Calendar.HOUR_OF_DAY)));
        myColumnValues.put("Minute", Integer.toString(lr.getDateTime().get(Calendar.MINUTE)));

        List<String> columns = Arrays.asList(lr.getDescription().split(" "));
        for (String s : columns) {

            String p;
            boolean wasSplit = false;
            if (s.contains(":")) {
                p = s.split(":")[0];
                wasSplit = true;
            } else {
                p = s;
            }

            if (!columnWithValue.contains(p) && wasSplit) {
                p = s;
            }

            if (!getHeaders().contains(p)) {
                getHeaders().add(p);
            }

            if (!exclusions.contains(p)) {
                if (!columnWithValue.contains(p)) {
                    myColumnValues.put(p, "1");
                } else {
                    String[] keyVal = s.split(":");
                    myColumnValues.put(keyVal[0], keyVal[1]);
                }
            }
        }
    }


    @Override
    public List<String[]> toCSV(List<?> l) {

        List<PivotRow> pr = (List<PivotRow>) l;
        List<String[]> ret = new ArrayList<>();

        int numberOfColumns = PivotRow.getHeaders().size();
        String[] headerRow = new String[numberOfColumns];
        // add the first 3 columns
        for (int i = 0; i <= 2; i++) {
            headerRow[i] = PivotRow.getHeaders().get(0);
            PivotRow.getHeaders().remove(0);
        }

        List<String> sortedPivotRowHeaders = PivotRow.getHeaders().stream().sorted().collect(Collectors.toList());
        numberOfColumns = sortedPivotRowHeaders.size();

        for (int i = 0; i < numberOfColumns; i++) {
            headerRow[i+3] = sortedPivotRowHeaders.get(0);
            sortedPivotRowHeaders.remove(0);
        }
        ret.add(headerRow);

        numberOfColumns = headerRow.length;
        for (PivotRow p : pr) {

            String[] newRow = new String[numberOfColumns];
            for (int i = 0; i < numberOfColumns; i++) {
                if (p.getMyColumnValues().containsKey(headerRow[i])) {
                    newRow[i] = p.getMyColumnValues().get(headerRow[i]);
                } else {
                    newRow[i] = "";
                }
            }
            ret.add(newRow);
        }

        return ret;

    }
}
