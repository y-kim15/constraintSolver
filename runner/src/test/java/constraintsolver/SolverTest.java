package constraintsolver;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assume.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
    private static String print = "N";
    private static String fullPath;

    @Parameterized.Parameters
    public static Iterable<? extends Object[]> config() throws IOException{
        String wd = System.getProperty("user.dir");
        workdir = wd;
        String dir;
        if(System.getProperty("dir").isEmpty()) dir = "resources";
        else dir = System.getProperty("dir");
        Path path = Paths.get(wd, "src/test/"+dir);
        String absPath = path.toAbsolutePath().toString();
        File resources = new File(path.toAbsolutePath().toString());
        inputs = resources.list();
        List<String> files = new ArrayList<>();
        int num = 0;
        for(String fs : inputs){
            files.add(absPath+"/"+fs);
            num++;
        }
        String type; String type1;
        if(System.getProperty("print").isEmpty()) type = print;
        else type = System.getProperty("print");
        if(System.getProperty("m").isEmpty()){ type1 = "b"; }
        else type1 = System.getProperty("m");
        List<String> list = Collections.nCopies(num, type);
        List<String> mType = Collections.nCopies(num, type1);
        return prepareParam(files, list, mType);
    }

    public static List<Object[]> prepareParam(List<String> files, List<String> type, List<String> type1) throws IOException {
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
            newList.add(new Object[]{files.get(i), type.get(i), type1.get(i), fullPath});
        }
        return newList;
    }

    //@Parameterized.Parameter
    private String fn;
    private static boolean type;
    private String output;
    private boolean exists;
    private boolean testFC;
    private boolean testMAC;

    public SolverTest(String fn, String printType, String method, String output) {
        this.fn = fn;
        type = (printType.equals("Y"));
        this.output = output;
        switch (method) {
            case "fc":
                testFC = true;
                testMAC = false;
                break;
            case "mac":
                testFC = false;
                testMAC = true;
                break;
            default:
                testFC = true;
                testMAC = true;

        }
    }

    @BeforeClass
    public static void makeReader(){
        System.out.println("Solver Unit Tests===================");
        reader = new BinaryCSPReader();


    }

    @Before
    public void readProblem() {

        BinaryCSP prob = reader.readBinaryCSP(fn);
        String[] splits = fn.split("/");
        String filename = splits[splits.length-1];
        fc = new Solver(prob, Heuristics.SDF, Heuristics.ASCEND);
        System.out.println("file name is " + filename);
    }

    @Test
    public void solveFC()throws IOException{
        assumeTrue(testFC);
        System.out.println("FC --------------------------------");
        exists = fc.solve(true);
        fc.printSol(exists, type, output, "sdf#asc");
    }

    @Test
    public void solveMAC()throws IOException{
        assumeTrue(testMAC);
        System.out.println("MAC ---------------------------------");
        exists = fc.solve(false);
        fc.printSol(exists, type, output, "sdf#asc");

    }

    @After
    public void reset() {
        fc.reset();
    }


}
