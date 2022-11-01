package jetbrains.parser;

import jetbrains.exceptions.FunctionParameterException;
import jetbrains.exceptions.ParserException;
import jetbrains.parser.LexicalAnalyzer.Token;
import jetbrains.table.ExcelTable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static jetbrains.parser.LexicalAnalyzer.TokenType.*;

public class FormulaParser {
//    TODO: add $to cells to copy formulas
//    TODO: separate this big file to small files
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

        public Object calculate(BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException {
            return calculator.calculate(this, tableValuesFunction);
        }

        public void addAllCellPositions(Set<ExcelTable.CellPosition> cellPositions) {
            for (TreeNode child : children) {
                child.addAllCellPositions(cellPositions);
            }
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

        public void addAllCellPositions(Set<ExcelTable.CellPosition> cellPositions) {
            if (token.tokenType == CELL_POSITION) {
                cellPositions.add((ExcelTable.CellPosition) token.data);
            } else if (token.tokenType == CELL_DIAPASON) {
                ExcelTable.CellDiapason cellDiapason = (ExcelTable.CellDiapason) token.data;
                ExcelTable.CellPosition fromCellPosition = cellDiapason.fromCellPosition;
                ExcelTable.CellPosition toCellPosition = cellDiapason.toCellPosition;
                for (int row = fromCellPosition.row; row <= toCellPosition.row; row++) {
                    for (int column = fromCellPosition.column; column <= toCellPosition.column; column++) {
                        cellPositions.add(new ExcelTable.CellPosition(row, column));
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

    public enum Calculator {
        SUM {
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException {
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
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException {
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
            private final Map<String, FunctionWithParameterLimits> FUNCTION_NAME_TO_FUNCTION = Map.of(
                    "sin", new FunctionWithParameterLimits(this::sin, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "cos", new FunctionWithParameterLimits(this::cos, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "tan", new FunctionWithParameterLimits(this::tan, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "ln", new FunctionWithParameterLimits(this::ln, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "exp", new FunctionWithParameterLimits(this::exp, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "abs", new FunctionWithParameterLimits(this::abs, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "pow", new FunctionWithParameterLimits(this::pow, List.of(ParamLimit.ONLY_DOUBLE, ParamLimit.ONLY_DOUBLE, ParamLimit.END_PARAMS)),
                    "min", new FunctionWithParameterLimits(this::min, List.of(ParamLimit.ANY)),
                    "max", new FunctionWithParameterLimits(this::max, List.of(ParamLimit.ANY)),
                    "sum", new FunctionWithParameterLimits(this::sum, List.of(ParamLimit.ANY))
            );

            private double sin(List<Object> params) {
                return Math.sin((double) params.get(0));
            }

            private double cos(List<Object> params) {
                return Math.cos((double) params.get(0));
            }

            private double tan(List<Object> params) {
                return Math.tan((double) params.get(0));
            }

            private double ln(List<Object> params) {
                return Math.log((double) params.get(0));
            }

            private double exp(List<Object> params) {
                return Math.exp((double) params.get(0));
            }

            private double abs(List<Object> params) {
                return Math.abs((double) params.get(0));
            }

            private double pow(List<Object> params) {
                return Math.pow((double) params.get(0), (double) params.get(1));
            }

            private double min(List<Object> params) {
                double res = Double.MAX_VALUE;
                for (Object param : params) {
                    if (param instanceof Double) {
                        res = Math.min(res, (Double) param);
                    } else {
                        double[][] matrix = (double[][]) param;
                        double matrixMin = Arrays.stream(matrix)
                                .map(row -> Arrays.stream(row).min().orElse(Double.MAX_VALUE))
                                .min(Double::compareTo).orElse(Double.MAX_VALUE);
                        res = Math.min(res, matrixMin);
                    }
                }
                return res;
            }

            private double max(List<Object> params) {
                double res = -Double.MAX_VALUE;
                for (Object param : params) {
                    if (param instanceof Double) {
                        res = Math.max(res, (Double) param);
                    } else {
                        double[][] matrix = (double[][]) param;
                        double matrixMin = Arrays.stream(matrix)
                                .map(row -> Arrays.stream(row).max().orElse(-Double.MIN_VALUE))
                                .max(Double::compareTo).orElse(-Double.MIN_VALUE);
                        res = Math.max(res, matrixMin);
                    }
                }
                return res;
            }

            private double sum(List<Object> params) {
                double res = 0;
                for (Object param : params) {
                    if (param instanceof Double) {
                        res += (double) param;
                    } else {
                        double[][] matrix = (double[][]) param;
                        double matrixSum = Arrays.stream(matrix).flatMapToDouble(Arrays::stream).sum();
                        res += matrixSum;
                    }
                }
                return res;
            }


            //            TODO: add more matrix functions
            @Override
            public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException {
                String functionName = (String) ((TerminalNode) treeNode.children.get(0)).token.data;
                TreeNode paramsNode = treeNode.children.get(1);
                List<Object> paramValues = new ArrayList<>();
                for (TreeNode paramNode : paramsNode.children) {
                    Object paramValue = paramNode.calculate(tableValuesFunction);
                    paramValues.add(paramValue);
                }
                FunctionWithParameterLimits functionWithParameterLimits = FUNCTION_NAME_TO_FUNCTION.get(functionName);
                functionWithParameterLimits.checkParams(paramValues);
                return functionWithParameterLimits.function.apply(paramValues);
            }

            static class FunctionWithParameterLimits {
                Function<List<Object>, Double> function;
                List<ParamLimit> paramLimits;

                public FunctionWithParameterLimits(Function<List<Object>, Double> function, List<ParamLimit> paramLimits) {
                    this.function = function;
                    this.paramLimits = paramLimits;
                }

                public void checkParams(List<Object> paramValues) throws FunctionParameterException {
                    int paramId = 0;
                    for (ParamLimit paramLimit : paramLimits) {
                        paramId += paramLimit.checkParams(paramValues, paramId);
                    }
                }
            }

            enum ParamLimit {
                ONLY_DOUBLE {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        checkNotEmptyParams(paramValues, paramId);
                        if (!(paramValues.get(paramId) instanceof Double)) {
                            throw new FunctionParameterException("Expected double param, but " +
                                    paramValues.get(paramId).getClass().getName().toLowerCase() + " was found");
                        }
                        return 1;
                    }
                },
                ONLY_CELL_DIAPASON {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        checkNotEmptyParams(paramValues, paramId);
                        if (!(paramValues.get(paramId) instanceof Double[][])) {
                            throw new FunctionParameterException("Expected cell diapason param, but " +
                                    paramValues.get(paramId).getClass().getName().toLowerCase() + " was found");
                        }
                        return 1;
                    }
                },
                ANY {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        checkNotEmptyParams(paramValues, paramId);
                        return 1;
                    }
                },
                CONTINUE_DOUBLES {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        for (int i = paramId; i < paramValues.size(); i++) {
                            ONLY_DOUBLE.checkParams(paramValues, i);
                        }
                        return paramValues.size() - paramId;
                    }
                },
                CONTINUE_CELL_DIAPASONS {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        for (int i = paramId; i < paramValues.size(); i++) {
                            ONLY_CELL_DIAPASON.checkParams(paramValues, i);
                        }
                        return paramValues.size() - paramId;
                    }
                },
                END_PARAMS {
                    @Override
                    public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                        if (paramId < paramValues.size()) {
                            throw new FunctionParameterException("Extra parameters were found");
                        }
                        return 0;
                    }
                };

                private static void checkNotEmptyParams(List<Object> paramValues, int paramId) throws FunctionParameterException {
                    if (paramId == paramValues.size()) {
                        throw new FunctionParameterException("Not enough parameters");
                    }
                }

                abstract public int checkParams(List<Object> paramValues, int paramId) throws FunctionParameterException;
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

        abstract public Object calculate(TreeNode treeNode, BiFunction<Integer, Integer, Double> tableValuesFunction) throws FunctionParameterException;
    }
}


