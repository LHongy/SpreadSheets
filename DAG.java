import java.util.Map;
import java.util.Set;
import java.util.List;

public class DAG{
    
    // Construct an empty DAG
    public DAG() {
        
    }
    
    // Produce a string representaton of the DAG which shows the
    // upstream and downstream links in the graph.  The format should be
    // as follows:
    //
    // Upstream Links:
    //   A1 : [E1, F2, C1]
    //   C1 : [E1, F2]
    //  BB8 : [D1, C1, RU37]
    // RU37 : [E1]
    // Downstream Links:
    //   E1 : [A1, C1, RU37]
    //   F2 : [A1, C1]
    //   D1 : [BB8]
    // RU37 : [BB8]
    //   C1 : [A1, BB8]
    public String toString() {
        return null;
    }
    
    // Return the upstream links associated with the given ID.  If there
    // are no links associated with ID, return the empty set.
    //
    // TARGET COMPLEXITY: O(1)
    public Set<String> getUpstreamLinks(String id) {
        return null;
    }
    
    // Return the downstream links associated with the given ID.  If
    // there are no links associated with ID, return the empty set.
    //
    // TARGET COMPLEXITY: O(1)
    public Set<String> getDownstreamLinks(String id) {
        return null;
    }
    
    // Class representing a cycle that is detected on adding to the
    // DAG. Raised in checkForCycles(..) and add(..).
    public static class CycleException extends RuntimeException{
        
        public CycleException(String msg) {
            
        }
        
    }
    
    // Add a node to the DAG with the provided set of upstream links.
    // Add the new node to the downstream links of each upstream node.
    // If the upstreamIDs argument is either null or empty, remove the
    // node with the given ID.
    //
    // After adding the new node, check whether it has created any
    // cycles through use of the checkForCycles() method.  If a cycle is
    // created, revert the DAG back to its original form so it appears
    // there is no change and raise a CycleException with a message
    // showing the cycle that would have resulted from the addition.
    // 
    // TARGET RUNTIME COMPLEXITY: O(N + L)
    // MEMORY OVERHEAD: O(P)
    //   N : number of nodes in the DAG
    //   L : number of upstream links in the DAG
    //   P : longest path in the DAG starting from node id
    public void add(String id, Set<String> upstreamIDs) {
        
    }
    
    // Determine if there is a cycle in the graph represented in the
    // links map.  List curPath is the current path through the graph,
    // the last element of which is the current location in the graph.
    // This method should do a recursive depth-first traversal of the
    // graph visiting each neighbor of the current element. Each
    // neighbor should be checked to see if it equals the first element
    // in curPath in which case there is a cycle.
    //
    // This method should return true if a cycle is found and curPath
    // should be left to contain the cycle that is found.  Return false
    // if no cycles exist and leave the contents of curPath as they were
    // originally.
    //
    // The method should be used during add(..) which will initialize
    // curPath to the new node being added and use the upstream links as
    // the links passed in.
    public static boolean checkForCycles(Map<String, Set<String>> links, List<String> curPath) {
        return false;
    }
    
    // Remove the given id by eliminating it from the downstream links
    // of other ids and eliminating its upstream links.  If the ID has
    // no upstream dependencies, do nothing.
    //
    // TARGET COMPLEXITY: O(L_i)
    //   L_i : number of upstream links node id has
    public void remove(String id) {
        
    }
    
}