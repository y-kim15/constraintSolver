
import java.util.* ;

public final class BinaryConstraint {
  private int firstVar, secondVar ;
  private ArrayList<BinaryTuple> tuples ;
  
  public BinaryConstraint(int fv, int sv, ArrayList<BinaryTuple> t) {
    firstVar = fv ;
    secondVar = sv ;
    tuples = t ;
  }

  public int getFirstVar() {
    return firstVar;
  }

  public String toString() {
    StringBuffer result = new StringBuffer() ;
    result.append("c("+firstVar+", "+secondVar+")\n") ;
    for (BinaryTuple bt : tuples)
      result.append(bt+"\n") ;
    return result.toString() ;
  }

  /**
   * check matching of variable names
   * (to be used to find desired binary constraint)
   * @param v1 first variable to check with
   * @param v2 second variable to check with
   * @return boolean if matches
   */
  public boolean checkVars(int v1, int v2) { return (firstVar == v1) && (firstVar == v2) ;
  }

  /**
   * returns variables in this constraint as a binary tuple
   * @return binary tuple of VARIABLE NAMES (NOT VALUES)
   */
  public BinaryTuple getVars(){
    return new BinaryTuple(firstVar, secondVar);
  }

  /**
   * check if the given tuple exists in the domain
   * (to be used by REVISE method)
   * @param v1 (d_i)
   * @param v2 (d_j)
   * @return boolean
   */
  public boolean checkMatch(int v1, int v2){
    boolean match = false;
    for (BinaryTuple bt: tuples){
      if(bt.matches(v1, v2)) match = true;
    }
    return match;
  }

  /**
   * removes any tuples containing the pruned domain value
   * @param v domain value to remove (first)
   * @return list of removed tuples
   */
  public List<BinaryTuple> removeTuple(int v){
    List<BinaryTuple> rms = new ArrayList<>();
    for(int i = 0; i < tuples.size(); i++){
      if(tuples.get(i).has(v)){
        rms.add(tuples.get(i));
        tuples.remove(i);
      }
    }
    return rms;
  }

  /**
   * adds list of tuples to allowed binary tuples
   * @param tups tuples to add
   */
  public void addTuples(BinaryTuple[] tups){
    Collections.addAll(tuples, tups);
  }
  
  // SUGGESTION: You will want to add methods here to reason about the constraint
}
