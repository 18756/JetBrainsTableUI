package jetbrains.table;

import jetbrains.parser.FormulaParser;
import jetbrains.parser.ParserException;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

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
                String textToShow = tableCells[row][column - 1].getTextToShow();
                setValueAt(textToShow, row, column);
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
            setValueAt(tableCells[row][column - 1].text, row, column);
            lastEditCellPosition = new CellPosition(row, column);
        }
        return super.editCellAt(row, column, eventObject);
    }

    private class CellElement {
        String text;
//        change to formula class param
        FormulaParser.TreeNode treeNode;
        Double formulaValue;
        String errorMessage;

        public CellElement(String text) {
            updateText(text);
        }

        public void updateText(String text) {
            this.text = text;
            formulaValue = null;
            errorMessage = null;
            try {
                treeNode = FormulaParser.parse(text);
                recalculateFormulaValue();
            } catch (ParserException e) {
                errorMessage = e.getMessage();
            }
//            parse text and setUp formulaValue and cellType
//            add edges to graph
        }

        public void recalculateFormulaValue() {
            BiFunction<Integer, Integer, Double> tableValuesFunction = (row, column) -> tableCells[row][column - 1].getValue();
            formulaValue = (Double) treeNode.calculate(tableValuesFunction);
        }

        public double getValue() {
            return formulaValue == null ? 0.0 : formulaValue;
        }

        public String getTextToShow() {
            if (errorMessage != null) {
                return errorMessage;
            } else if (formulaValue != null) {
                return formulaValue + "";
            }
            return text;
        }
    }

    public static class CellPosition {
        public int row;
        public int column;

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
}
