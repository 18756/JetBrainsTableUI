package jetbrains.table;

import jetbrains.exceptions.FunctionParameterException;
import jetbrains.formulas.graph.FormulaDependencyGraph;
import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.formulas.parser.FormulaParser;
import jetbrains.exceptions.ParserException;
import jetbrains.formulas.parser.LexicalAnalyzer;
import jetbrains.formulas.parser.nodes.TreeNode;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;


/*
selectPreviousRowExtendSelection
selectLastColumn
selectPreviousRowChangeLead
startEditing
addToSelection
extendTo
selectFirstRowExtendSelection
scrollUpChangeSelection
selectFirstColumn
selectFirstColumnExtendSelection
scrollDownExtendSelection
selectLastRow
scrollRightChangeSelection
selectNextColumnCell
cancel
moveSelectionTo
scrollLeftChangeSelection
selectNextRowExtendSelection
selectNextColumnChangeLead
selectFirstRow
selectPreviousColumnChangeLead
selectNextRowChangeLead
scrollLeftExtendSelection
selectNextColumn
copy
scrollDownChangeSelection
selectLastColumnExtendSelection
selectPreviousColumnCell
selectNextRowCell
focusHeader
clearSelection
cut
selectLastRowExtendSelection
selectPreviousColumn
scrollUpExtendSelection
selectPreviousRowCell
toggleAndAnchor
selectAll
paste
selectPreviousRow
selectPreviousColumnExtendSelection
scrollRightExtendSelection
selectNextColumnExtendSelection
selectNextRow
*/

public class ExcelTable extends JTable {
    //    my TableModel
    private JTextField textFieldToSynchronize;
    private boolean isEditFromTextFieldSynchronize;
    private final CellElement[][] tableCells;
    private final FormulaDependencyGraph formulaDependencyGraph = new FormulaDependencyGraph();

    private CopyCellInfo copyCellInfo;
    private CellPosition lastEditCellPosition;
    private CellPosition selectedCellPosition;

    private final Stack<UpdateAction> updateActionsToBackUp = new Stack<>();

    public ExcelTable(String[][] tableData, String[] columnHeader) {
        super(tableData, columnHeader);
        tableCells = new CellElement[getRowCount()][getColumnCount()];
        for (int i = 0; i < tableData.length; i++) {
            for (int j = 1; j < tableData[i].length; j++) {
                tableCells[i][j - 1] = new CellElement(new CellPosition(i, j), tableData[i][j]);
            }
        }

        getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CellElement cell = tableCells[getSelectedRow()][getSelectedColumn() - 1];
                copyCellInfo = new CopyCellInfo(cell.text, cell.cellPosition, cell.isValidFormula());
                System.out.println("Copied text: " + cell.text);
            }
        });

        getActionMap().put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (copyCellInfo != null) {
                    String textToPaste = copyCellInfo.textToCopy;
                    if (copyCellInfo.isValidFormula) {
                        CellPosition pasteCell = tableCells[getSelectedRow()][getSelectedColumn() - 1].cellPosition;
                        textToPaste = LexicalAnalyzer.getFormulaWithShiftedCells(copyCellInfo.textToCopy, copyCellInfo.cellCopyFrom, pasteCell);
                    }
                    tableCells[getSelectedRow()][getSelectedColumn() - 1].updateText(textToPaste, true);
                    System.out.println("Pasted text: " + textToPaste);
                }
            }
        });

        getActionMap().put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!updateActionsToBackUp.isEmpty()) {
                    UpdateAction updateAction = updateActionsToBackUp.pop();
                    CellPosition cellPositionToBackUp = updateAction.cellPosition;
                    tableCells[cellPositionToBackUp.row][cellPositionToBackUp.column - 1]
                            .updateText(updateAction.oldText, false);
                }
                System.out.println("Undo");
            }
        });

        getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");

