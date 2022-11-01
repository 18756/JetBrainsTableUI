package jetbrains.graph;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.table.ExcelTable;

import java.util.*;

public class FormulaDependencyGraph {
    private int dfsRuns = 0;
    private final Map<ExcelTable.CellPosition, Vertex> cellPositionToVertex = new HashMap<>();

    public void addEdge(ExcelTable.CellPosition fromCellPosition, ExcelTable.CellPosition toCellPosition) {
        Vertex fromVertex = getVertex(fromCellPosition);
        Vertex toVertex = getVertex(toCellPosition);
        fromVertex.outgoingEdges.add(toVertex);
        toVertex.incomingEdges.add(fromVertex);
    }

    public void removeIncomingEdges(ExcelTable.CellPosition cellPosition) {
        Vertex vertex = getVertex(cellPosition);
        for (Vertex fromVertex : vertex.incomingEdges) {
            fromVertex.outgoingEdges.remove(vertex);
            tryRemoveVertex(fromVertex);
        }
        vertex.incomingEdges.clear();
        tryRemoveVertex(vertex);
    }

    public List<ExcelTable.CellPosition> getCalculateOrder(ExcelTable.CellPosition startCellPosition) throws FormulaCalculatorException {
        Vertex startVertex = getVertex(startCellPosition);
        List<ExcelTable.CellPosition> calculateOrder = new ArrayList<>();
        dfsRuns++;
        dfs(startVertex, calculateOrder);
        tryRemoveVertex(startVertex);
        Collections.reverse(calculateOrder);
        return calculateOrder;
    }

    private void dfs(Vertex vertex, List<ExcelTable.CellPosition> calculateOrder) throws FormulaCalculatorException {
        vertex.dfsColor = getGrayColorId();
        for (Vertex toVertex : vertex.outgoingEdges) {
            if (toVertex.dfsColor == getGrayColorId()) {
                throw new FormulaCalculatorException("Cyclic dependency was found.");
            } else if (toVertex.dfsColor != getBlackColorId()) {
                dfs(toVertex, calculateOrder);
            }
        }
        vertex.dfsColor = getBlackColorId();
        calculateOrder.add(vertex.cellPosition);
    }

    private Vertex getVertex(ExcelTable.CellPosition cellPosition) {
        if (!cellPositionToVertex.containsKey(cellPosition)) {
            cellPositionToVertex.put(cellPosition, new Vertex(cellPosition));
        }
        return cellPositionToVertex.get(cellPosition);
    }

    private void tryRemoveVertex(Vertex vertex) {
        if (vertex.outgoingEdges.isEmpty() && vertex.incomingEdges.isEmpty()) {
            cellPositionToVertex.remove(vertex.cellPosition);
        }
    }

    private int getGrayColorId() {
        return 2 * dfsRuns + 1;
    }

    private int getBlackColorId() {
        return 2 * dfsRuns + 2;
    }

    private static class Vertex {
        int dfsColor = 0;
        ExcelTable.CellPosition cellPosition;
        Set<Vertex> outgoingEdges = new HashSet<>();
        Set<Vertex> incomingEdges = new HashSet<>();

        public Vertex(ExcelTable.CellPosition cellPosition) {
            this.cellPosition = cellPosition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Vertex vertex = (Vertex) o;
            return Objects.equals(cellPosition, vertex.cellPosition);
        }

        @Override
        public int hashCode() {
            return Objects.hash(cellPosition);
        }
    }
}
