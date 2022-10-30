package jetbrains.frames;

import javax.swing.*;
import java.awt.*;
import jetbrains.table.TableGenerator;

public class TableFrame extends JFrame {
    public TableFrame(int rows, int columns) {

        setTitle("Table");

        JTable table = TableGenerator.getExcelTable(rows, columns);

        Container c = getContentPane();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        c.setLayout(fl);

        JScrollPane scrollPane = new JScrollPane(table);

        getContentPane().setLayout(new BorderLayout());
        c.add(scrollPane);

        setSize(800, 500);
        setVisible(true);

    }
}
