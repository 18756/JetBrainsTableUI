package jetbrains.frames;

import jetbrains.table.ExcelTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

import static jetbrains.frames.FileChooserFrames.showOpenTableFileChooser;
import static jetbrains.frames.FileChooserFrames.showSaveTableFileChooser;

public class TableFrame extends JFrame {
    private Integer pressedYToResizeRow;
    private Integer borderIdToResizeRow;
    private Integer oldRowHeight;
    private final int minHeight = 10;

    private boolean isCursorOutOfTable = false;

    public TableFrame(ExcelTable table) {

        setTitle("Table");
        setUpMenuBar(table);
        setUpRowHeightResize(table);

        JPanel panel = new JPanel();

        JTextField topTextField = new JTextField(50);
        table.setTextFieldToSynchronize(topTextField);
        topTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                table.setTextToSelectedCell(topTextField.getText() + (e.getKeyChar() != '\b' ? e.getKeyChar() : ""));
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        topTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("FOCUS GAINED " + e.paramString());
                table.getCellEditor(0, 0).stopCellEditing();
                topTextField.setText(table.getSelectedCellText());
                table.setTextToSelectedCell(topTextField.getText());
            }

            @Override
            public void focusLost(FocusEvent e) {}
        });


        table.getTableHeader().setReorderingAllowed(false);
        table.getColumn(table.getColumnName(0)).setResizable(false);
        table.setSelectionBackground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//        Container container = getContentPane();
        FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT);
        panel.setLayout(flowLayout);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));

//        getContentPane().setLayout(new BorderLayout());
        panel.add(topTextField);
        panel.add(scrollPane);

        add(panel);

        setSize(800, 500);
        setVisible(true);

    }

    private void setUpRowHeightResize(ExcelTable table) {
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Clicked " + e.getX() + " " + e.getY());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                System.out.println("Pressed " + e.getX() + " " + e.getY());
                borderIdToResizeRow = table.getRowBorderId(e.getX(), e.getY());
                if (borderIdToResizeRow != null) {
                    pressedYToResizeRow = e.getY();
                    oldRowHeight = table.getRowHeight(borderIdToResizeRow - 1);
                    System.out.println("Started to resize border " + borderIdToResizeRow);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                System.out.println("Released " + e.getX() + " " + e.getY());
                borderIdToResizeRow = null;
                pressedYToResizeRow = null;
                oldRowHeight = null;
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                System.out.println("Entered " + e.getX() + " " + e.getY());
                isCursorOutOfTable = false;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("Exited " + e.getX() + " " + e.getY());
                isCursorOutOfTable = true;
            }
        });

        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.println("Dragged " + e.getX() + " " + e.getY());
                if (borderIdToResizeRow != null) {
                    int yDiff = e.getY() - pressedYToResizeRow;
                    int newRowHeight = Math.max(oldRowHeight + yDiff, minHeight);
                    table.setRowHeight(borderIdToResizeRow - 1, newRowHeight);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                System.out.println("Moved " + e.getX() + " " + e.getY() + " " + table.getRowBorderId(e.getX(), e.getY()));
                if (table.getRowBorderId(e.getX(), e.getY()) != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    private void setUpMenuBar(ExcelTable table) {
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
