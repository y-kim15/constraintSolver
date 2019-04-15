import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.junit.Assume.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RunWith(Parameterized.class)
public class BinaryCSPReaderTest {
    private static BinaryCSPReader reader;
    private Solver solver;
    private static int lb = 5;
    private static int ub = 10;

    @Parameterized.Parameters()
    public static Iterable<? extends Object> config() throws IOException {
        int low, up;
        if(System.getProperty("lower").isEmpty()) low = lb;
        else low = (int) Integer.valueOf(System.getProperty("lower"));
        if(System.getProperty("upper").isEmpty()) up = ub;
        else up = (int) Integer.valueOf(System.getProperty("upper"));
        List<String> ints = new ArrayList<>();
        for(int i = low; i <= up; i++) ints.add(Integer.toString(i));
        return ints;
    }

    @Parameterized.Parameter
    public String len;

    public BinaryCSPReaderTest(String len){
        this.len = len;
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
        BinaryCSP prob = reader.readBinaryCSP(fn);
        solver = new FCSolver(prob, Heuristics.SDF, Heuristics.ASCEND);
    }

    @Test
    public void solve(){
        System.out.println("----------------------------------------");
    }
}