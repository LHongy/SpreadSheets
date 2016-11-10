import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Scanner;

// Basic model for a spreadsheet.
public class Spreadsheet{
    
    private Map<String, Cell> cellMap;
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
        Set<Map.Entry<String, Cell>> cellSet = cellMap.entrySet(); // get a set from the map
        Iterator<Map.Entry<String, Cell>> iterator = cellSet.iterator(); // this set contains keys and values
        while(iterator.hasNext()) {
            Map.Entry<String, Cell> cellMapEntry = iterator.next();
            id = cellMapEntry.getKey();
            value = getCellDisplayString(id);
            contents = getCellContents(id);
            builder.append(String.format("%6s |%7s | '%s'\n", id, value, contents));
        }
        builder.append("\nCell Dependencies\n");
        builder.append(dag);
        return builder.toString();
    }
    
    // Produce a saveable string of the spreadsheet. A reasonable format
    // is each cell id and its contents on a line.  You may choose
    // whatever format you like so long as the spreadsheet can be
    // completely recreated using the fromSaveString(s) method.
    public String toSaveString() {
        StringBuilder builder = new StringBuilder();
        Set<Map.Entry<String, Cell>> cellSet = cellMap.entrySet(); // get a set from the map
        Iterator<Map.Entry<String, Cell>> iterator = cellSet.iterator(); // this set contains keys and values
        while(iterator.hasNext()) {
            Map.Entry<String, Cell> cellMapEntry = iterator.next();
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
            throw new RuntimeException();
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
        cellMap.remove(id);
        dag.remove(id);
        notifyDownstreamOfChange(id);
    }
    
    // Set the given cell with the given contents. If contents is "" or
    // null, delete the cell indicated.
    public void setCell(String id, String contents) {
        if(contents.length() == 0 || contents == null) {
            deleteCell(id);
            return;
        }
        try {
            verifyIDFormat(id);
            cellMap.remove(id); 
            //  deleteCell(id);
            Cell newCell = Cell.make(contents);
            Set<String> upstreamIDS = newCell.getUpstreamIDs();
            dag.add(id, upstreamIDS);
            cellMap.put(id, newCell);
            newCell.updateValue(cellMap);
            notifyDownstreamOfChange(id);
        } catch(DAG.CycleException e) {
            System.err.println(" " + e.getMessage());
        } catch(RuntimeException e) {
            System.err.println("Cell id 'zebra' is badly formatted");
        }
    }
    
    // Notify all downstream cells of a change in the given cell.
    // Recursively notify subsequent cells. Guaranteed to terminate so
    // long as there are no cycles in cell dependencies.
    public void notifyDownstreamOfChange(String id) {
        Set<String> downstreamLinks = dag.getDownstreamLinks(id);
        Iterator<String> iterator = downstreamLinks.iterator();
        while(iterator.hasNext()) {
            String cellID = iterator.next();
            Cell cell = cellMap.get(cellID);
            cell.updateValue(cellMap);
            notifyDownstreamOfChange(cellID);
        }
    }
    
}