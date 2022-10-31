package jetbrains.parser;

import jetbrains.table.TableGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static jetbrains.parser.FormulaParser.*;
import static jetbrains.parser.LexicalAnalyzer.Token;
import static jetbrains.parser.LexicalAnalyzer.TokenType.*;
import static jetbrains.table.ExcelTable.CellDiapason;
import static jetbrains.table.ExcelTable.CellPosition;

class FormulaParserTest {
    private static Stream<Arguments> validFormulaTreesSource() {
        return Stream.of(
                Arguments.of("1", new TerminalNode(new Token(NUMBER, 1.0))),
                Arguments.of("3.14", new TerminalNode(new Token(NUMBER, 3.14))),
                Arguments.of("3.", new TerminalNode(new Token(NUMBER, 3.0))),
                Arguments.of("-2.7", new TreeNode(List.of(
                        new TerminalNode(new Token(MINUS, null)),
                        new TerminalNode(new Token(NUMBER, 2.7))
                ))),
                Arguments.of("=3.14", new TerminalNode(new Token(NUMBER, 3.14))),
                Arguments.of("=-3.14", new TreeNode(List.of(
                        new TerminalNode(new Token(MINUS, null)),
                        new TerminalNode(new Token(NUMBER, 3.14))
                ))),
                Arguments.of("=B2", new TerminalNode(new Token(CELL_POSITION, new CellPosition(1, 2)))),
                Arguments.of("=-3.14+A3", new TreeNode(List.of(
                        new TerminalNode(new Token(MINUS, null)),
                        new TerminalNode(new Token(NUMBER, 3.14)),
                        new TerminalNode(new Token(CELL_POSITION, new CellPosition(2, 1)))
                ))),
                Arguments.of("=random()", new TreeNode(List.of(
                        new TerminalNode(new Token(FUNCTION_NAME, "random")),
                        new TreeNode(List.of())
                ))),
                Arguments.of("=matrix_prod(A1:C5, D1:H3)", new TreeNode(List.of(
                        new TerminalNode(new Token(FUNCTION_NAME, "matrix_prod")),
                        new TreeNode(List.of(
                                new TerminalNode(new Token(CELL_DIAPASON,
                                        new CellDiapason(
                                                new CellPosition(0, 1),
                                                new CellPosition(4, 3)
                                        )
                                )),
                                new TerminalNode(new Token(CELL_DIAPASON,
                                        new CellDiapason(
                                                new CellPosition(0, 4),
                                                new CellPosition(2, 8)
                                        )
                                ))
                        ))
                ))),
                Arguments.of("=-(-3)", new TreeNode(List.of(
                        new TerminalNode(new Token(MINUS, null)),
                        new TreeNode(List.of(
                                new TerminalNode(new Token(MINUS, null)),
                                new TerminalNode(new Token(NUMBER, 3.0))
                        ))
                ))),
                Arguments.of("=(2+2)*2", new TreeNode(List.of(
                        new TreeNode(List.of(
                                new TerminalNode(new Token(NUMBER, 2.0)),
                                new TerminalNode(new Token(NUMBER, 2.0))
                        )),
                        new TerminalNode(new Token(MUL, null)),
                        new TerminalNode(new Token(NUMBER, 2.0))
                ))),
                Arguments.of("=-3.14/1.5+A3*2", new TreeNode(List.of(
                        new TerminalNode(new Token(MINUS, null)),
                        new TreeNode(List.of(
                                new TerminalNode(new Token(NUMBER, 3.14)),
                                new TerminalNode(new Token(DIV, null)),
                                new TerminalNode(new Token(NUMBER, 1.5))
                        )),
                        new TreeNode(List.of(
                                new TerminalNode(new Token(CELL_POSITION, new CellPosition(2, 1))),
                                new TerminalNode(new Token(MUL, null)),
                                new TerminalNode(new Token(NUMBER, 2.0))
                        ))
                )))
        );
    }

    @ParameterizedTest
    @MethodSource("validFormulaTreesSource")
    public void parseValidFormulaTreeTest(String text, TreeNode expectedTree) throws ParserException {
        TreeNode actualTree = parse(text);
        Assertions.assertEquals(expectedTree, actualTree);
    }

    private static Stream<Arguments> formulaValuesSource() {
        return Stream.of(
                Arguments.of("1", getTableValuesFunction(Map.of()), 1.0),
                Arguments.of("-1.1", getTableValuesFunction(Map.of()), -1.1),
                Arguments.of("=1.2", getTableValuesFunction(Map.of()), 1.2),
                Arguments.of("=-2", getTableValuesFunction(Map.of()), -2.0),
                Arguments.of("=-(-(-2))", getTableValuesFunction(Map.of()), -2.0),
                Arguments.of("=-2+2", getTableValuesFunction(Map.of()), 0.0),
                Arguments.of("=2+2*2", getTableValuesFunction(Map.of()), 6.0),
                Arguments.of("=(2+2)*2", getTableValuesFunction(Map.of()), 8.0),
                Arguments.of("=A3", getTableValuesFunction(Map.of()), 0.0),
                Arguments.of("=A3", getTableValuesFunction(Map.of("A3", 3.0)), 3.0),
                Arguments.of("=A3*2", getTableValuesFunction(Map.of("A3", 3.0)), 6.0),
                Arguments.of("=A3*(-A3+1)", getTableValuesFunction(Map.of("A3", 3.0)), -6.0),
                Arguments.of("=sin(A3)", getTableValuesFunction(Map.of("A3", 3.0)), Math.sin(3.0)),
                Arguments.of("=3/2", getTableValuesFunction(Map.of()), 1.5),
                Arguments.of("=-(3*B1-B2)*(B1+B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), -51.0)
        );
    }

    @ParameterizedTest
    @MethodSource("formulaValuesSource")
    public void formulaValueTest(String text, BiFunction<Integer, Integer, Double> tableValuesFunction, double expectedValue) throws ParserException {
        TreeNode tree = parse(text);
        Object actualValue = tree.calculate(tableValuesFunction);
        Assertions.assertEquals(expectedValue, actualValue);
    }

    private static BiFunction<Integer, Integer, Double> getTableValuesFunction(Map<String, Double> cellNameToValue) {
        return (row, column) -> {
            String cellName = TableGenerator.getColumnNameById(column - 1) + (row + 1);
            return cellNameToValue.getOrDefault(cellName, 0.0);
        };
    }

    private static Stream<Arguments> invalidFormulasSource() {
        return Stream.of(
                Arguments.of("--1"),
                Arguments.of("==1"),
                Arguments.of("=1 2"),
                Arguments.of("=1++2"),
                Arguments.of("=1+-2"),
                Arguments.of("=1--2"),
                Arguments.of("=1+2+"),
                Arguments.of("=1+A1:B3"),
                Arguments.of("=(1"),
                Arguments.of("=1)"),
                Arguments.of("=()"),
                Arguments.of("=sin(2"),
                Arguments.of("=max(2 3)"),
                Arguments.of("=max 2, 3"),
                Arguments.of("=2*/2"),
                Arguments.of("=2*2*"),
                Arguments.of("A3"),
                Arguments.of("sin(3)")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidFormulasSource")
    public void parseInvalidFormulaTest(String text) throws ParserException {
        Assertions.assertThrows(ParserException.class, () -> parse(text));
    }
}