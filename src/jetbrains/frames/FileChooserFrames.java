package jetbrains.frames;

import jetbrains.exceptions.TableFileManagerException;
import jetbrains.table.ExcelTable;
import jetbrains.table.TableFileManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.function.Consumer;

public class FileChooserFrames {
    public static void showSaveTableFileChooser(ExcelTable table, Component parent) {
        showFileChooser(
                parent,
                "Choose a path to save table",
                "Save",
                selectedFile -> {
                    try {
                        TableFileManager.saveTableToFile(table, selectedFile);
                        JOptionPane.showMessageDialog(
                                parent,
                                "Table was saved.",
                                "Save status",
                                JOptionPane.PLAIN_MESSAGE
                        );
                    } catch (TableFileManagerException exception) {
                        JOptionPane.showMessageDialog(
                                parent,
                                exception.getMessage(),
                                "File writing error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
    }

    public static void showOpenTableFileChooser(Consumer<ExcelTable> newTableOpener, Component parent) {
        showFileChooser(
                parent,
                "Choose a table file to open",
                "Open",
                selectedFile -> {
                    try {
                        ExcelTable tableFromFile = TableFileManager.getTableFromFile(selectedFile);
                        newTableOpener.accept(tableFromFile);
                    } catch (TableFileManagerException exception) {
                        JOptionPane.showMessageDialog(
                                parent,
                                exception.getMessage(),
                                "File opening error",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
    }

    private static void showFileChooser(Component parent,
                                        String dialogTitle,
                                        String approveButtonText,
                                        Consumer<File> selectedFileConsumer) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(dialogTitle);
        int fileSelection = fileChooser.showDialog(parent, approveButtonText);

        if (fileSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println(selectedFile.getAbsolutePath());
            selectedFileConsumer.accept(selectedFile);
        }
    }
}
