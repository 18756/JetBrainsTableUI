package jetbrains.formulas.parser.nodes;

import jetbrains.formulas.calculator.FormulaCalculator;
import jetbrains.formulas.parser.LexicalAnalyzer;
import jetbrains.table.structures.CellDiapason;
import jetbrains.table.structures.CellPosition;

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

    public void addAllCellPositions(Set<CellPosition> cellPositions) {
        if (token.tokenType == CELL_POSITION) {
            cellPositions.add((CellPosition) token.data);
        } else if (token.tokenType == CELL_DIAPASON) {
            CellDiapason cellDiapason = (CellDiapason) token.data;
            CellPosition fromCellPosition = cellDiapason.fromCellPosition;
            CellPosition toCellPosition = cellDiapason.toCellPosition;
            for (int row = fromCellPosition.row; row <= toCellPosition.row; row++) {
                for (int column = fromCellPosition.column; column <= toCellPosition.column; column++) {
                    cellPositions.add(new CellPosition(row, column));
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
