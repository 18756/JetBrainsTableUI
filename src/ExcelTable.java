import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Arrays;

public class ExcelTable {
    private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};

    public static JTable getExcelTable(int rows, int columns) {
        String[] columnHeader = getColumnHeader(columns);
        String[][] tableData = getTableData(rows, columns);
        JTable table = new JTable(tableData, columnHeader) {
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
//        resizeColumnWidth(table);
//        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        return table;
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
        StringBuilder columnName;
        for (int i = 0; i < columns; i++) {
            int columnId = i;
            columnName = new StringBuilder();
            while (columnId >= 0) {
                columnName.append(ALPHABET[columnId % ALPHABET.length]);
                columnId /= ALPHABET.length;
                columnId--;
            }
            columnHeader[i + 1] = columnName.reverse().toString();
        }
        return columnHeader;
    }
}
