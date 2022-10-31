package jetbrains.parser;

import jetbrains.parser.LexicalAnalyzer.Token;
import jetbrains.table.ExcelTable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static jetbrains.parser.LexicalAnalyzer.TokenType.*;

public class FormulaParser {
    public static TreeNode parse(String text) throws ParserException {
        List<Token> tokenList = LexicalAnalyzer.getTokensFromText(text);
        Collections.reverse(tokenList);
        Stack<Token> tokenStack = new Stack<>();
        tokenStack.addAll(tokenList);
        TreeNode tree = parseFormula(tokenStack);
        if (!tokenStack.isEmpty()) {
            throw new ParserException("Unexpected symbols since: " + tokenStack.peek().tokenType.name().toLowerCase() + ".");
        }
        return tree;
    }

    private static TreeNode parseFormula(Stack<Token> tokenStack) throws ParserException {
        if (tokenStack.empty()) {
            throw new ParserException("Empty formula.");
        }

        switch (tokenStack.peek().tokenType) {
            case NUMBER -> {
                return new TerminalNode(tokenStack.pop(), Calculator.NUMBER);
            }
            case MINUS -> {
                List<TreeNode> children = new ArrayList<>();
                children.add(new TerminalNode(tokenStack.pop()));
                if (tokenStack.empty() || tokenStack.peek().tokenType != NUMBER) {
                    throw new ParserException(getParserExceptionMessage(List.of("number"), tokenStack));
                }
                children.add(new TerminalNode(tokenStack.pop(), Calculator.NUMBER));
                return new TreeNode(children, Calculator.SUM);
            }
            case EQ -> {
                tokenStack.pop();
                return parseExp(tokenStack);
            }
            case default -> {
                throw new ParserException(getParserExceptionMessage(List.of("=", "number", "-"), tokenStack));
            }
        }
    }

