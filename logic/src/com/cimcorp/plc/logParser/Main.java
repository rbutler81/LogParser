package com.cimcorp.plc.logParser;

import com.custom.ArgNotFoundException;
import com.custom.FileNotFoundException;
import csvUtils.CSVUtil;
import configFileReader.ConfigFileReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static final String PATH = Paths.get(".").toAbsolutePath().normalize().toString() + "\\";
    public static final String CONFIG_FILE = PATH + "config.ini";
    public static final int REF_START_DAY = 13;
    public static final int REF_END_DAY = 14;
    public static final int REF_MONTH = 8;

    public static void main(String[] args) throws FileNotFoundException, ArgNotFoundException {

        // Check for files needed
        if (Array.getLength(args) == 0){
            throw new ArgNotFoundException("LogFileToParse");
        }

        final String logFileStr = PATH + args[0];

        File configFile = new File(CONFIG_FILE);
        File logFile = new File(logFileStr);

        if (!configFile.exists()){
            throw new FileNotFoundException(configFile);
        }

        // read the config file, gather parameters
        Map<String,List<String>> configParams = ConfigFileReader.fromFile(CONFIG_FILE);

        if (!logFile.exists()){
            throw new FileNotFoundException(logFile);
        }

        final String outputFile = PATH + "output.csv";

        // Read the log csv file from the PLC
        List<String[]> csvLines = null;
        try {
            csvLines = CSVUtil.read(logFileStr,",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Calendar reference time - set to when this log start
        Calendar refTimeStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        refTimeStart.clear();
        refTimeStart.setLenient(false);
        refTimeStart.set(2019,REF_MONTH, REF_START_DAY);

        Calendar refTimeEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        refTimeEnd.clear();
        refTimeEnd.setLenient(false);
        refTimeEnd.set(2019,REF_MONTH, REF_END_DAY);

        // Make a list of the log entries found in the csv file. Separate from 'bad lines'.
        final String[] header = csvLines.get(0);
        csvLines.remove(0);

        List<LogRow> logEntries = new ArrayList<>();
        List<String[]> badLines = new ArrayList<>();
        for (String[] s : csvLines) {
            if (Array.getLength(s) >= 6) {
                logEntries.add(new LogRow(s));
            }
            else{
                badLines.add(s);
            }
        }
        csvLines = null;

        //Filter any event that counts crates at certain point in the system - make a separate list - write it to a csv file
        List<LogRow> crateCountEntries = logEntries.stream()
                .filter(p->p.getHeader().contains("MET"))
                .filter(p->p.getDateTime().get(Calendar.DAY_OF_MONTH) == REF_START_DAY)
                .collect(Collectors.toList());

        List<String> descriptions = new ArrayList<>();
        crateCountEntries.stream()
                .peek(p -> {
                    if (!descriptions.contains(p.getDescription())) {
                        descriptions.add(p.getDescription());
                    }
                })
                .forEach(p -> {
                    p.setColumnLabel(descriptions);
                });

        try {
            CSVUtil.writeObject(crateCountEntries,outputFile,",");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        //Find all the ON/OFF items - make a list and time how long each one was on and off
        OnOffEntries ofe = new OnOffEntries(refTimeStart, refTimeEnd);

        List<LogRow> rtsEntries = logEntries.stream()
                .filter(p->p.getDateTime().get(Calendar.DAY_OF_MONTH) == REF_START_DAY)
                .collect(Collectors.toList());

        OnOffEntries.checkAndAddEvent(ofe,rtsEntries.stream().filter(p->!p.getDescription().contains("ReadyToSend")).collect(Collectors.toList()),refTimeStart, refTimeEnd);

        List<LogRow> rts = rtsEntries.stream()
                .filter(p->!p.getDescription().contains("ReadyToSend"))
                .peek(x-> {
                    x.setDescription("ReadyToSend");
                })
                .collect(Collectors.toList());

        OnOffEntries.checkAndAddEvent(ofe,rts,refTimeStart,refTimeEnd);

        BufferedWriter writer = null;

        for (OnOffEntry o : ofe.getEntries()) {

            try {
                writer = new BufferedWriter(new FileWriter(PATH + "OnOffTimes.txt", true));
                writer.write(o.toString() + "\n\n");

            } catch (IOException e) {
            } finally {
                try {
                    if (writer != null)
                        writer.close();
                } catch (IOException e) {
                }
            }
        }

    }

}

