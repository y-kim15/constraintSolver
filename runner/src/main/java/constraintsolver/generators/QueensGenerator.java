package constraintsolver.generators;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
public final class QueensGenerator {
  public String main (String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: java constraintsolver.generators.QueensGenerator <n>") ;
      return "";
    }
    int n = Integer.parseInt(args[0]) ;
    String wd = System.getProperty("user.dir");
    String path = "src/test/extra/";
    String fn = path +n+"Queens.csp";
    PrintWriter writer = new PrintWriter(wd + "/" + fn);
    writer.println("//"+n+"-Queens.") ;
    writer.println("\n// Number of variables:\n"+n) ;
    writer.println("\n// Domains of the variables: 0.. (inclusive)") ;
    for (int i = 0; i < n; i++)
      writer.println("0, "+(n-1)) ;
    writer.println("\n// constraints (vars indexed from 0, allowed tuples):") ;
    
    for (int row1 = 0; row1 < n-1; row1++)
      for (int row2 = row1+1; row2 < n; row2++) {
        writer.println("c("+row1+", "+row2+")") ;
        for (int col1 = 0; col1 < n; col1++)
          for (int col2 = 0; col2 < n; col2++) {
            if ((col1 != col2) &&
                (Math.abs(col1 - col2) != (row2-row1))) {
              writer.println(col1+", "+col2) ;
            }
          }
        writer.println() ;
      }
    writer.close();
    return fn;
  }
}
