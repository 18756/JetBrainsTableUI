package jetbrains.formulas.parser;

import jetbrains.exceptions.ParserException;
import jetbrains.formulas.calculator.FormulaCalculator;
import jetbrains.formulas.parser.LexicalAnalyzer.Token;
import jetbrains.formulas.parser.nodes.TerminalNode;
import jetbrains.formulas.parser.nodes.TreeNode;

import java.util.*;

import static jetbrains.formulas.parser.LexicalAnalyzer.TokenType.*;

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
                return new TerminalNode(tokenStack.pop(), FormulaCalculator.NUMBER);
            }
            case MINUS -> {
                List<TreeNode> children = new ArrayList<>();
                children.add(new TerminalNode(tokenStack.pop()));
                if (tokenStack.empty() || tokenStack.peek().tokenType != NUMBER) {
                    throw new ParserException(getParserExceptionMessage(List.of("number"), tokenStack));
                }
                children.add(new TerminalNode(tokenStack.pop(), FormulaCalculator.NUMBER));
                return new TreeNode(children, FormulaCalculator.SUM);
            }
            case EQ -> {
                tokenStack.pop();
                return parseExp(tokenStack);
            }
            case default -> throw new ParserException(getParserExceptionMessage(List.of("=", "number", "-"), tokenStack));
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
        return new TreeNode(children, FormulaCalculator.SUM);
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
        return new TreeNode(children, FormulaCalculator.PRODUCT);
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
                return new TerminalNode(tokenStack.pop(), FormulaCalculator.NUMBER);
            }
            case CELL_POSITION -> {
                return new TerminalNode(tokenStack.pop(), FormulaCalculator.CELL);
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
                return new TreeNode(children, FormulaCalculator.FUNCTION);
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
                return new TerminalNode(tokenStack.pop(), FormulaCalculator.CELL_DIAPASON);
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
                " tokens, but " +
                (tokenStack.isEmpty() ? "end of formula" : tokenStack.peek().tokenType.name().toLowerCase()) +
                " was found.";
    }
}


