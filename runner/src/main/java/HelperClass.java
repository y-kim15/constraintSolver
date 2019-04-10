
public class HelperClass {

    /**
     * writes domain variables in 2d array given
     * @param n number of variables
     * @param domainBounds domains for each
     * @return all values in domain for all variables stored in 2d array
     */
    public static int[][] writeDomain(int n, int[][] domainBounds){
        int[][] domains = new int[n][domainBounds[0][1]+1];
        for(int i = 0; i < n; i++) {
            for (int j = 0; j <= domainBounds[i][1]; j++) {
                domains[i][j] = j;
            }
        }
        return domains;
    }

    /**
     * returns index of binary constraint interested located in constraint list
     * @param constraints to check
     * @param v1 first variable
     * @param v2 second variable
     * @return index of it located/ -1 if invalid (doesn't exists)
     */
    public static int getConstraint(ArrayList<BinaryConstraint> constraints, int v1, int v2){
        int i = 0;
        for(BinaryConstraint bc: constraints){
            if(bc.checkVars(v1,v2)) return i;
            i++;
        }
        return EMPTY;
    }

    /**
     * sorts varList in smallest domain first order (counts domain size and order in variables
     * with smallest domain first) to be called before FC selectVar
     * @param domains to base its sorting on
     * @param varList a list of unassigned variables
     * @return sorted varList
     */
    public static List<Integer> sortVarList(int[][] domains, List<Integer> varList){
        System.out.println("-------Sort Var List---------");
        HashMap<Integer, Integer> varCounts = new HashMap<>();
        for(int i = 0; i < varList.size(); i++){
            // number of positive values
            int times = 0;
            int var = varList.get(i);
            int[] varD = domains[var];
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
        Collections.sort(sortedList);
        System.out.println("------sorted------");
        return sortedList;
    }

    /**
     * selects value from the variable domain to choose to assign,
     * chosen according to ascending assignment ordering, find the first non-negative value in
     * domains array
     * @param domains domains to select from
     * @param var variable interested
     * @return value to assign
     */
    public static int selectVal(int[][] domains, int var){
        int[] domain = domains[var];
        // assuming that it is already sorted in -1, -1, ... some values order
        int val = -1;
        for(int i = 0; i < domain.length; i++){
            if(domain[i] > EMPTY) {
                val = domain[i];
                break;
            }
        }
        return val;
    }

    /**
     * removes value from domain of variable var
     * @param var variable of interest
     * @param val value to remove from the domain of var
     * @return updated domain
     */
    public static int[][] removeVal(int[][] domains, int var, int val){
        System.out.println("Inside remove");
        int ind = -1;
        int[] dom = domains[var];
        // delete val from domain, and sort in ascending order
        for(int i = 0; i < dom.length; i++){
            if(dom[i] == val){
                dom[i] = EMPTY;
                //Arrays.sort(domains[var]);
                ind = i;
                break;
            }
        }
        try {
            if(ind < 0) throw new ArrayIndexOutOfBoundsException("No such value in domain");
        }
        catch (ArrayIndexOutOfBoundsException e){ e.getMessage(); }
        return domains; //ind;

    }

    /**
     * checks if domain is empty
     * @param domain domain to check
     * @return boolean for T/F
     */
    public static boolean isEmptyDomain(int[] domain){
        return Arrays.stream(domain).allMatch(i -> i < 0);
    }

    /**
     * checks if all values are assigned
     * @param assigned values assigned
     * @return all not empty T/F
     */
    public static boolean completeAssignment(int[] assigned) {
        return Arrays.stream(assigned).allMatch(i -> i > EMPTY);
    }


    /**
     * prints out solution
     * @param variables
     * @param assigned found solution
     */
    public static void print_sol(int[] variables, int[] assigned){
        for(int i = 0; i < assigned.length; i++){
            System.out.println("Var" + variables[i] + ", " + assigned[i]);
        }
    }







}