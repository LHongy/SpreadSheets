import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Scanner;

// Basic model for a spreadsheet.
public class Spreadsheet{
    
    // Stores Cell IDs as keys, actual Cell Objects as values
    private Map<String, Cell> cellMap; 
    // For detecting cycle
    private DAG dag; 
    
    // Construct a new empty spreadsheet
    public Spreadsheet() {
        cellMap = new HashMap<>();
        dag = new DAG();
    }
    
    // Return a string representation of the spreadsheet. This should
    // show a table of the cells ids, values, and contents along with
    // the upstream and downstream links between cells. Ensure that
    // StringBuilder and iterators over various maps are used to
    // efficiently construct the string. The expected format is as
    // follows.
    //
    //     ID |  Value | Contents
    // -------+--------+---------------
    //     A1 |    5.0 | '5'
    //     D1 |    4.0 | '=4'
    //     C1 |  178.0 | '=22*A1 + 17*D1'
    //     B1 |     hi | 'hi'
    // 
    // Cell Dependencies
    // Upstream Links:
    //   C1 : [A1, D1]
    // Downstream Links:
    //   A1 : [C1]
    //   D1 : [C1]
    //
    public String toString() {
        String id = "ID";
        String value = "Value";
        String contents = "Contents";
        
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%6s |%7s | %s\n", id, value, contents));
        builder.append("-------+--------+---------------\n");
        // get a set from cellMap that contains keys and values
        Set<Map.Entry<String, Cell>> cellSet = cellMap.entrySet();
        Iterator<Map.Entry<String, Cell>> iterator = cellSet.iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Cell> cellMapEntry = iterator.next();
            // if the Cell Object with the ID exsits
            if(cellMapEntry.getValue() != null) {
                id = cellMapEntry.getKey();
                value = getCellDisplayString(id);
                contents = getCellContents(id);
                // Append ID, value, and contents in well formated string
                builder.append(String.format("%6s |%7s | '%s'\n", id, value, contents));
            }
        }
        builder.append("\nCell Dependencies\n");
        // Append upstreamLinks and downstreamLinks
        builder.append(dag);
        return builder.toString();
    }
    
    // Produce a saveable string of the spreadsheet. A reasonable format
    // is each cell id and its contents on a line.  You may choose
    // whatever format you like so long as the spreadsheet can be
    // completely recreated using the fromSaveString(s) method.
    public String toSaveString() {
        StringBuilder builder = new StringBuilder();
        // get a set from cellMap that contains keys and values
        Set<Map.Entry<String, Cell>> cellSet = cellMap.entrySet(); 
        Iterator<Map.Entry<String, Cell>> iterator = cellSet.iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, Cell> cellMapEntry = iterator.next();
            // Append a Cell's ID and contents
            String id = cellMapEntry.getKey();
            String contents = getCellContents(id);
            builder.append(String.format("%s %s\n", id, contents));
        }
        return builder.toString();
    }
    
    // Load a spreadsheet from the given save string. Typical
    // implementations will creat an empty spreadsheet and repeatedly
    // read input from the provided string setting cells based on the
    // contents read.
    public static Spreadsheet fromSaveString(String s) {
        Scanner input = new Scanner(s);
        Spreadsheet sheet = new Spreadsheet();
        while(input.hasNext()) {
            // Read the ID and contents to set a Cell object
            String id = input.next();
            String contents = input.nextLine();
            sheet.setCell(id, contents);
        } 
        return sheet;
    }
    
    // Check if a cell ID is well formatted.  It must match the regular
    // expression
    // 
    //  ^[A-Z]+[1-9][0-9]*$
    // 
    // to be well formatted. If the ID is not formatted correctly, throw
    // a RuntimeException.  The str.matches(..) method is useful for
    // this method.
    public static void verifyIDFormat(String id) {
        if(!id.matches("^[A-Z]+[1-9][0-9]*$")) {
            throw new RuntimeException
                (String.format("Cell id '%s' is badly formatted", id));
        }
    }
    
    // Retrive a string which should be displayed for the value of the
    // cell with the given ID. Return "" if the specified cell is empty.
    public String getCellDisplayString(String id) {
        Cell cell = cellMap.get(id);
        if(cell == null) {
            return "";
        }
        return cell.displayString();
    }
    
    // Retrive a string which is the actual contents of the cell with
    // the given ID. Return "" if the specified cell is empty.
    public String getCellContents(String id) {
        Cell cell = cellMap.get(id);
        if(cell == null) {
            return "";
        }
        return cell.contents();
    }
    
    // Delete the contents of the cell with the given ID. Update all
    // downstream cells of the change. If specified cell is empty, do
    // nothing.
    public void deleteCell(String id) {
        if(cellMap.get(id) == null) {
            return;
        }
        // Remove Cell Object from cellMap and Cell ID from dag
        cellMap.remove(id);
        dag.remove(id);
        notifyDownstreamOfChange(id);
    }
    
    // Set the given cell with the given contents. If contents is "" or
    // null, delete the cell indicated.
    public void setCell(String id, String contents) {
        // If contents is "" or null, delete the cell indicated.
        if(contents == null || contents.length() == 0) {
            deleteCell(id);
            return;
        }
        
        // store the original state of the Cell Object to variable oldCell
        Cell oldCell = cellMap.get(id); 
        verifyIDFormat(id);
        // Delete the Cell Object that match to the CellID in cellMap
        cellMap.remove(id); 
        // Create a new cell with contents
        Cell newCell = Cell.make(contents);
        // Extract the upstream dependencies for the newCell 
        Set<String> upstreamIDS = newCell.getUpstreamIDs();
        
        try {
            // Try add newCell to dag
            dag.add(id, upstreamIDS);
            // If successful, no cycles are created and newCell is valid
            cellMap.put(id, newCell);
            newCell.updateValue(cellMap);
            notifyDownstreamOfChange(id);
        } catch(DAG.CycleException e) {
            // dag.add(id, upstreamIDS) caused a cyle in the dag
            // setCell failed
            // we should put the newCell back to its original state
            // and throw a new DAG.CycleException
            cellMap.put(id, oldCell);
            throw new DAG.CycleException
                (String.format
                     ("Cell %s with formula '%s' creates cycle: ", id, contents)
                     + e.getMessage());
        }
    }
    
    // Notify all downstream cells of a change in the given cell.
    // Recursively notify subsequent cells. Guaranteed to terminate so
    // long as there are no cycles in cell dependencies.
    public void notifyDownstreamOfChange(String id) {
        // Get the set of downstream cells
        Set<String> downstreamLinks = dag.getDownstreamLinks(id);
        Iterator<String> iterator = downstreamLinks.iterator();
        while(iterator.hasNext()) {
            // for each downstream cell
            // update its value and notify its downstream cells
            String cellID = iterator.next();
            Cell cell = cellMap.get(cellID);
            cell.updateValue(cellMap);
            notifyDownstreamOfChange(cellID);
        }
    }
    
}