//        addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//                System.out.println("Tttyped");
//                updateTextFieldToSynchronize(
//                        (String) getValueAt(selectedCellPosition.row, selectedCellPosition.column) +
//                                (e.getKeyChar() != '\b' ? e.getKeyChar() : "")
//                );
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {}
//
//            @Override
//            public void keyReleased(KeyEvent e) {}
//        });
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject eventObject) {
        if (isCellEditable(row, column)) {
            System.out.println("POOR EDITING " + eventObject.toString());
            if (selectedCellPosition != null && isEditFromTextFieldSynchronize) {
                isEditFromTextFieldSynchronize = false;
                String text = (String) getValueAt(selectedCellPosition.row, selectedCellPosition.column);
                tableCells[selectedCellPosition.row][selectedCellPosition.column - 1].updateText(text, true);
            }
            selectedCellPosition = tableCells[row][column - 1].cellPosition;
            if ((eventObject instanceof MouseEvent && ((MouseEvent) eventObject).getClickCount() == 2) ||
                    (eventObject instanceof KeyEvent && !((KeyEvent) eventObject).isActionKey())) {
                System.out.println("START EDITING " + row + " " + column);
                setValueAt(tableCells[row][column - 1].text, row, column);
                updateTextFieldToSynchronize(tableCells[row][column - 1].text);
                lastEditCellPosition = new CellPosition(row, column);
            } else {
                updateTextFieldToSynchronize(tableCells[row][column - 1].getTextToSyncTextField());
            }
        }
        return super.editCellAt(row, column, eventObject);
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);
        System.out.println("STOP EDITING " + e.getSource().toString());
        int row = lastEditCellPosition.row;
        int column = lastEditCellPosition.column;
        String newText = (String) getValueAt(row, column);
        tableCells[row][column - 1].updateText(newText, true);
        updateTextFieldToSynchronize(tableCells[row][column - 1].getTextToSyncTextField());
    }



    public String getTextAt(int row, int column) {
        return tableCells[row][column].text;
    }

    public void setTextAt(int row, int column, String text) {
        tableCells[row][column].updateText(text, false);
    }

    public Integer getRowBorderId(int x, int y) {
        int offset = 7;
        y += offset;
        int eps = 2;
        if (x > getColumn(getColumnName(0)).getWidth()) {
            System.out.println("Out of first column");
            return null;
        }
        int height = 0;
        int rowBorderId = 0;
        while (height < y - eps) {
            height += getRowHeight(rowBorderId);
            rowBorderId++;
        }
        if (y - eps <= height && height <= y + eps && rowBorderId != 0) {
            return rowBorderId;
        }
        return null;
    }

    public void setTextFieldToSynchronize(JTextField textFieldToSynchronize) {
        this.textFieldToSynchronize = textFieldToSynchronize;
    }

    private void updateTextFieldToSynchronize(String text) {
        if (textFieldToSynchronize != null) {
            textFieldToSynchronize.setText(text);
        }
    }

    public void setTextToSelectedCell(String text) {
        if (selectedCellPosition != null) {
            isEditFromTextFieldSynchronize = true;
            setValueAt(text, selectedCellPosition.row, selectedCellPosition.column);
        }
    }

    public String getSelectedCellText() {
        if (selectedCellPosition != null) {
            return tableCells[selectedCellPosition.row][selectedCellPosition.column - 1].text;
        }
        return "";
    }

    private class CellElement {
        CellPosition cellPosition;
        String text;
        TreeNode treeNode;
        Double formulaValue;
        String errorMessage;

        public CellElement(CellPosition cellPosition, String text) {
            this.cellPosition = cellPosition;
            updateText(text, false);
        }

        public void updateText(String text, boolean isSaveToBackUp) {
            if (isSaveToBackUp && !Objects.equals(this.text, text)) {
                updateActionsToBackUp.add(new UpdateAction(cellPosition, this.text));
            }
            this.text = text;
            formulaValue = null;
            errorMessage = null;
            formulaDependencyGraph.removeIncomingEdges(cellPosition);
            try {
                treeNode = FormulaParser.parse(text);
                Set<CellPosition> cellPositionsInFormula = new HashSet<>();
                treeNode.addAllCellPositions(cellPositionsInFormula);
                cellPositionsInFormula
                        .forEach(fromCellPosition -> formulaDependencyGraph.addEdge(fromCellPosition, cellPosition));
            } catch (ParserException e) {
                errorMessage = e.getMessage();
            }

            try {
                List<CellPosition> calculateOrder = formulaDependencyGraph.getCalculateOrder(cellPosition);
                if (errorMessage != null) {
                    calculateOrder = calculateOrder.subList(1, calculateOrder.size());
                }
                calculateOrder.forEach(cell -> tableCells[cell.row][cell.column - 1].recalculateFormulaValue());
            } catch (FormulaCalculatorException e) {
                errorMessage = e.getMessage();
            }
            setValueAt(getTextToCell(), cellPosition.row, cellPosition.column);
            updateTextFieldToSynchronize(getTextToSyncTextField());
        }

        public void recalculateFormulaValue() {
            BiFunction<Integer, Integer, Double> tableValuesFunction = (row, column) -> tableCells[row][column - 1].getValue();
            try {
                formulaValue = (Double) treeNode.calculate(tableValuesFunction);
            } catch (FunctionParameterException | FormulaCalculatorException e) {
                errorMessage = e.getMessage();
            }
            setValueAt(getTextToCell(), cellPosition.row, cellPosition.column);
        }

        public double getValue() {
            return formulaValue == null ? 0.0 : formulaValue;
        }

        public boolean isValidFormula() {
            return treeNode != null;
        }

        public boolean isInvalidFormula() {
            return text.trim().startsWith("=") && errorMessage != null;
        }

        public String getTextToCell() {
            if (formulaValue != null) {
                return formulaValue + "";
            }
            return getTextToSyncTextField();
        }

        public String getTextToSyncTextField() {
            if (isInvalidFormula()) {
                return errorMessage;
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

        @Override
        public int hashCode() {
            return Objects.hash(row, column);
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

    public static class CopyCellInfo {
        String textToCopy;
        CellPosition cellCopyFrom;
        boolean isValidFormula;

        public CopyCellInfo(String textToCopy, CellPosition cellCopyFrom, boolean isValidFormula) {
            this.textToCopy = textToCopy;
            this.cellCopyFrom = cellCopyFrom;
            this.isValidFormula = isValidFormula;
        }
    }

    public static class UpdateAction {
        CellPosition cellPosition;
        String oldText;

        public UpdateAction(CellPosition cellPosition, String oldText) {
            this.cellPosition = cellPosition;
            this.oldText = oldText;
        }
    }
}
