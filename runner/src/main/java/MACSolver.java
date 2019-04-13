import java.util.*;

import impl.exception.QueueEmptyException;

public class MACSolver extends Solver implements IMACSolver {
    private int depth = 0;

    MACSolver(BinaryCSP csp){
        super(csp);
    }

    /**
     * returns all arcs in both directions, where there is a total
     * of v variables
     * @param v to be the number of vars
     * @return array list of arcs saved as binary tuples
     */
    public DLinkedListPriorityQueue getAllArcs(int v) {
        DLinkedListPriorityQueue vars = new DLinkedListPriorityQueue();
        //TODO enqueue by selecting constraint will smallest number of allowed tuples first!
//        HashMap<Integer, Integer> tupCounts = new HashMap<>();
//        int index = 0;
//        for(BinaryConstraint bc : constraints){
//            tupCounts.put(index++, bc.getNTuples());
//        }
        List<Integer> variables = new ArrayList<>(getVariables().keySet());
        ArrayList<BinaryConstraint> constraints = getConstraints();
        for(BinaryConstraint bc : constraints){
            vars.enqueue(bc.getVars());
        }
        for(BinaryConstraint bc: constraints){
            vars.enqueue(new BinaryTuple(bc.getSecondVar(), bc.getFirstVar()));
        }
//        for(int i = variables.get(0) ; i < v; i++){
//            for(int j = variables.get(0); j < v; j++){
//                if(i == j) continue;
//                System.out.println("check i: " + i + ", j: " + j);
//                if(getConstraint(i,j) > EMPTY){
//                    System.out.println("IT EXISTS! ADD TO THE QUEUE");
//                    vars.enqueue(new BinaryTuple(i,j));
//                }
//            }
//        }




//        for(int i = 0; i < v; i++){
//            for(int j = 0; j < v; j++){
//                if(i != j){
//                    vars.enqueue(new BinaryTuple(i, j));
//                }
//            }
//        }
//        for(BinaryConstraint bc: constraints){
//            if(bc.getFirstVar() == v)
//                vars.enqueue(bc.getVars());
//
//        }

        return vars;
    }

    /**
     * enforces arc consistency, get all arcs and enqueue as a block to start
     * uses doubly linked list queue
     * @throws QueueEmptyException
     */
    private boolean AC3() throws QueueEmptyException {
        System.out.println("************888888888 Inside AC3");
        //DLinkedListPriorityQueue queue = new DLinkedListPriorityQueue();
        int[] vars = getVariables().keySet().stream().mapToInt(i -> i).toArray();
        DLinkedListPriorityQueue queue = getAllArcs(vars.length);
//        queue.blockEnqueue(all);
//        for(int v: variables) {
//            if(v == variables.length-1) continue;
//            DLinkedListPriorityQueue small = getAllArcs(v);
//            queue.blockEnqueue(small);
//        }
        Map<BinaryTuple, BinaryTuple[]> map = new HashMap<>();
        push(map);
        //stack.push(map);
        while(!queue.isEmpty()){
            BinaryTuple tup = (BinaryTuple) queue.dequeue();
//            System.out.println("DEQUEUED!!!!!!!");
//            System.out.println("getval1: " + tup.getVal1() + " , getval2: " + tup.getVal2());
            int index = getConstraint(tup.getVal1(), tup.getVal2());
//            System.out.println("index is " + index);
            if(revise(false, getConstraintIndex(index), tup.getVal1(), tup.getVal2())){
//                System.out.println("done revise enqueue rest");
                ArrayList<BinaryConstraint> consts = getConstraints();
//                int i = 0;
//                for(BinaryConstraint bc: consts){
//                    if(bc.checkVars(vars[i]))
//                }
                for(int vv: vars){
                    if(vv!=tup.getVal2() && vv!=tup.getVal1()){
                        // first check that the arc doesn't exist, and second check there exists such arc
                        if(!checkIfExists(queue, vv, tup.getVal1()) && getConstraint(vv, tup.getVal1())>EMPTY) {
                            queue.enqueue(new BinaryTuple(vv, tup.getVal1()));
                        }
                    }
                }
            }
            // fail received

            if(getFail()){
                System.out.println("FAIL RECEIVED");
                setFail(false);
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
        DLinkedListNode head = queue.getHead();
        DLinkedListNode start = head.next;
        boolean exists = false;
        while(start != queue.getTail() && !exists){
            if(start.element.matches(v1, v2, true)) exists = true;
            start = start.next;
        }
        return exists;
    }

    /**
     * MAC Implementation
     * @param varList a list of unassigned variables
     * @return integer value to denote return status (2 : complete so exit, 0 : continue)
     * @throws QueueEmptyException
     */
    public int MAC3(List<Integer> varList) throws QueueEmptyException {
        if(start == 0) start = System.nanoTime();
        System.out.println("**********=========== " + depth++ + " th MAC CALL ==========");
        int code = 0;
        varList = sortVarList(varList);
        int var = varList.get(0);
        int val = selectVal(var);
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

        else if(AC3()){
//            System.out.println("AC held, all supported, can proceed");
            varList.remove(Integer.valueOf(var));
//            System.out.println("removed the var, do MAC3");
            code = MAC3(varList);
            if(code > 0) return EXIT;
        }
//        System.out.println("recover, undo pruning, unassign value " + val + " to var " + var);
        undoPruning();
        if(!unassign(var, removed)) System.out.println("ERROR");
        //assigned[var] = EMPTY;
        unassignVal(var);
        varList.add(var);
        sortVarList(varList);
        // index of variable removed
        int ind = removeVal(var, val);
//        System.out.println("Removed value " + val + " from var " + var);
        int[] doms = getVarDomain(var);
        if(!isEmptyDomain(doms)){
//            System.out.println("If domain not empty apply AC3 again");
            if(AC3()) {
//                System.out.println("do MAC3");
                code = MAC3(varList);
                if(code > 0) return EXIT;
            }
            else {
//                System.out.println("ac3 didn't work undopruning again");
            }
            undoPruning();

        }
        for(int i = 0; i < doms.length; i++){
            if(doms[i] < 0){
                doms[i] = val;
                break;
            }
        }
        return 0;

    }

    @Override
    protected void print_sol() {
        System.out.println("=================== MAC Output ===============");
        super.print_sol();
        System.out.println("Depth: (no of mac calls) " + depth);
    }
}
