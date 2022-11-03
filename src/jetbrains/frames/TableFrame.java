package jetbrains.frames;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import jetbrains.exceptions.TableFileManagerException;
import jetbrains.table.ExcelTable;
import jetbrains.table.TableFileManager;

import static jetbrains.frames.FileChooserFrames.showOpenTableFileChooser;
import static jetbrains.frames.FileChooserFrames.showSaveTableFileChooser;

public class TableFrame extends JFrame {
    public TableFrame(ExcelTable table) {

        setTitle("Table");

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem openFileMenuItem = new JMenuItem("Open file");
        JMenuItem saveFileMenuItem = new JMenuItem("Save file");
        JMenuItem newTableMenuItem = new JMenuItem("New table");


        saveFileMenuItem.addActionListener(e -> {
            System.out.println("Save file " + e.toString());
            showSaveTableFileChooser(table, saveFileMenuItem);
        });

        openFileMenuItem.addActionListener(e -> {
            System.out.println("Open file " + e.toString());
            showOpenTableFileChooser(this::openNewTable, openFileMenuItem);
        });

        newTableMenuItem.addActionListener(e -> goToTableSizeFrame());

        fileMenu.add(openFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveFileMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(newTableMenuItem);

        menuBar.add(fileMenu);

        menuBar.setBackground(Color.GRAY);
        menuBar.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        menuBar.setMargin(new Insets(5, 10, 20, 0));

        setJMenuBar(menuBar);

        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);


        Container c = getContentPane();
        FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
        c.setLayout(fl);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        getContentPane().setLayout(new BorderLayout());
        c.add(scrollPane);

        setSize(800, 500);
        setVisible(true);

    }

    private void openNewTable(ExcelTable newTable) {
        this.dispose();
        new TableFrame(newTable);
    }

    private void goToTableSizeFrame() {
        this.dispose();
        new TableSizeFrame();
    }
}
