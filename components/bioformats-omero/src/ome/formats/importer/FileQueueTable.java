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

public class FileQueueTable 
    extends JPanel
    implements ActionListener, IObserver
{

    public QueueTableModel table = new QueueTableModel();
    public ETable queue = new ETable(table);

    private static final long serialVersionUID = -4239932269937114120L;


    JButton         refreshBtn;
    JButton         addBtn;
    JButton         removeBtn;
    JButton         importBtn;
    JButton         clearDoneBtn;
    JButton         clearFailedBtn;
    
    private int row;
    private int maxPlanes;
    public boolean cancel = false;
    public boolean importing = false;
    public boolean failedFiles;
    public boolean doneFiles;
    
    FileQueueTable() {

// ----- Variables -----
        // Debug Borders
        Boolean debugBorders = false;
        
        // Size of the add/remove/refresh buttons (which are square).
        int buttonSize = 40;
        // Add graphic for the refresh button
        //String refreshIcon = "gfx/recycled.png";
        // Add graphic for add button
        String addIcon = "gfx/add.png";
        // Remove graphics for remove button
        String removeIcon = "gfx/remove.png";
        
        // Width of the status columns
        int statusWidth = 100;

// ----- GUI Layout Elements -----
        // Start layout here
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6,5,9,8));
        
        JPanel buttonPanel = new JPanel();
        if (debugBorders == true) 
            buttonPanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        
