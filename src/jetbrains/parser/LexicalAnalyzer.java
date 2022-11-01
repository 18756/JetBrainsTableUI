package jetbrains.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.table.ExcelTable;
import jetbrains.table.TableGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LexicalAnalyzer {
    private static final String SKIP_SYMBOLS = "\\s*";
    public static List<Token> getTokensFromText(String text) throws ParserException {
//        text = text.replaceAll(SKIP_SYMBOLS,"");
        text = text.trim();
        List<Token> tokens = new ArrayList<>();
        int curId = 0;
        boolean isProgress;
        while (curId < text.length()) {
            isProgress = false;
            for (TokenType tokenType: TokenType.values()) {
                Pattern pattern = Pattern.compile(tokenType.regularExpression);
                Matcher matcher = pattern.matcher(text);
                if (matcher.find(curId) && matcher.start() == curId) {
                    tokens.add(tokenType.getToken(matcher));
                    String parsedText = matcher.group();
                    curId += parsedText.length();
                    isProgress = true;
                    break;
                }
            }
            if (!isProgress) {
                throw new ParserException("Unexpected formula elements since: " + text.substring(curId));
            }
        }
        return tokens;
    }

    public static class Token {
        public TokenType tokenType;
        public Object data;

        public Token(TokenType tokenType, Object data) {
            this.tokenType = tokenType;
            this.data = data;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Token token = (Token) o;
            return tokenType == token.tokenType && Objects.equals(data, token.data);
        }
    }

    public enum TokenType {
        EQ("="),
        PLUS("\\+"),
        MINUS("-"),
        MUL("\\*"),
        DIV("/"),
        OPEN("\\("),
        CLOSE("\\)"),
        COMMA(","),
        NUMBER("\\d+(\\.\\d*)?") {
            @Override
            public Object getData(Matcher matcher) {
                return Double.parseDouble(matcher.group());
            }
        },
        CELL_DIAPASON("([A-Z]+)(\\d+):([A-Z]+)(\\d+)") {
            @Override
            public Object getData(Matcher matcher) {
                ExcelTable.CellPosition fromCellPosition = getCellPositionFromHeaderNames(matcher.group(2), matcher.group(1));
                ExcelTable.CellPosition toCellPosition = getCellPositionFromHeaderNames(matcher.group(4), matcher.group(3));
                return new ExcelTable.CellDiapason(fromCellPosition, toCellPosition);
            }
        },
        CELL_POSITION("([A-Z]+)(\\d+)") {
            @Override
            public Object getData(Matcher matcher) {
                return getCellPositionFromHeaderNames(matcher.group(2), matcher.group(1));
            }
        },
        FUNCTION_NAME("[a-z][a-z0-9_]*") {
            @Override
            public Object getData(Matcher matcher) {
//                check if function name exist
                return matcher.group();
            }
        };

        final String regularExpression;

        TokenType(String regularExpression) {
            this.regularExpression = SKIP_SYMBOLS + regularExpression;
        }

        public Object getData(Matcher matcher) {
            return null;
        }

        public Token getToken(Matcher matcher) {
            return new Token(this, getData(matcher));
        }

        private static ExcelTable.CellPosition getCellPositionFromHeaderNames(String rowName, String columnName) {
            int rowId = Integer.parseInt(rowName);
            int columnId = TableGenerator.getColumnIdByName(columnName);
            return new ExcelTable.CellPosition(rowId - 1, columnId + 1);
        }
    }
}
