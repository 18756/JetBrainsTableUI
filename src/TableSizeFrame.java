import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableSizeFrame extends JFrame {
    private JLabel sizeLabel;
    private JLabel xLabel;
    private JTextField rowsTextField;
    private JTextField columnsTextField;
    private JButton createTableButton;

    public TableSizeFrame() {

        setTitle("Menu");

        sizeLabel = new JLabel("Table size:");
        rowsTextField = new JTextField(5);
        xLabel = new JLabel("x");
        columnsTextField = new JTextField(5);


        createTableButton = new JButton("Create table");
        createTableButton.addActionListener(e -> goToTableFrame());



        Container c = getContentPane();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        c.setLayout(fl);

        c.add(sizeLabel);
        c.add(rowsTextField);
        c.add(xLabel);
        c.add(columnsTextField);
        c.add(createTableButton);

        setSize(400, 300);
        setVisible(true);

    }

    private void goToTableFrame() {
        this.dispose();
        int rows = Integer.parseInt(rowsTextField.getText());
        int columns = Integer.parseInt(columnsTextField.getText());
        new TableFrame(rows, columns);
    }
}
