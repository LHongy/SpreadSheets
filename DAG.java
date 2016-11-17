import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class DAG{
    
    private Map<String, Set<String>> upstreamLinksMap; // map to stroe the nodes and their upstreamLinks
    private Map<String, Set<String>> downstreamLinksMap; // map to store the nodes and their downstreamlinks
    // Construct an empty DAG
    public DAG() {
        upstreamLinksMap = new HashMap<>();
        downstreamLinksMap = new HashMap<>();
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
        StringBuilder builder = new StringBuilder();
        builder.append("Upstream Links:\n");
        // get a set from the upstreamLinksMap that contains keys and values
        Set<Map.Entry<String, Set<String>>> upstreamSet = upstreamLinksMap.entrySet();
        Iterator<Map.Entry<String, Set<String>>> iterator = upstreamSet.iterator();
        // iterator to iterate the set
        while(iterator.hasNext()) {
            Map.Entry<String, Set<String>> upstreamMapEntry = iterator.next();
            // if the upstreamLinks of a node isn't empty
            if(!upstreamMapEntry.getValue().isEmpty()) {
                builder.append(String.format("%4s",upstreamMapEntry.getKey()) + " : "); // append the node to the builder
                builder.append(upstreamMapEntry.getValue()); // append the node's upstreamLinks
                builder.append("\n");
            }
        }
        
        builder.append("Downstream Links:\n");
        // get a set from the downstreamLinksMap that contains keys and values
        Set<Map.Entry<String, Set<String>>> downstreamSet = downstreamLinksMap.entrySet(); 
        iterator = downstreamSet.iterator(); 
        while(iterator.hasNext()) {
            Map.Entry<String, Set<String>> downstreamMapEntry = iterator.next();
            // if the downstreamLinks of a node isn't empty
            if(!downstreamMapEntry.getValue().isEmpty()) {
                builder.append(String.format("%4s",downstreamMapEntry.getKey()) + " : "); // append the node
                builder.append(downstreamMapEntry.getValue()); // append the node's upstreamLinks
                builder.append("\n");
            }
        }
        return builder.toString();
    }
    
    // Return the upstream links associated with the given ID.  If there
    // are no links associated with ID, return the empty set.
    //
    // TARGET COMPLEXITY: O(1)
    public Set<String> getUpstreamLinks(String id) {
        // If there are no links associated with ID,
        // map ID to a new empty set
        if(upstreamLinksMap.get(id) == null) {
            upstreamLinksMap.put(id, new HashSet<String>());
        }
        return upstreamLinksMap.get(id);
    }
    
    // Return the downstream links associated with the given ID.  If
    // there are no links associated with ID, return the empty set.
    //
    // TARGET COMPLEXITY: O(1)
    public Set<String> getDownstreamLinks(String id) {
        if(downstreamLinksMap.get(id) == null) {
            downstreamLinksMap.put(id, new HashSet<String>());
        }
        return downstreamLinksMap.get(id);
    }
    
    // Class representing a cycle that is detected on adding to the
    // DAG. Raised in checkForCycles(..) and add(..).
    public static class CycleException extends RuntimeException{
        
        public CycleException(String msg) {
            super(msg);
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
        Set<String> preUpstreamLinks = getUpstreamLinks(id); // get original UpstreamLinks of id
        // remove id in any case
        remove(id);
        if(upstreamIDs == null || upstreamIDs.isEmpty()) {
            return;
        }
        // Add id and its upstreamIDs to the upstreamLinksMap
        upstreamLinksMap.put(id, upstreamIDs);
        
        Iterator<String> iterator = upstreamIDs.iterator();
        // Add the new node to the downstream links of each upstream node
        while(iterator.hasNext()) {
            String oneUpstreamID = iterator.next();
            Set<String> downstreamLinks = getDownstreamLinks(oneUpstreamID);
            downstreamLinks.add(id);
        }
        
        // The search is started at a designated node, usually the new node which is added
        // So we add the new node to the path as first element,
        // and start searching if there is a cycle
        List<String> path = new ArrayList<>();
        path.add(id);
        boolean cycle = checkForCycles(upstreamLinksMap, path);
        if(cycle) {
            // If a cycle is created, revert the DAG back to its original form so it appears
            // there is no change and raise a CycleException with a message
            // showing the cycle that would have resulted from the addition.
            
            // This is like adding the id again
            // but with preUpstreamLinks 
            // so that we can revert the DAG back
            remove(id); 
            // Add id and preUpstreamLinks to the upstreamLinksMap
            upstreamLinksMap.put(id, preUpstreamLinks);
            
            iterator = preUpstreamLinks.iterator();
            // Add the new node to the downstream links of each upstream node
            while(iterator.hasNext()) {
                String oneUpstreamID = iterator.next();
                Set<String> downstreamLinks = getDownstreamLinks(oneUpstreamID);
                downstreamLinks.add(id);
            }
            
            StringBuilder builder = new StringBuilder();
            builder.append(path); // the path of the cycle
            throw new CycleException(builder.toString());
        }
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
        // LASTNODE = get last element from PATH
        String lastNode = curPath.get(curPath.size() - 1);
        // NEIGHBORS = get set of neighbors associated with LASTNODE from LINKS
        Set<String> neighbors = links.get(lastNode);
        // if NEIGHBORS is empty or null then 
        // return false as this path has reached a dead end
        if(neighbors == null || neighbors.isEmpty()) {
            return false;
        }
        // otherwise continue
        Iterator<String> iterator = neighbors.iterator();
        // for every NID in NEIGHBORS
        while(iterator.hasNext()) {
            String NID = iterator.next();
            // append NID to the end of PATH
            curPath.add(NID);
            // if the first element in PATH equals NID then
            // return true because PATH now contains a cycle
            if(curPath.get(0).equals(NID)) {
                return true;
            }
            // otherwise continue
            // RESULT = checkForCycles(LINKS,PATH), recursively visit the neighbor
            boolean result = checkForCycles(links, curPath); 
            // if RESULT is true then
            // return true because PATH contains a cycle
            if(result) {
                return true;
            }
            // otherwise continue
            // remove the last element from PATH which should be NID
            curPath.remove(curPath.size() - 1);
        }
        // after exploring all NEIGHBORS, no cycles were found so return false
        return false;
    }
    
    // Remove the given id by eliminating it from the downstream links
    // of other ids and eliminating its upstream links.  If the ID has
    // no upstream dependencies, do nothing.
    //
    // TARGET COMPLEXITY: O(L_i)
    //   L_i : number of upstream links node id has
    public void remove(String id) {
        Set<String> upstreamLinks = getUpstreamLinks(id);
        if(upstreamLinks.isEmpty()) {
            // If the ID has
            // no upstream dependencies, do nothing.
            return;
        }
        Iterator<String> iterator = upstreamLinks.iterator();
        while(iterator.hasNext()) {
            // eliminating the given id from the downstream links
            // of other ids
            String oneUpstreamID = iterator.next();
            Set<String> downstreamLinks = getDownstreamLinks(oneUpstreamID);
            downstreamLinks.remove(id);
        }
        // eliminating the given id's upstream links
        upstreamLinksMap.remove(id);
    }
    
}