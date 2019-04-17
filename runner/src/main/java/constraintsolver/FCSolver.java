package constraintsolver;
import constraintsolver.impl.BinaryTuple;

import java.io.IOException;
import java.util.*;

public class FCSolver extends Solver {
    private int nodes = 0;
    private int fcNodes = 0;


    FCSolver(BinaryCSP csp, Heuristics type, Heuristics selType){
        super(csp, type, selType);
    }

    public void doForwardCheck(){
        List<Integer> varList = getVarList();
       // start = System.nanoTime();
        FC(varList);
    }

    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     * @return integer to denote status, return EXIT (2) if soln is found
     */
    private int FC(List<Integer> varList){
        fcNodes++;
        System.out.println("########### FC " + ++nodes + "th Node#############");
//        if(completeAssignment()){
//            System.out.println("Print Solution");
//            printSol();
//            return EXIT;
//        }
        // all positive so all variables are assigned
        if(completeAssignment()){
           // end = System.nanoTime();
            System.out.println(" SOLUTION FOUND! EXIT");
            //printSol();
            return Solver.EXIT;
        }
        System.out.println(varList.toString());
        //varList = sortVarList(varList);
        System.out.println("after sorting: "  + varList.toString());
       // if(last == EMPTY) last = varList.get(0);
        int var = selectVar(varList);//, last);//varList.get(0); //or call selectVar
        //last = var;
        int val = selectVal(varList, var);
        System.out.println("GO TO LEFT");
        List<Integer> copVarList = (List<Integer>) ((ArrayList<Integer>) varList).clone();
        if(branchFCLeft(varList, var, val) == Solver.EXIT) return Solver.EXIT ;
        System.out.println("GO TO RIGHT");
        if(branchFCRight(copVarList, var, val) == Solver.EXIT) return Solver.EXIT ;
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

//        assigned[var] = val;
//        int[] removed = assign(var, val);
        int[] removed = assignVal(var, val);
        System.out.println("assigned - var: " + var + ", value: " + val);
        if(reviseFA(varList, var, val)){
            System.out.println("assignment is consistent, has support");
            System.out.println("before removing : " + varList.toString() + " with var " + var);
            varList.remove(Integer.valueOf(var));
            //last = -1;
            System.out.println("after removing : " + varList.toString());
            //FC(varList without var)
            if(FC(varList) == Solver.EXIT){
                //System.out.println("FOUND");
                return Solver.EXIT;
            }
        }
        System.out.println("false, domain was emptied by the assignment undo");
        undoPruning();
        System.out.println("Undone pruning, unassigned value " + val + " to var " + var);
        // un-assign
        if(!unassign(var, removed)) System.out.println("ERROR");
//        assigned[var] = EMPTY;
        unassignVal( var);
        varList.add(var);
        sortVarList(varList);
        System.out.println("###### End of Left Branch #######");
        return 0;
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
        int[] doms = getVarDomain(var);
        for (int v:doms) { s += Integer.toString(v);
            s += ", ";
        }
        System.out.println(s);
        // checks if domain is empty (if sum of all values = -(length)
        if(Arrays.stream(doms).sum() > (Solver.EMPTY)*(doms.length)){
            if(reviseFA(varList, var, val)){
                if(FC(varList) == Solver.EXIT) return Solver.EXIT;
            }
            undoPruning();
        }
        //restoreVal(var, val);
        //restore
        for(int i = 0; i < doms.length; i++){
            if(doms[i] < 0){
                doms[i] = val;
                break;
            }
        }
        // domains[var][ind] = val;
        System.out.print("#### End of Right Branch #####");
        return 0;
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
        push(map);
        //stack.push(map);
        // assuming that varList is already order with smallest domain first!
        for(Integer futureVar: varList){
            if(futureVar.equals(var)) continue;
            int index = getConstraint(futureVar, var);
            if(index < 0) continue; // constraint doesn't exists so no need to revise
            consistent = revise(true, getConstraintIndex(index), futureVar, var);//, val);
            //consistent = revise(index, futureVar, var);//, val);
            if(!consistent) return false;
        }
        return true;
    }

    protected void printSol() throws IOException {
        System.out.println("================= FC Output =============");
        super.printSol(false, "", "");
        System.out.println("No. Nodes: " + nodes);
        System.out.println("No of fc calls " + fcNodes);
    }


}
