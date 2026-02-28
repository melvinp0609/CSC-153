// Members who contributed to the draft:
// Alyssa Young (Aly3673) - Contributed file I/O system and editing function.
// Bryanna Wilson (wilsonb2742) - Contributed file I/O system, list layout for the main panel, detail panel function, and CSV files with player and staff information.
// Prince Melvin (melvinp0609) - Help autosave anydata that is written

import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class GroupProject_02 {
    private static String filePath;
    private static JTable table;
    private static DefaultTableModel tableModel;
    private static JPanel cardPanel;
    private static JPanel mainPanel;
    private static JPanel editPanel;
    private static JList<String> csvList;
    private static DefaultListModel<String> listModel;
    private static JButton editButton;

    public static void main(String[] args) {
        JFrame frame = new JFrame("NFL Team Roster");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardPanel = new JPanel(new CardLayout());
        mainPanel = new JPanel(new BorderLayout());
        editPanel = new JPanel(new BorderLayout());

        listModel = new DefaultListModel<>();
        csvList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(csvList);

        // Main panel.
        JButton loadButton = new JButton("Load CSV");
        editButton = new JButton("Edit CSV");
        editButton.setEnabled(false);

        loadButton.addActionListener(e -> openFile());
        editButton.addActionListener(e -> switchToEditPanel());

        JPanel mainButtonPanel = new JPanel();
        mainButtonPanel.add(loadButton);
        mainButtonPanel.add(editButton);
        mainPanel.add(mainButtonPanel, BorderLayout.NORTH);
        mainPanel.add(listScrollPane, BorderLayout.CENTER);

        // Edit panel.
        tableModel = new CustomTableModel();
        table = new JTable(tableModel);

        table.getColumnModel().addColumnModelListener(new javax.swing.event.TableColumnModelListener() {
           @Override
           public void columnMarginChanged(javax.swing.event.ChangeEvent e) {
            adjustRowHeights(table);
           }
           
           @Override public void columnAdded(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnRemoved(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnMoved(javax.swing.event.TableColumnModelEvent e) {}
           @Override public void columnSelectionChanged(javax.swing.event.ListSelectionEvent e) {}
        });

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        
        TextAreaRenderer textRenderer = new TextAreaRenderer();
        table.setDefaultRenderer(Object.class, textRenderer);
        table.setDefaultEditor(Object.class, new TextAreaEditor());

        table.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    System.out.println("Table data modified. Row: " + row);
                    adjustRowHeights(table);
                } else {
                    adjustRowHeights(table);
                }
            }
        });
        
        JScrollPane tablescrollPane = new JScrollPane(table);
        table.setCellSelectionEnabled(true);

        csvList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (!csvList.isSelectionEmpty()) {
                    String selected = csvList.getSelectedValue();
                    showDetails(selected);
                }
            }
        });

        JButton saveButton = new JButton("Save CSV");
        JButton addRowButton = new JButton("Add Row");
        JButton addColumnButton = new JButton("Add Column");
        JButton deleteRowButton = new JButton("Delete Row");
        JButton deleteColumnButton = new JButton("Delete Column");

        saveButton.addActionListener(e -> {
            if (table.isEditing()) {
                TableCellEditor editor = table.getCellEditor();
                if (editor != null) editor.stopCellEditing();
                else table.getDefaultEditor(table.getColumnClass(table.getEditingColumn())).stopCellEditing();
            }

            System.out.println("Saving data...");
            saveCsv();
            
            refreshListModel();
            
            csvList.revalidate();
            csvList.repaint();

            switchToMainPanel();
        });

        addRowButton.addActionListener(e -> {
            Object[] newRowData = new Object[tableModel.getColumnCount()];
            for (int i = 0; i < newRowData.length; i++) {
                newRowData[i] = "";
            }
            tableModel.addRow(newRowData);
            adjustRowHeights(table);
        });
        
        addColumnButton.addActionListener(e -> {
            String newColumnName = JOptionPane.showInputDialog("Enter name for new column:");
            if (newColumnName != null && !newColumnName.trim().isEmpty()) {
                tableModel.addColumn(newColumnName);
                adjustColumnWidths();
                adjustRowHeights(table);
            }
        });

        deleteRowButton.addActionListener(e -> {
            int rowCount = tableModel.getRowCount();
            if (rowCount > 0) {
                tableModel.removeRow(rowCount - 1);
                adjustRowHeights(table);
            }
        });

        deleteColumnButton.addActionListener(e -> {
            int columnCount = tableModel.getColumnCount();
            if (columnCount > 0) {
                tableModel.setColumnCount(columnCount - 1);
                adjustColumnWidths();
                adjustRowHeights(table);
            }
        });

        editPanel.add(tablescrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addRowButton);
        buttonPanel.add(deleteRowButton);
        buttonPanel.add(addColumnButton);
        buttonPanel.add(deleteColumnButton);
        buttonPanel.add(saveButton);

        editPanel.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(mainPanel, "MainPanel");
        cardPanel.add(editPanel, "EditPanel");

        frame.add(cardPanel);
        frame.setVisible(true);
    }

    private static void switchToEditPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "EditPanel");
        table.setEnabled(true);
        adjustColumnWidths();
    }

    private static void switchToMainPanel() {
        CardLayout cl = (CardLayout) (cardPanel.getLayout());
        cl.show(cardPanel, "MainPanel");
        table.setEnabled(false);
    }

    private static void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userSelection = fileChooser.showOpenDialog(null);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            filePath = fileToOpen.getAbsolutePath();
            boolean ok = loadCsv();

            if (ok) {
                csvList.revalidate();
                csvList.repaint();
                table.revalidate();
                table.repaint();

                editButton.setEnabled(true);
                
                SwingUtilities.invokeLater(() -> {
                    adjustColumnWidths();
                    adjustRowHeights(table);
                    JOptionPane.showMessageDialog(null, "CSV file loaded successfully.");
                });
            } else {
                JOptionPane.showMessageDialog(null, "Error loading CSV file.");
            }
        }
    }

    private static boolean loadCsv() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            listModel.clear();
            tableModel.setRowCount(0);
            tableModel.setColumnCount(0);

            if ((line = reader.readLine()) != null) {
                String[] headers = line.split(",");
                for (String header : headers) {
                    tableModel.addColumn(header);
                }
            }

            int colCount = tableModel.getColumnCount();
            while ((line = reader.readLine()) != null) {
                String[] rowData = line.split(",", colCount);

                if (rowData.length < colCount) {
                    String[] padded = new String[colCount];
                    System.arraycopy(rowData, 0, padded, 0, rowData.length);
                    for (int k = rowData.length; k < padded.length; k++) padded[k] = "";
                    rowData = padded;
                }

                listModel.addElement(rowData[0]);
                tableModel.addRow(rowData);
            }

            refreshListModel();
            adjustColumnWidths();
            adjustRowHeights(table);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void showDetails(String selectedItem) {
        String[] values = selectedItem.split(", ");
        
        if (values.length > 0) {
            String name = values[0].trim();
            
            JDialog dialog = new JDialog((Frame) null, "Details", true);
            JTextArea textArea = new JTextArea();
            Font font = new Font("Arial", Font.BOLD, 14);
            textArea.setFont(font);
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);

            StringBuilder messageBuilder = new StringBuilder();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (tableModel.getValueAt(i, 0).equals(name)) {
                    for (int j = 0; j < tableModel.getColumnCount(); j++) {
                        messageBuilder.append(tableModel.getColumnName(j))
                                      .append(": ")
                                      .append(tableModel.getValueAt(i, j))
                                      .append("\n");
                    }
                    break;
                }
            }

            textArea.setText(messageBuilder.toString());
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(300, 200));
            dialog.add(scrollPane, BorderLayout.CENTER);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(okButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.getRootPane().setDefaultButton(okButton);

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(null, "No details available.");
        }
    }

    private static void saveCsv() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                writer.write(tableModel.getColumnName(i));
                if (i < tableModel.getColumnCount() - 1) writer.write(",");
            }
            writer.newLine();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    writer.write(String.valueOf(tableModel.getValueAt(i, j)));
                    if (j < tableModel.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine();
            }
            JOptionPane.showMessageDialog(null, "CSV file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error saving CSV file.");
        }
    }

    private static void refreshListModel() {
        listModel.clear();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            StringBuilder rowDisplay = new StringBuilder();
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                rowDisplay.append(tableModel.getValueAt(i, j));
                if (j < tableModel.getColumnCount() - 1) {
                    rowDisplay.append(", ");
                }
            }
            listModel.addElement(rowDisplay.toString());
        }
        csvList.setModel(listModel);
    }

    private static void adjustColumnWidths() {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int width = 0;
            for (int j = 0; j < tableModel.getRowCount(); j++) {
                width = Math.max(width, table.getCellRenderer(j, i)
                    .getTableCellRendererComponent(table, tableModel.getValueAt(j, i), false, false, j, i)
                    .getPreferredSize().width);
            }
            table.getColumnModel().getColumn(i).setPreferredWidth(width + 10);
        }
    }

    private static void adjustRowHeights(JTable table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            int maxHeight = table.getRowHeight();

            for (int column = 0; column < table.getColumnCount(); column++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                
                int colWidth = table.getColumnModel().getColumn(column).getWidth();
                comp.setSize(new Dimension(colWidth, comp.getPreferredSize().height));
                
                int prefHeight = comp.getPreferredSize().height;
                maxHeight = Math.max(maxHeight, prefHeight);
            }
            if (table.getRowHeight(row) != maxHeight + 10) {
                table.setRowHeight(row, maxHeight + 10);
            }
        }
    }

    static class TextAreaRenderer extends JTextArea implements TableCellRenderer {
        public TextAreaRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());

            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            this.setSize(new Dimension(colWidth, getPreferredSize().height));

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setFont(table.getFont());
            return this;
        }
    }

    static class TextAreaEditor extends AbstractCellEditor implements TableCellEditor {
        private final JScrollPane scrollPane;
        private final JTextArea textArea;

        public TextAreaEditor() {
            textArea = new JTextArea();
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(null);
        }

        @Override
        public Object getCellEditorValue() {
            return textArea.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            textArea.setText(value == null ? "" : value.toString());
            textArea.setFont(table.getFont());
            int colWidth = table.getColumnModel().getColumn(column).getWidth();
            scrollPane.setPreferredSize(new Dimension(colWidth, 100));
            return scrollPane;
        }
    }

    static class CustomTableModel extends DefaultTableModel {
        @Override
        public void addColumn(Object columnName) {
            if (columnName != null && !columnName.toString().trim().isEmpty()) {
                super.addColumn(columnName);
            }
        }

        @Override
        public Object getValueAt(int row, int column) {
            Object value = super.getValueAt(row, column);
            return value == null ? "" : value;
        }
    }
}
