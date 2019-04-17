package constraintsolver.generators;
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

  private static int[][] read2(String fn){
    try{
      //System.out.println("started reading second .pls format!");
      inFR = new FileReader(fn) ;
      in = new StreamTokenizer(inFR) ;
      in.ordinaryChar('o') ;
      in.ordinaryChar('r') ;
      in.ordinaryChar('d') ;
      in.ordinaryChar('e') ;
      in.ordinaryChar('r') ;
      in.wordChars('-', '-');
      for(int i = 0; i < 6; i++) in.nextToken() ;    // space
      int n = (int)in.nval ;
      //in.nextToken(); // space in next line
      int[][] values = new int[n][n] ;
      //in.nextToken();                             // '|'
      for (int i = 0; i < n; i++) {
        //System.out.println((i+1)+"th row");
        for(int j = 0; j < n; j++) {
          int val = 0;

          //in.nextToken();
          //in.nextToken();  //  3 x spaces
          int token = in.nextToken(); // value (check if it is -s)
          if(token != (StreamTokenizer.TT_NUMBER)){
            //System.out.println("its -1");
            values[i][j] = 0;
            in.nextToken();
          }
          else{
            //in.nextToken();
            //System.out.println("else add one!");
           val = ((int) in.nval);
            values[i][j] = ++val;
          }
          //System.out.println((j+1)+"th col val: " + values[i][j]);
                                            // ','
          //in.nextToken();
        }
        //in.nextToken();                               // '|'
      }
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
    System.out.println("Usage: java constraintsolver.generators.QuasiGroupGenerator") ;
    int n = Integer.parseInt(args[1]);
    System.out.println("n is " + n);
    PrintStream ps = new PrintStream("runner/src/test/extra/"+n+"_"+"QuasiGroup.csp");
    System.setOut(ps);
    System.out.println("//QuasiGroupCompletion.") ;
    System.out.println("\n// "+(n*n)+" variables:\n"+(n*n)) ;
    int[][] values;
    if(args[0].endsWith(".pls")) values = read2(args[0]);
    else values = read(args[0]);
    System.out.println("\n// Domains of the variables: 1.."+n+" (inclusive)") ;

    int holes = 0;

    try {
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          if (values[i][j] == 0){
            holes++;
            System.out.println("1, " + n);
          }
          else System.out.println(values[i][j] + ", " + values[i][j]);
        }
      }
    }
    catch(NullPointerException e){
      System.out.println("Invalid file reading");
      e.getMessage();
    }
    System.out.println("\n// constraints (vars indexed from 0, allowed tuples):") ;

    System.out.println("\n//Number of Holes: "+holes+"\n");

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
