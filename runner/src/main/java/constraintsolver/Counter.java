package constraintsolver;
import com.opencsv.CSVWriter;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
public class Counter {
    private boolean type;
    private String name;
    private long start;
    private long end;
    private int node;
    private int extra;


    // true for FC, false for MAC
    Counter(boolean type) {
        this.type = type;
        start = 0;
        end = 0;
        if(type) name = "Total FC Calls";
        else name = "Number of Arc Revisions";
        node = 0;
        extra = 0;
    }

    public void setStart(){
        start = System.nanoTime();
    }

    public void setEnd(){
        end = System.nanoTime();
    }

    // true for node, false for extra
    public void increment(boolean type){
        if(type) node++;
        else extra++;
    }
    //printtype true == print in csv
    public void printStats(String prob, boolean printType, String fn, String heuristics) throws IOException {
        double milliseconds  = (double)(end - start) / 1000000.0;
        double seconds = milliseconds / 1000;
        String var, val;
        if(heuristics.equals("")){
            var = "sdf";
            val = "asc";
        }
        else {
            var = heuristics.split("#")[0];
            val = heuristics.split("#")[1];
        }
        File file = new File(fn);
        FileWriter fr = new FileWriter(file, true);
        if(!printType) {
            fr.write("+++++++++++++++++" + prob + "+++++++++++++++++++++\n");
            if (type) {
                fr.write("=================== FC Output ===============\n");
            } else {
                fr.write("=================== MAC Output ===============\n");

            }
            fr.write("Depth: " + node + "\n");
            fr.write(name + ": " + extra + "\n");

            fr.write("Elapsed Time 1: " + String.format("%.2f", milliseconds) + " milliseconds\n");
            fr.write("Elapsed Time 2: " + String.format("%.2f", seconds) + " seconds\n");
            fr.write("Variable Ordering: " + var + "\n");
            fr.write("Value Ordering: " + val + "\n");
            fr.close();
        }
        else{
            String solver = "FC";
            if(!type) solver = "MAC";
            BufferedWriter br = new BufferedWriter(fr);
             br.write(prob + "," + solver + "," + String.format("%.2f", milliseconds) + "," + String.format("%.2f", seconds)
                    + "," + node + "," + extra + ","+ var +"," + val + "\n");

            br.close();
            fr.close();

        }

    }

}