    private static TreeNode parseExp(Stack<Token> tokenStack) throws ParserException {
        List<TreeNode> children = new ArrayList<>();
        while (!tokenStack.isEmpty() && !Set.of(CLOSE, COMMA).contains(tokenStack.peek().tokenType)) {
            if (!Set.of(PLUS, MINUS).contains(tokenStack.peek().tokenType) && !children.isEmpty()) {
                throw new ParserException(getParserExceptionMessage(List.of("+", "-"), tokenStack));
            }
            if (tokenStack.peek().tokenType == MINUS) {
                children.add(new TerminalNode(tokenStack.pop()));
            } else if (tokenStack.peek().tokenType == PLUS) {
                tokenStack.pop();
            }
            children.add(parseT(tokenStack));
        }
        if (children.isEmpty()) {
            throw new ParserException("Empty expression was found.");
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return new TreeNode(children, Calculator.SUM);
    }

    private static TreeNode parseT(Stack<Token> tokenStack) throws ParserException {
        List<TreeNode> children = new ArrayList<>();
        children.add(parseF(tokenStack));
        while (!tokenStack.isEmpty() && !Set.of(PLUS, MINUS, CLOSE, COMMA).contains(tokenStack.peek().tokenType)) {
            if (!Set.of(MUL, DIV).contains(tokenStack.peek().tokenType)) {
                throw new ParserException(getParserExceptionMessage(List.of("*", "/"), tokenStack));
            }
            children.add(new TerminalNode(tokenStack.pop()));
            children.add(parseF(tokenStack));
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return new TreeNode(children, Calculator.PRODUCT);
    }

    private static TreeNode parseF(Stack<Token> tokenStack) throws ParserException {
        if (tokenStack.isEmpty()) {
            throw new ParserException(getParserExceptionMessage(
                    List.of("(", "number", "cell position", "function name"), tokenStack)
            );
        }
        switch (tokenStack.peek().tokenType) {
            case OPEN -> {
                tokenStack.pop();
                TreeNode exp = parseExp(tokenStack);
                if (tokenStack.isEmpty() || tokenStack.pop().tokenType != CLOSE) {
                    throw new ParserException("Invalid bracket sequence");
                }
                return exp;
            }
            case NUMBER -> {
                return new TerminalNode(tokenStack.pop(), Calculator.NUMBER);
            }
            case CELL_POSITION -> {
                return new TerminalNode(tokenStack.pop(), Calculator.CELL);
            }
            case FUNCTION_NAME -> {
                List<TreeNode> children = new ArrayList<>();
                children.add(new TerminalNode(tokenStack.pop()));
                if (tokenStack.isEmpty() || tokenStack.peek().tokenType != OPEN) {
                    throw new ParserException(getParserExceptionMessage(List.of("("), tokenStack));
                }
                tokenStack.pop();
                children.add(parseParams(tokenStack));
                if (tokenStack.isEmpty() || tokenStack.peek().tokenType != CLOSE) {
                    throw new ParserException(getParserExceptionMessage(List.of(")"), tokenStack));
                }
                tokenStack.pop();
                return new TreeNode(children, Calculator.FUNCTION);
            }
            case default -> throw new ParserException(getParserExceptionMessage(
                    List.of("(", "number", "cell position", "function name"), tokenStack)
            );
        }
    }

    private static TreeNode parseParams(Stack<Token> tokenStack) throws ParserException {
        List<TreeNode> children = new ArrayList<>();
        if (!tokenStack.isEmpty() &&
                Set.of(CELL_DIAPASON, MINUS, OPEN, NUMBER, CELL_POSITION, FUNCTION_NAME).contains(tokenStack.peek().tokenType)) {
            children.add(parseParam(tokenStack));
            while (!tokenStack.isEmpty() && tokenStack.peek().tokenType == COMMA) {
                tokenStack.pop();
                children.add(parseParam(tokenStack));
            }
        }
        return new TreeNode(children, null);
    }

    private static TreeNode parseParam(Stack<Token> tokenStack) throws ParserException {
        if (tokenStack.isEmpty()) {
            throw new ParserException("Empty function parameter.");
        }
        switch (tokenStack.peek().tokenType) {
            case CELL_DIAPASON -> {
                return new TerminalNode(tokenStack.pop(), Calculator.CELL_DIAPASON);
            }
            case MINUS, COMMA, NUMBER, CELL_POSITION, FUNCTION_NAME -> {
                return parseExp(tokenStack);
            }
            case default -> throw new ParserException(getParserExceptionMessage(
                    List.of("cell diapason", "cell position", "-", ",", "number", "function name"), tokenStack)
            );
        }
    }

    private static String getParserExceptionMessage(List<String> expectedTokens, Stack<Token> tokenStack) {
        return "Expected " + String.join(", ", expectedTokens) +
                "tokens, but " +
                (tokenStack.isEmpty() ? "end of formula" : tokenStack.peek().tokenType.name().toLowerCase()) +
                " was found.";
    }

    public static class TreeNode {
        public final List<TreeNode> children;
        public final Calculator calculator;

        public TreeNode(List<TreeNode> children) {
            this(children, null);
        }

        public TreeNode(List<TreeNode> children, Calculator calculator) {
            this.children = children;
            this.calculator = calculator;
        }

        public Object calculate(BiFunction<Integer, Integer, Double> tableValuesFunction) {
            return calculator.calculate(this, tableValuesFunction);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TreeNode treeNode = (TreeNode) o;
            return Objects.equals(children, treeNode.children);
        }
    }

    public static class TerminalNode extends TreeNode {
        public final Token token;

        public TerminalNode(LexicalAnalyzer.Token token) {
            this(token, null);
        }

        public TerminalNode(LexicalAnalyzer.Token token, Calculator calculator) {
            super(List.of(), calculator);
            this.token = token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TerminalNode that = (TerminalNode) o;
            return Objects.equals(token, that.token);
        }
    }

    public static enum Calculator {
        SUM {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                double sum = 0;
                int k = 1;
                for (TreeNode child : treeNode.children) {
                    if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == MINUS) {
                        k = -1;
                    } else {
                        double childValue = (double) child.calculate(tableValuesFunction);
                        sum += k * childValue;
                        k = 1;
                    }
                }
                return sum;
            }
        },
        PRODUCT {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                double product = 1;
                boolean isMul = true;
                for (TreeNode child : treeNode.children) {
                    if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == DIV) {
                        isMul = false;
                    } else if (child instanceof TerminalNode && ((TerminalNode) child).token.tokenType == MUL) {
                        isMul = true;
                    } else {
                        double childValue = (double) child.calculate(tableValuesFunction);
                        if (isMul) {
                            product *= childValue;
                        } else {
                            product /= childValue;
                        }
                    }
                }
                return product;
            }
        },
        FUNCTION {
            //            TODO: add params size and type checking
            private final Map<String, Function<List<Object>, Double>> FUNCTION_NAME_TO_FUNCTION = Map.of(
                    "sin", this::sin
            );

            private double sin(List<Object> params) {
                double param = (double) params.get(0);
                return Math.sin(param);
            }

            //            TODO: add more functions
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                String functionName = (String) ((TerminalNode) treeNode.children.get(0)).token.data;
                TreeNode paramsNode = treeNode.children.get(1);
                List<Object> paramValues = new ArrayList<>();
                for (TreeNode paramNode : paramsNode.children) {
                    double paramValue = (double) paramNode.calculate(tableValuesFunction);
                    paramValues.add(paramValue);
                }
                Function<List<Object>, Double> function = FUNCTION_NAME_TO_FUNCTION.get(functionName);
                return function.apply(paramValues);
            }
        },
        NUMBER {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                return ((TerminalNode) treeNode).token.data;
            }
        },
        CELL {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                ExcelTable.CellPosition cellPosition = (ExcelTable.CellPosition) ((TerminalNode) treeNode).token.data;
                return tableValuesFunction.apply(cellPosition.row, cellPosition.column);
            }
        },
        CELL_DIAPASON {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) {
                ExcelTable.CellDiapason cellDiapason = (ExcelTable.CellDiapason) ((TerminalNode) treeNode).token.data;
                int rows = cellDiapason.toCellPosition.row - cellDiapason.fromCellPosition.row + 1;
                int columns = cellDiapason.toCellPosition.column - cellDiapason.fromCellPosition.column + 1;
                int minRow = cellDiapason.fromCellPosition.row;
                int minColumn = cellDiapason.fromCellPosition.column;
                int maxRow = cellDiapason.toCellPosition.row;
                int maxColumn = cellDiapason.toCellPosition.column;

                double[][] cellDiapasonValues = new double[rows][columns];
                for (int row = minRow; row <= maxRow; row++) {
                    for (int column = minColumn; column <= maxColumn; column++) {
                        double cellValue = tableValuesFunction.apply(row, column);
                        int diapasonRow = row - minRow;
                        int diapasonColumn = column - minColumn;
                        cellDiapasonValues[diapasonRow][diapasonColumn] = cellValue;
                    }
                }
                return cellDiapasonValues;
            }
        };

        abstract public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction);
    }
}


