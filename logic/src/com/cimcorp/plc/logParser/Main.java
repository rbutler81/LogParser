package com.cimcorp.plc.logParser;

import com.custom.FileNotFoundException;
import csvUtils.CSVUtil;
import com.cimcorp.configFile.Config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class Main {



    // static methods /////////////////////////////////////////////////////////////////////////
    static int decideEndMonth(int startDay, int endDay, int startMonth) {
        if (endDay < startDay) {
            return startMonth + 1;
        } else {
            return startMonth;
        }
    }

    // setup static variables ///////////////////////////////////////////////////////////////////
    static final String PATH = Paths.get(".").toAbsolutePath().normalize().toString() + "\\";


    static BiPredicate<LogRow,List<String>> headerIsInList = (r, ls) -> {
        return ls.contains(r.getHeader());
    };

    static BiPredicate<LogRow,List<String>> substringExistsInString = (r, ls) -> {
        boolean ret = false;
        for (String s : ls) {
            ret = r.getDescription().indexOf(s) >= 0;
            if (ret) {break;};
        }
        return ret;
    };

    public static void main(String[] args) throws FileNotFoundException, ArgNotFoundException, IOException {

        // Check for INI file ///////////////////////////////////////////////////////////////
        if (Array.getLength(args) < 3){
            throw new ArgNotFoundException("<Log File> <Config File> <Output File>");
        }

        final String CONFIG_FILE = PATH + args[1];

        File iniFile = new File(CONFIG_FILE);

        if (!iniFile.exists()){
            throw new FileNotFoundException(iniFile);
        }

        // read the config file, gather parameters
        final Config CONFIG_PARAMS = Config.readIniFile(CONFIG_FILE);
        final int START_MONTH = CONFIG_PARAMS.getSingleParamAsInt("Month");
        final int START_DAY = CONFIG_PARAMS.getSingleParamAsInt("StartDay");
        final int END_DAY = CONFIG_PARAMS.getSingleParamAsInt("EndDay");
        final int YEAR = CONFIG_PARAMS.getSingleParamAsInt("Year");
        final int END_MONTH = decideEndMonth(START_DAY, END_DAY, START_MONTH);
        final List<String> HEADERS_TO_FILTER = CONFIG_PARAMS.getParam("HeadersToFilter");
        final List<String> KEYS_TO_KEEP = CONFIG_PARAMS.getParam("KeysToKeep");

        // Check for log file ///////////////////////////////////////////////////////////////
        if (Array.getLength(args) == 0){
            throw new ArgNotFoundException("LogFileToParse");
        }

        final String LOG_FILE = PATH + args[0];

        File logFile = new File(LOG_FILE);

        if (!logFile.exists()){
            throw new FileNotFoundException(logFile);
        }

        // setup output file
        final String OUTPUT_FILE = PATH + args[2];
        File outputFile = new File(OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile = null;

        // Print to console
        System.out.println();
        System.out.println("Log File to Read: " + LOG_FILE);
        System.out.println("Config File to Use: " + CONFIG_FILE);
        System.out.println("Output File to Write: " + OUTPUT_FILE);
        System.out.println();

        // Read the log csv file from the PLC //////////////////////////////////////////////////
        System.out.println("Reading " + LOG_FILE);
        System.out.println();

        List<String[]> csvLines = null;
        try {
            csvLines = CSVUtil.read(LOG_FILE,",");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Calendar reference time - set to when this log start /////////////////////////////////
        Calendar refTimeStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        refTimeStart.clear();
        refTimeStart.setLenient(false);
        refTimeStart.set(YEAR, START_MONTH, START_DAY);

        Calendar refTimeEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        refTimeEnd.clear();
        refTimeEnd.setLenient(false);
        refTimeEnd.set(YEAR, END_MONTH, END_DAY);

        // Make a list of the log entries found in the csv file. Separate from 'bad lines'. ////
        final String[] header = csvLines.get(0);
        csvLines.remove(0);

        // Scan through the list of strings (csv file) and create a LogRow for each one ////////
        List<LogRow> logEntries = new ArrayList<>();
        List<String[]> badLines = new ArrayList<>();
        for (String[] s : csvLines) {
            if (Array.getLength(s) >= 6) {
                logEntries.add(new LogRow(s));
            } else {
                badLines.add(s);
            }
        }
        csvLines = null;

        //Filter any event that counts crates at certain point in the system - make a separate list
        System.out.println("Filtering Log Entries... ");
        System.out.println();

        List<LogRow> filteredLogRows = logEntries.stream()
                .filter(p-> headerIsInList.test(p,HEADERS_TO_FILTER))
                .filter(p-> substringExistsInString.test(p,KEYS_TO_KEEP))
                .filter(p->p.getDateTime().get(Calendar.DAY_OF_MONTH) == START_DAY)
                .collect(Collectors.toList());

        List<PivotRow> pr = new ArrayList<>();
        int filteredRowSize = filteredLogRows.size();
        int i = 0;
        for (LogRow lr : filteredLogRows) {
            pr.add(new PivotRow(lr, KEYS_TO_KEEP));
            i = i + 1;
            System.out.println("Generating pivot table... " + i + " of " + filteredRowSize);
        }

        System.out.println();

        try {
            CSVUtil.writeObject(pr, OUTPUT_FILE,",");
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
        /*OnOffEntries ofe = new OnOffEntries(refTimeStart, refTimeEnd);

        List<LogRow> rtsEntries = logEntries.stream()
                .filter(p->p.getDateTime().get(Calendar.DAY_OF_MONTH) == START_DAY)
                .collect(Collectors.toList());

        OnOffEntries.checkAndAddEvent(ofe,rtsEntries.stream().filter(p->!p.getDescription().contains("ReadyToSend")).collect(Collectors.toList()),refTimeStart, refTimeEnd);

        List<LogRow> rts = rtsEntries.stream()
                .filter(p->!p.getDescription().contains("ReadyToSend"))
                .peek(x-> {
                    x.setDescription("ReadyToSend");
                })
                .collect(Collectors.toList());

        OnOffEntries.checkAndAddEvent(ofe,rts,refTimeStart,refTimeEnd);*/

        /*BufferedWriter writer = null;

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
        }*/

    }
}



