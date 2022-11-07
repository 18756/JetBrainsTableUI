package jetbrains.frames;

import jetbrains.exceptions.UserInputException;
import jetbrains.table.TableGenerator;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.List;

public class TableSizeFrame extends JFrame {
    private final JTextField rowsTextField;
    private final JTextField columnsTextField;

    public TableSizeFrame() {
        setTitle("Choose table size");

        JLabel sizeLabel = new JLabel("Table size:");
        rowsTextField = new JTextField(5);
        JLabel xLabel = new JLabel("x");
        columnsTextField = new JTextField(5);

        JPanel sizePanel = new JPanel();
        sizePanel.add(sizeLabel);
        sizePanel.add(rowsTextField);
        sizePanel.add(xLabel);
        sizePanel.add(columnsTextField);

        sizePanel.setBorder(new LineBorder(Color.BLACK));

        JButton createTableButton = new JButton("Create table");
        createTableButton.addActionListener(e -> goToTableFrame());

        JPanel mainPanel = new CenteredPanel(List.of(
                new CenteredPanel.ResizableComponent(sizePanel, 1.0, 0.5),
                new CenteredPanel.ResizableComponent(Box.createVerticalStrut(10), 1.0, 0.3),
                new CenteredPanel.ResizableComponent(createTableButton, 1.0, 0.6)
        ), 0.7, 1.0);

        setContentPane(mainPanel);
        setMinimumSize(new Dimension(450, 350));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void goToTableFrame() {
        try {
            int rows = Integer.parseInt(rowsTextField.getText());
            int columns = Integer.parseInt(columnsTextField.getText());
            if (rows <= 0 || columns <= 0) {
                throw new UserInputException("Rows and columns can't be less than or equal to zero.");
            }
            this.dispose();
            new TableFrame(TableGenerator.getExcelTable(rows, columns));
        } catch (NumberFormatException | UserInputException e) {
            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Table size error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
