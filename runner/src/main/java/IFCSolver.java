public interface IFCSolver {

    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     * @return integer to denote status, return EXIT (2) if soln is found
     */
    public int FC(List<Integer> varList);


    /**
     * implementation of left branch in Forward Checking
     * @param varList a list of unassigned variables
     * @param var variable chosen to use
     * @param val value to be assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCLeft(List<Integer> varList, int var, int val);


    /**
     * implementation of right branch in Forward Checking
     * delete by removing from domain bounds, to be restored later
     * @param varList a list of unassigned variables (includes var which is in use in left branch)
     * @param var variable name
     * @param val value assigned to that variable
     * @return return value for status, EXIT to finish
     */
    private int branchFCRight(List<Integer> varList, int var, int val);

    /**
     * arc revision with all future variables
     * @param varList varlist is smf ordered list of variables containing var
     * @param var variable selected to work on
     * @return boolean true if the future assignment makes it consistent/ else false mean unpruning should be done
     */
    private boolean reviseFA(List<Integer> varList, int var, int val);








    }