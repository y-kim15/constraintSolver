package constraintsolver;
import constraintsolver.impl.BinaryConstraint;
import constraintsolver.impl.BinaryTuple;

import java.io.* ;
import java.util.* ;

/**
 * encoded heuristics to be used as enum
 */
enum Heuristics {
    MAXDEG(-3), MAXCAR(-1), SDF(3), BRELAZ(4), DOMDEG(5), ASCEND(6), MINCONF(7);
    private final int val;
    Heuristics(int val){
        this.val = val;
    }
    public int getVal(){ return val;}
}


/**
 * A reader tailored for binary extensional CSPs.
 * It is created from a FileReader and a StreamTokenizer
 */
public final class BinaryCSPReader {
  private FileReader inFR ;
  private StreamTokenizer in ;

  /**
   * Main (for testing)
   */
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: java constraintsolver.BinaryCSPReader <file.csp>") ;
      return ;
    }
    BinaryCSPReader reader = new BinaryCSPReader() ;
	BinaryCSP prob = reader.readBinaryCSP(args[0]);
	Solver solver = new Solver(prob, Heuristics.SDF, Heuristics.ASCEND);
	boolean found = solver.solve(true);
	if(found) System.out.println("FOUND");
	else System.out.println("SOLUTION");
	solver.printSol( found, false, "", "sdf#asc");
	solver.reset();
  }

  /**
   * File format:
   * <no. vars>
   * NB vars indexed from 0
   * We assume that the domain of all vars is specified in terms of bounds
   * <lb>, <ub> (one per var)
   * Then the list of constraints
   * c(<varno>, <varno>)
   * binary tuples
   * <domain val>, <domain val>
   */
  public BinaryCSP readBinaryCSP(String fn) {
    try {
      inFR = new FileReader(fn) ;
      in = new StreamTokenizer(inFR) ;
      in.ordinaryChar('(') ;
      in.ordinaryChar(')') ;
      in.nextToken() ;                                         // n
      int n = (int)in.nval ;
      int[][] domainBounds = new int[n][2] ;
      for (int i = 0; i < n; i++) {
	      in.nextToken() ;                                  // ith ub
	      domainBounds[i][0] = (int)in.nval ;
		    in.nextToken() ;                                   // ','
		    in.nextToken() ;
	      domainBounds[i][1] = (int)in.nval ;
      }
      String[] splits = fn.split("/");
      String filename = splits[splits.length-1];
      ArrayList<BinaryConstraint> constraints = readBinaryConstraints() ;
      BinaryCSP csp = new BinaryCSP(filename, domainBounds, constraints) ;
      // TESTING:
      // System.out.println(csp) ;
      inFR.close() ;
      return csp ;
    }
    catch (FileNotFoundException e) {System.out.println(e);}
    catch (IOException e) {System.out.println(e);}
    return null ;
  }

  /**
   *
   */
  private ArrayList<BinaryConstraint> readBinaryConstraints() {
    ArrayList<BinaryConstraint> constraints = new ArrayList<BinaryConstraint>() ;
	
    try {
      in.nextToken() ;                                  //'c' or EOF
      while(in.ttype != in.TT_EOF) {
	      // scope
	      in.nextToken() ;                                       //'('
		    in.nextToken() ;                                       //var
	      int var1 = (int)in.nval ;
		    in.nextToken() ;                                       //','
		    in.nextToken() ;                                       //var
        int var2 = (int)in.nval ;
		    in.nextToken() ;                                       //')'

        //tuples
		    ArrayList<BinaryTuple> tuples = new ArrayList<BinaryTuple>() ;
        in.nextToken() ;              //1st allowed val of 1st tuple
        while (!"c".equals(in.sval) && (in.ttype != in.TT_EOF)) {
          int val1 = (int)in.nval ;
	        in.nextToken() ;                                   //','
	        in.nextToken() ;                               //2nd val
		      int val2 = (int)in.nval ;
		      tuples.add(new BinaryTuple(val1, val2)) ;
		      in.nextToken() ;      //1stallowed val of next tuple/c/EOF
		    }
        BinaryConstraint c = new BinaryConstraint(var1, var2, tuples) ;
        constraints.add(c) ;
      }
	  
      return constraints ;
    }
    catch (IOException e) {System.out.println(e);}
    return null ;  
  }
}
