/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ome.formats.importer.util.Actions;
import ome.formats.importer.util.ETable;
import ome.model.containers.Dataset;

public class FileQueueTable extends JPanel implements ActionListener {

    public QueueTableModel table = new QueueTableModel();

    public ETable queue = new ETable(table);

    private static final long serialVersionUID = -4239932269937114120L;

    JButton addBtn;

    JButton removeBtn;

    JButton importBtn;

    private int row;

    private int maxPlanes;

    public boolean cancel = false;

    public boolean importing = false;

    FileQueueTable() {

        // ----- Variables -----
        // Debug Borders
        Boolean debugBorders = false;

        // Size of the add/remove buttons (which are square).
        int buttonSize = 40;
        // Add graphic for add button
        String addIcon = "gfx/add.png";
        // Remove graphics for remove button
        String removeIcon = "gfx/remove.png";

        // Width of the status columns
        int statusWidth = 100;

        // ----- GUI Layout Elements -----
        // Start layout here
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6, 5, 9, 8));

        JPanel buttonPanel = new JPanel();
        if (debugBorders == true) {
            buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        }
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        addBtn = addButton(">>", addIcon, null);
        addBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        addBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setSize(new Dimension(buttonSize, buttonSize));
        addBtn.setActionCommand(Actions.ADD);
        addBtn.addActionListener(this);

        removeBtn = addButton("<<", removeIcon, null);
        removeBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setActionCommand(Actions.REMOVE);
        removeBtn.addActionListener(this);

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        buttonPanel.add(removeBtn);
        buttonPanel.add(Box.createVerticalGlue());
        add(buttonPanel);
        add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel queuePanel = new JPanel();
        if (debugBorders == true) {
            queuePanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        }
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.PAGE_AXIS));
        queuePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Import Queue:");
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
        queuePanel.add(labelPanel);
        queuePanel.add(Box.createRigidArea(new Dimension(0, 5)));

        TableColumnModel cModel = queue.getColumnModel();

        MyTableHeaderRenderer myHeader = new MyTableHeaderRenderer();

        // Create a custom header for the table
        cModel.getColumn(0).setHeaderRenderer(myHeader);
        cModel.getColumn(1).setHeaderRenderer(myHeader);
        cModel.getColumn(2).setHeaderRenderer(myHeader);
        cModel.getColumn(0).setCellRenderer(new LeftDotRenderer());
        cModel.getColumn(1).setCellRenderer(new TextCellCenter());
        cModel.getColumn(2).setCellRenderer(new TextCellCenter());

        // Set the width of the status column
        TableColumn statusColumn = queue.getColumnModel().getColumn(2);
        statusColumn.setPreferredWidth(statusWidth);
        statusColumn.setMaxWidth(statusWidth);
        statusColumn.setMinWidth(statusWidth);

        SelectionListener listener = new SelectionListener(queue);
        queue.getSelectionModel().addListSelectionListener(listener);
        queue.getColumnModel().getSelectionModel().addListSelectionListener(
                listener);

        // Hide 3rd to 5th columns
        TableColumnModel tcm = queue.getColumnModel();
        TableColumn datasetColumn = tcm.getColumn(3);
        tcm.removeColumn(datasetColumn);
        TableColumn pathColumn = tcm.getColumn(3);
        tcm.removeColumn(pathColumn);
        TableColumn archiveColumn = tcm.getColumn(3);
        tcm.removeColumn(archiveColumn);

        // Add the table to the scollpane
        JScrollPane scrollPane = new JScrollPane(queue);

        queuePanel.add(scrollPane);

        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.LINE_AXIS));
        importBtn = addButton("Import", null, null);
        importPanel.add(Box.createHorizontalGlue());
        importPanel.add(importBtn);
        importBtn.setEnabled(true);
        importBtn.setActionCommand(Actions.IMPORT);
        importBtn.addActionListener(this);
        queuePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        queuePanel.add(importPanel);
        add(queuePanel);
    }

    public void setProgressInfo(int row, int maxPlanes) {
        this.row = row;
        this.maxPlanes = maxPlanes;
    }

    public void setProgressPending(int row) {
        if (table.getValueAt(row, 2).equals("added")) {
            table.setValueAt("pending", row, 2);
        }
    }

    public void setImportProgress(int step) {
        String text = step + "/" + maxPlanes;
        table.setValueAt(text, row, 2);
    }

    public void setProgressPrepping(int row) {
        table.setValueAt("importing", row, 2);
    }

    public void setProgressDone(int row) {
        table.setValueAt("done", row, 2);
    }

    public void setProgressArchiving(int row) {
        table.setValueAt("archiving", row, 2);
    }

    public int getMaximum() {
        return maxPlanes;
    }

    static JButton addButton(String name, String image, String tooltip) {
        JButton button = null;

        if (image == null) {
            button = new JButton(name);
        } else {
            java.net.URL imgURL = Main.class.getResource(image);
            if (imgURL != null) {
                button = new JButton(null, new ImageIcon(imgURL));
            } else {
                button = new JButton(name);
                System.err.println("Couldn't find icon: " + image);
            }
        }
        return button;
    }

    public ImportContainer[] getFilesAndDataset() {

        int num = table.getRowCount();
        ImportContainer[] fads = new ImportContainer[num];

        for (int i = 0; i < num; i++) {
            try {
                boolean archive = (Boolean) table.getValueAt(i, 5);
                File file = new File(table.getValueAt(i, 4).toString());
                Dataset dataset = (Dataset) table.getValueAt(i, 3);
                String imageName = table.getValueAt(i, 0).toString();
                fads[i] = new ImportContainer(file, dataset, imageName, archive);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
        return fads;
    }

    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == addBtn) {
            firePropertyChange(Actions.ADD, false, true);
        }
        if (src == removeBtn) {
            firePropertyChange(Actions.REMOVE, false, true);
        }
        if (src == importBtn) {
            queue.clearSelection();

            firePropertyChange(Actions.IMPORT, false, true);
        }
    }

    class QueueTableModel extends DefaultTableModel implements
            TableModelListener {

        private static final long serialVersionUID = 1L;

        private String[] columnNames = { "Files in Queue", "Project/Dataset",
                "Status", "DatasetNum", "Path", "Archive" };

        public void tableChanged(TableModelEvent arg0) {
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        public boolean rowSelectionAllowed() {
            return false;
        }
    }

    public class MyTableHeaderRenderer extends DefaultTableCellRenderer {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            // setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setBorder(BorderFactory.createLineBorder(new Color(0xe0e0e0)));
            setForeground(UIManager.getColor("TableHeader.foreground"));
            setBackground(UIManager.getColor("TableHeader.background"));
            setFont(UIManager.getFont("TableHeader.font"));

            // Configure the component with the specified value
            setFont(getFont().deriveFont(Font.BOLD));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            setText(value.toString());
            setOpaque(true);

            // Set tool tip if desired
            setToolTipText((String) value);

            setEnabled(table == null || table.isEnabled());

            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            // Since the renderer is a component, return itself
            return this;
        }

        // The following methods override the defaults for performance reasons
        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        protected void firePropertyChange(String propertyName, Object oldValue,
                Object newValue) {
        }

        @Override
        public void firePropertyChange(String propertyName, boolean oldValue,
                boolean newValue) {
        }
    }

    @SuppressWarnings("serial")
    class LeftDotRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {
            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            int availableWidth = table.getColumnModel().getColumn(column)
                    .getWidth();
            availableWidth -= table.getIntercellSpacing().getWidth();
            Insets borderInsets = getBorder().getBorderInsets(this);
            availableWidth -= borderInsets.left + borderInsets.right;
            String cellText = getText();
            FontMetrics fm = getFontMetrics(getFont());
            // Set tool tip if desired

            if (fm.stringWidth(cellText) > availableWidth) {
                String dots = "...";
                int textWidth = fm.stringWidth(dots);
                int nChars = cellText.length() - 1;
                for (; nChars > 0; nChars--) {
                    textWidth += fm.charWidth(cellText.charAt(nChars));

                    if (textWidth > availableWidth) {
                        break;
                    }
                }

                setText(dots + cellText.substring(nChars + 1));
            }

            setFont(UIManager.getFont("TableCell.font"));

            return this;
        }
    }

    public class TextCellCenter extends DefaultTableCellRenderer {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                Object value, boolean isSelected, boolean hasFocus, int row,
                int column) {

            super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);

            setFont(UIManager.getFont("TableCell.font"));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            // Set tool tip if desired
            setToolTipText((String) value);

            // Since the renderer is a component, return itself
            return this;
        }
    }

    public class SelectionListener implements ListSelectionListener {
        JTable table;

        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }

        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events
            // are fired
            if (e.getSource() == table.getSelectionModel()
                    && table.getRowSelectionAllowed()) {
                // Column selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                dselectRow(first, last);
            } else if (e.getSource() == table.getColumnModel()
                    .getSelectionModel()
                    && table.getColumnSelectionAllowed()) {
                // Row selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                dselectRow(first, last);
            }

            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            }
        }

        private void dselectRow(int first, int last) {
            for (int i = first; i < last; i++) {
                try {
                    // System.err.println("first: " + first +
                    // " last: " + last + " i: " + i);
                    if (!table.getValueAt(i, 2).equals("added")
                            && table.getSelectionModel().isSelectedIndex(i)) {
                        table.getSelectionModel().removeSelectionInterval(i, i);
                        table.clearSelection();
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName();
        // laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        // laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        // laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        // laf = "javax.swing.plaf.metal.MetalLookAndFeel";

        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            System.err.println(laf + " not supported.");
        }

        FileQueueTable q = new FileQueueTable();
        JFrame f = new JFrame();
        f.getContentPane().add(q);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
