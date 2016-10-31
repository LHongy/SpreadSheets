import java.util.Map;
import java.util.Set;

// Spreadsheet Cells can be one of three different kinds:
// - Formulas always start with the = sign.  If the 0th character in
//   contents is a '=', use the method
//     FNode root = FNode.parseFormulaString(contents);
//   to create a formula tree of FNodes for later use.
// - Numbers can be parsed as doubles using the
//   Double.parseDouble(contents) method.  
// - Strings are anything else aside from Formulas and Numbers and
//   store only the contents given.
//
// Cells are largely immutable: once the contents of a cell are set,
// they do not change except to reflect movement of upstream dependent
// cells in a formula.  The value of a formula cell may change after
// if values change in its dependent cells. To make changes in a
// spreadsheet, one will typically create a new Cell with different
// contents using the method
//   newCell = Cell.make(contents);
//
// This class may contain nested static subclasses to implement the
// differnt kinds of cells.
public class Cell {
    
    private String kind;
    
    private Cell(String kind) {
        this.kind = kind;
    }
    
    // Factory method to create cells with the given contents linked to
    // the given spreadsheet.  The method is static so that one invokes it with:
    // 
    //   Cell c = Cell.make("=A21*2");
    //
    // The return value may be a subclass of Cell which is not possible
    // with constructors.  Call trim() on the contents string to remove
    // whitespace at the beginning and end.  If the contents is null or
    // empty, return null.  If contents is not valid, a RuntimeException
    // is generated.
    //
    // If the cell is a formula, it is not possible to evaluate its
    // formula during Cell.make() as other references to other cells
    // cannot be resolved.  The formula can only be reliably evaluated
    // after a call to cell.updateValue(cellMap) is made later.  Until
    // that time the cell should be in the ERROR state with
    // cell.isError() == true and displayString() == "ERROR" and
    // cell.numberValue() == null.
    public static Cell make(String contents) {
        String trimContents = contents.trim();
        String kind = "";
        try {
            Double.parseDouble(trimContents);
            kind = "number";
        } catch(Exception e){
            if(trimContents.charAt(0) == '=') {
                kind = "formula";
            } else {
                kind = "string";
            }
        }
        return new Cell(kind);
    }
    
    // Return the kind of the cell which is one of "string", "number",
    // or "formula".
    public String kind() {
        return kind;
    }
    
    // Returns whether the cell is currently in an error state. Cells
    // with kind() "string" and "number" are never in error.  Formula
    // cells are in error and show ERROR if their formula involves cells
    // which are blank or have kind "string" and therefore cannot be
    // used to calculate the value of the cell.
    public boolean isError() {
        return false;
    }
    
    // Produce a string to display the contents of the cell.  For kind()
    // "string" and "number", this method returns the original contents
    // of the cell.  For formula cells which are in error, return the
    // string "ERROR".  Formula cells which are not in error return a
    // string of their numeric value with 1 decimal digit of accuracy
    // which is easiest to produce with the String.format() method.
    //
    // Target Complexity: O(1)
    // Avoid repeated formula evaluation by traversing the formula tree
    // only in updateFormulaValue()
    public String displayString() {
        return null;
    }
    
    // Return the numeric value of this cell.  If the cell is kind
    // "number", this is the double value of its contents.  For kind
    // "formula", it is the evaluated value of the formula.  For kind
    // "string" return null.
    //
    // Target Complexity: O(1)
    // Avoid repeated formula evaluation by traversing the formula tree
    // only in updateFormulaValue()
    public Double numberValue() {
        return null;
    }
    
    // Return the raw contents of the cell. For kind() "number" and
    // "string", this is the original contents entered into the cell.
    // For kind() "formula", this is the text of the formula.
    //
    // Target Complexity: O(1)
    public String contents() {
        return null;   
    }
    
    // Update the value of the cell value. If the cell is not a formula
    // (string and number), do nothing.  Formulas should re-evaluate the
    // stored formula tree to determine a numeric value.  This method
    // may be called when the cell is initially created to give it a
    // numeric value in which case an empty map should be used.
    // Whenever an upstream cell changes value, the housing spreadsheet
    // will call this method to recompute the numeric value to reflect
    // the change.  This method should not raise any exceptions if there
    // are problems evaluating the formula due to other unusable cells.
    // It should set the state of this cell to be in error so that a
    // call to isError() returns true.  If the cell formula is
    // successfully evaluated, isError() should return false.
    //
    // Target Complexity: 
    //   O(1) for "number" and "string" cells
    //   O(T) for "formula" nodes where T is the number of nodes in the
    //        formula tree
    public void updateValue(Map<String,Cell> cellMap) {
        
    }
    
    // A simple class to reflect problems evaluating a formula tree.
    public static class EvalFormulaException extends RuntimeException{
        
        public EvalFormulaException(String msg) {
            
        }
        
    }
    
    // Recursively evaluate the formula tree rooted at the given
    // node. Return the computed value.  Use the given map to retrieve
    // the number value of cells which appear in the formula.  If any
    // cell ID in the formula is unusable (blank, error, string), this
    // method raises an EvalFormulaException.  
    // 
    // This method is public and static to allow for testing independent
    // of any individual cell but should be used in the
    // updateFormulaValue() method to allow individual cells to compute
    // their formula values.
    //
    // Inspect the FNode and TokenType classes to gain insight into what
    // information is available in FNodes to inspect during the
    // post-order traversal for evaluation.
    // 
    // Target Complexity: O(T) 
    //   T: the number of nodes in the formula tree
    public static Double evalFormulaTree(FNode node, Map<String,Cell> cellMap) {
        return null;
    }
    
    // Return a set of upstream cells from this cell. Cells of kind
    // "string" and "number" return an empty set.  Formula cells are
    // dependent on the contents of any cell whose ID appears in the
    // formula and returns all such ids in a set.  For formula cells,
    // this method should call a recursive helper method to traverse the
    // formula tree and accumulate a set of ids in the formula tree.
    // 
    // Target Complexity: O(1)
    // Avoid repeated formula evaluation by traversing the formula tree
    // only in updateFormulaValue()
    public Set<String> getUpstreamIDs() {
        return null;
    }
    
}