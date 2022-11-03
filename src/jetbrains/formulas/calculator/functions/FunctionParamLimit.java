package jetbrains.formulas.calculator.functions;

import jetbrains.exceptions.FunctionParameterException;

import java.util.List;

enum FunctionParamLimit {
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
