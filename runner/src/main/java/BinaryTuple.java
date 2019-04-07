import java.util.concurrent.CompletableFuture;
/**
 * Assumes tuple values are integers
 */
public final class BinaryTuple implements Comparable {
  private int val1, val2 ;
  
  public BinaryTuple(int v1, int v2) {
    val1 = v1 ;
    val2 = v2 ;
  }
  
  public String toString() {
    return "<"+val1+", "+val2+">" ;
  }
  
  public boolean matches(int v1, int v2) {
    return (val1 == v1) && (val2 == v2) ;
  }

  public boolean has(int v1){
    return (val1==v1);
  }

  @Override
  public boolean equals(Object obj) {
    BinaryTuple bt = (BinaryTuple) obj;
    return matches(bt.val1, bt.val2);
  }

  public int getVal1(){ return val1; }
  public int getVal2(){ return val2; }

  @Override
  public int compareTo(Object o) {
    BinaryTuple bt = (BinaryTuple) o;
    if(equals(o)) return 0;
    return -1;
  }
}
