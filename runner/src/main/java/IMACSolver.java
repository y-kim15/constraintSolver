public interface IMACSolver {

    /**
     * returns all arcs in both directions, where there is a total
     * of v variables
     * @param v to be the number of vars
     * @return array list of arcs saved as binary tuples
     */
    private DLinkedListPriorityQueue getAllArcs(int v);

    /**
     * enforces arc consistency, get all arcs and enqueue as a block to start
     * uses doubly linked list queue
     * @throws QueueEmptyException
     */
    private boolean AC3() throws QueueEmptyException;

    /**
     * MAC Implementation
     * @param varList a list of unassigned variables
     * @return integer value to denote return status (2 : complete so exit, 0 : continue)
     * @throws QueueEmptyException
     */
    public int MAC3(List<Integer> varList) throws QueueEmptyException;


}