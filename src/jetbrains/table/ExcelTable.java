package jetbrains.table;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

public class ExcelTable extends JTable {
//    my TableModel
    CellElement[][] tableCells;
    CellPosition lastEditCellPosition;
    List<Object> dependencyLinks = new ArrayList<>();

    public ExcelTable(String[][] tableData, String[] columnHeader) {
        super(tableData, columnHeader);
        tableCells = new CellElement[getRowCount()][getColumnCount()];
        for (int i = 0; i < tableData.length; i++) {
            for (int j = 1; j < tableData[i].length; j++) {
                tableCells[i][j - 1] = new CellElement(tableData[i][j]);
            }
        }

        this.getCellEditor(0, 0).addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                System.out.println("STOP EDITING " + e.getSource().toString());
                int row = lastEditCellPosition.row;
                int column = lastEditCellPosition.column;
                String newText = (String) getValueAt(row, column);
                tableCells[row][column - 1].updateText(newText);
                setValueAt("2", row, column);
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject eventObject) {
        if (isCellEditable(row, column) && eventObject instanceof MouseEvent && ((MouseEvent) eventObject).getClickCount() == 2) {
            System.out.println("START EDITING " + row + " " + column);
            setValueAt("1", row, column);
            lastEditCellPosition = new CellPosition(row, column);
        }
        return super.editCellAt(row, column, eventObject);
    }

    private static class CellElement {
        String text;
//        change to formula class param
        Double formulaValue;
        CellType cellType;

        public CellElement(String text) {
            updateText(text);
        }

        public void updateText(String text) {
            this.text = text;
//            parse text and setUp formulaValue and cellType
//            add edges to graph
        }

        public void recalculateFormulaValue() {

        }

        public static enum CellType {
            NOT_FORMULA,
            VALID_FORMULA,
            INVALID_FORMULA
        }
    }

    public static class CellPosition {
        int row;
        int column;

        public CellPosition(int row, int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellPosition that = (CellPosition) o;
            return row == that.row && column == that.column;
        }
    }

    public static class CellDiapason {
        CellPosition fromCellPosition;
        CellPosition toCellPosition;

        public CellDiapason(CellPosition fromCellPosition, CellPosition toCellPosition) {
            this.fromCellPosition = fromCellPosition;
            this.toCellPosition = toCellPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CellDiapason diapason = (CellDiapason) o;
            return Objects.equals(fromCellPosition, diapason.fromCellPosition) && Objects.equals(toCellPosition, diapason.toCellPosition);
        }
    }
}
