package jetbrains.formulas.parser.nodes;

import jetbrains.formulas.calculator.FormulaCalculator;
import jetbrains.formulas.parser.LexicalAnalyzer;
import jetbrains.table.ExcelTable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.CELL_DIAPASON;
import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.CELL_POSITION;

public class TerminalNode extends TreeNode {
    public final LexicalAnalyzer.Token token;

    public TerminalNode(LexicalAnalyzer.Token token) {
        this(token, null);
    }

    public TerminalNode(LexicalAnalyzer.Token token, FormulaCalculator formulaCalculator) {
        super(List.of(), formulaCalculator);
        this.token = token;
    }

    public void addAllCellPositions(Set<ExcelTable.CellPosition> cellPositions) {
        if (token.tokenType == CELL_POSITION) {
            cellPositions.add((ExcelTable.CellPosition) token.data);
        } else if (token.tokenType == CELL_DIAPASON) {
            ExcelTable.CellDiapason cellDiapason = (ExcelTable.CellDiapason) token.data;
            ExcelTable.CellPosition fromCellPosition = cellDiapason.fromCellPosition;
            ExcelTable.CellPosition toCellPosition = cellDiapason.toCellPosition;
            for (int row = fromCellPosition.row; row <= toCellPosition.row; row++) {
                for (int column = fromCellPosition.column; column <= toCellPosition.column; column++) {
                    cellPositions.add(new ExcelTable.CellPosition(row, column));
                }
            }
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TerminalNode that = (TerminalNode) o;
        return Objects.equals(token, that.token);
    }
}
