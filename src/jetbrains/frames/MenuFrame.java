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

        JPanel mainPanel = new CenteredPanel(List.of(
                new CenteredPanel.ResizableComponent(fileTableButton, 1.0, 1.0),
                new CenteredPanel.ResizableComponent(Box.createVerticalStrut(20), 1.0, 0.5),
                new CenteredPanel.ResizableComponent(newTableButton, 1.0, 1.0)
        ), 0.7, 1.0);

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(450, 350));
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
