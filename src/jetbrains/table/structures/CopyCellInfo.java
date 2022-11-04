package jetbrains.table.structures;

public class CopyCellInfo {
    public String textToCopy;
    public CellPosition cellCopyFrom;
    public boolean isValidFormula;

    public CopyCellInfo(String textToCopy, CellPosition cellCopyFrom, boolean isValidFormula) {
        this.textToCopy = textToCopy;
        this.cellCopyFrom = cellCopyFrom;
        this.isValidFormula = isValidFormula;
    }
}

