import java.util.Map;
import java.util.Set;
import java.util.HashSet;

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
 
    private String kind; // the kind of the cell
    private boolean isError; // to track whether cell is in Error state
    private Double numberValue; // numeric  value for cell of number and formula kind 
    // Display the orginal contents for string kind, 
    // numeric value of 1 decimal point of accuracy
    // for number and formula kind
    private String displayString; 
    private String contents; // Original contents
    private FNode treeRoot;  // Root for a formula kind cell
    private Set<String> upstreamIDs; // a set of upstreamIDs for formula kind cell
    
    // private constrctor to create a Cell object
    private Cell(String kind, boolean isError, Double numberValue, 
                 String displayString, String contents, FNode treeRoot) {
        this.kind = kind;
        this.isError = isError;
        this.numberValue = numberValue;
        this.displayString = displayString;
        this.contents = contents;
        this.treeRoot = treeRoot;
        this.upstreamIDs = new HashSet<>();
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
        // If the contents is null or
        // empty, return null.
        if(contents == null) {
            return null;
        }
        String trimContents = contents.trim();
        if(trimContents.equals("")) {
            return null;
        }
        
        // variables for passing to Cell constructor
        String kind = "";
        String displayString = "";
        boolean isError = false;
        Double numberValue = null;
        FNode treeRoot = null;
        try {
            // if Double.parseDouble(trimContents) does not raise exception,
            // it is a numberCell
            // numberCells are never in Error
            double value = Double.parseDouble(trimContents);
            kind = "number";
            numberValue = new Double(value);
            displayString = String.format("%.1f", numberValue);
        } catch(Exception e){
            // if Double.parseDouble(trimContents) raise exception,
            // if first char is =, it is formulaCell
            // untill updateValue of this formulaCell
            // it is in Error state
            if(trimContents.charAt(0) == '=') {
                kind = "formula";
                isError = true;
                displayString = "ERROR";
                treeRoot = FNode.parseFormulaString(trimContents);
            } else {
                // else it is stringCell
                // stringCells are never in Error
                kind = "string";
                displayString = trimContents;
            }
        }
        return new Cell(kind, isError, numberValue,
                        displayString, trimContents, treeRoot);
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
        return isError;
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
        return displayString;
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
        return numberValue;
    }
    
    // Return the raw contents of the cell. For kind() "number" and
    // "string", this is the original contents entered into the cell.
    // For kind() "formula", this is the text of the formula.
    //
    // Target Complexity: O(1)
    public String contents() {
        return contents;   
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
        try {
            if(kind.equals("formula")) {
                // if it is a formulaCell, evaluate the cell
                // and if there is no exception during 
                // evalFormulaTree, it is no more in Error state
                numberValue = evalFormulaTree(treeRoot, cellMap);
                displayString = String.format("%.1f", numberValue);
                isError = false;
            }
            // else it is numberCell or stringCell, 
            // do nothing
        } catch(EvalFormulaException e) {
            // if exception occur during evalFormulaTree
            // it is in Error state
            numberValue = null;
            displayString = "ERROR";
            isError = true;
        }
    }
    
    // A simple class to reflect problems evaluating a formula tree.
    public static class EvalFormulaException extends RuntimeException{
        
        public EvalFormulaException(String msg) {
            super(msg);
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
        if(node == null) {
            // If the node is null,
            // no value is going to be added, so add 0
            return 0.0;
        }
        if(node.type.equals(TokenType.Plus)) {
            // if the node is type +, return the value of left child + the value of right child
            return evalFormulaTree(node.left, cellMap) + evalFormulaTree(node.right, cellMap);
        } else if(node.type.equals(TokenType.Minus)) {
            // if the node is type -, return the value of left child - the value of right child
            return evalFormulaTree(node.left, cellMap) - evalFormulaTree(node.right, cellMap);
        } else if(node.type.equals(TokenType.Multiply)) {
            // if the node is type *, return the value of left child * the value of right child
            return evalFormulaTree(node.left, cellMap) * evalFormulaTree(node.right, cellMap);
        } else if(node.type.equals(TokenType.Divide)) {
            // if the node is type /, return the value of left child / the value of right child
            return evalFormulaTree(node.left, cellMap) / evalFormulaTree(node.right, cellMap);
        } else if(node.type.equals(TokenType.Negate)) {
            // if the node is type negate, return the negative value of left child 
            return -evalFormulaTree(node.left, cellMap);
        } else if(node.type.equals(TokenType.CellID)) {
            // if the node is type CellID
            // get the cell object from cellMap using the CellID as the key
            Cell cell = cellMap.get(node.data);
            if(cell == null) {
                // if the cell is null, we are not able to evaluate the expression
                // throw EvalFormulaException.
                throw new EvalFormulaException("The cell is blank");
            }
            if(cell.kind().equals("formula") && !cell.isError()) {
                // if the cell is a formulaCell and not in Error state
                // return the value of the cell
                return evalFormulaTree(cell.treeRoot, cellMap);
            } else if(cell.kind().equals("number")) {
                // if it is a numberCell, return its value
                return cell.numberValue();
            } else {
                // the cell is a whether a stringCell or 
                // it is a formulaCell in Error state
                // not able to evaluate the expression
                // throw EvalFormulaException.
                throw new EvalFormulaException("Cell unusable(error, string)");
            }
        } else if(node.type.equals(TokenType.Number)) {
            // if the node is type number, return the value of the node
            return Double.parseDouble(node.data);
        } else {
            // something strange happened
            throw new RuntimeException("Something strange happened"); 
        }
    }
    
    // Return a set of upstream cells from this cell. Cells of kind
    // "string" and "number" return an empty set.  Formula cells are
    // dependent on the contents of any cell whose ID appears in the
    // formula and returns all such ids in a set.  For formula cells,
    // this method should call a recursive helper method to traverse the
    // formula tree and accumulate a set of ids in the formula tree.
    // 
    // Target Complexity: O(T)
    public Set<String> getUpstreamIDs() {
        postOrder(treeRoot); // call helper method to traverse the formula tree
        return upstreamIDs;
    }
    
    // A recursive helper method to traverse the formula tree
    // in postOrder and accumulate a set of ids in the formula tree.
    private void postOrder(FNode node) {
        if(node == null) {
            return;
        }
        postOrder(node.left); // go to left child
        postOrder(node.right); // go to right child
        if(node.type.equals(TokenType.CellID)) {
            // Add to upStreamIDs if the node's data is a cell ID
            upstreamIDs.add(node.data);
        }
    }
}