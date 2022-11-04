package jetbrains.table;

import java.util.Arrays;

public class TableGenerator {
    private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static ExcelTable getExcelTable(int rows, int columns) {
        String[] columnHeader = getColumnHeader(columns);
        String[][] tableData = getTableData(rows, columns);
        return new ExcelTable(tableData, columnHeader);
    }

    private static String[][] getTableData(int rows, int columns) {
        String[][] tableData = new String[rows][columns + 1];
        for (int i = 0; i < rows; i++) {
            Arrays.fill(tableData[i], "");
            tableData[i][0] = i + 1 + "";
        }
        return tableData;
    }

    private static String[] getColumnHeader(int columns) {
        String[] columnHeader = new String[columns + 1];
        columnHeader[0] = "";
        for (int i = 0; i < columns; i++) {
            columnHeader[i + 1] = getColumnNameById(i);
        }
        return columnHeader;
    }

    public static String getColumnNameById(int columnId) {
        StringBuilder columnName = new StringBuilder();
        while (columnId >= 0) {
            columnName.append(ALPHABET[columnId % ALPHABET.length]);
            columnId /= ALPHABET.length;
            columnId--;
        }
        return columnName.reverse().toString();
    }

    public static int getColumnIdByName(String columnName) {
        int columnId = 0;
        int p = 1;
        for (int i = columnName.length() - 1; i >= 0; i--) {
            int letterId = columnName.charAt(i) - 'A';
            if (p != 1) {
                letterId++;
            }
            columnId += letterId * p;
            p *= ALPHABET.length;
        }
        return columnId;
    }
}
