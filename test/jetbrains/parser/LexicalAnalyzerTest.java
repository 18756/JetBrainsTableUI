package jetbrains.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.parser.LexicalAnalyzer.Token;
import jetbrains.parser.LexicalAnalyzer.TokenType;
import jetbrains.table.ExcelTable.CellDiapason;
import jetbrains.table.ExcelTable.CellPosition;
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
                Arguments.of("Z1", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(0, 26)))),
                Arguments.of("F3", List.of(new Token(TokenType.CELL_POSITION, new CellPosition(2, 6)))),

                Arguments.of("f", List.of(new Token(TokenType.FUNCTION_NAME, "f"))),
                Arguments.of("sin", List.of(new Token(TokenType.FUNCTION_NAME, "sin"))),
                Arguments.of("log2", List.of(new Token(TokenType.FUNCTION_NAME, "log2"))),
                Arguments.of("matrix_prod", List.of(new Token(TokenType.FUNCTION_NAME, "matrix_prod"))),

                Arguments.of("1 + B2", List.of(
                        new Token(TokenType.NUMBER, 1.0),
                        new Token(TokenType.PLUS, null),
                        new Token(TokenType.CELL_POSITION, new CellPosition(1, 2))
                )),
                Arguments.of("f(B2:B4, 2)", List.of(
                        new Token(TokenType.FUNCTION_NAME,  "f"),
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
                Arguments.of("A3:a4"),
                Arguments.of("A3:A"),
                Arguments.of("a3:A4"),
                Arguments.of("4+3 &")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTokensSource")
    public void getTokensFromInvalidTextTest(String text) throws ParserException {
        Assertions.assertThrows(ParserException.class, () -> LexicalAnalyzer.getTokensFromText(text));
    }
}