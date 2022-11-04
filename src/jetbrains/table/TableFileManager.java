package jetbrains.table;

import jetbrains.exceptions.TableFileManagerException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TableFileManager {
    private static final String INVALID_FILE_MESSAGE = "Invalid table file.";
    private static final String FILE_EXTENSION = ".table";

    public static void saveTableToFile(ExcelTable table, File file) throws TableFileManagerException {
        try (Writer fileWriter = new FileWriter(file.getPath() + FILE_EXTENSION)) {
            fileWriter.write(table.getRowCount() + "," + (table.getColumnCount() - 1) + ";");
            for (int row = 0; row < table.getRowCount(); row++) {
                for (int column = 0; column < table.getColumnCount() - 1; column++) {
                    String cellText = table.getTextAt(row, column);
                    if (cellText != null && !cellText.isEmpty()) {
                        fileWriter.write(row + "," + column + "," + cellText.length() + ":" + cellText);
                    }
                }
            }
            fileWriter.write(".");
        } catch (IOException e) {
            throw new TableFileManagerException(e.getMessage());
        }
    }

    public static ExcelTable getTableFromFile(File file) throws TableFileManagerException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String tableText = reader.lines().collect(Collectors.joining("\n"));
            Pattern tableSizePattern = Pattern.compile("(\\d+),(\\d+);");
            Matcher tableSizeMatcher = tableSizePattern.matcher(tableText);
            int curTextId = 0;
            if (!tableSizeMatcher.find(curTextId)) {
                throw new TableFileManagerException(INVALID_FILE_MESSAGE);
            }
            int rows = Integer.parseInt(tableSizeMatcher.group(1));
            int columns = Integer.parseInt(tableSizeMatcher.group(2));
            ExcelTable table = TableGenerator.getExcelTable(rows, columns);
            curTextId += tableSizeMatcher.group().length();

            Pattern cellInfoPattern = Pattern.compile("(\\d+),(\\d+),(\\d+):");
            Matcher cellInfoMatcher = cellInfoPattern.matcher(tableText);

            while (cellInfoMatcher.find(curTextId)) {
                if (cellInfoMatcher.start() != curTextId) {
                    throw new TableFileManagerException(INVALID_FILE_MESSAGE);
                }
                int row = Integer.parseInt(cellInfoMatcher.group(1));
                int column = Integer.parseInt(cellInfoMatcher.group(2));
                int cellTextLen = Integer.parseInt(cellInfoMatcher.group(3));
                curTextId += cellInfoMatcher.group().length();
                if (curTextId + cellTextLen >= tableText.length() || row >= rows || column >= columns) {
                    throw new TableFileManagerException(INVALID_FILE_MESSAGE);
                }
                String cellText = tableText.substring(curTextId, curTextId + cellTextLen);
                table.setTextAt(row, column, cellText);
                curTextId += cellTextLen;
            }
            if (curTextId != tableText.length() - 1 || tableText.charAt(curTextId) != '.') {
                throw new TableFileManagerException(INVALID_FILE_MESSAGE);
            }
            return table;
        } catch (IOException e) {
            throw new TableFileManagerException(e.getMessage());
        }
    }
}
