package jetbrains.formulas.calculator;

import jetbrains.exceptions.FormulaCalculatorException;
import jetbrains.exceptions.FunctionParameterException;
import jetbrains.exceptions.ParserException;
import jetbrains.formulas.parser.nodes.TreeNode;
import jetbrains.table.TableGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static jetbrains.formulas.parser.FormulaParser.parse;
import static org.junit.jupiter.api.Assertions.*;

class FormulaCalculatorTest {
    private static Stream<Arguments> calculateFormulaSource() {
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
                Arguments.of("=-(3*B1-B2)*(B1+B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), -51.0),
                Arguments.of("=min(2, B1, B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), -2.0),
                Arguments.of("=min(B1:B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), -2.0),
                Arguments.of("=max(B1:B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), 5.0),
                Arguments.of("=max(B1:B2, 6)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), 6.0),
                Arguments.of("=sum(B1:B2)", getTableValuesFunction(Map.of("B1", 5.0, "B2", -2.0)), 3.0)
        );
    }

    @ParameterizedTest
    @MethodSource("calculateFormulaSource")
    public void calculateFormulaTest(String text, BiFunction<Integer, Integer, Double> tableValuesFunction, double expectedValue) throws ParserException, FunctionParameterException, FormulaCalculatorException {
        TreeNode tree = parse(text);
        Object actualValue = tree.calculate(tableValuesFunction);
        Assertions.assertEquals(expectedValue, actualValue);
    }

    private static Stream<Arguments> functionParameterExceptionSource() {
        return Stream.of(
                Arguments.of("=sin()"),
                Arguments.of("=sin(1, 2)"),
                Arguments.of("=sin(A1:A2)"),
                Arguments.of("=sin(1, A1:A2)"),
                Arguments.of("=pow(1)"),
                Arguments.of("=pow(1, 3, 5)"),
                Arguments.of("=pow(1, A1:B2)"),
                Arguments.of("=min()")
        );
    }

    @ParameterizedTest
    @MethodSource("functionParameterExceptionSource")
    public void functionParameterExceptionTest(String text) throws ParserException, FunctionParameterException {
        final BiFunction<Integer, Integer, Double> tableValuesFunction = getTableValuesFunction(Map.of());
        TreeNode tree = parse(text);
        Assertions.assertThrows(FunctionParameterException.class, () -> tree.calculate(tableValuesFunction));
    }

    private static BiFunction<Integer, Integer, Double> getTableValuesFunction(Map<String, Double> cellNameToValue) {
        return (row, column) -> {
            String cellName = TableGenerator.getColumnNameById(column - 1) + (row + 1);
            return cellNameToValue.getOrDefault(cellName, 0.0);
        };
    }
}