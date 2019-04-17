package constraintsolver;
import constraintsolver.generators.QueensGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@RunWith(Parameterized.class)
public class QueensTest {
    private static BinaryCSPReader reader;
    private Solver solver;
    private static int lb = 5;
    private static int ub = 50;
    private static String workdir;
    private static String fullPath;
    private static String print = "N";

    @Parameterized.Parameters()
    public static Iterable<? extends Object> config() throws IOException {
        String wd = System.getProperty("user.dir");
        workdir = wd;
        int low, up;
        if(System.getProperty("lower").isEmpty()) low = lb;
        else {
            low = Integer.valueOf(System.getProperty("lower"));
            lb = low;
        }
        if(System.getProperty("upper").isEmpty()) up = ub;
        else{
            up = Integer.valueOf(System.getProperty("upper"));
            ub = up;
        }
        List<String> ints = new ArrayList<>();
        for(int i = low; i <= up; i++) ints.add(Integer.toString(i));
        String type;
        if(System.getProperty("printType").isEmpty()) type = print;
        else type = System.getProperty("printType");
        List<String> list = Collections.nCopies(up-low+1,type);
        return prepareParam(ints, list);
    }

    public static List<Object[]> prepareParam(List<String> ints, List<String> type) throws IOException {
        List<Object[]> newList = new ArrayList<>();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd_HH-mm");
        Date date = new Date();
        String time = formatter.format(date);
        if(type.get(0).equals("Y")) {
            fullPath = workdir + "/src/test/output/" + time + "_" + lb + "-" + ub + "MultiQueens_Solver_out.csv";
            File file = new File(fullPath);
            FileWriter fr = new FileWriter(file);
            fr.write("Filename,Type,Time1,Time2,NodeCount1,NodeCount2\n");
            fr.close();
        }
        else fullPath = workdir + "/src/test/output/" + time + "_" + lb + "-" + ub + "MultiQueens_Solver_out.txt";
        for(int i = 0 ; i < ints.size(); i++){
            newList.add(new Object[]{ints.get(i), type.get(i), fullPath});
        }
        return newList;
    }

    public String len;
    private static boolean type;
    private String output;
    private String input;
    private boolean exists;

    public QueensTest(String len, String printType, String output){
       this.len = len;
        type = (printType.equals("Y"));
        this.output = output;
    }

    @BeforeClass
    public static void makeReader(){
       reader = new BinaryCSPReader();
    }

    @Before
    public void readProblem() throws IOException{
        QueensGenerator generator = new QueensGenerator();
        String[] in = {len};
        String fn = generator.main(in);
        BinaryCSP prob = reader.readBinaryCSP(workdir + "/" + fn);

        input = fn;
        System.out.println("Test Input: "+ prob.getName()+"---------------");
        solver = new Solver(prob, Heuristics.SDF, Heuristics.ASCEND);
    }

    @Test
    public void solveFC(){
       exists = solver.solve(true);
    }

    @Test
    public void solveMAC(){
        exists = solver.solve(false);
    }

    @After
    public void printReset() throws IOException{
        solver.printSol(exists, type, output,"sdf#asc");
        solver.reset();
        File f = new File(input);
        f.delete();
    }
}