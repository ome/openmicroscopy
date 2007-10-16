package table;

/*
 * ome.formats.importer.FileQueueTable
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
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

// import ome.model.containers.Dataset;


public class FileQueueTable 
    extends JPanel 
{

    public NewTableModel tableModel = new NewTableModel();
    public DefaultTableModel defaultTableModel = new DefaultTableModel();
    
    public ETable eTable = new ETable(defaultTableModel);

    private static final long serialVersionUID = -4239932269937114120L;
    
    MyTableHeaderRenderer myHeader;
    
    public boolean cancel = false;
    public boolean importing = false;
    
    FileQueueTable() {


// ----- GUI Layout Elements -----
        // Start layout here
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBorder(BorderFactory.createEmptyBorder(6,5,9,8));

        JPanel queuePanel = new JPanel();
        queuePanel.setLayout(new BoxLayout(queuePanel, BoxLayout.PAGE_AXIS));
        
        TableColumnModel cModel =  eTable.getColumnModel();
        
        System.out.println("cModel has " + cModel.getColumnCount() + " columns");
               
        myHeader = new MyTableHeaderRenderer();
              
        // Create a custom header for the tableModel
        for (int i = 0; i < cModel.getColumnCount(); i++) {
        	cModel.getColumn(i).setHeaderRenderer(myHeader);
        	cModel.getColumn(i).setCellRenderer(new LeftDotRenderer());
        }
        
               
        //  Add a column without affecting existing columns
        addNewColumn(eTable, "Col3");
        addNewColumn(eTable, "Col4");
        addNewColumn(eTable, "Col5");

        eTable.setCellSelectionEnabled(true);
        //eTable.setValueAt("cellTest", 3, 0);
        
        cModel = eTable.getColumnModel();
        System.out.println("cModel has " + cModel.getColumnCount() + " columns");
        
              
        SelectionListener listener = new SelectionListener(eTable);
        eTable.getSelectionModel().addListSelectionListener(listener);
        eTable.getColumnModel().getSelectionModel()
            .addListSelectionListener(listener);
           
        // Add the tableModel to the scollpane
        JScrollPane scrollPane = new JScrollPane(eTable);
        
        queuePanel.add(scrollPane);
        
        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.LINE_AXIS));
       
        importPanel.add(Box.createHorizontalGlue());
       
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        queuePanel.add(importPanel);
        add(queuePanel);
    }
    
    // This method adds a new column to table without reconstructing
    // all the other columns.
    public void addNewColumn(JTable table, Object headerLabel) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        TableColumn col = new TableColumn(model.getColumnCount());
    
        // Ensure that auto-create is off
        if (table.getAutoCreateColumnsFromModel()) {
        	table.setAutoCreateColumnsFromModel(false);
            //throw new IllegalStateException();
        }
        col.setHeaderValue(headerLabel);
        col.setHeaderRenderer(myHeader);
        col.setCellRenderer(new LeftDotRenderer());
        table.addColumn(col);
        //model.addColumn(headerLabel.toString(), values);
    }

    class NewTableModel 
    extends DefaultTableModel 
    implements TableModelListener {

    private String[] columnNames = {"Column 1", "Column 2", "Column 3", "Column 4"};

    public void tableChanged(TableModelEvent arg0) { }
    
    public int getColumnCount() { return columnNames.length; }

    public String getColumnName(int col) { return columnNames[col]; }
    
    public boolean isCellEditable(int row, int col) { return true; }
    
    public boolean rowSelectionAllowed() { return true; }
}

    class QueueTableModel 
        extends DefaultTableModel 
        implements TableModelListener {
        
        private static final long serialVersionUID = 1L;
        private String[] columnNames = {"Files in Queue", "Project/Dataset", "Status", "DatasetNum", "Path", "Archive"};

        public void tableChanged(TableModelEvent arg0) { }
        
        public int getColumnCount() { return columnNames.length; }

        public String getColumnName(int col) { return columnNames[col]; }
        
        public boolean isCellEditable(int row, int col) { return false; }
        
        public boolean rowSelectionAllowed() { return false; }
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
            if (value != null) setText(value.toString());
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
            
            // Since the renderer is a component, return itself
            return this;
        }
    }

    public class SelectionListener 
        implements ListSelectionListener {
        JTable table;
    
        // It is necessary to keep the tableModel since it is not possible
        // to determine the tableModel from the event's source
        SelectionListener(JTable table) {
            this.table = table;
        }
        public void valueChanged(ListSelectionEvent e) {
            // If cell selection is enabled, both row and column change events are fired
            if (e.getSource() == table.getSelectionModel()
                  && table.getRowSelectionAllowed()) {
                // Column selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                dselectRow(first, last);
            } else if (e.getSource() == table.getColumnModel().getSelectionModel()
                   && table.getColumnSelectionAllowed() ){
                // Row selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                dselectRow(first, last);
            }
    
            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            }
        }
        
        private void dselectRow(int first, int last)
        {
            for (int i = first; i < last; i++ )
            {
                try
                {
                    //System.err.println("first: " + first + 
                    //    " last: " + last + " i: " + i);
                    if (!table.getValueAt(i, 2).equals("added") 
                            && table.getSelectionModel().isSelectedIndex(i))
                    {
                        table.getSelectionModel().removeSelectionInterval(i, i);
                        table.clearSelection();
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
    
}
