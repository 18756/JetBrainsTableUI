package jetbrains.formulas.calculator.functions;

import jetbrains.exceptions.FunctionParameterException;
import jetbrains.exceptions.ParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FunctionRepository {
    private static final Map<String, FunctionWithParameterLimits> FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS = Map.of(
            "sin", new FunctionWithParameterLimits(FunctionRepository::sin, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "cos", new FunctionWithParameterLimits(FunctionRepository::cos, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "tan", new FunctionWithParameterLimits(FunctionRepository::tan, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "ln", new FunctionWithParameterLimits(FunctionRepository::ln, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "exp", new FunctionWithParameterLimits(FunctionRepository::exp, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "abs", new FunctionWithParameterLimits(FunctionRepository::abs, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "pow", new FunctionWithParameterLimits(FunctionRepository::pow, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS)),
            "min", new FunctionWithParameterLimits(FunctionRepository::min, List.of(FunctionParamLimit.ANY)),
            "max", new FunctionWithParameterLimits(FunctionRepository::max, List.of(FunctionParamLimit.ANY)),
            "sum", new FunctionWithParameterLimits(FunctionRepository::sum, List.of(FunctionParamLimit.ANY))
    );

    public static void checkFunctionName(String functionName) throws ParserException {
        if (!FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.containsKey(functionName)) {
            throw new ParserException("Invalid function name: " + functionName);
        }
    }

    public static FunctionWithParameterLimits getFunctionWithParamLimits(String functionName) {
        return FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.get(functionName);
    }

    private static double sin(List<Object> params) {
        return Math.sin((double) params.get(0));
    }

    private static double cos(List<Object> params) {
        return Math.cos((double) params.get(0));
    }

    private static double tan(List<Object> params) {
        return Math.tan((double) params.get(0));
    }

    private static double ln(List<Object> params) {
        return Math.log((double) params.get(0));
    }

    private static double exp(List<Object> params) {
        return Math.exp((double) params.get(0));
    }

    private static double abs(List<Object> params) {
        return Math.abs((double) params.get(0));
    }

    private static double pow(List<Object> params) {
        return Math.pow((double) params.get(0), (double) params.get(1));
    }

    private static double min(List<Object> params) {
        return getDoubleValuesFromParams(params).stream().min(Double::compareTo).get();
    }

    private static double max(List<Object> params) {
        return getDoubleValuesFromParams(params).stream().max(Double::compareTo).get();
    }

    private static double sum(List<Object> params) {
        return getDoubleValuesFromParams(params).stream().reduce(0.0, Double::sum);
    }

    private static List<Double> getDoubleValuesFromParams(List<Object> params) {
        List<Double> doubleValues = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Double) {
                doubleValues.add((Double) param);
            } else {
                double[][] matrix = (double[][]) param;
                for (int i = 0; i < matrix.length; i++) {
                    for (int j = 0; j < matrix[i].length; j++) {
                        doubleValues.add(matrix[i][j]);
                    }
                }
            }
        }
        return doubleValues;

    }


    //            TODO: add more matrix functions
    public static class FunctionWithParameterLimits {
        public Function<List<Object>, Double> function;
        List<FunctionParamLimit> functionParamLimits;

        public FunctionWithParameterLimits(Function<List<Object>, Double> function, List<FunctionParamLimit> functionParamLimits) {
            this.function = function;
            this.functionParamLimits = functionParamLimits;
        }

        public void checkParams(List<Object> paramValues) throws FunctionParameterException {
            int paramId = 0;
            for (FunctionParamLimit functionParamLimit : functionParamLimits) {
                paramId += functionParamLimit.checkParams(paramValues, paramId);
            }
        }
    }
}
