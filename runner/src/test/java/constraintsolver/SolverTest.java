package constraintsolver;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(Parameterized.class)
public class SolverTest {
    private static BinaryCSPReader reader;
    private static String workdir;
    private static Solver fc;
    private static String[] inputs;
    private PrintStream ps;
    private static String print = "N";
    private static String fullPath;

    @Parameterized.Parameters
    public static Iterable<? extends Object[]> config() throws IOException{
        String wd = System.getProperty("user.dir");
        System.out.println("working directory is "+ wd);
        workdir = wd;
        String dir = "";
        if(System.getProperty("dataDir").isEmpty()) dir = "resources";
        else dir = System.getProperty("dataDir");
        Path path = Paths.get(wd, "src/test/"+dir);
        String absPath = path.toAbsolutePath().toString();
        System.out.println("path abs is " + path.toAbsolutePath().toString());
        File resources = new File(path.toAbsolutePath().toString());
        inputs = resources.list();
        List<String> files = new ArrayList<>();
        int num = 0;
        for(String fs : inputs){
            System.out.println("file name is " + fs);
            files.add(absPath+"/"+fs);
            num++;
        }
        Object[] obj = files.toArray();
        String type;
        if(System.getProperty("printType").isEmpty()) type = print;
        else type = System.getProperty("printType");
        List<String> list = Collections.nCopies(num,type);
        return prepareParam(files, list);
    }

    public static List<Object[]> prepareParam(List<String> files, List<String> type) throws IOException {
        List<Object[]> newList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd_HH-mm");
        Date date = new Date();
        String time = formatter.format(date);

        if(type.get(0).equals("Y")) {
            fullPath = workdir + "/src/test/output/" + time + "_" + "Solver_out.csv";
            File file = new File(fullPath);
            FileWriter fr = new FileWriter(file);
            fr.write("Filename,Type,Time1,Time2,NodeCount1,NodeCount2\n");
            fr.close();
        }
        else fullPath = workdir + "/src/test/output/" + time + "_" + "Solver_out.txt";
        for(int i = 0 ; i < files.size(); i++){
            newList.add(new Object[]{files.get(i), type.get(i), fullPath});
        }
        return newList;
    }

    //@Parameterized.Parameter
    private String fn;
    private static boolean type;
    private String output;

    public SolverTest(String fn, String printType, String output) throws IOException{
        this.fn = fn;
        type = (printType.equals("Y"));
        this.output = output;



    }

    @BeforeClass
    public static void makeReader() throws IOException {
        System.out.println("FC Solver Unit Tests===================");
        reader = new BinaryCSPReader();


    }

    @Before
    public void readProblem() {

        BinaryCSP prob = reader.readBinaryCSP(fn);
        String[] splits = fn.split("/");
        String filename = splits[splits.length-1];

        System.out.println("Test Input: "+ filename+"---------------");

        fc = new Solver(prob, Heuristics.SDF, Heuristics.ASCEND);
        //fc.setNew(prob);

    }

    @Test
    public void solveFC(){
        fc.solve(true);
        System.out.println("----------------------------------------");
    }

    @Test
    public void solveMAC(){
        fc.solve(false);
        System.out.println("----------------------------------------");
    }

    @After
    public void printReset() throws IOException{
        System.out.println("fullpath is "+ output);
        fc.printSol(type, output, "sdf#asc");
        fc.reset();
    }


}
