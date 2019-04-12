
import java.util.*;
import java.util.stream.Collectors;

import impl.exception.QueueEmptyException;


import static java.util.stream.Collectors.toMap;
public class Solver{
    protected static final int EMPTY = -1;
    protected static final int EXIT = 2;
    private HashMap<Integer, Integer> variables;
    private int num;
    private int[] assigned;
    private int[][] domains;
    private int[][] domainBounds ;
    private boolean fail = false;
    private boolean first = true;
    private int nodes = 0;
    private ArrayList<BinaryConstraint> constraints ;
    // added to act as a stack
    private ListStack<Map<BinaryTuple, BinaryTuple[]>> stack = new ListStack<>();

    Solver(BinaryCSP csp){
        domainBounds = csp.getDomainBounds();
        constraints = csp.getConstraints();
        variables = csp.getVariables();
        num = variables.size();
                //new int[csp.getNoVariables()];
        //for(int i = 0; i < csp.getNoVariables(); i++){ variables[i] = i; }
        writeDomain();
        assigned = new int[num];
        for(int i = 0; i < num; i++){ assigned[i] = EMPTY; }
    }

    boolean getFail(){ return fail; }
    void setFail(boolean fail){ this.fail = fail; }

    private void setDomains(){


    }
    // ***

    /**
     * write domain using the domain bounds, creates ragged array
     */
    private void writeDomain(){
        domains = new int[num][];
        for(int i = 0; i < num; i++) {
            int length = domainBounds[i][1] - domainBounds[i][0] + 1;
            domains[i] = new int[length];
            int low = domainBounds[i][0];
            for (int j = 0; j < length; j++) {
                domains[i][j] = low++;
            }
        }
    }

    /**
     * resets to start with the same csp problem
     *
     */
    void reset(){
        writeDomain();
        assigned = new int[num];
        for(int i = 0; i < num; i++){ assigned[i] = EMPTY; }
    }


    /**
     * reinitialise the solver with the new csp problem
     * @param csp new csp problem to solve
     */
    void setNew(BinaryCSP csp){
        domainBounds = csp.getDomainBounds();
        constraints = csp.getConstraints();
        variables = csp.getVariables();
        reset();
    }

    protected HashMap<Integer, Integer> getVariables(){ return variables; }

    /**
     * returns index of binary constraint interested located in constraint list
     * @param v1 first variable
     * @param v2 second variable
     * @return index of it located/ -1 if invalid (doesn't exists)
     */
    protected int getConstraint(int v1, int v2){
        int i = 0;
        for(BinaryConstraint bc: constraints){
            if(bc.checkVars(v1,v2)) return i;
            i++;
        }
        return EMPTY;
    }

