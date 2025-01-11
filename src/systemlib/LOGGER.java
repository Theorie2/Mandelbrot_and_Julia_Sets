package systemlib;

import java.io.*;
import java.time.LocalTime;
import java.util.Scanner;

public class LOGGER {

    private static String LOG = "";
    private static File logFile;
    public static void load(String logFilePath){
        try {
            logFile = new File(logFilePath);
            logFile.getParentFile().mkdirs();
            if (!logFile.exists()) {
                logFile.createNewFile();
                LOGGER.log("File created: " + logFile.getName());
            } else {
                Scanner myReader = new Scanner(logFile);
                StringBuilder oldLog = new StringBuilder();
                while (myReader.hasNextLine()) {
                    oldLog.append(myReader.nextLine()).append("\n");
                }
                myReader.close();
                if (oldLog.toString().length()>13){
                LOGGER.log(oldLog.toString(),false);
                }
                LOGGER.log("Loaded old Log-File");
            }
        } catch (IOException e) {
            LOGGER.log("An error occurred while logging. Please report this bug!");
            e.printStackTrace();
        }
    }
    public static void log(String txt){
        log(txt,true);
    }

    public static void log(String txt, boolean withTime){
        if (withTime){
            LOG += "["+LocalTime.now()+"] " +txt+" \n";
            System.out.println(txt);
        } else{
            LOG += txt+" \n";
        }
        try {

            FileWriter myWriter = new FileWriter(logFile);
            for (String e : LOG.split("\n")) {
                myWriter.write(e+"\n");
            }
            myWriter.close();
        } catch (IOException e) {
            LOGGER.log("An error occurred during logging.");
            e.printStackTrace();
        }
    }
    public static void resetLog(){
        LOG = "";
        try {

            FileWriter myWriter = new FileWriter(logFile);
                myWriter.write("");
            myWriter.close();
        } catch (IOException e) {
            LOGGER.log("An error occurred during resetting the logger.");
            e.printStackTrace();
        }
    }

    public static String getLOG() {
        return LOG;
    }
    /*public static void printLog(){
        //System.out.println(new FileReader("/logs/log").read());
    }*/
}
