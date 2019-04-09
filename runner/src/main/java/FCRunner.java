

import java.util.*;

import static java.util.stream.Collectors.toMap;

public class FCRunner {
    private static final int EMPTY = -1;
    private static final int EXIT = 2;
    private int[] variables;
    private int[] assigned;
    private int[][] domains;
    private int[][] domainBounds;
    private boolean fail = false;
    private ArrayList<BinaryConstraint> constraints ;
    // added to act as a stack
    private ListStack<Map<BinaryTuple, BinaryTuple[]>> stack = new ListStack<>();
    private int nodes = 0;
    private boolean first = true;

    public FCRunner(BinaryCSP csp){
        constraints = csp.getConstraints();
        variables = new int[csp.getNoVariables()];
        System.out.println("number of vars " + csp.getNoVariables());
        for(int i = 0; i < csp.getNoVariables(); i++){ variables[i] = i; }
        domainBounds = csp.getDomainBounds();
        writeDomain();
        assigned = new int[variables.length];
        for(int i = 0; i < variables.length; i++){ assigned[i] = -1; }
    }

    private void writeDomain(){
        domains = new int[variables.length][domainBounds[0][1]+1];
        for(int i = 0; i < variables.length; i++) {
            for (int j = 0; j <= domainBounds[i][1]; j++) {
                domains[i][j] = j;
            }
        }
    }

    public int[] getVariables() {
        return variables;
    }

