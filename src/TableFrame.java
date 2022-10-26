import javax.swing.*;
import java.awt.*;

public class TableFrame extends JFrame {
    public TableFrame(int rows, int columns) {

        setTitle("Table");

        JTable table = ExcelTable.getExcelTable(rows, columns);

        Container c = getContentPane();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        c.setLayout(fl);

        JScrollPane scrollPane = new JScrollPane(table);

        c.add(scrollPane);
//        c.add(table);

        setSize(800, 500);
        setVisible(true);

    }
}
