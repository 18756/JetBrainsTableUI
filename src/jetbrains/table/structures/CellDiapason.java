package jetbrains.table.structures;

import java.util.Objects;

public class CellDiapason {
    public CellPosition fromCellPosition;
    public CellPosition toCellPosition;

    public CellDiapason(CellPosition fromCellPosition, CellPosition toCellPosition) {
        int minRow = Math.min(fromCellPosition.row, toCellPosition.row);
        int maxRow = Math.max(fromCellPosition.row, toCellPosition.row);
        int minColumn = Math.min(fromCellPosition.column, toCellPosition.column);
        int maxColumn = Math.max(fromCellPosition.column, toCellPosition.column);

        this.fromCellPosition = new CellPosition(minRow, minColumn);
        this.toCellPosition = new CellPosition(maxRow, maxColumn);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CellDiapason diapason = (CellDiapason) o;
        return Objects.equals(fromCellPosition, diapason.fromCellPosition) && Objects.equals(toCellPosition, diapason.toCellPosition);
    }
}

