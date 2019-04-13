import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.lang.reflect.Array;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;
public class Control {
    private static final int EMPTY = -1;
    private HashMap<Integer, Integer> variables;
    private ArrayList<BinaryConstraint> constraints;
    private HashMap<Integer, List<Integer>> connections;
    Control(HashMap<Integer, Integer> variables, ArrayList<BinaryConstraint> constraints, HashMap<Integer, List<Integer>> connections){
        this.variables = variables;
        this.constraints = constraints;
        this.connections = connections;
    }

    // order the variables by descending order of degree

    /**
     * static ordering - maximum degree
     * @param varList list of all variables
     * @return list of static ordered variables
     */
    public List<Integer> orderByDeg(List<Integer> varList){
        HashMap<Integer, Integer> varConstCounts = new HashMap<>();
        for(BinaryConstraint bc : constraints){
            int v1 = bc.getFirstVar();
            int v2 = bc.getSecondVar();
            if(varConstCounts.containsKey(v1)) varConstCounts.put(v1, varConstCounts.get(v1)+1);
            else varConstCounts.put(v1, 1);
            if (varConstCounts.containsKey(v2)) varConstCounts.put(v2, varConstCounts.get(v2) + 1);
            else varConstCounts.put(v2, 1);
        }
        // sort by decreasing order of values
        HashMap<Integer, Integer> sorted = varConstCounts
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        List<Integer> vars = new ArrayList<>(sorted.keySet());
        return vars;
    }

    /**
     * select the next variable to assign the value to by
     * maximum cardinality rule : from the current value, choose the variable
     * it's connected largest number with amongst already assigned
     * @param varList all variables
     * @param var start variable
     * @return static ordering list of variables
     */
    public List<Integer> orderByCard(List<Integer> varList, int var){
        List<Integer> ordered = new ArrayList<>();
        while(ordered.size() < varList.size()){
            List<Integer> values = connections.get(var);
            int curN = -1;
            int curVal = -1;
            for(int v: values){
                if(varList.indexOf(v) == EMPTY) continue;
                List<Integer> vals = connections.get(v);
                int N = vals.size();
                for(int v1: vals){
                    if(ordered.indexOf(v1) > EMPTY) N--;
                }
                if(curN < 0 || N < curN){
                    curN = N;
                    curVal = v;
                }
            }
            ordered.add(curVal);

        }

        return ordered;
    }

    /**
     * static ordering input reader to return the ordered list
     * @param fn file path to file
     * @return ordered list
     */
    public List<Integer> orderByInput(String fn){
        FileReader inFR;
        StreamTokenizer in;
        List<Integer> list = new ArrayList<>();
        try {
            inFR = new FileReader(fn) ;
            in = new StreamTokenizer(inFR) ;
            in.ordinaryChar('(') ;
            in.ordinaryChar(')') ;
            in.nextToken() ;                                         // n
            int n = (int)in.nval ;
            //int[] vars = new int[n];
            for (int i = 0; i < n; i++) {
                in.nextToken() ;                                  // ith ub
                list.add((int)in.nval);
                in.nextToken() ;                                   // ','
                in.nextToken() ;
                list.add((int)in.nval);
            }
            // TESTING:
            // System.out.println(csp) ;
            inFR.close() ;
        }
        catch (FileNotFoundException e) {System.out.println(e);}
        catch (IOException e) {System.out.println(e);}
        return list;

    }

    /**
     * brelax implementation, pass the value of var of currently assigned
     * then select the variable with max degree in constraint sub-graph of future variables
     * First var should be selected by SDF! Use varList to check if the variables in bt are not assigned.
     * @param varList list of unassigned variables including var
     * @param var chosen next value using sdf method
     * @return next var
     */
    public int brelaz(List<Integer> varList, int var){
        int maxDeg = -1;
        int val = -1;
        for (int v1 : varList) {
            int nDeg = 0;
            for (BinaryConstraint bt : constraints) {
                BinaryTuple vars = bt.getVars();
                if (varList.indexOf(vars.getVal1()) == EMPTY || varList.indexOf(vars.getVal2()) == EMPTY) continue;
                if (vars.has(v1, true) || bt.getVars().has(v1, false)) nDeg++;
            }
            if (maxDeg == EMPTY || nDeg > maxDeg) {
                maxDeg = nDeg;
                val = v1;
            }
        }
        if(val == var) return  var;
        return val;

    }

    /**
     * dynamic ordering of variable by descending order of degree
     * @param varList currently unassigned variables (if includes current var, ignore)
     * @param var current var
     * @return sorted by degree with future variables in HashMap<Variable, Degree> pairs
     */
    public HashMap<Integer, Integer> sortByDegree(List<Integer> varList, int var){
        HashMap<Integer, Integer> degCounts = new HashMap<>();
        for (int v1 : varList) {
            if(v1 == var) continue;
            int nDeg = 0;
            for (BinaryConstraint bt : constraints) {
                BinaryTuple vars = bt.getVars();
                // check if it is assigned, if so bypass
                if (varList.indexOf(vars.getVal1()) == EMPTY || varList.indexOf(vars.getVal2()) == EMPTY) continue;
                if (vars.has(v1, true) || bt.getVars().has(v1, false)) nDeg++;
            }
            degCounts.put(v1, nDeg);
        }
        // sort in descending order
        HashMap<Integer, Integer> sorted = degCounts
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));
        return sorted;

    }

    protected HashMap<Integer, Integer> sortByDomain(List<Integer> varList, int var, int[][] domains){
        System.out.println("-------Sort By Domain---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int v = varList.get(i);
            if(v == var) continue;

            int[] varD = domains[variables.get(v)];//domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > EMPTY) times++;
            }
            varCounts.put(i, times);
        } // sort in ascending order of values
        HashMap<Integer, Integer> sorted = varCounts
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .collect(
                        toMap(e -> e.getKey(), e -> e.getValue(), (e1, e2) -> e2,
                                LinkedHashMap::new));
        List<Integer> sortedVars = new ArrayList<>(sorted.keySet());
        System.out.println("------sorted------");
        return sorted;//sortedVars;
    }

    /**
     * find the next value which gives the minimum quotient of dom size/degree
     * @param varList varlist with
     * @param var
     * @param domains
     * @return
     */
    public int domDeg(List<Integer> varList, int var, int[][] domains){
        HashMap<Integer, Integer> dom = sortByDomain(varList, var, domains);
        HashMap<Integer, Integer> deg = sortByDegree(varList, var);
        double minRatio = -1;
        int minVar = -1;
        for(int i = 0; i < dom.size(); i++){
            double ratio = dom.get(i)/(1.0)*(deg.get(i));
            if(minRatio < 0 || minRatio > ratio){
                minRatio = ratio;
                minVar = i;
            }
        }
        return minVar;
    }



}
