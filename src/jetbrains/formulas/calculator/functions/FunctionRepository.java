package jetbrains.formulas.calculator.functions;

import jetbrains.exceptions.FunctionParameterException;
import jetbrains.exceptions.ParserException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class FunctionRepository {
    private static final Map<String, FunctionWithParameterLimits> FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS = new HashMap<>();

    static {
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "sin", new FunctionWithParameterLimits(FunctionRepository::sin, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "cos", new FunctionWithParameterLimits(FunctionRepository::cos, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "tan", new FunctionWithParameterLimits(FunctionRepository::tan, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "ln", new FunctionWithParameterLimits(FunctionRepository::ln, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "exp", new FunctionWithParameterLimits(FunctionRepository::exp, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "abs", new FunctionWithParameterLimits(FunctionRepository::abs, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "pow", new FunctionWithParameterLimits(FunctionRepository::pow, List.of(FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.ONLY_DOUBLE, FunctionParamLimit.END_PARAMS))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "min", new FunctionWithParameterLimits(FunctionRepository::min, List.of(FunctionParamLimit.ANY))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "max", new FunctionWithParameterLimits(FunctionRepository::max, List.of(FunctionParamLimit.ANY))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "sum", new FunctionWithParameterLimits(FunctionRepository::sum, List.of(FunctionParamLimit.ANY))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "mean", new FunctionWithParameterLimits(FunctionRepository::mean, List.of(FunctionParamLimit.ANY))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "std", new FunctionWithParameterLimits(FunctionRepository::std, List.of(FunctionParamLimit.ANY))
        );
        FUNCTION_NAME_TO_FUNCTION_WITH_PARAM_LIMITS.put(
                "cor", new FunctionWithParameterLimits(FunctionRepository::cor, List.of(FunctionParamLimit.ONLY_CELL_DIAPASON, FunctionParamLimit.ONLY_CELL_DIAPASON, FunctionParamLimit.END_PARAMS))
        );
    }


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

    private static double mean(List<Object> params) {
        return sum(params) / getDoubleValuesFromParams(params).size();
    }

    private static double std(List<Object> params) {
        double mean = mean(params);
        double stdSum = getDoubleValuesFromParams(params).stream().reduce(0.0, (a, b) -> (a + (b - mean) * (b - mean)));
        return Math.sqrt(stdSum / getDoubleValuesFromParams(params).size());
    }

    private static double cor(List<Object> params) {
        List<Double> xs = getDoubleValuesFromParams(List.of(params.get(0)));
        List<Double> ys = getDoubleValuesFromParams(List.of(params.get(1)));
        if (xs.size() != ys.size()) {
            return 0.0;
        }
        double xyMean = IntStream.range(0, xs.size()).mapToDouble(i -> xs.get(i) * ys.get(i)).sum() / xs.size();

        double xMean = mean(List.of(params.get(0)));
        double yMean = mean(List.of(params.get(1)));
        double xStd = std(List.of(params.get(0)));
        double yStd = std(List.of(params.get(1)));

        return (xyMean - xMean * yMean) / (xStd * yStd);
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
