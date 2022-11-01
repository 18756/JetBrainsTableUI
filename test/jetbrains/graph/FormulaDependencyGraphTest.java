package jetbrains.graph;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.table.ExcelTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import jetbrains.table.ExcelTable.*;

class FormulaDependencyGraphTest {
    private final static CellPosition CELL1 = new CellPosition(0, 0);
    private final static CellPosition CELL2 = new CellPosition(1, 1);
    private final static CellPosition CELL3 = new CellPosition(2, 2);
    private final static CellPosition CELL4 = new CellPosition(3, 3);

    private static FormulaDependencyGraph formulaDependencyGraph;

    @BeforeEach
    void setUp() {
        formulaDependencyGraph = new FormulaDependencyGraph();
    }

    private static Stream<Arguments> getCalculateOrderSource() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        List.of(CELL1)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2)
                        ),
                        List.of(CELL1, CELL2)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3)
                        ),
                        List.of(CELL1, CELL2, CELL3)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL1, CELL2)
                        ),
                        List.of(CELL1, CELL2, CELL3)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL1, CELL3)
                        ),
                        List.of(CELL1, CELL2, CELL3)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL1, CELL3),
                                addEdgeConsumer(CELL2, CELL4),
                                addEdgeConsumer(CELL3, CELL4)
                        ),
                        List.of(CELL1, CELL2, CELL3, CELL4)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL1, CELL3),
                                addEdgeConsumer(CELL2, CELL4),
                                addEdgeConsumer(CELL3, CELL4),
                                removeIncomingEdgesConsumer(CELL1)
                        ),
                        List.of(CELL1, CELL2, CELL3, CELL4)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL1, CELL3),
                                addEdgeConsumer(CELL2, CELL4),
                                addEdgeConsumer(CELL3, CELL4),
                                removeIncomingEdgesConsumer(CELL4)
                        ),
                        List.of(CELL1, CELL2, CELL3)
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL3, CELL1),
                                removeIncomingEdgesConsumer(CELL1)
                        ),
                        List.of(CELL1, CELL2, CELL3)
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getCalculateOrderSource")
    public void getCalculateOrderOneCallTest(List<Consumer<FormulaDependencyGraph>> operationConsumers,
                                      List<CellPosition> topologicalSort) throws FormulaCalculatorException {
        getCalculateOrderTest(operationConsumers, topologicalSort, 1);
    }


    @ParameterizedTest
    @MethodSource("getCalculateOrderSource")
    public void getCalculateOrderFiveCallsTest(List<Consumer<FormulaDependencyGraph>> operationConsumers,
                                      List<CellPosition> topologicalSort) throws FormulaCalculatorException {
        getCalculateOrderTest(operationConsumers, topologicalSort, 5);
    }

    private void getCalculateOrderTest(List<Consumer<FormulaDependencyGraph>> operationConsumers,
                                       List<CellPosition> topologicalSort,
                                       int timesToCall) throws FormulaCalculatorException {
        operationConsumers.forEach(operationConsumer -> operationConsumer.accept(formulaDependencyGraph));
        for (int i = 0; i < topologicalSort.size(); i++) {
            CellPosition startCellPosition = topologicalSort.get(i);
            List<CellPosition> expectedCalculateOrder = topologicalSort.subList(i, topologicalSort.size());
            for (int t = 0; t < timesToCall; t++) {
                List<CellPosition> actualCalculateOrder = formulaDependencyGraph.getCalculateOrder(startCellPosition);
                Assertions.assertEquals(expectedCalculateOrder, actualCalculateOrder);
            }
        }
    }

    private static Stream<Arguments> cycleGraphSource() {
        return Stream.of(
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL1)
                        ),
                        CELL1
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL3, CELL1)
                        ),
                        CELL1
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL3, CELL4),
                                addEdgeConsumer(CELL4, CELL1)
                        ),
                        CELL4
                ),
                Arguments.of(
                        List.of(
                                addEdgeConsumer(CELL1, CELL2),
                                addEdgeConsumer(CELL2, CELL3),
                                addEdgeConsumer(CELL3, CELL4),
                                addEdgeConsumer(CELL4, CELL2)
                        ),
                        CELL2
                )
        );
    }

    @ParameterizedTest
    @MethodSource("cycleGraphSource")
    public void cycleGraphTest(List<Consumer<FormulaDependencyGraph>> operationConsumers,
                               CellPosition startCellPosition) {
        operationConsumers.forEach(operationConsumer -> operationConsumer.accept(formulaDependencyGraph));
        Assertions.assertThrows(FormulaCalculatorException.class, () -> formulaDependencyGraph.getCalculateOrder(startCellPosition));
    }

    private static Consumer<FormulaDependencyGraph> addEdgeConsumer(ExcelTable.CellPosition fromCellPosition,
                                                                    ExcelTable.CellPosition toCellPosition) {
        return formulaDependencyGraph -> formulaDependencyGraph.addEdge(fromCellPosition, toCellPosition);
    }

    private static Consumer<FormulaDependencyGraph> removeIncomingEdgesConsumer(ExcelTable.CellPosition cellPosition) {
        return formulaDependencyGraph -> formulaDependencyGraph.removeIncomingEdges(cellPosition);
    }
}