import exception.QueueEmptyException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RunWith(Parameterized.class)
public class MACSolverTest {
    private static BinaryCSPReader reader;
    private MACSolver mac;
    private static String[] inputs;
    private static String absPath;

    @Parameterized.Parameters
    public static Iterable<? extends Object> inputs(){
        String wd = System.getProperty("user.dir");
        System.out.println("working directory is "+ wd);
        Path path = Paths.get(wd, "src/test/resources");
        absPath = path.toAbsolutePath().toString();
        System.out.println("path abs is " + path.toAbsolutePath().toString());
        File resources = new File(path.toAbsolutePath().toString());
        inputs = resources.list();
        List<String> files = new ArrayList<>();
        for(String fs : inputs){
            System.out.println("file name is " + fs);
            files.add(absPath+"/"+fs);
        }
        return files;
    }

    @Parameterized.Parameter
    public String fn;

    @BeforeClass
    public static void makeReader(){
        System.out.println("MAC Solver Unit Tests===================");
        reader = new BinaryCSPReader();
    }

    @Before
    public void readProblem(){
        BinaryCSP prob = reader.readBinaryCSP(fn);
        String[] splits = fn.split("/");
        String filename = splits[splits.length-1];
        System.out.println("Test Input: "+ filename+"---------------");
        mac = new MACSolver(prob, Heuristics.SDF, Heuristics.ASCEND);
    }

    @Test
    public void solve() throws QueueEmptyException {
        mac.doMAC();
        System.out.println("----------------------------------------");
    }
}