//        refreshBtn = addButton("¤", refreshIcon, null);
//        refreshBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setActionCommand(Actions.REFRESH);
//        refreshBtn.addActionListener(this);
        
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
        
        buttonPanel.add(Box.createRigidArea(new Dimension(0,60)));
        //buttonPanel.add(refreshBtn);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(addBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
        buttonPanel.add(removeBtn);
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(Box.createRigidArea(new Dimension(0,60)));
        add(buttonPanel);
        add(Box.createRigidArea(new Dimension(5,0)));

        JPanel queuePanel = new JPanel();
        if (debugBorders == true)
            queuePanel.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.PAGE_AXIS));
        queuePanel.add(Box.createRigidArea(new Dimension(0,10)));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS)); 
        JLabel label = new JLabel("Import Queue:");
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
        queuePanel.add(labelPanel);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        TableColumnModel cModel =  queue.getColumnModel();
        
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
        //queue.getColumnModel().getSelectionModel()
        //    .addListSelectionListener(listener);
        
        // Hide 3rd to 6th columns
        TableColumnModel tcm = queue.getColumnModel();
        TableColumn projectColumn = tcm.getColumn(6);
        tcm.removeColumn(projectColumn);
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
        clearDoneBtn = addButton("Clear Done", null, null);
        clearFailedBtn = addButton("Clear Failed", null, null);
        importBtn = addButton("Import", null, null);
        importPanel.add(Box.createHorizontalGlue());
        importPanel.add(clearDoneBtn);
        clearDoneBtn.setEnabled(false);
        clearDoneBtn.setActionCommand(Actions.CLEARDONE);
        clearDoneBtn.addActionListener(this);
        importPanel.add(Box.createRigidArea(new Dimension(0,5)));
        importPanel.add(clearFailedBtn);
        clearFailedBtn.setEnabled(false);
        clearFailedBtn.setActionCommand(Actions.CLEARFAILED);
        clearFailedBtn.addActionListener(this);
        importPanel.add(Box.createRigidArea(new Dimension(0,10)));
        importPanel.add(importBtn);
        importBtn.setEnabled(false);
        importBtn.setActionCommand(Actions.IMPORT);
        importBtn.addActionListener(this);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        queuePanel.add(importPanel);
        add(queuePanel);
    }
    
    public void setProgressInfo(int row, int maxPlanes)
    {
        this.row = row;
        this.maxPlanes = maxPlanes;
    }
 
    public boolean setProgressPending(int row)
    {
        if (table.getValueAt(row, 2).equals("added"))
        {
            table.setValueAt("pending", row, 2); 
            return true;
        }
        return false;
            
    }
    
    public void setProgressInvalid(int row)
    {
        if (table.getValueAt(row, 2).equals("added"))
            table.setValueAt("invalid format", row, 2);    
    }
    
        public void setImportProgress(int count, int series, int step)
    {
        String text;
        if (count > 1)
            text = series + 1 + "/" + count + ": " + step + "/" + maxPlanes;
        else
            text = step + "/" + maxPlanes;
        table.setValueAt(text, row, 2);   
    }

    public void setProgressFailed(int row)
    {
     	table.setValueAt("failed", row, 2);
        failedFiles = true;
        table.fireTableDataChanged();
    }
    
    public void setProgressUnknown(int row)
    {
        table.setValueAt("unknown format", row, 2);
        failedFiles = true;
        table.fireTableDataChanged();
    }    
        
    public void setProgressPrepping(int row)
    {
        table.setValueAt("prepping", row, 2); 
    }

    public void setProgressDone(int row)
    {
        table.setValueAt("done", row, 2);
        doneFiles = true;
        table.fireTableDataChanged();
    }
    
    public void setProgressArchiving(int row)
    {
        table.setValueAt("archiving", row, 2);       
    }
    

    public void setProgressAnalyzing(int row)
    {
        table.setValueAt("analyzing", row, 2); 
    }
    
    public int getMaximum()
    {
        return maxPlanes;
    }
        
    static JButton addButton(String name, String image, String tooltip)
    {
        JButton button = null;

        if (image == null) 
        {
            button = new JButton(name);
        } else {
            java.net.URL imgURL = Main.class.getResource(image);
            if (imgURL != null)
            {
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

        for (int i = 0; i < num; i++)
        {
            try {
                boolean archive = (Boolean) table.getValueAt(i, 5);
                File file = new File(table.getValueAt(i, 4).toString());
                Long projectID = (Long) table.getValueAt(i, 6);
                Dataset dataset = (Dataset) table.getValueAt(i, 3);
                String imageName = table.getValueAt(i, 0).toString();
                fads[i] = new ImportContainer(file, projectID, dataset, imageName, archive);            }
            catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }

        }
        return fads;
    }

    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (src == addBtn)
            firePropertyChange(Actions.ADD, false, true);
        if (src == removeBtn)
            firePropertyChange(Actions.REMOVE, false, true);
//        if (src == refreshBtn)
//            firePropertyChange(Actions.REFRESH, false, true);
        if (src == clearDoneBtn)
            firePropertyChange(Actions.CLEARDONE, false, true);
        if (src == clearFailedBtn)
            firePropertyChange(Actions.CLEARFAILED, false, true);
        if (src == importBtn)
        {
            queue.clearSelection();
            firePropertyChange(Actions.IMPORT, false, true); 
        }
    }

    class QueueTableModel 
        extends DefaultTableModel 
        implements TableModelListener {
        
        private static final long serialVersionUID = 1L;
        private String[] columnNames = {"Files in Queue", "Project/Dataset", "Status", "DatasetNum", "Path", "Archive", "ProjectNum"};

        public void tableChanged(TableModelEvent arg0) { }
        
        public int getColumnCount() { return columnNames.length; }

        public String getColumnName(int col) { return columnNames[col]; }
        
        public boolean isCellEditable(int row, int col) { return false; }
        
        public boolean rowSelectionAllowed() { return true; }
    }
 
    public class MyTableHeaderRenderer 
        extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

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
            setToolTipText((String)value);
            
            setEnabled(table == null || table.isEnabled());
                        
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
            // Since the renderer is a component, return itself
            return this;
        }
        
        // The following methods override the defaults for performance reasons
        public void validate() {}
        public void revalidate() {}
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }
    
    @SuppressWarnings("serial")
    class LeftDotRenderer 
        extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            int availableWidth = table.getColumnModel().getColumn(column).getWidth();
            availableWidth -= table.getIntercellSpacing().getWidth();
            Insets borderInsets = getBorder().getBorderInsets((Component)this);
            availableWidth -= (borderInsets.left + borderInsets.right);
            String cellText = getText();
            FontMetrics fm = getFontMetrics( getFont() );
            // Set tool tip if desired
 
            if (fm.stringWidth(cellText) > availableWidth)
            {
                String dots = "...";
                int textWidth = fm.stringWidth( dots );
                int nChars = cellText.length() - 1;
                for (; nChars > 0; nChars--)
                {
                    textWidth += fm.charWidth(cellText.charAt(nChars));
 
                    if (textWidth > availableWidth)
                    {
                        break;
                    }
                }
 
                setText( dots + cellText.substring(nChars + 1) );
            }

            setFont(UIManager.getFont("TableCell.font"));
            if (queue.getValueAt(row, 2).equals("done"))
            { this.setEnabled(false);} 
            else
            { this.setEnabled(true); }
           
            return this;
        }
    }
    
    public class TextCellCenter
        extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            setFont(UIManager.getFont("TableCell.font"));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            // Set tool tip if desired
            setToolTipText((String)value);
            
            if (queue.getValueAt(row, 2).equals("done") || 
                    queue.getValueAt(row, 2).equals("failed"))
            { this.setEnabled(false); } 
            else
            { this.setEnabled(true); }
            
            // Since the renderer is a component, return itself
            return this;
        }
    }

    public class SelectionListener 
        implements ListSelectionListener {
        JTable table;
    
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
            if (e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed()) 
            {
                    dselectRows();
            } 
        }
        
        private void dselectRows()
        {
            // Column selection changed
            int rows = queue.getRowCount();

            for (int i = 0; i < rows; i++ )
            {
                try
                {
                    if (!(queue.getValueAt(i, 2).equals("added") ||
                            queue.getValueAt(i, 2).equals("pending")) 
                            && table.getSelectionModel().isSelectedIndex(i))
                    {
                        table.getSelectionModel().removeSelectionInterval(i, i);
                    }
                } catch (ArrayIndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
        
    public static void main (String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }
        
        FileQueueTable q = new FileQueueTable(); 
        JFrame f = new JFrame();   
        f.getContentPane().add(q);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }

    public void update(IObservable importLibrary, Object message, Object[] args)
    {
        if (message == Actions.LOADING_IMAGE)
        {
            setProgressPrepping((Integer) args[1]);
        }
        if (message == Actions.LOADED_IMAGE)
        {
            setProgressAnalyzing((Integer) args[1]);
        }
        
        if (message == Actions.DATASET_STORED)
        {
            setProgressInfo((Integer)args[1], (Integer)args[6]);
        }
        
        if (message == Actions.IMPORT_STEP)
        {
            if ((Integer)args[1] <= getMaximum()) 
            {   
                setImportProgress((Integer)args[2], (Integer)args[0], (Integer)args[1]);
            }
        }
        
        if (message == Actions.IMPORT_DONE)
        {
            setProgressDone((Integer)args[1]);
        }
    }
}
