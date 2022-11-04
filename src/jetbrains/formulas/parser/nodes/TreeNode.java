package jetbrains.formulas.parser.nodes;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.exceptions.FunctionParameterException;
import jetbrains.formulas.calculator.FormulaCalculator;
import jetbrains.table.structures.CellPosition;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class TreeNode {
    public final List<TreeNode> children;
    public final FormulaCalculator formulaCalculator;

    public TreeNode(List<TreeNode> children) {
        this(children, null);
    }

    public TreeNode(List<TreeNode> children, FormulaCalculator formulaCalculator) {
        this.children = children;
        this.formulaCalculator = formulaCalculator;
    }

    public Object calculate(BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException, FormulaCalculatorException {
        return formulaCalculator.calculate(this, tableValuesFunction);
    }

    public void addAllCellPositions(Set<CellPosition> cellPositions) {
        for (TreeNode child : children) {
            child.addAllCellPositions(cellPositions);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TreeNode treeNode = (TreeNode) o;
        return Objects.equals(children, treeNode.children);
    }
}
