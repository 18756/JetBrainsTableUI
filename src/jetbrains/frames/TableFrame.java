package jetbrains.frames;

import jetbrains.table.ExcelTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

import static jetbrains.frames.FileChooserFrames.showOpenTableFileChooser;
import static jetbrains.frames.FileChooserFrames.showSaveTableFileChooser;

public class TableFrame extends JFrame {
    private Integer pressedYToResizeRow;
    private Integer borderIdToResizeRow;
    private Integer oldRowHeight;
    private final int minHeight = 10;

    public TableFrame(ExcelTable table) {

        setTitle("Table");
        setUpMenuBar(table);
        setUpRowHeightResize(table);

        JTextField syncTextField = setUpSyncTextField(table);

        table.getTableHeader().setReorderingAllowed(false);
        table.getColumn(table.getColumnName(0)).setResizable(false);
        table.setSelectionBackground(Color.WHITE);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.setBorder(new LineBorder(Color.BLACK));

        JPanel mainPanel = new CenteredPanel(List.of(syncTextField, Box.createVerticalStrut(10), scrollPane));
        mainPanel.setPreferredSize(new Dimension(500, 500));
        mainPanel.setBorder(new LineBorder(Color.BLACK));

        resizeComponents(syncTextField, scrollPane, mainPanel);

        add(mainPanel);

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents(syncTextField, scrollPane, mainPanel);
                System.out.println(getWidth() + " x " + getHeight());
            }

            @Override
            public void componentMoved(ComponentEvent e) {}

            @Override
            public void componentShown(ComponentEvent e) {}

            @Override
            public void componentHidden(ComponentEvent e) {}
        });

        setMinimumSize(new Dimension(600, 500));
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void resizeComponents(JTextField topTextField, JScrollPane scrollPane, JPanel mainPanel) {
        topTextField.setPreferredSize(new Dimension(
                (int) (mainPanel.getWidth() * 0.9),
                30
        ));
        scrollPane.setPreferredSize(new Dimension(
                (int) (mainPanel.getWidth() * 0.9),
                (int) Math.min(mainPanel.getHeight() - 80, mainPanel.getHeight() * 0.9))
        );
    }

    private JTextField setUpSyncTextField(ExcelTable table) {
        JTextField syncTextField = new JTextField(50);
        table.setTextFieldToSynchronize(syncTextField);
        syncTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                table.setTextToSelectedCell(syncTextField.getText() + (e.getKeyChar() != '\b' ? e.getKeyChar() : ""));
            }

            @Override
            public void keyPressed(KeyEvent e) {}

            @Override
            public void keyReleased(KeyEvent e) {}
        });

        syncTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("FOCUS GAINED " + e.paramString());
                table.getCellEditor(0, 0).stopCellEditing();
                syncTextField.setText(table.getSelectedCellText());
                table.setTextToSelectedCell(syncTextField.getText());
            }

            @Override
            public void focusLost(FocusEvent e) {}
        });

        return syncTextField;
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
            }

            @Override
            public void mouseExited(MouseEvent e) {
                System.out.println("Exited " + e.getX() + " " + e.getY());
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
