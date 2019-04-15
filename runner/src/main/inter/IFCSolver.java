import java.util.List;

public interface IFCSolver {
    int nodes = 0;
    /**
     * implementation of forward checking
     * @param varList a list of unassigned variables
     * @return integer to denote status, return EXIT (2) if soln is found
     */
    int FC(List<Integer> varList);









    }