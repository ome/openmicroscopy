/*
 * ome.formats.importer.gui.AddDatasetDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ETable;
import ome.formats.importer.util.ErrorContainer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The layout for the ErrorTable used by the importer gui
 * 
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class ErrorTable
    extends JPanel
    implements ActionListener, PropertyChangeListener, IObserver, IObservable, MouseListener
{
    /** Logger for this class */
    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(ErrorTable.class);

    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    public ErrorTableModel table = new ErrorTableModel();
    public ETable eTable = new ETable(table);
    
 // ----- Variables -----
    // Debug Borders
    Boolean debug = false;
    
    // Size of the add/remove/refresh buttons (which are square).
    int buttonSize = 40;
    
    // width of certain columns
    int statusWidth = 80;
    int errorWidth = 470;
    int uploadWidth = 20;

    // Add graphic for add button
    String searchIcon = "gfx/add.png";
    // Remove graphics for remove button
    String clearIcon = "gfx/nuvola_editdelete16.png";
    
    JPanel                  mainPanel;

    private JButton sendBtn;
    private JButton cancelBtn;
    private JButton clearDoneBtn;
    
    private ArrayList<ErrorContainer> errors;

    private boolean failedFiles = false;
    
    private CheckboxRenderer cbr;
    private CheckboxCellEditor cbe;

    private JPanel progressPanel;
    
    private JProgressBar bytesProgressBar; // byte progress for one file
    private JProgressBar filesProgressBar; // number of files in set (1 of 10 for example)
    
    private Thread runThread;

    /**
     * Constructor for class
     */
    public ErrorTable()
    {   
        // set to layout that will maximize on resizing
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setOpaque(false);
        
        // Main Panel containing all elements  
        // Set up the main panel layout
        double mainTable[][] =
                {{5, 200, 140, TableLayout.FILL, 140, 5}, // columns
                { 5, TableLayout.PREFERRED, TableLayout.FILL, 5, 29, 5}}; // rows
        
        mainPanel = GuiCommonElements.addMainPanel(this, mainTable, 0,0,0,0, debug); 
                
        String message = "All errors accumulated during your import are displayed here, " +
                "and will be uploaded to us if check-marked. You can send us feedback on " +
                "these problems by clicking the 'Send Feedback' button.";

        JTextPane instructions = 
        	GuiCommonElements.addTextPane(mainPanel, message, "1,1,4,0", debug);
        instructions.setMargin(new Insets(10,10,10,10));
        
        TableColumnModel cModel =  eTable.getColumnModel();
        
        // *** remove last 3 rows from display ***
        TableColumn hiddenColumn = cModel.getColumn(6);
        cModel.removeColumn(hiddenColumn);
        hiddenColumn = cModel.getColumn(5);
        cModel.removeColumn(hiddenColumn);
        hiddenColumn = cModel.getColumn(4);
        cModel.removeColumn(hiddenColumn);
        
        MyTableHeaderRenderer myHeader = new MyTableHeaderRenderer();
        //LeftTableHeaderRenderer leftHeader = new LeftTableHeaderRenderer();
        
        // Create a custom header for the table
        cModel.getColumn(0).setHeaderRenderer(myHeader);
        cModel.getColumn(1).setHeaderRenderer(myHeader);
        cModel.getColumn(2).setHeaderRenderer(myHeader);  
        cModel.getColumn(3).setHeaderRenderer(myHeader); 
               
        cbe = new CheckboxCellEditor(new JCheckBox());
        cbe.checkbox.addMouseListener(this);
        cbr = new CheckboxRenderer();
        
        
        cModel.getColumn(0).setCellEditor(cbe);
        cModel.getColumn(0).setCellRenderer(cbr);
        cModel.getColumn(1).setCellRenderer(new LeftDotRenderer());
        cModel.getColumn(2).setCellRenderer(new TextLeftRenderer());
        cModel.getColumn(3).setCellRenderer(new StatusRenderer());
               
        // Set the width of the status column
        TableColumn statusColumn = eTable.getColumnModel().getColumn(3);
        statusColumn.setPreferredWidth(statusWidth);
        statusColumn.setMaxWidth(statusWidth);
        statusColumn.setMinWidth(statusWidth);

        // Set the width of the error column
        TableColumn dateColumn = eTable.getColumnModel().getColumn(2);
        dateColumn.setPreferredWidth(errorWidth);
        dateColumn.setMaxWidth(errorWidth);
        dateColumn.setMinWidth(errorWidth);

        // Set the width of the upload column
        TableColumn uploadColumn = eTable.getColumnModel().getColumn(0);
        uploadColumn.setPreferredWidth(uploadWidth);
        uploadColumn.setMaxWidth(uploadWidth);
        uploadColumn.setMinWidth(uploadWidth);
        
        eTable.setRowSelectionAllowed(false);
        
        // Add the table to the scollpane
        JScrollPane scrollPane = new JScrollPane(eTable);
        
        mainPanel.add(scrollPane, "1,2,4,1");

        double progressTable[][] =
        {{200}, // columns
        {12, 5, 12}}; // rows
        
        progressPanel = GuiCommonElements.addPlanePanel(mainPanel, progressTable, debug);
        
        runThread = new Thread()
        {
            public void run()
            {
                try
                {
                    bytesProgressBar = new JProgressBar();
                    progressPanel.add(bytesProgressBar, "0,0");
                    
                    filesProgressBar = new JProgressBar(0,20);
                    progressPanel.add(filesProgressBar, "0,2");
                }
                catch (Throwable error)
                { 
                }
            }
        };
        runThread.start();
        
        mainPanel.add(progressPanel, "1,4");
        
        progressPanel.setVisible(false);

        
        cancelBtn = GuiCommonElements.addButton(mainPanel, "Cancel", 'c', "Cancel sending", "2,4,L,C", debug);
        cancelBtn.addActionListener(this);
        
        cancelBtn.setVisible(false);

        clearDoneBtn = GuiCommonElements.addButton(mainPanel, "Clear Done", 'd', "Clear done", "3,4,R,C", debug);
        clearDoneBtn.addActionListener(this);
        clearDoneBtn.setOpaque(false);
        clearDoneBtn.setEnabled(false);
        //clearDoneBtn.setVisible(false); // Disabled (See #5250)
        
        sendBtn = GuiCommonElements.addButton(mainPanel, "Send Feedback", 's', "Send your errors to the OMERO team", "4,4,R,C", debug);
        sendBtn.setOpaque(false);
        sendBtn.addActionListener(this);
        sendBtn.setEnabled(false);
        
        this.add(mainPanel);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e) {} 

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == sendBtn)
        {
            notifyObservers(new ImportEvent.ERRORS_SEND());
        }
        if (event.getSource() == cancelBtn)
        {
            enableCancelBtn(false);
            notifyObservers(new ImportEvent.ERRORS_UPLOAD_CANCELLED());
        }
        if (event.getSource() == clearDoneBtn)
        {
                int numRows = table.getRowCount();

                for (int i = (numRows - 1); i >= 0; i--)
                {
                    if (table.getValueAt(i, 3) == (Integer)20)
                    {
                        removeFileFromQueue(i);
                        notifyObservers(new ImportEvent.ERRORS_CLEARED(i));
                    }
                }
                clearDoneBtn.setEnabled(false);
        }
        
    } 

    /**
     * Remove file from Queue
     * 
     * @param row - row index of file to remove
     */
    private void removeFileFromQueue(int row)
    {
        table.removeRow(row);
        //qTable.table.fireTableRowsDeleted(row, row);
        if (table.getRowCount() == 0)
            sendBtn.setEnabled(false);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(MouseEvent e) {} 


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    public void mouseEntered(MouseEvent e) {} 


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    public void mouseExited(MouseEvent e) {} 


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    public void mousePressed(MouseEvent e) {} 


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    public void mouseReleased(MouseEvent e)
    {
        if (e.getSource() == cbe.checkbox)
        {
           cbe.stopCellEditing();
        }
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public void update(IObservable importLibrary, ImportEvent event) {} 
        
    // Observable methods    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#addObserver(ome.formats.importer.IObserver)
     */
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#deleteObserver(ome.formats.importer.IObserver)
     */
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }
    
    /**
     * Return an array of ErrorContainers
     * 
     * @return errors
     */
    public ArrayList<ErrorContainer> getErrors() {
        return errors;
    }

    /**
     * Set internal errorsContainers
     * 
     * @param errors
     */
    public void setErrors(ArrayList<ErrorContainer> errors) {
        this.errors = errors;
    }
    
    /**
     *  Fire tableTableDataChanged in the table
     */
    public void fireTableDataChanged()
    {
        table.fireTableDataChanged();
    }
    
    /**
     * Add a row to the table and enable the sendBtn
     * 
     * @param rowData
     */
    public void addRow(Vector<Object> rowData)
    {
        table.addRow(rowData);
        sendBtn.setEnabled(true);
    }
        
    /**
     * Change the progress to sending for the row
     * 
     * @param row
     */
    public void setProgressSending(int row)
    {
        table.setValueAt(1, row, 3);
        setFailedFiles(false);
        progressPanel.setVisible(true);
        cancelBtn.setVisible(true); 
        invalidate();
    } 
    
    /**
     * Set the progress for the row to 'done'
     * 
     * @param row
     */
    public void setProgressDone(int row)
    {
        table.setValueAt(20, row, 3);
        setFailedFiles(false);
        clearDoneBtn.setEnabled(true);
    }
    
    /**
     * Reset the progress bars
     * 
     */
    public void resetProgress()
    {
    	filesProgressBar.setValue(0);
    	bytesProgressBar.setValue(0);
    	progressPanel.setVisible(false);
    }
    
    /**
     * Set Files Progress 
     * 
     * @param value
     */
    public void setFilesProgress(int value)
    {
        filesProgressBar.setValue(value);
    }
    
    /**
     * Set Files in Set maximum value
     * @param value
     */
    public void setFilesInSet(int value)
    {
        filesProgressBar.setMaximum(value);
    }
    
    /**
     * Set file bytes progress
     * 
     * @param value
     */
    public void setBytesProgress(int value)
    {
        bytesProgressBar.setValue(value);
    }

    /**
     * Set bytes file maximum size 
     * 
     * @param value
     */
    public void setBytesFileSize(int value) {
        bytesProgressBar.setMaximum(value);
    }
    
    /**
     * Enable or disable send button 
     * 
     * @param enabled
     */
    public void enableSendBtn(boolean enabled)
    {
        sendBtn.setEnabled(enabled);
    }


    /**
     * Enable or disable cancel button 
     * 
     * @param enabled
     */
    public void enableCancelBtn(boolean enabled) {
        if (enabled)
        {   
            cancelBtn.setText("Cancel");
            cancelBtn.setEnabled(enabled);
        }
        else
        {
            cancelBtn.setText("Cancelling...");
            cancelBtn.setEnabled(enabled);
        }
    }


    /**
     * Set text on cancel button to 'cancelled'
     */
    public void setCancelBtnCancelled() {
        cancelBtn.setText("Cancelled");
    }

    /**
     * Set cancel button visible/invisible
     * @param visible
     */
    public void setCancelBtnVisible(boolean visible)
    {
        cancelBtn.setVisible(visible);
    }
    
    //
    // Inner classes
    //
    
    /**
	 * @param failedFiles the failedFiles to set
	 */
	public void setFailedFiles(boolean failedFiles) {
		this.failedFiles = failedFiles;
	}

	/**
	 * @return the failedFiles
	 */
	public boolean getFailedFiles() {
		return failedFiles;
	}

	/**
	 * @author Brian Loranger brain at lifesci.dundee.ac.uk
	 *
	 */
	static class ErrorTableModel extends DefaultTableModel implements TableModelListener 
    {
        
        private static final long serialVersionUID = 1L;
        private String[] columnNames = {"", "Image Filename (checkmark to send)", "Error Message", "Status", "FilePath", "DatasetID", "ProjectID"};
    
        public void tableChanged(TableModelEvent arg0) { }
        
        public int getColumnCount() { return columnNames.length; }
    
        public String getColumnName(int col) { return columnNames[col]; }
        
        public boolean isCellEditable(int row, int col) 
        { 
            if (col == 0)
                return true;
            return false; 
        }
        
        public boolean rowSelectionAllowed() { return false; }
        
    }

	/**
	 * @author Brian Loranger brain at lifesci.dundee.ac.uk
	 *
	 */
    static class MyTableHeaderRenderer extends DefaultTableCellRenderer 
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
            //setToolTipText((String)value);
             
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    
            // Since the renderer is a component, return itself
            return this;
        }
        
        // The following methods override the defaults for performance reasons
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#validate()
         */
        
        public void validate() {}
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#revalidate()
         */
        public void revalidate() {}
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)
         */
        protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {}
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#firePropertyChange(java.lang.String, boolean, boolean)
         */
        public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {}
    }

    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class LeftDotRenderer extends DefaultTableCellRenderer
    {
        private static final long serialVersionUID = 1L;
        
        /* Called each time a column header using this renderer needs to be rendered.
         * (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(
         * javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column)
        {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
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
            setFont(UIManager.getFont("TableCell.font"));

            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                setEnabled(false);
            }
            else 
            {
                setEnabled(true);
            }
            
            return this;
        }
    }


    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class TextLeftRenderer extends DefaultTableCellRenderer 
    {    
        private static final long serialVersionUID = 1L;
           
        /* Called each time a column header using this renderer needs to be rendered.
         * (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setFont(UIManager.getFont("TableCell.font"));
            // Since the renderer is a component, return itself
           
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setToolTipText(value.toString());
            
            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                setEnabled(false);
            }
            else 
            {
                setEnabled(true);
            }
            
            return this;
        }
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class TextCellCenter extends DefaultTableCellRenderer 
    {
    	private static final long serialVersionUID = 1L;
        
        /* Called each time a column header using this renderer needs to be rendered.
         * (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setFont(UIManager.getFont("TableCell.font"));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            // Since the renderer is a component, return itself
           
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                setEnabled(false);
            }
            else 
            {
                setEnabled(true);
            }
            
            return this;
        }
    }
 
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class StatusRenderer extends DefaultTableCellRenderer 
    {
        private static final long serialVersionUID = 1L;

        /* Called each time a column header using this renderer needs to be rendered.
         * (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setFont(UIManager.getFont("TableCell.font"));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            
            Integer i = (Integer) value;
            String text = "pending";
            if (i < 0) 
            {
                text = "pending";
            }
            else if (i < 20) 
            {
                text = "sending";
            }
            else if (i == 20)
            {
                text = "done";
            }
            
            super.getTableCellRendererComponent(table, text, isSelected, hasFocus, row, column);
            
            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                setEnabled(false);
            }
            else 
            {
                setEnabled(true);
            }
            
            return this;
        }
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class CheckboxRenderer extends JCheckBox implements TableCellRenderer
    {
        private static final long serialVersionUID = 1L;

        /* Called each time a column header using this renderer needs to be rendered.
         * (non-Javadoc)
         * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value instanceof Boolean){
                setSelected(((Boolean) value).booleanValue());
            }
                    
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            setBorderPaintedFlat(true);
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            setFocusable(false);
            
            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                setEnabled(false);   
            }
            else 
            {
                setEnabled(true);
            }
            
            return this;
            
        }
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    class CheckboxCellEditor extends AbstractCellEditor implements TableCellEditor   
    {
        private static final long serialVersionUID = 1L;
        JCheckBox checkbox;
        
        /**
         * pass in checkbox to class variable
         * 
         * @param checkbox
         */
        public CheckboxCellEditor(JCheckBox checkbox)
        {
            this.checkbox = checkbox;
        }

        /* (non-Javadoc)
         * @see javax.swing.CellEditor#getCellEditorValue()
         */
        public Object getCellEditorValue() {
            return Boolean.valueOf(checkbox.isSelected());
        }
        
        /* (non-Javadoc)
         * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
         */
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column) {
 
            checkbox.setSelected(((Boolean) value).booleanValue());    
            checkbox.setBackground(eTable.colorForRow(row));
            
            checkbox.setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            checkbox.setBorderPaintedFlat(true);
            checkbox.setOpaque(true);
            checkbox.setLayout(new BorderLayout());
            checkbox.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(0,0,0,0));  
            setFocusable(false);
            
            if (eTable.getValueAt(row, 3) == (Integer)20)
            {
                checkbox.setEnabled(false); 
            }
            else 
            {
                checkbox.setEnabled(true);
            }

            return checkbox;
        }
    }
}
