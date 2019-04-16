import java.util.*;

import impl.exception.QueueEmptyException;

public class MACSolver extends Solver {
    private int depth = 0;

    MACSolver(BinaryCSP csp, Heuristics type, Heuristics selType){
        super(csp, type, selType);
    }

    public void doMAC() throws QueueEmptyException{
        List<Integer> varList = getVarList();
        System.out.println("got list for the first time");
        varList.forEach(System.out::println);
        start = System.nanoTime();
        AC3(varList, EMPTY);
        MAC3(varList);
    }

    /**
     * returns all arcs in both directions, where there is a total
     * of v variables. When var == EMPTY, get all arcs for general enforcement again
     * @return array list of arcs saved as binary tuples
     */
    public DLinkedListPriorityQueue getAllArcs(List<Integer> varList, int var) {
        DLinkedListPriorityQueue vars = new DLinkedListPriorityQueue();
//        for(BinaryConstraint bc: getConstraints()){
//            vars.enqueue(bc.getVars());
//            vars.enqueue(new BinaryTuple(bc.getSecondVar(), bc.getFirstVar()));
//        }
        if(var > EMPTY) {
            List<Integer> cons = getConnectionVar(var);
            // add to arc only if it is those which are not assigned
            for (int v2 : cons) {
                //if (varList.indexOf(v2) > EMPTY)
                vars.enqueue(new BinaryTuple(v2, var));
            }
        }
        else{
            for(BinaryConstraint bc: getConstraints()){
                vars.enqueue(bc.getVars());
                vars.enqueue(new BinaryTuple(bc.getSecondVar(), bc.getFirstVar()));
            }
        }
        return vars;
    }

    /**
     * enforces arc consistency, get all arcs and enqueue as a block to start
     * uses doubly linked list queue
     * @throws QueueEmptyException
     */
    private boolean AC3(List<Integer> varList, int var) throws QueueEmptyException {
        System.out.println("************888888888 Inside AC3");
        //DLinkedListPriorityQueue queue = new DLinkedListPriorityQueue();
        //int[] vars = getVariables().keySet().stream().mapToInt(i -> i).toArray();
        DLinkedListPriorityQueue queue = getAllArcs(varList, var);
        Map<BinaryTuple, BinaryTuple[]> map = new HashMap<>();
        push(map);
        //stack.push(map);
        while(!queue.isEmpty()){
            BinaryTuple tup = (BinaryTuple) queue.dequeue();
            int v1 = tup.getVal1();
            int v2 = tup.getVal2();
//            System.out.println("DEQUEUED!!!!!!!");
//            System.out.println("getval1: " + tup.getVal1() + " , getval2: " + tup.getVal2());
              int index = getConstraint(v1,v2);
//            System.out.println("index is " + index);
            if(revise(false, getConstraintIndex(index), v1, v2)){
                // domain changed, add more arcs
//                System.out.println("done revise enqueue rest");
                //ArrayList<BinaryConstraint> consts = getConstraints();
                List<Integer> connected = getConnectionVar(v1);
                for(int vv : connected){
                    if(vv == v2) continue;// || varList.indexOf(vv) == EMPTY) continue;
                    if(!checkIfExists(queue, vv, v1)) queue.enqueue(new BinaryTuple(vv, v1));
                }
            }
            // fail received

            if(getFail()){
                System.out.println("FAIL RECEIVED");
                setFail(false);
                //break;
                return false;
            }

        }

        return true;
    }

    /**
     * checks if the given arc already exists in the queue
     * @param queue the current queue
     * @param v1 first var
     * @param v2 second var
     * @return if exists, don't add, if doesn't exists, do add
     */
    public boolean checkIfExists(DLinkedListPriorityQueue queue, int v1, int v2){
        return queue.checkIfExists(new BinaryTuple(v1, v2));
//        DLinkedListNode head = queue.getHead();
//        DLinkedListNode start = head.next;
//        boolean exists = false;
//        while(start != queue.getTail() && !exists){
//            if(start.element.matches(v1, v2, true))  exists = true;
//            start = start.next;
//        }
//        return exists;
    }

    /**
     * MAC Implementation
     * @param varList a list of unassigned variables
     * @return integer value to denote return status (2 : complete so exit, 0 : continue)
     * @throws QueueEmptyException
     */
    private int MAC3(List<Integer> varList) throws QueueEmptyException {
        System.out.println("**********=========== " + depth++ + " th MAC CALL ==========");
        int code = 0;
        //varList = sortVarList(varList);
        //if(last == EMPTY) last = varList.get(0);
        int var =  selectVar(varList);//, last);//varList.get(0);
        int val = selectVal(varList, var);
        //assigned[var] = val;
        //int[] removed = assign(var, val);
        int[] removed = assignVal(var, val);
//        System.out.println("assigned - var: " + var + ", value: " + val);

        if(completeAssignment()){
            end = System.nanoTime();
            System.out.println("complete");
            print_sol();
            return EXIT;
        }

        else if(AC3(varList, var)){
//            System.out.println("AC held, all supported, can proceed");
            varList.remove(Integer.valueOf(var));
            last = var;
//            System.out.println("removed the var, do MAC3");
            code = MAC3(varList);
            if(code > 0) return EXIT;
        }
        System.out.println("recover, undo pruning, unassign value " + val + " to var " + var);
        undoPruning();
        if(!unassign(var, removed)) System.out.println("ERROR");
        //assigned[var] = EMPTY;
        unassignVal(var);
        varList.add(0,var);
        //sortVarList(varList);
        // index of variable removed - ignored return value
        removeVal(var, val);
//        System.out.println("Removed value " + val + " from var " + var);
        int[] doms = getVarDomain(var);
        if(!isEmptyDomain(doms)){
            //var = EMPTY;
//            System.out.println("If domain not empty apply AC3 again");
            if(AC3(varList, var)) {
//                System.out.println("do MAC3");
                code = MAC3(varList);
                if(code > 0) return EXIT;
            }
            System.out.println("undopruning");
            undoPruning();

        }
        Arrays.sort(doms);
        for(int i = 0; i < doms.length; i++){
            if(doms[i] < 0){
                doms[i] = val;
                break;
            }
        }
        System.out.println("reached end");
//        lastTry = new BinaryTuple(var,EMPTY);
//        MAC3(varList);
        return 0;

    }

    @Override
    protected void print_sol() {
        System.out.println("=================== MAC Output ===============");
        super.print_sol();
        System.out.println("Depth: (no of mac calls) " + depth);
    }
}
