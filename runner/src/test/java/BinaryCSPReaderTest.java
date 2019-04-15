import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;


@RunWith(Parameterized.class)
public class BinaryCSPReaderTest {
    private Solver solver;

    @Parameterized.Parameters()
    public static Iterable<Object[]> config() throws IOException {
        return null;
    }

    @BeforeClass
    public void makeReader(){
        BinaryCSPReader reader = new BinaryCSPReader();
    }

    @Before
    public void readProblem(){

    }


}