    /**
     * returns index of binary constraint interested located in constraint list
     * @param v1 first variable
     * @param v2 second variable
     * @return index of it located/ -1 if invalid (doesn't exists)
     */
    private int getConstraint(int v1, int v2){
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
    private List<Integer> sortVarList(List<Integer> varList){
        System.out.println("------Sort Var List---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);
            int[] varD = domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > 0) times++;
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
        System.out.println("------sorted-------");
        return sortedList;
    }

    /**
     * selects value from the variable domain to choose to assign,
     * chosen according to ascending assignment ordering, find the first non-negative value in
     * domains array
     * @param var variable interested
     * @return value to assign
     */
    private int selectVal(int var){
        int[] domain = domains[var];
        // assuming that it is already sorted in -1, -1, ... some values order
        int val = -1;
        for(int i = 0; i < domains.length; i++){
            if(domain[i] > EMPTY) {
                val = domain[i];
                break;
            }
        }
        return val;
    }

    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     * @return integer to denote status, return EXIT (2) if soln is found
     */
    public int FC(List<Integer> varList){
        System.out.println("########### FC " + ++nodes + "th Node#############");
//        if(completeAssignment()){
//            System.out.println("Print Solution");
//            print_sol();
//            return EXIT;
//        }
        // all positive so all variables are assigned
        if(Arrays.stream(assigned).allMatch(i -> i > EMPTY)){
            System.out.println(" SOLUTION FOUND! EXIT");
            print_sol();
            return EXIT;
        }
        System.out.println(varList.toString());
        varList = sortVarList(varList);
        System.out.println("after sorting: "  + varList.toString());
        int var = varList.get(0); //or call selectVar
        int val = selectVal(var);
        System.out.println("GO TO LEFT");
        List<Integer> copVarList = (List<Integer>) ((ArrayList<Integer>) varList).clone();
        if(branchFCLeft(varList, var, val) == EXIT) return EXIT ;
        System.out.println("GO TO RIGHT");
        if(branchFCRight(copVarList, var, val) == EXIT) return EXIT ;
        return 0;
    }

    /**
     * implementation of left branch in Forward Checking
     * @param varList a list of unassigned variables
     * @param var variable chosen to use
     * @param val value to be assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCLeft(List<Integer> varList, int var, int val){
        System.out.println("############ " + ++nodes + "th Node - LEFT ##########");
        // assign
        assigned[var] = val;
        int[] removed = assign(var, val);
        System.out.println("assigned - var: " + var + ", value: " + val);
        if(reviseFA(varList, var, val)){
            System.out.println("assignment is consistent, has support");
            System.out.println("before removing : " + varList.toString() + " with var " + var);
            varList.remove(Integer.valueOf(var));
            System.out.println("after removing : " + varList.toString());
            //FC(varList without var)
            if(FC(varList) == EXIT){
                System.out.println("FOUND");
                return EXIT;
            }
        }
        System.out.println("false, domain was emptied by the assignment undo");
        undoPruning();
        System.out.println("Undone pruning, unassigned value " + val + " to var " + var);
        // un-assign
        if(!unassign(var, removed)) System.out.println("ERROR");
        assigned[var] = EMPTY;
        varList.add(var);
        sortVarList(varList);
        System.out.println("###### End of Left Branch #######");
        return 0;
    }

    /**
     * assign value to the var and remove all other values from its domain
     * @param var variable to assign value to
     * @param val value to assign
     * @return array of removed values
     */
    public int[] assign(int var, int val){
        // count number of values atm
        int valid = (int) Arrays.stream(domains[var]).filter(e -> e > EMPTY).count();
        int[] removed = new int[valid-1];
        int j = 0;
        int[] dom = domains[var];
        for(int i = 0; i < domains[var].length; i++){
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
    public boolean unassign(int var, int[] removed){
        int[] dom = domains[var];
        int j = 0;
        for(int i = 0; i < domains[var].length; i++){
            if(dom[i] < 0 && j < removed.length) dom[i] = removed[j++];
        }
        return (j == removed.length);
    }

    /**
     * implementation of right branch in Forward Checking
     * delete by removing from domain bounds, to be restored later
     * @param varList a list of unassigned variables (includes var which is in use in left branch)
     * @param var variable name
     * @param val value assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCRight(List<Integer> varList, int var, int val){
        System.out.println("###########" + ++nodes + "th Node - RIGHT ###########");
        System.out.println("Remove value " + val + " from var: " + var);
        int ind = removeVal(var, val);
        System.out.println("check if removed: ");
        String s = "";
        for (int v:domains[var]) { s += Integer.toString(v);
                s += ", ";
        }
        System.out.println(s);
        // checks if domain is empty (if sum of all values = -(length)
        if(Arrays.stream(domains[var]).sum() > (EMPTY)*(domains[var].length)){
            if(reviseFA(varList, var, val)){
                if(FC(varList) == EXIT) return EXIT;
            }
            undoPruning();
        }
        //restore
        for(int i = 0; i < domains[var].length; i++){
            if(domains[var][i] < 0){
                domains[var][i] = val;
                break;
            }
        }
        // domains[var][ind] = val;
        System.out.print("#### End of Right Branch #####");
        return 0;
    }

    /**
     * removes value from domain of variable var
     * @param var variable of interest
     * @param val value to remove from the domain of var
     * @return the index at which the value was located in domains array of var
     */
    private int removeVal(int var, int val){
        System.out.println("Inside remove");
        int ind = -1;
        // delete val from domain, and sort in ascending order
        for(int i = 0; i < domains[var].length; i++){
            if(domains[var][i] == val){
                domains[var][i] = EMPTY;
                // Arrays.sort(domains[var]);
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
    private void undoPruning(){
        System.out.println("Undo Pruning-----");
        Map<BinaryTuple, BinaryTuple[]> pruned = stack.pop();
        for (Map.Entry<BinaryTuple, BinaryTuple[]> pair : pruned.entrySet()) {
            // add back removed tuples to constraint tuple list
            for(BinaryConstraint bc: constraints){
                if(pair.getKey().both(bc.getFirstVar(), bc.getSecondVar())){
                    System.out.println("this is the right constraint to add bt to");
                    bc.addTuples(pair.getValue());
                }
            }
            int futureVar = pair.getKey().getVal1();
            int val = pair.getValue()[0].getVal1();
            // it is reverse order, get the second as futureVar and get the value of that variable
            // whose tuples are all removed
            if(!pair.getKey().getFirst()){
                System.out.println("opposite");
                futureVar = pair.getKey().getVal2();
                val = pair.getValue()[0].getVal2();
            }
            System.out.println("var to recover is " + futureVar);
            System.out.println("value to recover is " + val);
//            if(!first) {
//                futureVar = pair.getKey().getVal2();
//                val = pair.getValue()[0].getVal2();
//            }
            // add back value to the domain
            for(int i = 0; i < domains[futureVar].length; i++){
                if(domains[futureVar][i] < 0){
                    domains[futureVar][i] = val;
                    //sort?, will put all -1s in the front
                    break;
                }
            }
            Arrays.sort(domains[futureVar]);
        }

    }

    /**
     * arc revision with all future variables
     * @param varList varlist is smf ordered list of variables containing var
     * @param var variable selected to work on
     * @return boolean true if the future assignment makes it consistent/ else false mean unpruning should be done
     */
    private boolean reviseFA(List<Integer> varList, int var, int val){
        System.out.println("===== ReviseFA ======");
        boolean consistent = true;
        Map<BinaryTuple, BinaryTuple[]> map = new HashMap<>();
        stack.push(map);
        // assuming that varList is already order with smallest domain first!
        for(Integer futureVar: varList){
            if(futureVar.equals(var)) continue;
            int index = getConstraint(futureVar, var);
            if(index < 0) continue; // constraint doesn't exists so no need to revise
            consistent = reviseFC(constraints.get(index), futureVar, var);//, val);
            if(!consistent) return false;
        }
        return true;
    }

    /**
     * revise domains of x_i and does pruning!
     * @param bt binary constraint of interest
     * @param var1 x_i
     * @param var2 x_j
     * @return boolean denoting if the domain of x_i has changed
     */
    public boolean reviseFC(BinaryConstraint bt, int var1, int var2) {
        System.out.println("===== ReviseFC ======");
        System.out.println("Arc revision in REVISEFC - var1: " + var1 + ", var2: " + var2);
        int[] d1 = domains[var1];
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);
        int[] d2 = domains[var2];
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
                //BinaryTuple[] rmsArray = new BinaryTuple[rms.size()];
                BinaryTuple[] rmsArray = rms.toArray(new BinaryTuple[0]);
                Map<BinaryTuple, BinaryTuple[]> map = stack.pop();
                BinaryTuple copyRightOrder = bt.getVars();
                if(!first) copyRightOrder.setFirst(false);
                map.put(copyRightOrder, rms.toArray(rmsArray));
                stack.push(map);
                System.out.println("after pushing");
                System.out.println("first is "+ first);
                // remove value from dom by setting the value to -1
                System.out.println("drop i, ind: " + ind);
                d1[ind] = EMPTY;
            }
        }
        System.out.println("after loop");
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);
        //int i = assigned[var2];
        if(isEmptyDomain(d1)) {
            System.out.println("Empty domain for " + var1);
            // domain is empty set fail flag and return immediately
            //fail = true;
            return false;
        }
        System.out.println("reached end of revise fc");
        //System.out.println("reached end of revise fc with changed: " + changed);
        //if(!changed) return true;
        return true;
    }



    /*
    private boolean reviseFC(BinaryConstraint bt, int futureVar, int var, int val){
        System.out.println("Arc revision in REVISEFC - f: " + futureVar + ", var: " + var);
        System.out.println("Value of Var " + var + " as " + val);
        int[] d1 = domainBounds[futureVar];
        int[] d2 = domainBounds[var];
        boolean first = false;
        if(bt.getFirstVar() == futureVar) first = true;
        boolean changed = false;
        int ind = 0;
        for(int i : d1){
            if(i < 0) continue;
            boolean supported = false;
            int j = -1;
            while(!supported && j < d2.length-1){
                j++;
                if(d2[j] < 0) continue;
                // no need to look for time taken
                if (bt.checkMatch(i, val, first)){
                    System.out.println("( " + i + ", " +  val + " ) supported! " + "first " + first );
                    supported = true;
                }
                /*if(d2[j] == val){

                }
            }
            if(!supported){
                System.out.println("no support for " + i + " of futurevar " + futureVar + "!");
                System.out.println("drop " + i + " add all tuples from bt from it");
                // add
                int v = d1[ind];
                List<BinaryTuple> rms = bt.removeTuple(d1[ind], first);
                BinaryTuple[] rmsArray = new BinaryTuple[rms.size()];
                Map<BinaryTuple,BinaryTuple[] > map = stack.pop();
                map.put(bt.getVars(), rms.toArray(rmsArray));
                stack.push(map);
                //pruned.add(d1[ind]);

                // remove value from dom by setting the value to -1
                domainBounds[futureVar][ind] = EMPTY;
                d1[ind] = EMPTY;

                changed = true;
            }
            ind++;
        }
        if(isEmptyDomain(domainBounds[futureVar])) {
            // domain is empty set fail flag and return immediately
            fail = true;
            return false;
        }
        return changed;
    }*/

    private boolean isEmptyDomain(int[] domain){
        return Arrays.stream(domain).allMatch(i -> i < 0);
        //return (Arrays.stream(domain).sum() == (-1)*(domain.length));
    }

    private boolean completeAssignment() {
        return Arrays.stream(assigned).allMatch(i -> i > 0);
    }

    private void print_sol(){
        for(int i = 0; i < assigned.length; i++){
            System.out.println("Var" + variables[i] + ", " + assigned[i]);
        }
    }

}