    /**
     * sorts varList in smallest domain first order (counts domain size and order in variables
     * with smallest domain first) to be called before FC selectVar
     * @param varList a list of unassigned variables
     * @return sorted varList
     */
    protected List<Integer> sortVarList(List<Integer> varList){
        System.out.println("-------Sort Var List---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);

            int[] varD = domains[variables.get(var)];//domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > EMPTY) times++;
            }
            varCounts.put(i, times);
        }
        // now let's sort the map in decreasing order of value
        HashMap<Integer, Integer> sorted = varCounts
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        List<Integer> sortedList = new ArrayList<>();
        int ind = 0;
        for(Integer key : sorted.keySet()) {
            sortedList.add(ind++, varList.get(key));
        }
        Collections.sort(sortedList);
        System.out.println("------sorted------");
        return sortedList;
    }

    /**
     * selects value from the variable domain to choose to assign,
     * chosen according to ascending assignment ordering, find the first non-negative value in
     * domains array
     * @param var variable interested
     * @return value to assign
     */
    protected int selectVal(int var){
        int[] domain = domains[variables.get(var)];//domains[var];
        // assuming that it is already sorted in -1, -1, ... some values order
        int val = -1;
        for(int i = 0; i < domain.length; i++){
            if(domain[i] > EMPTY) {
                val = domain[i];
                break;
            }
        }
        return val;
    }

    /**
     * removes value from domain of variable var
     * @param var variable of interest
     * @param val value to remove from the domain of var
     * @return the index at which the value was located in domains array of var
     */
    protected int removeVal(int var, int val){
        System.out.println("Inside remove");
        int ind = -1;
        int[] dom = domains[variables.get(var)];//domains[var];
        // delete val from domain, and sort in ascending order
        for(int i = 0; i < dom.length; i++){
            if(dom[i] == val){
                dom[i] = EMPTY;
                //Arrays.sort(domains[var]);
                ind = i;
                break;
            }
        }
        try {
            if(ind < 0) throw new ArrayIndexOutOfBoundsException("No such value in domain");
        }
        catch (ArrayIndexOutOfBoundsException e){ e.getMessage(); }
        return ind;

    }

    /**
     * reverses the operation of reviseFA by popping saved to restore
     * previous binary tuples for each binary constraint and domain values for
     * futureVar
     */
    protected void undoPruning(){
        Map<BinaryTuple, BinaryTuple[]> pruned = stack.pop();
        for (Map.Entry<BinaryTuple, BinaryTuple[]> pair : pruned.entrySet()) {
            for(BinaryConstraint bc: constraints){
                if(pair.getKey().both(bc.getFirstVar(), bc.getSecondVar())){
                    System.out.println("this is the right constraint to add bt to");
                    bc.addTuples(pair.getValue());
                }
            }
            int futureVar;
            int val;
            // from L to R get the first var
            // for the case where the getValue does not have removed tuples in the first place
            if(pair.getValue().length == 1 && pair.getValue()[0].getVal2() < 0){
                if(!pair.getKey().getFirst()) futureVar = pair.getKey().getVal2();
                else futureVar = pair.getKey().getVal1();
                val = pair.getValue()[0].getVal1();
            }
            else {
                futureVar = pair.getKey().getVal1();
                val = pair.getValue()[0].getVal1();

                // from R to L get the second var
                if (!pair.getKey().getFirst()) {
                    System.out.println("opposite");
                    futureVar = pair.getKey().getVal2();
                    val = pair.getValue()[0].getVal2();
                }
            }
            System.out.println("var to recover is " + futureVar);
            System.out.println("value to recover is " + val);
            int varIndex = variables.get(futureVar);
            for(int i = 0; i < domains[varIndex].length; i++){
                if(domains[varIndex][i] < 0){
                    domains[varIndex][i] = val;
                    //sort?, will put all -1s in the front

                    break;
                }
            }
//            for(int i = 0; i < domains[futureVar].length; i++){
//                if(domains[futureVar][i] < 0){
//                    domains[futureVar][i] = val;
//                    //sort?, will put all -1s in the front
//
//                    break;
//                }
//            }
            Arrays.sort(domains[futureVar]);
        }

    }

    /**
     * revise domains of x_i and does pruning!
     * @param type T for FC and F for MAC
     * @param bt binary constraint of interest
     * @param var1 x_i
     * @param var2 x_j
     * @return boolean denoting if the domain of x_i has changed
     */
    protected boolean revise(boolean type, BinaryConstraint bt, int var1, int var2) {
        System.out.println("===== ReviseFC ======");
        System.out.println("Arc revision in REVISEFC - var1: " + var1 + ", var2: " + var2);
        int[] d1 = domains[variables.get(var1)];//domains[var1];
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);
        int[] d2 = domains[variables.get(var2)];//domains[var2];
        System.out.println("d2 domains");
        for(int v: d2) System.out.println(v);
        //boolean first = false;
        if (bt.getFirstVar() != var1) first = false;
        else first = true;
        System.out.println("order (first) is " + first);
        boolean changed = false;
        int ind = -1;
        for(int i : d1){
            ind++;
            System.out.println("i is " + i);
            if(i < 0) continue;
            boolean supported = false;
            int j = -1;
            while(!supported && j < d2.length - 1){
                j++;
                System.out.println("d2 is " + d2[j]);
                if(d2[j] < 0) continue;
                if(bt.checkMatch(i, d2[j], first)){
                    System.out.println(var1 + ": " + i + " has support in " + var2 + " var2: " + d2[j]) ;
                    System.out.println("first is " + first);
                    supported = true;
                }
            }
            if(!supported){
                System.out.println("no support for value " + i + " of futurevar " + var1 + "!");
                System.out.println("drop " + i + " and all tuples from bt from it");
                // add
                int v = i;
                List<BinaryTuple> rms = bt.removeTuple(i, first);
                System.out.println("&&&&&&&&&&&&&&&&&&&& removed tuples have length " + rms.size() + " ! &&&&&&&&&&&&&&&&&");
                //BinaryTuple[] rmsArray = new BinaryTuple[rms.size()];
                BinaryTuple[] rmsArray;
                if(rms.size() > 0 ) {
                    rmsArray = rms.toArray(new BinaryTuple[0]);
                    rms.toArray(rmsArray);
                }
                else {
                    // case where there are no removed tuples
                    rmsArray = new BinaryTuple[1];
                    rmsArray[0] = new BinaryTuple(i, -1);
                }

                Map<BinaryTuple, BinaryTuple[]> map = stack.pop();
                BinaryTuple copyRightOrder = bt.getVars();
                if(!first) copyRightOrder.setFirst(false);
                map.put(copyRightOrder, rmsArray);//rms.toArray(rmsArray));
                stack.push(map);
                System.out.println("after pushing");
                System.out.println("first is "+ first);
                // remove value from dom by setting the value to -1
                System.out.println("drop i, ind: " + ind);
                d1[ind] = EMPTY;

                changed = true;
            }
        }
        System.out.println("after loop");
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);
        //int i = assigned[var2];
        if(isEmptyDomain(d1)) {
            System.out.println("Empty domain for " + var1);
            // domain is empty set fail flag and return immediately
            if(!type) fail = true;
            return false;
        }
        System.out.println("reached end of revise fc");
        //System.out.println("reached end of revise fc with changed: " + changed);
        //if(!changed) return true;
        if(!type) return changed;
        return true;
    }

    /**
     * assign value to the var and remove all other values from its domain
     * @param var variable to assign value to
     * @param val value to assign
     * @return array of removed values
     */
    protected int[] assign(int var, int val){
        // count number of values atm
        //int valid = (int) Arrays.stream(domains[var]).filter(e -> e > EMPTY).count();
        int valid = (int) Arrays.stream(domains[variables.get(var)]).filter(e -> e > EMPTY).count();
        int[] removed = new int[valid-1];
        int j = 0;
        int varIndex = variables.get(var);
        int[] dom = domains[varIndex];
        for(int i = 0; i < domains[varIndex].length; i++){
            if(dom[i] != EMPTY && dom[i] != val){
                removed[j++] = dom[i];
                dom[i] = EMPTY;
            }
        }
        return removed;
    }

    /**
     * recover values removed previously to var's domain
     * @param var variable considered
     * @param removed array of values previously removed
     * @return
     */
    protected boolean unassign(int var, int[] removed){
        int varIndex = variables.get(var);
        int[] dom = domains[varIndex];
        int j = 0;
        for(int i = 0; i < domains[varIndex].length; i++){
            if(dom[i] < 0 && j < removed.length) dom[i] = removed[j++];
        }
        return (j == removed.length);
    }

    /**
     * checks if domain is empty
     *
     * @return boolean for T/F
     */
    protected boolean isEmptyDomain(int[] domain){
        return Arrays.stream(domain).allMatch(i -> i < 0);
    }

    /**
     * checks if all values are assigned
     *
     * @return all not empty T/F
     */
    protected boolean completeAssignment() {
        return Arrays.stream(assigned).allMatch(i -> i > EMPTY);
    }


    /**
     * prints out solution
     *
     */
    protected void print_sol(){
        int i = 0;
        for(Integer v: variables.keySet()){
            System.out.println("var " + v + ", " + assigned[i++]);
        }
//        for(int i = 0; i < assigned.length; i++){
//            System.out.println("Var" + variables[i] + ", " + assigned[i]);
//        }
    }

    void push(Map<BinaryTuple, BinaryTuple[]> map){
        stack.push(map);
    }

    BinaryConstraint getConstraintIndex(int index){
        return constraints.get(index);
    }

    int[] getVarDomain(int var){
        return domains[variables.get(var)];
        //return domains[var];
    }

    int getNum(){ return num; }

    int[] assignVal(int var, int val){
        assigned[variables.get(var)] = val;
        //assigned[var] = val;
        int[] removed = assign(var, val);
        return removed;
    }

    void unassignVal(int var){
        assigned[variables.get(var)] = EMPTY;
        //assigned[var] = EMPTY;
    }

    // Function to find the index of an element in a primitive array in Java
    public static int find(int[] a, int target)
    {
        return Arrays.stream(a) 					// IntStream
                .boxed()						// Stream<Integer>
                .collect(Collectors.toList())   // List<Integer>
                .indexOf(target);
    }
}