import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuFrame extends JFrame {
    private JButton fileTableButton;
    private JButton newTableButton;

    public MenuFrame() {

        setTitle("Menu");
        fileTableButton = new JButton("Open table from file");

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
}
