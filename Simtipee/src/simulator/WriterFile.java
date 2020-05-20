package simulator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WriterFile {


    public static void write(List<Statistic> data,String outPath){
        List<String> dataCsv = new ArrayList<>();

        for(Statistic d: data) dataCsv.add(d.getHostsThroughput().toString());
        String text = String.join(";",dataCsv);

        write(text,outPath);
    }

    public static void write(String text){
        write(text,"out.csv");
    }


    public static void write(String text,String path){
        Writer writer = null;

        try {
            FileWriter fw = new FileWriter(path + ".csv", true); //the true will append the new data
            fw.write(text);//appends the string to the file
            fw.write("\n");
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {writer.close();} catch (Exception ex) {/*ignore*/}
        }
    }
}
