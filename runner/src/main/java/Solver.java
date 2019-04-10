public class Solver{
    private static final int EMPTY = -1;
    private static final int EXIT = 2;
    private int[] variables;
    private int[] assigned;
    private int[][] domains;
    private int[][] domainBounds ;
    private boolean fail = false;
    private boolean first = true;
    private ArrayList<BinaryConstraint> constraints ;
    // added to act as a stack
    private ListStack<Map<BinaryTuple, BinaryTuple[]>> stack = new ListStack<>();

    public Runner(BinaryCSP csp){
        domainBounds = csp.getDomainBounds();
        constraints = csp.getConstraints();
        variables = new int[csp.getNoVariables()];
        for(int i = 0; i < csp.getNoVariables(); i++){ variables[i] = i; }
        writeDomain();
        assigned = new int[variables.length];
        for(int i = 0; i < variables.length; i++){ assigned[i] = EMPTY; }
    }

    // ***
    private void writeDomain(){
        domains = new int[variables.length][domainBounds[0][1]+1];
        for(int i = 0; i < variables.length; i++) {
            for (int j = 0; j <= domainBounds[i][1]; j++) {
                domains[i][j] = j;
            }
        }
    }

    /**
     * resets to start with the same csp problem
     *
     */
    private void reset(){
        writeDomain();
        assigned = new int[variables.length];
        for(int i = 0; i < variables.length; i++){ assigned[i] = EMPTY; }
    }


    /**
     * reinitialise the solver with the new csp problem
     * @param csp new csp problem to solve
     */
    private void setNew(BinaryCSP csp){
        domainBounds = csp.getDomainBounds();
        constraints = csp.getConstraints();
        variables = new int[csp.getNoVariables()];
        for(int i = 0; i < csp.getNoVariables(); i++){ variables[i] = i; }
        reset();
    }

    public int[] getVariables(){ return variables; }

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
        System.out.println("-------Sort Var List---------");
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
    private int selectVal(int var){
        int[] domain = domains[var];
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
    private int removeVal(int var, int val){
        System.out.println("Inside remove");
        int ind = -1;
        int[] dom = domains[var];
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
    private void undoPruning(){
        Map<BinaryTuple, BinaryTuple[]> pruned = stack.pop();
        for (Map.Entry<BinaryTuple, BinaryTuple[]> pair : pruned.entrySet()) {
            for(BinaryConstraint bc: constraints){
                if(pair.getKey().both(bc.getFirstVar(), bc.getSecondVar())){
                    System.out.println("this is the right constraint to add bt to");
                    bc.addTuples(pair.getValue());
                }
            }
            int futureVar = pair.getKey().getVal1();
            int val = pair.getValue()[0].getVal1();

            if(!pair.getKey().getFirst()){
                System.out.println("opposite");
                futureVar = pair.getKey().getVal2();
                val = pair.getValue()[0].getVal2();
            }
            System.out.println("var to recover is " + futureVar);
            System.out.println("value to recover is " + val);
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
     * revise domains of x_i and does pruning!
     * @param bt binary constraint of interest
     * @param var1 x_i
     * @param var2 x_j
     * @return boolean denoting if the domain of x_i has changed
     */
    public boolean revise(BinaryConstraint bt, int var1, int var2){
        System.out.println("===== ReviseFC ======");
        System.out.println("Arc revision in REVISEFC - var1: " + var1 + ", var2: " + var2);
        int[] d1 = domains[var1];
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);
        int[] d2 = domains[var2];
        System.out.println("d2 domains");
        for(int v: d2) System.out.println(v);

        if(bt.getFirstVar() != var1) first = false;
        else first = true;
        boolean changed = false;
        System.out.println("order (first) is " + first);
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
                BinaryTuple[] rmsArray = rms.toArray(new BinaryTuple[0]);
                Map<BinaryTuple,BinaryTuple[] > map = stack.pop();
                BinaryTuple copyRightOrder = bt.getVars();
                if(!first) copyRightOrder.setFirst(false);
                map.put(copyRightOrder, rms.toArray(rmsArray));
                stack.push(map);
                //pruned.add(d1[ind]);
                System.out.println("after pushing");
                System.out.println("first is "+ first);
                // remove value from dom by setting the value to -1
                System.out.println("drop i, ind: " + ind);
                d1[ind] = EMPTY;
                // remove value from dom by setting the value to -1


                changed = true;
            }
        }
        System.out.println("after loop");
        System.out.println("d1 domains");
        for(int v: d1) System.out.println(v);


        if(isEmptyDomain(d1)) {
            System.out.println("Empty domain for " + var1);

            // domain is empty set fail flag and return immediately
            fail = true;
            return false;
        }
        System.out.println("reached end of revise fc");
        return changed;
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
     * checks if domain is empty
     * @param domain domain to check
     * @return boolean for T/F
     */
    public static boolean isEmptyDomain(int[] domain){
        return Arrays.stream(domain).allMatch(i -> i < 0);
    }

    /**
     * checks if all values are assigned
     * @param assigned values assigned
     * @return all not empty T/F
     */
    public static boolean completeAssignment(int[] assigned) {
        return Arrays.stream(assigned).allMatch(i -> i > EMPTY);
    }


    /**
     * prints out solution
     * @param variables
     * @param assigned found solution
     */
    public static void print_sol(int[] variables, int[] assigned){
        for(int i = 0; i < assigned.length; i++){
            System.out.println("Var" + variables[i] + ", " + assigned[i]);
        }
    }

}