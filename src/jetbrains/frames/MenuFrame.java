package jetbrains.frames;

import jetbrains.table.ExcelTable;
import jetbrains.table.TableFileManager;

import javax.swing.*;
import java.awt.*;

import static jetbrains.frames.FileChooserFrames.showOpenTableFileChooser;

public class MenuFrame extends JFrame {
    private final JButton fileTableButton;
    private final JButton newTableButton;

    public MenuFrame() {

        setTitle("Menu");
        fileTableButton = new JButton("Open table from file");

        fileTableButton.addActionListener(e -> showOpenTableFileChooser(this::goToTableFrame, this));

        newTableButton = new JButton("New Table");

        newTableButton.addActionListener(e -> goToTableSizeFrame());

        Container c = getContentPane();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        c.setLayout(fl);

        c.add (fileTableButton);
        c.add (newTableButton);

        setSize(400, 300);
        setVisible(true);

    }

    private void goToTableSizeFrame() {
        this.dispose();
        new TableSizeFrame();
    }

    private void goToTableFrame(ExcelTable table) {
        this.dispose();
        new TableFrame(table);
    }
}
