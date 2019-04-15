import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
public final class BlockQueensGenerator {
  public static void main (String[] args) throws FileNotFoundException {
    int n = Integer.parseInt(args[0]) ;
    int m = Integer.parseInt(args[1]) ;
    PrintStream ps = new PrintStream("runner/src/main/resources/"+n+"_"+m+"Queens.csp");
    System.setOut(ps);
//    if (args.length != 1) {
//      System.out.println("Usage: java QueensGenerator <n>") ;
//      return ;
//    }

    System.out.println("//"+n+"-"+m+"Queens.") ;
    System.out.println("\n// Number of variables:\n"+n) ;
    System.out.println("\n// Domains of the variables: 0.. (inclusive)") ;
    for (int i = 0; i < n; i++)
      System.out.println("0, "+(n-1)) ;
    System.out.println("\n// constraints (vars indexed from 0, allowed tuples):") ;

    List<BinaryTuple> blocks = getBlocks(n, m, Integer.parseInt(args[2]), 0);
    for (int row1 = 0; row1 < n-1; row1++)
      for (int row2 = row1+1; row2 < n; row2++) {
        System.out.println("c("+row1+", "+row2+")") ;
        for (int col1 = 0; col1 < n; col1++)
          for (int col2 = 0; col2 < n; col2++) {
            if ((col1 != col2) &&
                (Math.abs(col1 - col2) != (row2-row1))) {
                if( exists(blocks, row1, col1) < 0 && exists(blocks, row2, col2) < 0)
              System.out.println(col1+", "+col2) ;
            }
          }
        System.out.println() ;
      }
  }

  public static List<BinaryTuple> getBlocks(int n, int m, int min, int seed){
    Random ran = new Random(seed);
    List<BinaryTuple> pairs = new ArrayList<>();
    int[][] rows = new int[n][n];
    int[][] cols = new int[n][n];
//    List<Integer[]>  rows = new ArrayList<>();
//    List<Integer[]> cols = new ArrayList<>();
    for(int i = 0; i < n; i ++){
      for(int j = 0; j < n; j++){
        pairs.add(new BinaryTuple(i,j));
        int[] r = rows[i];
        r[i] = j;
        rows[i] = r;
        int[] c = cols[j];
        c[j] = i;
        cols[j] = c;
      }
    }
    Collections.shuffle(pairs);
    int[] rcount = new int[n];
    int[] ccount = new int[n];
    List<BinaryTuple> blocks = new ArrayList<>();
    for(BinaryTuple bt: pairs){
      int v1 = bt.getVal1();
      int v2 = bt.getVal2();
      if(rcount[v1] < n-min && ccount[v2] < n-min){
        rows[v1][v2] = -1;
        cols[v2][v1] = -1;
//        rows.get(v1).remove(Integer.valueOf(v2));
//        cols.get(v2).remove(Integer.valueOf(v1));
        blocks.add(new BinaryTuple(v1,v2));
        rcount[v1]++;
        ccount[v2]++;
      }
      if(blocks.size() == m) break;
    }
    Collections.sort(blocks);
    for(BinaryTuple bt: blocks){
      System.out.println("r: "+bt.getVal1()+", c: "+bt.getVal2());
    }
    return blocks;

  }

  public static int exists(List<BinaryTuple> tuples, int r, int c){
    int i = 0;
    int ind = -1;
    for(BinaryTuple bt: tuples){
      if(bt.matches(r,c,true)) {
        ind = i;
        break;
      }
      i++;

    }
    return ind;
  }
}
