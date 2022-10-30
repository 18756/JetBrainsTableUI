package jetbrains.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.swing.*;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class TableGeneratorTest {
    private static final int ALPHABET_SIZE = 26;
    private JTable table;

    private static Stream<Arguments> tableSizeSource() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(1, 1),
                Arguments.of(3, 5),
                Arguments.of(5, 3),
                Arguments.of(10, 10),
                Arguments.of(50, 100)
        );
    }

    @ParameterizedTest
    @MethodSource("tableSizeSource")
    public void sizeTest(int rows, int columns) {
        table = TableGenerator.getExcelTable(rows, columns);
        Assertions.assertEquals(rows, table.getRowCount());
        Assertions.assertEquals(columns + 1, table.getColumnCount());
    }

    private static Stream<Arguments> rowHeaderSource() {
        return Stream.of(
                Arguments.of(1, 1, List.of("1")),
                Arguments.of(2, 1, List.of("1", "2")),
                Arguments.of(2, 3, List.of("1", "2")),
                Arguments.of(3, 1, List.of("1", "2", "3")),
                Arguments.of(3, 2, List.of("1", "2", "3"))
        );
    }

    @ParameterizedTest
    @MethodSource("rowHeaderSource")
    public void rowHeaderTest(int rows, int columns, List<String> expectedRowHeader) {
        table = TableGenerator.getExcelTable(rows, columns);
        List<Object> actualRowHeader = IntStream.range(0, rows).mapToObj(row -> table.getValueAt(row, 0)).toList();
        Assertions.assertEquals(expectedRowHeader, actualRowHeader);
    }

    private static Stream<Arguments> columnHeaderSource() {
        return Stream.of(
                Arguments.of(0, "A"),
                Arguments.of(1, "B"),
                Arguments.of(2, "C"),
                Arguments.of(ALPHABET_SIZE - 1, "Z"),
                Arguments.of(ALPHABET_SIZE, "AA"),
                Arguments.of(ALPHABET_SIZE + 1, "AB"),
                Arguments.of(2 * ALPHABET_SIZE, "BA"),
                Arguments.of(2 * ALPHABET_SIZE + 1, "BB"),
                Arguments.of(3 * ALPHABET_SIZE, "CA"),
                Arguments.of(ALPHABET_SIZE * ALPHABET_SIZE, "ZA"),
                Arguments.of((ALPHABET_SIZE + 1) * ALPHABET_SIZE, "AAA"),
                Arguments.of((ALPHABET_SIZE + 1) * ALPHABET_SIZE + 1, "AAB")
        );
    }

    @ParameterizedTest
    @MethodSource("columnHeaderSource")
    public void getColumnNameByIdTest(int columnId, String expectedColumnName) {
        Assertions.assertEquals(expectedColumnName, TableGenerator.getColumnNameById(columnId));
    }


    @ParameterizedTest
    @MethodSource("columnHeaderSource")
    public void getColumnIdByNameTest(int expectedColumnId, String columnName) {
        Assertions.assertEquals(expectedColumnId, TableGenerator.getColumnIdByName(columnName));
    }
}