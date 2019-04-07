
import java.util.*;
import impl.exception.QueueEmptyException;

import static java.util.stream.Collectors.toMap;
//TODO: divide this two 2 separate classes one for FCRunner, another for MACRunner
public class Helper {
    private int[] variables;
    private int[] assigned;
    private int[][] domainBounds ;
    private boolean fail = false;
    private ArrayList<BinaryConstraint> constraints ;
    // added to act as a stack
    private ListStack<Map<BinaryTuple, BinaryTuple[]>> stack = new ListStack<>();

    public Helper(BinaryCSP csp){
        domainBounds = csp.getDomainBounds();
        constraints = csp.getConstraints();
        variables = new int[csp.getNoVariables()];
        for(int i = 0; i < csp.getNoVariables(); i++){ variables[i] = i; }
        assigned = new int[variables.length];
        for(int i = 0; i < variables.length; i++){ assigned[i] = -1; }
    }

    /**
     * returns all arcs (pairs from constraints) from value v
     * @param v arcs of which to collect
     * @return array list of arcs saved as binary tuples
     */
    public DLinkedListPriorityQueue getAllArcs(int v) {
        DLinkedListPriorityQueue vars = new DLinkedListPriorityQueue();
        //TODO enqueue by selecting constraint will smallest number of allowed tuples first!
        for(BinaryConstraint bc: constraints){
            if(bc.getFirstVar() == v)
                vars.enqueue(bc.getVars());
        }
        return vars;
    }

    /**
     * returns index of binary constraint interested located in constraint list
     * @param v1 first variable
     * @param v2 second variable
     * @return index of it located/ -1 if invalid (doesn't exists)
     */
    public int getArc(int v1, int v2){
        int i = 0;
        for(BinaryConstraint bc: constraints){
            if(bc.checkVars(v1,v2)) return i;
            i++;
        }
        return -1;
    }

