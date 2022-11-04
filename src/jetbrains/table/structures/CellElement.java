package jetbrains.table.structures;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.exceptions.FunctionParameterException;
import jetbrains.exceptions.ParserException;
import jetbrains.formulas.parser.FormulaParser;
import jetbrains.formulas.parser.nodes.TreeNode;
import jetbrains.table.ExcelTable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class CellElement {
    ExcelTable table;
    public CellPosition cellPosition;
    public String text;
    TreeNode treeNode;
    Double formulaValue;
    String errorMessage;

    public CellElement(CellPosition cellPosition, String text, ExcelTable table) {
        this.cellPosition = cellPosition;
        this.table = table;
        updateText(text, false);
    }

    public void updateText(String text,
                           boolean isSaveToBackUp) {
        if (isSaveToBackUp && !Objects.equals(this.text, text)) {
            table.getUpdateActionsToBackUp().add(new UpdateAction(cellPosition, this.text));
        }
        this.text = text;
        formulaValue = null;
        errorMessage = null;
        table.getFormulaDependencyGraph().removeIncomingEdges(cellPosition);
        try {
            treeNode = FormulaParser.parse(text);
            Set<CellPosition> cellPositionsInFormula = new HashSet<>();
            treeNode.addAllCellPositions(cellPositionsInFormula);
            cellPositionsInFormula
                    .forEach(fromCellPosition -> table.getFormulaDependencyGraph().addEdge(fromCellPosition, cellPosition));
        } catch (ParserException e) {
            errorMessage = e.getMessage();
        }

        try {
            List<CellPosition> calculateOrder = table.getFormulaDependencyGraph().getCalculateOrder(cellPosition);
            if (errorMessage != null) {
                calculateOrder = calculateOrder.subList(1, calculateOrder.size());
            }
            calculateOrder.forEach(cell -> table.getCellElement(cell.row, cell.column - 1).recalculateFormulaValue());
        } catch (FormulaCalculatorException e) {
            errorMessage = e.getMessage();
        }
        table.setValueAt(getTextToCell(), cellPosition.row, cellPosition.column);
        table.updateTextFieldToSynchronize(getTextToSyncTextField());
    }

    public void recalculateFormulaValue() {
        BiFunction<Integer, Integer, Double> tableValuesFunction = (row, column) -> table.getCellElement(row, column - 1).getValue();
        try {
            formulaValue = (Double) treeNode.calculate(tableValuesFunction);
        } catch (FunctionParameterException | FormulaCalculatorException e) {
            errorMessage = e.getMessage();
        }
        table.setValueAt(getTextToCell(), cellPosition.row, cellPosition.column);
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

