package com.cimcorp.plc.logParser;

import com.custom.ArgNotFoundException;
import com.custom.FileNotFoundException;
import csvUtils.CSVUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static final String PATH = Paths.get(".").toAbsolutePath().normalize().toString() + "\\";
    public static final String CONFIG_FILE = PATH + "config.ini";

    public static void main(String[] args) throws FileNotFoundException, ArgNotFoundException {

        if (Array.getLength(args) == 0){
            throw new ArgNotFoundException("LogFileToParse");
        }

        final String logFileStr = PATH + args[0];

        File configFile = new File(CONFIG_FILE);
        File logFile = new File(logFileStr);

        if (!configFile.exists()){
            throw new FileNotFoundException(configFile);
        }

        if (!logFile.exists()){
            throw new FileNotFoundException(logFile);
        }

        final String outputFile = PATH + "output.csv";

        List<String[]> csvLines = null;
        try {
            csvLines = CSVUtil.read(logFileStr,",");
        } catch (IOException e) {
            e.printStackTrace();
        }

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

        logEntries = logEntries.stream()
                .filter(p->p.getHeader().contains("MET"))
                .filter(p->p.getDateTime().get(Calendar.DAY_OF_MONTH)==9)
                .collect(Collectors.toList());

        List<String> descriptions = new ArrayList<>();
        logEntries.stream()
                .peek(p -> {
                    if (!descriptions.contains(p.getDescription())) {
                        descriptions.add(p.getDescription());
                    }
                })
                .forEach(p -> {
                    p.setColumnLabel(descriptions);
                });


        System.out.printlm();

        try {
            CSVUtil.writeObject(logEntries,outputFile,",");
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


        System.out.println();

    }

}

