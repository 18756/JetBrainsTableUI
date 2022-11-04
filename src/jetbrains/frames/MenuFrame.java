package jetbrains.frames;

import jetbrains.table.ExcelTable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static jetbrains.frames.FileChooserFrames.showOpenTableFileChooser;

public class MenuFrame extends JFrame {
    public MenuFrame() {

        setTitle("Menu");
        JButton fileTableButton = new JButton("Open table from file");
        fileTableButton.setPreferredSize(new Dimension(200, 50));
        fileTableButton.addActionListener(e -> showOpenTableFileChooser(this::goToTableFrame, this));

        JButton newTableButton = new JButton("New Table");
        newTableButton.setPreferredSize(new Dimension(200, 50));
        newTableButton.addActionListener(e -> goToTableSizeFrame());

        JPanel mainPanel = new CenteredPanel(List.of(fileTableButton, Box.createVerticalStrut(20), newTableButton));
        add(mainPanel);

        setSize(350, 250);
        setLocationRelativeTo(null);
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
