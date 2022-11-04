package jetbrains.table.structures;

public class UpdateAction {
    public CellPosition cellPosition;
    public String oldText;

    public UpdateAction(CellPosition cellPosition, String oldText) {
        this.cellPosition = cellPosition;
        this.oldText = oldText;
    }
}