    /**
     * sorts varList in smallest domain first order (counts domain size and order in variables
     * with smallest domain first) to be called before FC selectVar
     * @param varList a list of unassigned variables
     * @return sorted varList
     */
    public List<Integer> sortVarList(List<Integer> varList){
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);
            int[] varD = domainBounds[var];
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
        return sortedList;
    }

    /**
     * returns the variable to be used
     * @param varList assumed to be already sorted
     * @return variable selected
     */
    public int selectVar(List<Integer> varList){
        /*// traverse each and should return variable with smallest domain length
        int cur = -1;
        int counts = -1;
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);
            int[] varD = domainBounds[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > 0) times++;
            }
            if(counts < times){
                cur = i;
                counts = times;
            }
        }
        if(cur >= 0) return varList.get(cur);
        else return varList.get(0);*/
        return varList.get(0);

    }

    /**
     * selects value from the variable domain to choose to assign,
     * chosen according to ascending assignment ordering, find the first non-negative value in
     * domains array
     * @param var variable interested
     * @return value to assign
     */
    public int selectVal(int var){
        int[] domains = domainBounds[var];
        // assuming that it is already sorted in -1, -1, ... some values order
        int val = -1;
        for(int i = 0; i < domains.length; i++){
            if(domains[i] > -1) {
                val = domains[i];
                break;
            }
        }
        return val;
    }

    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     */
    public void FC(List<Integer> varList){
        // all positive so all variables are assigned
        if(Arrays.stream(variables).allMatch(i -> i > 0)){
            System.out.println(Arrays.asList(variables));
            //TODO: exit method to terminate
        }

        //TODO : write complete Solution
        varList = sortVarList(varList);
        int var = varList.get(0); //or call selectVar
        int val = selectVal(var);
        branchFCLeft(varList, var, val);
        branchFCRight(varList, var, val);
    }

    /**
     * implementation of left branch in Forward Checking
     * @param varList a list of unassigned variables
     * @param var variable chosen to use
     * @param val value to be assigned to that variable
     */
    public void branchFCLeft(List<Integer> varList, int var, int val){
        // assign
        assigned[var] = val;
        if(reviseFA(varList, var, val)){
            varList.remove(var);
            //FC(varList without var)
            FC(varList);
        }
        undoPruning();
        // un-assign
        assigned[var] = -1;
    }

    /**
     * implementation of right branch in Forward Checking
     * delete by removing from domain bounds, to be restored later
     * @param varList a list of unassigned variables (includes var which is in use in left branch)
     * @param var variable name
     * @param val value assigned to that variable
     */
    public void branchFCRight(List<Integer> varList, int var, int val){
        int ind = removeVal(var, val);
        // checks if domain is empty (if sum of all values = -(length)
        if(Arrays.stream(domainBounds[var]).sum() > (-1)*(domainBounds[var].length)){
            if(reviseFA(varList, var, val)){
                // FC(varList)
            }
            undoPruning();
        }
        //restore
        domainBounds[var][ind] = val;
    }

    /**
     * removes value from domain of variable var
     * @param var variable of interest
     * @param val value to remove from the domain of var
     * @return the index at which the value was located in domains array of var
     */
    public int removeVal(int var, int val){
        int ind = -1;
        // delete val from domain, and sort in ascending order
        for(int i = 0; i < domainBounds[var].length; i++){
            if(domainBounds[var][i] == val){
                domainBounds[var][i] = -1;
                Arrays.sort(domainBounds[var]);
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
    public void undoPruning(){
        Map<BinaryTuple, BinaryTuple[]> pruned = stack.pop();
        for (Map.Entry<BinaryTuple, BinaryTuple[]> pair : pruned.entrySet()) {
            for(BinaryConstraint bc: constraints){
                if(pair.getKey().equals(bc.getVars())){
                    bc.addTuples(pair.getValue());
                }
            }
            int futureVar = pair.getKey().getVal1();
            int val = pair.getValue()[0].getVal1();
            for(int i = 0; i < domainBounds[futureVar].length; i++){
                if(domainBounds[futureVar][i] < 0){
                    domainBounds[futureVar][i] = val;
                    //sort?, will put all -1s in the front
                    Arrays.sort(domainBounds[futureVar]);
                    break;
                }
            }
        }

    }

    /**
     * arc revision with all future variables
     * @param varList varlist is smf ordered list of variables containing var
     * @param var variable selected to work on
     * @return boolean true if the future assignment makes it consistent/ else false mean unpruning should be done
     */
    public boolean reviseFA(List<Integer> varList, int var, int val){
        boolean consistent = true;
        Map<BinaryTuple, BinaryTuple[]> map = new HashMap<>();
        stack.push(map);
        // assuming that varList is already order with smallest domain first!
        for(Integer futureVar: varList){
            if(futureVar.equals(var)) continue;
            int index = getArc(futureVar, var);
            if(index < 0) continue; // constraint doesn't exists so no need to revise
            consistent = reviseFC(constraints.get(index), futureVar, var, val);
            if(!consistent) return false;
        }
        return true;
    }

    public boolean reviseFC(BinaryConstraint bt, int futureVar, int var, int val){
        int[] d1 = domainBounds[futureVar];
        int[] d2 = domainBounds[var];
        boolean changed = false;
        int ind = 0;
        for(int i : d1){
            if(i < 0) continue;
            boolean supported = false;
            int j = -1;
            while(!supported){
                j++;
                if(d2[j] < 0) continue;
                if(d2[j] == val){
                    if(bt.checkMatch(i, val)) supported = true;
                }
            }
            if(!supported){
                // add
                int v = d1[ind];
                List<BinaryTuple> rms = bt.removeTuple(d1[ind]);
                BinaryTuple[] rmsArray = new BinaryTuple[rms.size()];
                Map<BinaryTuple,BinaryTuple[] > map = stack.pop();
                map.put(bt.getVars(), rms.toArray(rmsArray));
                stack.push(map);
                //pruned.add(d1[ind]);

                // remove value from dom by setting the value to -1
                domainBounds[futureVar][ind] = -1;
                d1[ind] = -1;

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
    }

    /**
     * revise domains of x_i and does pruning!
     * @param bt binary constraint of interest
     * @param var1 x_i
     * @param var2 x_j
     * @return boolean denoting if the domain of x_i has changed
     */
    public boolean revise(BinaryConstraint bt, int var1, int var2){
        int[] d1 = domainBounds[var1];
        int[] d2 = domainBounds[var2];

        boolean changed = false;
        int ind = 0;
        for(int i : d1){
            if(i < 0) continue;
            boolean supported = false;
            int j = -1;
            while(!supported){
                j++;
                if(d2[j] < 0) continue;
                if(bt.checkMatch(i, d2[j])) supported = true;
            }
            if(!supported){
                // add
                int v = d1[ind];
                List<BinaryTuple> rms = bt.removeTuple(d1[ind]);
                BinaryTuple[] rmsArray = new BinaryTuple[rms.size()];
                Map<BinaryTuple,BinaryTuple[] > map = stack.pop();
                map.put(bt.getVars(), rms.toArray(rmsArray));
                stack.push(map);
                //pruned.add(d1[ind]);

                // remove value from dom by setting the value to -1
                domainBounds[var1][ind] = -1;
                d1[ind] = -1;

                changed = true;
            }
            ind++;
        }

        if(isEmptyDomain(d1)) {
            // domain is empty set fail flag and return immediately
            fail = true;
            return false;
        }
        return changed;
    }

    public boolean isEmptyDomain(int[] domain){
        return (Arrays.stream(domain).sum() == (-1)*(domain.length));
    }

    /**
     * enforces arc consistency, get all arcs and enqueue as a block to start
     * uses doubly linked list queue
     * @throws QueueEmptyException
     */
    public boolean AC3() throws QueueEmptyException{
        DLinkedListPriorityQueue queue = new DLinkedListPriorityQueue();
        for(int v: variables){
            queue.blockEnqueue(getAllArcs(v));
            while(!queue.isEmpty()){
                BinaryTuple tup = (BinaryTuple) queue.dequeue();
                if(revise(constraints.get(getArc(tup.getVal1(), tup.getVal2())), tup.getVal1(), tup.getVal2())){
                    for(int vv: variables){
                        if(vv!=tup.getVal2() && vv!=tup.getVal1()) queue.enqueue(new BinaryTuple(vv, tup.getVal1()));
                    }
                }
                // fail received
                if(fail) return false;

            }
        }
        return true;
    }

    public void MAC3(List<Integer> varList) throws QueueEmptyException {
        varList = sortVarList(varList);
        int var = varList.get(0);
        int val = selectVal(var);
        assigned[var] = val;
        //TODO if(completeAssignement()) printsoln() and exit() (same as those required by FC())
        if(val>0){
            //dummy
        }
        else if(AC3()){
            varList.remove(var);
            MAC3(varList);
        }
        undoPruning();
        assigned[var] = -1;
        // index of variable removed
        int ind = removeVal(var, val);
        if(!isEmptyDomain(domainBounds[var])){
            if(AC3()) MAC3(varList);
            undoPruning();
        }
        //restore
        domainBounds[var][ind] = val;

    }







}
