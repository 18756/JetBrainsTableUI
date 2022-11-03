package jetbrains.formulas.calculator;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.exceptions.FunctionParameterException;
import jetbrains.formulas.calculator.functions.FunctionRepository;
import jetbrains.formulas.parser.nodes.TerminalNode;
import jetbrains.formulas.parser.nodes.TreeNode;
import jetbrains.table.ExcelTable;
import jetbrains.table.TableGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.*;

public enum FormulaCalculator {
    SUM {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException, FormulaCalculatorException {
            double sum = 0;
            int k = 1;
            for (TreeNode child : treeNode.children) {
                if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == MINUS) {
                    k = -1;
                } else {
                    double childValue = (double) child.calculate(tableValuesFunction);
                    sum += k * childValue;
                    k = 1;
                }
            }
            return sum;
        }
    },
    PRODUCT {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException, FormulaCalculatorException {
            double product = 1;
            boolean isMul = true;
            for (TreeNode child : treeNode.children) {
                if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == DIV) {
                    isMul = false;
                } else if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == MUL) {
                    isMul = true;
                } else {
                    double childValue = (double) child.calculate(tableValuesFunction);
                    if (isMul) {
                        product *= childValue;
                    } else {
                        product /= childValue;
                    }
                }
            }
            return product;
        }
    },
    FUNCTION {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException, FormulaCalculatorException {
            String functionName = (String) ((TerminalNode) treeNode.children.get(0)).token.data;
            TreeNode paramsNode = treeNode.children.get(1);
            List<Object> paramValues = new ArrayList<>();
            for (TreeNode paramNode : paramsNode.children) {
                Object paramValue = paramNode.calculate(tableValuesFunction);
                paramValues.add(paramValue);
            }
            FunctionRepository.FunctionWithParameterLimits functionWithParameterLimits =
                    FunctionRepository.getFunctionWithParamLimits(functionName);
            functionWithParameterLimits.checkParams(paramValues);
            return functionWithParameterLimits.function.apply(paramValues);
        }
    },
    NUMBER {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
            return ((TerminalNode) treeNode).token.data;
        }
    },
    CELL {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FormulaCalculatorException {
            ExcelTable.CellPosition cellPosition = (ExcelTable.CellPosition) ((TerminalNode) treeNode).token.data;
            return getCellValue(cellPosition.row, cellPosition.column, tableValuesFunction);
        }
    },
    CELL_DIAPASON {
        @Override
        public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FormulaCalculatorException {
            ExcelTable.CellDiapason cellDiapason = (ExcelTable.CellDiapason) ((TerminalNode) treeNode).token.data;
            int rows = cellDiapason.toCellPosition.row - cellDiapason.fromCellPosition.row + 1;
            int columns = cellDiapason.toCellPosition.column - cellDiapason.fromCellPosition.column + 1;
            int minRow = cellDiapason.fromCellPosition.row;
            int minColumn = cellDiapason.fromCellPosition.column;
            int maxRow = cellDiapason.toCellPosition.row;
            int maxColumn = cellDiapason.toCellPosition.column;

            double[][] cellDiapasonValues = new double[rows][columns];
            for (int row = minRow; row <= maxRow; row++) {
                for (int column = minColumn; column <= maxColumn; column++) {
                    double cellValue = getCellValue(row, column, tableValuesFunction);
                    int diapasonRow = row - minRow;
                    int diapasonColumn = column - minColumn;
                    cellDiapasonValues[diapasonRow][diapasonColumn] = cellValue;
                }
            }
            return cellDiapasonValues;
        }
    };

    public double getCellValue(int row, int column, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FormulaCalculatorException {
        try {
            return tableValuesFunction.apply(row, column);
        } catch (Exception e) {
            String cellName = TableGenerator.getColumnNameById(column - 1) + (row + 1);
            throw new FormulaCalculatorException("Cell " + cellName + " is outside the table.");
        }
    }

    abstract public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException, FormulaCalculatorException;
}
