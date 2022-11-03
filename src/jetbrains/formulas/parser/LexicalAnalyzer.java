package jetbrains.formulas.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.formulas.calculator.functions.FunctionRepository;
import jetbrains.table.ExcelTable;
import jetbrains.table.TableGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.getCellPositionFromHeaderNames;

public class LexicalAnalyzer {
    private static final String SKIP_SYMBOLS = "\\s*";

    public static List<Token> getTokensFromText(String text) throws ParserException {
        text = text.trim();
        List<Token> tokens = new ArrayList<>();
        int curId = 0;
        boolean isProgress;
        while (curId < text.length()) {
            isProgress = false;
            for (TokenType tokenType : TokenType.values()) {
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

    public static String getFormulaWithShiftedCells(String text, ExcelTable.CellPosition copyCell, ExcelTable.CellPosition pasteCell) {
        StringBuilder copyText = new StringBuilder();
        Pattern pattern = Pattern.compile(TokenType.CELL_POSITION.regularExpression);
        Matcher matcher = pattern.matcher(text);
        int curTextId = 0;
        while (matcher.find()) {
            copyText.append(text, curTextId, matcher.start());
            String parsedText = matcher.group();

            String columnNameToCopy = matcher.group(2);
            String rowNameToCopy = matcher.group(4);
            ExcelTable.CellPosition cellPositionToCopy = getCellPositionFromHeaderNames(rowNameToCopy, columnNameToCopy);
            if (!Objects.equals(matcher.group(1), "$")) {
                int columnDiff = pasteCell.column - copyCell.column;
                cellPositionToCopy.column += columnDiff;
                String columnNameToPaste = TableGenerator.getColumnNameById(cellPositionToCopy.column - 1);
                parsedText = parsedText.replace(columnNameToCopy, columnNameToPaste);
            }
            if (!Objects.equals(matcher.group(3), "$")) {
                int rowDiff = pasteCell.row - copyCell.row;
                cellPositionToCopy.row += rowDiff;
                String rowNameToPaste = (cellPositionToCopy.row + 1) + "";
                parsedText = parsedText.replace(rowNameToCopy, rowNameToPaste);
            }

            copyText.append(parsedText);
            curTextId = matcher.end();
        }
        copyText.append(text, curTextId, text.length());
        return copyText.toString();
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
        CELL_DIAPASON("\\$?([A-Z]+)\\$?(\\d+):\\$?([A-Z]+)\\$?(\\d+)") {
            @Override
            public Object getData(Matcher matcher) {
                ExcelTable.CellPosition fromCellPosition = getCellPositionFromHeaderNames(matcher.group(2), matcher.group(1));
                ExcelTable.CellPosition toCellPosition = getCellPositionFromHeaderNames(matcher.group(4), matcher.group(3));
                return new ExcelTable.CellDiapason(fromCellPosition, toCellPosition);
            }
        },
        CELL_POSITION("(\\$?)([A-Z]+)(\\$?)(\\d+)") {
            @Override
            public Object getData(Matcher matcher) {
                return getCellPositionFromHeaderNames(matcher.group(4), matcher.group(2));
            }
        },
        FUNCTION_NAME("([a-z][a-z0-9_]*)") {
            @Override
            public Object getData(Matcher matcher) throws ParserException {
                String functionName = matcher.group(1);
                FunctionRepository.checkFunctionName(functionName);
                return functionName;
            }
        };

        final String regularExpression;

        TokenType(String regularExpression) {
            this.regularExpression = SKIP_SYMBOLS + regularExpression;
        }

        public Object getData(Matcher matcher) throws ParserException {
            return null;
        }

        public Token getToken(Matcher matcher) throws ParserException {
            return new Token(this, getData(matcher));
        }

        public static ExcelTable.CellPosition getCellPositionFromHeaderNames(String rowName, String columnName) {
            int rowId = Integer.parseInt(rowName);
            int columnId = TableGenerator.getColumnIdByName(columnName);
            return new ExcelTable.CellPosition(rowId - 1, columnId + 1);
        }
    }
}
