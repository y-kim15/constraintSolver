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
    Heuristics type;
    Heuristics selType;
    boolean fixed;

    Control(HashMap<Integer, Integer> variables, ArrayList<BinaryConstraint> constraints,
            HashMap<Integer, List<Integer>> connections, Heuristics type, Heuristics selType, boolean fixed){
        this.variables = variables;
        this.constraints = constraints;
        this.connections = connections;
        this.type = type;
        this.selType = selType;
        this.fixed = fixed;
    }

    // order the variables by descending order of degree

    /**
     * static ordering - maximum degree
     * @return list of static ordered variables
     */
    public List<Integer> orderByDeg(){
        List<Integer> varList = new ArrayList<>(variables.keySet());
        HashMap<Integer, Integer> varConstCounts = new HashMap<>();
        for(int v1: varList){
            int deg = connections.get(v1).size();
            varConstCounts.put(v1, deg);
        }
//        for(BinaryConstraint bc : constraints){
//            int v1 = bc.getFirstVar();
//            int v2 = bc.getSecondVar();
//            if(varConstCounts.containsKey(v1)) varConstCounts.put(v1, varConstCounts.get(v1)+1);
//            else varConstCounts.put(v1, 1);
//            if (varConstCounts.containsKey(v2)) varConstCounts.put(v2, varConstCounts.get(v2) + 1);
//            else varConstCounts.put(v2, 1);
//        }
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
     * @return static ordering list of variables
     */
    public List<Integer> orderByCard(){
        List<Integer> varList = new ArrayList<>(variables.keySet());
        System.out.println("variables: ");
        System.out.println(Arrays.toString(varList.toArray()));

        //HashMap<Integer, Integer> cardCounts = new HashMap<>();
        List<Integer> ordered = new ArrayList<>();
        double randomDouble = Math.random() * (varList.size());
        int randomInt = (int) randomDouble;
        int var = varList.get(randomInt);
        System.out.println("first: " + var);
        ordered.add(var);
        while (ordered.size() < varList.size()){
            List<Integer> values = connections.get(var);
            System.out.println("values connected with var: " + var);
            for(int vv: values) System.out.println(vv);
            int curN = -1;
            for(int v: values){
                if(ordered.indexOf(v) > EMPTY) continue;
                //if(varList.indexOf(v) == EMPTY) continue;
                List<Integer> vals = connections.get(v);
                int N = 0;
                for(int v1: vals){
                    if(ordered.indexOf(v1) > EMPTY){
                        System.out.println("ready-assigned variable");
                        N++;
                    }
                }
                System.out.println("for adj var: " + v + ", have " + N + " variables that are ready assigned");
                if(curN < 0 || N > curN){
                    System.out.println("larger so set this to be top");
                    curN = N;
                    var = v;
                }
            }
            ordered.add(var);

            //cardCounts.put(var, curN);
        }
        System.out.println("END");
        Set<Integer> s = new HashSet<>(ordered);
        if(ordered.size() != s.size()) System.out.println("LENGTHS DIFFER!!");
        // sort by descending order
        //HashMap<Integer, Integer> sorted = sort(cardCounts, false);
        //return new ArrayList<>(sorted.keySet());
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
     * @return next var
     */
    public int brelaz(List<Integer> varList, int[][] domains){
        HashMap<Integer, Integer> map = sortByDomain(varList, domains);
        int i = 0;
        List<Integer> two = new ArrayList<>();
        for(int v: map.keySet()){
            if(i == 2) break;
            two.add(v);
            i++;
        }
        if(two.size() >= 2 && two.get(0).equals(two.get(1))){
            int maxDeg = -1;
            int val = -1;

            for(int j = 0; j < 2; j++){
                int nDeg = 0;
                for(int cons : connections.get(two.get(j))){
                    if(varList.indexOf(cons) > EMPTY) nDeg++;
                }
                if (maxDeg == EMPTY || nDeg > maxDeg) {
                    maxDeg = nDeg;
                    val = varList.get(i);
                }
            }
            return val;
        }
        return two.get(0);

//        for (int v1 : varList) {
//            int nDeg = 0;
////            HashSet cons = new HashSet(connections.get(v1));
////            System.out.println("cons size before: " + cons.size());
////            cons.retainAll(varList);
////            nDeg = cons.size();
////            System.out.println("cons size after: " + cons.size());
//            for(int cons : connections.get(v1)){
//                if(varList.indexOf(cons) > EMPTY) nDeg++;
//            }
//            for (BinaryConstraint bt : constraints) {
//                BinaryTuple vars = bt.getVars();
//                if (varList.indexOf(vars.getVal1()) == EMPTY || varList.indexOf(vars.getVal2()) == EMPTY) continue;
//                if (vars.has(v1, true) || bt.getVars().has(v1, false)) nDeg++;
//            }
//            if (maxDeg == EMPTY || nDeg > maxDeg) {
//                maxDeg = nDeg;
//                val = v1;
//            }
//        }
//        if(val == var) return  var;
//        return val;

    }

    /**
     * dynamic ordering of variable by descending order of degree
     * @param varList currently unassigned variables (if includes current var, ignore)
     * @return sorted by degree with future variables in HashMap<Variable, Degree> pairs
     */
    public HashMap<Integer, Integer> sortByDegree(List<Integer> varList){
        HashMap<Integer, Integer> degCounts = new HashMap<>();
        for (int v1 : varList) {
            //if(v1 == var) continue;
            int nDeg = 0;
            nDeg = connections.get(v1).size();
//            for (BinaryConstraint bt : constraints) {
//                BinaryTuple vars = bt.getVars();
//                // check if it is assigned, if so bypass
//                if (varList.indexOf(vars.getVal1()) == EMPTY || varList.indexOf(vars.getVal2()) == EMPTY) continue;
//                if (vars.has(v1, true) || bt.getVars().has(v1, false)) nDeg++;
//            }
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

    /**
     * sorts variable by the size of domain available for each (will be for variables which are nto assigned)
     * @param varList variables to be sorted, currently unassigned
     * @param domains current domain for all variables
     * @return Hashmap of variable, number of available values count pair
     */
    protected HashMap<Integer, Integer> sortByDomain(List<Integer> varList, int[][] domains){
        System.out.println("-------Sort By Domain---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int v :varList){
            // number of positive values
            int times = 0;
            //if(v == var) continue;

            int[] varD = domains[variables.get(v)];//domains[var];
            for(int j = 0; j < varD.length; j++){
                if(varD[j] > EMPTY) times++;
            }
            varCounts.put(v, times);
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
     * @param varList varlist with currently unassigned variables
     * @param domains current domains for all variables
     * @return the next variable to assign the value to
     */
    public int domDeg(List<Integer> varList, int[][] domains){
        HashMap<Integer, Integer> dom = sortByDomain(varList, domains);
        System.out.println("DOMAIN SORTED");
        for(int key : dom.keySet()) System.out.println("key: " + key + ", val: " + dom.get(key));
        HashMap<Integer, Integer> deg = sortByDegree(varList);
        System.out.println("DEGREE SORTED");
        for(int key : deg.keySet()) System.out.println("key: " + key + ", val: " + deg.get(key));
        double minRatio = -1;
        int minVar = -1;
        int times = 0;
        for(int vv : dom.keySet()){
            //if(vv == var) continue;
            double ratio = dom.get(vv)/((1.0)*(deg.get(vv)));
            if(minRatio < 0 || minRatio > ratio){
                minRatio = ratio;
                minVar = vv;

            }
            times++;
            if (times == 2) break;
        }
        return minVar;
    }





}
