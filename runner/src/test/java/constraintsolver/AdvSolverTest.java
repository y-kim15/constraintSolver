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

import static org.junit.Assume.assumeTrue;

@RunWith(Parameterized.class)
public class AdvSolverTest {
    private static BinaryCSPReader reader;
    private static String workdir;
    private static Solver fc;
    private static String[] inputs;
    private static String print = "N";
    private static String fullPath;
    private static String ordering = "def";

    @Parameterized.Parameters
    public static Iterable<? extends Object[]> config() throws IOException{
        String wd = System.getProperty("user.dir");
        workdir = wd;
        String dir = "";
        if(System.getProperty("dir").isEmpty()) dir = "resources";
        else dir = System.getProperty("dir");
        Path path = Paths.get(wd, "src/test/"+dir);
        String absPath = path.toAbsolutePath().toString();
        File resources = new File(path.toAbsolutePath().toString());
        inputs = resources.list();
        List<String> files = new ArrayList<>();
        int num = 0;
        for(String fs : inputs){
            System.out.println("file name is " + fs);
            files.add(absPath+"/"+fs);
            num++;
        }
        String type, order, type1;
        if(System.getProperty("print").isEmpty()) type = print;
        else type = System.getProperty("print");
        if(System.getProperty("order").isEmpty()) order = ordering;
        else order = System.getProperty("order");
        List<String> list = Collections.nCopies(num,type);
        if(System.getProperty("m").isEmpty()){ type1 = "b"; }
        else type1 = System.getProperty("m");
        List<String> mType = Collections.nCopies(num, type1);
        return prepareParam(files, list, order, mType);
    }

    public static List<Object[]> prepareParam(List<String> files, List<String> type, String ordering, List<String> mType) throws IOException {
        List<Object[]> newList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd_HH-mm");
        Date date = new Date();
        String time = formatter.format(date);
        if(type.get(0).equals("Y")) {
            fullPath = workdir + "/src/test/output/" + time + "_" + ordering + "_Solver_out.csv";
            File file = new File(fullPath);
            FileWriter fr = new FileWriter(file);
            fr.write("Filename,Type,Time1,Time2,NodeCount1,NodeCount2\n");
            fr.close();
        }
        else fullPath = workdir + "/src/test/output/" + time + "_" + "HeuSolver_out.txt";
        Heuristics[] heuristics_var;
        Heuristics[] heuristics_val;
        int var;
        int val;
        if(ordering.equals("all")){
            heuristics_var = new Heuristics[]{Heuristics.MAXDEG, Heuristics.MAXCAR, Heuristics.SDF, Heuristics.BRELAZ, Heuristics.DOMDEG};
            heuristics_val = new Heuristics[]{Heuristics.ASCEND, Heuristics.MINCONF};
            var = 5; val = 2;
        }
        else if(ordering.equals("dynamic")){
            heuristics_var = new Heuristics[]{Heuristics.SDF, Heuristics.BRELAZ, Heuristics.DOMDEG};
            heuristics_val = new Heuristics[]{Heuristics.ASCEND, Heuristics.MINCONF};
            var = 3; val = 2;
        }
        else{
            heuristics_var = new Heuristics[]{Heuristics.SDF};
            heuristics_val = new Heuristics[]{Heuristics.ASCEND};
            var = 1; val = 1;
        }
        for(int v1 = 0; v1 < var; v1++) {
            for(int v2 = 0; v2 < val; v2++) {
                for (int i = 0; i < files.size(); i++) {
                    newList.add(new Object[]{files.get(i), type.get(i), mType.get(i), fullPath, heuristics_var[v1],heuristics_val[v2]});
                }
            }
        }
        return newList;
    }

    private String fn;
    private static boolean type;
    private String output;
    private Heuristics var;
    private Heuristics val;
    private boolean exists;
    private boolean testFC;
    private boolean testMAC;

    public AdvSolverTest(String fn, String printType, String method, String output,  Heuristics var, Heuristics val) {
        this.fn = fn;
        type = (printType.equals("Y"));
        this.output = output;
        this.var = var;
        this.val = val;
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
    public static void makeReader() {
        System.out.println("Adv Solver Unit Tests===================");
        reader = new BinaryCSPReader();


    }

    @Before
    public void readProblem() {
        BinaryCSP prob = reader.readBinaryCSP(fn);
        String[] splits = fn.split("/");
        String filename = splits[splits.length-1];

        System.out.println("Test Input: "+ filename+"---------------");

        fc = new Solver(prob, var, val);

    }

    @Test
    public void solveFC() throws IOException{
        assumeTrue(testFC);
        System.out.println("FC --------------------------------- ");
        exists = fc.solve(true);
        fc.printSol(exists, type, output, getHeuristics(var, val));
        if(!type) System.out.println("----------------------------------------");
    }

    @Test
    public void solveMAC() throws IOException{
        assumeTrue(testMAC);
        System.out.println("MAC --------------------------------- ");
        exists = fc.solve(false);
        fc.printSol(exists, type, output, getHeuristics(var, val));
        if(!type) System.out.println("----------------------------------------");
    }

    @After
    public void reset() {
        fc.reset();
    }

    public static String getHeuristics(Heuristics var, Heuristics val){
        String str = "";
        switch (var){
            case SDF : str += "sdf#";
            break;
            case BRELAZ: str += "brelaz#";
            break;
            case DOMDEG: str += "domdeg#";
            break;
        }
        if(val == Heuristics.ASCEND) str+="asc";
        else str+="minconf";
        return str;
    }


}
