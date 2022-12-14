package jetbrains.formulas.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.formulas.parser.LexicalAnalyzer.Token;
import jetbrains.formulas.parser.LexicalAnalyzer.TokenType;
import jetbrains.table.structures.CellDiapason;
import jetbrains.table.structures.CellPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;


class LexicalAnalyzerTest {
    private static Stream<Arguments> validTokensSource() {
        return Stream.of(
                Arguments.of("", List.of()),
                Arguments.of("=", List.of(new Token(TokenType.EQ, null))),
                Arguments.of("+", List.of(new Token(TokenType.PLUS, null))),
                Arguments.of("-", List.of(new Token(TokenType.MINUS, null))),
                Arguments.of("*", List.of(new Token(TokenType.MUL, null))),
                Arguments.of("/", List.of(new Token(TokenType.DIV, null))),
                Arguments.of("(", List.of(new Token(TokenType.OPEN, null))),
                Arguments.of(")", List.of(new Token(TokenType.CLOSE, null))),
                Arguments.of(",", List.of(new Token(TokenType.COMMA, null))),

                Arguments.of("1", List.of(new Token(TokenType.NUMBER, 1.0))),
                Arguments.of("1.5", List.of(new Token(TokenType.NUMBER, 1.5))),
                Arguments.of("2.", List.of(new Token(TokenType.NUMBER, 2.0))),

                Arguments.of("A1:B2", List.of(
                        new Token(TokenType.CELL_DIAPASON,
                                new CellDiapason(
                                        new CellPosition(0, 1),
                                        new CellPosition(1, 2)
                                )
                        )
                )),
                Arguments.of("C45:C33", List.of(
                        new Token(TokenType.CELL_DIAPASON,
                                new CellDiapason(
                                        new CellPosition(44, 3),
                                        new CellPosition(32, 3)
                                )
                        )
                )),

                Arguments.of("B21", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(20, 2)))),
                Arguments.of("$B$21", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(20, 2)))),
                Arguments.of("$B21", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(20, 2)))),
                Arguments.of("B$21", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(20, 2)))),
                Arguments.of("Z1", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(0, 26)))),
                Arguments.of("F3", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(2, 6)))),

                Arguments.of("sin", List.of(new Token(TokenType.FUNCTION_NAME, "sin"))),
                Arguments.of("ln", List.of(new Token(TokenType.FUNCTION_NAME, "ln"))),

                Arguments.of("1 + B2", List.of(
                        new Token(TokenType.NUMBER, 1.0),
                        new Token(TokenType.PLUS, null),
                        new Token(TokenType.CELL_POSITION, new CellPosition(1, 2))
                )),
                Arguments.of("sum(B2:B4, 2)", List.of(
                        new Token(TokenType.FUNCTION_NAME,  "sum"),
                        new Token(TokenType.OPEN, null),
                        new Token(TokenType.CELL_DIAPASON,
                                new CellDiapason(
                                        new CellPosition(1, 2),
                                        new CellPosition(3, 2)
                                )
                        ),
                        new Token(TokenType.COMMA, null),
                        new Token(TokenType.NUMBER, 2.0),
                        new Token(TokenType.CLOSE, null))
                ),
                Arguments.of(" 2 * sin( 2 ) ", List.of(
                        new Token(TokenType.NUMBER,  2.0),
                        new Token(TokenType.MUL,  null),
                        new Token(TokenType.FUNCTION_NAME,  "sin"),
                        new Token(TokenType.OPEN, null),
                        new Token(TokenType.NUMBER, 2.0),
                        new Token(TokenType.CLOSE, null))
                ),
                Arguments.of("(- 5.1 + A4) / 3.14", List.of(
                        new Token(TokenType.OPEN,  null),
                        new Token(TokenType.MINUS, null),
                        new Token(TokenType.NUMBER, 5.1),
                        new Token(TokenType.PLUS, null),
                        new Token(TokenType.CELL_POSITION, new CellPosition(3, 1)),
                        new Token(TokenType.CLOSE, null),
                        new Token(TokenType.DIV, null),
                        new Token(TokenType.NUMBER, 3.14))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("validTokensSource")
    public void getTokensFromValidTextTest(String text, List<LexicalAnalyzer.Token> expectedTokens) throws ParserException {
        text = "\n \t" + text + "\n\t ";
        List<LexicalAnalyzer.Token> actualTokens = LexicalAnalyzer.getTokensFromText(text);
        Assertions.assertEquals(expectedTokens, actualTokens);
    }

    private static Stream<Arguments> invalidTokensSource() {
        return Stream.of(
                Arguments.of("~"),
                Arguments.of("^"),
                Arguments.of("!"),
                Arguments.of("["),
                Arguments.of("]"),
                Arguments.of(".5"),
                Arguments.of("F"),
                Arguments.of("_fun"),
                Arguments.of("sinn"),
                Arguments.of("A3:a4"),
                Arguments.of("A3:A"),
                Arguments.of("a3:A4"),
                Arguments.of("4+3 &")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTokensSource")
    public void getTokensFromInvalidTextTest(String text) {
        Assertions.assertThrows(ParserException.class, () -> LexicalAnalyzer.getTokensFromText(text));
    }

    private static Stream<Arguments> getFormulaWithShiftedCellsSource() {
        return Stream.of(
                Arguments.of("=A1", new CellPosition(1, 1), new CellPosition(1, 1), "=A1"),
                Arguments.of("=A1", new CellPosition(1, 1), new CellPosition(2, 2), "=B2"),
                Arguments.of("=A1", new CellPosition(1, 1), new CellPosition(1, 2), "=B1"),
                Arguments.of("=A1", new CellPosition(1, 1), new CellPosition(2, 1), "=A2"),
                Arguments.of("=$A$1", new CellPosition(1, 1), new CellPosition(2, 2), "=$A$1"),
                Arguments.of("=$A1", new CellPosition(1, 1), new CellPosition(2, 2), "=$A2"),
                Arguments.of("=A$1", new CellPosition(1, 1), new CellPosition(2, 2), "=B$1"),
                Arguments.of("=B2-C3", new CellPosition(3, 3), new CellPosition(2, 2), "=A1-B2"),
                Arguments.of("=B2-C3", new CellPosition(3, 3), new CellPosition(4, 3), "=B3-C4"),
                Arguments.of("= sin( B5 / 3)", new CellPosition(1, 2), new CellPosition(2, 1), "= sin( A6 / 3)")
        );
    }

    @ParameterizedTest
    @MethodSource("getFormulaWithShiftedCellsSource")
    public void getFormulaWithShiftedCellsTest(String textToCopy,
                                               CellPosition copeCell,
                                               CellPosition pasteCell,
                                               String expectedTextToPaste) {
        String actualTextToPaste = LexicalAnalyzer.getFormulaWithShiftedCells(textToCopy, copeCell, pasteCell);
        Assertions.assertEquals(expectedTextToPaste, actualTextToPaste);
    }
}