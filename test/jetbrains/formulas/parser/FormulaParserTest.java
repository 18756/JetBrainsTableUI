package jetbrains.formulas.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.formulas.parser.nodes.TerminalNode;
import jetbrains.formulas.parser.nodes.TreeNode;
import jetbrains.table.structures.CellDiapason;
import jetbrains.table.structures.CellPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static jetbrains.formulas.parser.FormulaParser.parse;
import static jetbrains.formulas.parser.LexicalAnalyzer.Token;
import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.*;

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
                Arguments.of("=sin(1)", new TreeNode(List.of(
                        new TerminalNode(new Token(FUNCTION_NAME, "sin")),
                        new TreeNode(List.of(new TerminalNode(new Token(NUMBER, 1.0))))
                ))),
                Arguments.of("=sum(A1:C5, D1:H3)", new TreeNode(List.of(
                        new TerminalNode(new Token(FUNCTION_NAME, "sum")),
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
    public void parseInvalidFormulaTest(String text) {
        Assertions.assertThrows(ParserException.class, () -> parse(text));
    }
}