import java.io.*;
import java.util.ArrayList;
public final class QuasiGroupGenerator {
  private static FileReader inFR;
  private static StreamTokenizer in;
  /**
   * The constraint is always the same != on 1..9 Only the
   */
  private static void diseqTuples(int n) {
    for (int val1 = 1; val1 <= n; val1++)
      for (int val2 = 1; val2 <= n; val2++)
        if (val1 != val2)
          System.out.println(val1+", "+val2) ;
  }

  private static int[][] read(String fn){
    try {
      //System.out.println("started reading");
      inFR = new FileReader(fn) ;
      in = new StreamTokenizer(inFR) ;
      in.ordinaryChar('|') ;
      in.ordinaryChar('N') ;
      in.ordinaryChar('=') ;
      in.ordinaryChar(';') ;
      in.ordinaryChar('[') ;
      in.ordinaryChar(']') ;
      in.ordinaryChar('s') ;
      in.ordinaryChar('t') ;
      in.ordinaryChar('a') ;
      in.ordinaryChar('r') ;
      in.ordinaryChar('t') ;
      for(int i = 0; i < 2; i++) in.nextToken();
      in.nextToken() ;                                         // n
      int n = (int)in.nval ;
      in.nextToken();
      for(int i = 0; i < 7; i++) in.nextToken();
      //System.out.println("n is " + n);
      int[][] values = new int[n][n] ;
      in.nextToken();                             // '|'
      for (int i = 0; i < n; i++) {
        //System.out.println((i+1)+"th row");
        for(int j = 0; j < n; j++) {
          in.nextToken();                                   //  value
          values[i][j] = (int) in.nval;
          //System.out.println((j+1)+"th col val: " + values[i][j]);
          in.nextToken();                                   // ','
          //in.nextToken();
        }
        //in.nextToken();                               // '|'
      }
      in.nextToken();
      in.nextToken();
      in.nextToken();
//      for(int i=0; i < n; i++){
//        for(int j=0; j < n; j++){
//          System.out.println(values[i][j]);
//        }
//      }
      // TESTING:
      // System.out.println(csp) ;
      inFR.close() ;
      return values;
    }
    catch (FileNotFoundException e) {System.out.println(e);}
    catch (IOException e) {System.out.println(e);}
    return null;
  }
  
  public static void main (String[] args) throws NullPointerException, FileNotFoundException {
    System.out.println("Usage: java QuasiGroupGenerator") ;
    int n = Integer.parseInt(args[1]);
//    PrintStream ps = new PrintStream("runner/src/main/resources/"+n+"_"+"QuasiGroup.csp");
//    System.setOut(ps);
    System.out.println("//QuasiGroupCompletion.") ;
    System.out.println("\n// "+(n*n)+" variables:\n"+(n*n)) ;
    System.out.println("\n// Domains of the variables: 1.."+n+" (inclusive)") ;
    int[][] values = read(args[0]);
    try {
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          if (values[i][j] == 0) System.out.println("1, " + n);
          else System.out.println(values[i][j] + ", " + values[i][j]);
        }
      }
    }
    catch(NullPointerException e){
      System.out.println("Invalid file reading");
      e.getMessage();
    }
    System.out.println("\n// constraints (vars indexed from 0, allowed tuples):") ;



    // Rows
    for (int row = 1; row <= n; row++) {
      System.out.println("//Row: "+row) ;
      for (int col1 = 1; col1 <= n-1; col1++)
        for (int col2 = col1+1; col2 <= n; col2++) {
          System.out.println("c("+((row-1)*n+col1-1)+", "+((row-1)*n+col2-1)+")") ;
          diseqTuples(n) ;
          System.out.println() ;
        }
    }

    // Cols
    for (int col = 1; col <= n; col++) {
      System.out.println("//Col: "+col) ;
      for (int row1 = 1; row1 <= n-1; row1++)
        for (int row2 = row1+1; row2 <= n; row2++) {
          System.out.println("c("+((row1-1)*n+col-1)+", "+((row2-1)*n+col-1)+")") ;
          diseqTuples(n) ;
          System.out.println() ;
        }
    }
   }
}
