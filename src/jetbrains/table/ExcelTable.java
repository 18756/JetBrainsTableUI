package jetbrains.table;

import jetbrains.formulas.graph.FormulaDependencyGraph;
import jetbrains.formulas.parser.LexicalAnalyzer;
import jetbrains.table.structures.CellElement;
import jetbrains.table.structures.CellPosition;
import jetbrains.table.structures.CopyCellInfo;
import jetbrains.table.structures.UpdateAction;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Stack;

public class ExcelTable extends JTable {
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
                tableCells[i][j - 1] = new CellElement(new CellPosition(i, j), tableData[i][j], this);
            }
        }

        getActionMap().put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isCellEditable(getSelectedRow(), getSelectedColumn())) {
                    CellElement cell = tableCells[getSelectedRow()][getSelectedColumn() - 1];
                    copyCellInfo = new CopyCellInfo(cell.text, cell.cellPosition, cell.isValidFormula());
                    System.out.println("Copied text: " + cell.text);
                }
            }
        });

        getActionMap().put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (copyCellInfo != null && isCellEditable(getSelectedRow(), getSelectedColumn())) {
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
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != 0;
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject eventObject) {
        if (isCellEditable(row, column)) {
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

    public void updateTextFieldToSynchronize(String text) {
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

    public FormulaDependencyGraph getFormulaDependencyGraph() {
        return formulaDependencyGraph;
    }

    public Stack<UpdateAction> getUpdateActionsToBackUp() {
        return updateActionsToBackUp;
    }

    public CellElement getCellElement(int row, int column) {
        return tableCells[row][column];
    }
}
