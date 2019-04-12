import java.util.List;

public interface IMACSolver {
    int depth = 0;
    /**
     * MAC Implementation
     * @param varList a list of unassigned variables
     * @return integer value to denote return status (2 : complete so exit, 0 : continue)
     *
     */
    int MAC3(List<Integer> varList) throws impl.exception.QueueEmptyException;

}