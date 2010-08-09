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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
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

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ETable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmicroscopy.shoola.util.ui.IconManager;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class FileQueueTable extends JPanel implements ActionListener, IObserver
{
	/** Logger for this class */
	private static Log log = LogFactory.getLog(FileQueueTable.class);
	
    private static QueueTableModel table = new QueueTableModel();
    private static ETable queue = new ETable(getTable());
    
    private static final long serialVersionUID = -4239932269937114120L;


    JButton         refreshBtn;
    JButton         addBtn;
    JButton         removeBtn;
    JButton         importBtn;
    JButton         clearDoneBtn;
    JButton         clearFailedBtn;

	JButton			groupBtn;
    
    private int row;
    private int maxPlanes;
    public boolean cancel = false;
    public boolean abort = false;
    public boolean importing = false;
    public boolean failedFiles;
    public boolean doneFiles;
    
    private MyTableHeaderRenderer headerCellRenderer;
    private LeftDotRenderer fileCellRenderer;
    private CenterTextRenderer dpCellRenderer;
    private CenterTextRenderer statusCellRenderer;

    /**
     * Set up and display the file queue table
     */
    FileQueueTable() 
    {
            
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
        
//        refreshBtn = addButton("+", refreshIcon, null);
//        refreshBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setSize(new Dimension(buttonSize, buttonSize));
//        refreshBtn.setActionCommand(Actions.REFRESH);
//        refreshBtn.addActionListener(this);
        
        addBtn = GuiCommonElements.addBasicButton(null, addIcon, null);
        addBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        addBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        addBtn.setSize(new Dimension(buttonSize, buttonSize));
        addBtn.setActionCommand(FileQueueHandler.ADD);
        addBtn.addActionListener(this);
        addBtn.setToolTipText("Add files to the import queue.");
        
        removeBtn = GuiCommonElements.addBasicButton(null, removeIcon, null);
        removeBtn.setMaximumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setPreferredSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setMinimumSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setSize(new Dimension(buttonSize, buttonSize));
        removeBtn.setActionCommand(FileQueueHandler.REMOVE);
        removeBtn.addActionListener(this);
        removeBtn.setToolTipText("Remove files from the import queue.");
        
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
        //queuePanel.add(Box.createRigidArea(new Dimension(0,10)));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS)); 
        JLabel label = new JLabel("Import Queue:");
        labelPanel.add(label);
        labelPanel.add(Box.createHorizontalGlue());
		groupBtn = GuiCommonElements.addBasicButton("Group: ", null, "Current Group");
		groupBtn.setToolTipText("The user group you are logged into.");
		//groupBtn.setEnabled(false);
        labelPanel.add(groupBtn);
        queuePanel.add(labelPanel);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        TableColumnModel cModel =  getQueue().getColumnModel();
        
        headerCellRenderer = new MyTableHeaderRenderer();
        fileCellRenderer = new LeftDotRenderer();
        dpCellRenderer = new CenterTextRenderer();
        statusCellRenderer = new CenterTextRenderer();
              
        // Create a custom header for the table
        cModel.getColumn(0).setHeaderRenderer(headerCellRenderer);
        cModel.getColumn(1).setHeaderRenderer(headerCellRenderer);
        cModel.getColumn(2).setHeaderRenderer(headerCellRenderer);
        cModel.getColumn(0).setCellRenderer(fileCellRenderer);
        cModel.getColumn(1).setCellRenderer(dpCellRenderer);
        cModel.getColumn(2).setCellRenderer(statusCellRenderer);            
        
        // Set the width of the status column
        TableColumn statusColumn = getQueue().getColumnModel().getColumn(2);
        statusColumn.setPreferredWidth(statusWidth);
        statusColumn.setMaxWidth(statusWidth);
        statusColumn.setMinWidth(statusWidth);
              

        SelectionListener listener = new SelectionListener(getQueue());
        getQueue().getSelectionModel().addListSelectionListener(listener);
        //queue.getColumnModel().getSelectionModel()
        //    .addListSelectionListener(listener);
        
        // Hide 3rd to 6th columns
        TableColumnModel tcm = getQueue().getColumnModel();
        TableColumn projectColumn = tcm.getColumn(6);
        tcm.removeColumn(projectColumn);
        TableColumn userPixelColumn = tcm.getColumn(6);
        tcm.removeColumn(userPixelColumn);
        TableColumn userSpecifiedNameColumn = tcm.getColumn(6);
        tcm.removeColumn(userSpecifiedNameColumn);
        TableColumn datasetColumn = tcm.getColumn(3);
        tcm.removeColumn(datasetColumn);
        TableColumn pathColumn = tcm.getColumn(3);
        tcm.removeColumn(pathColumn);
        TableColumn archiveColumn = tcm.getColumn(3);
        tcm.removeColumn(archiveColumn);
        
        // Add the table to the scollpane
        JScrollPane scrollPane = new JScrollPane(getQueue());

        queuePanel.add(scrollPane);
                        
        JPanel importPanel = new JPanel();
        importPanel.setLayout(new BoxLayout(importPanel, BoxLayout.LINE_AXIS));
        clearDoneBtn = GuiCommonElements.addBasicButton("Clear Done", null, null);
        clearFailedBtn = GuiCommonElements.addBasicButton("Clear Failed", null, null);
        importBtn = GuiCommonElements.addBasicButton("Import", null, null);
        importPanel.add(Box.createHorizontalGlue());
        importPanel.add(clearDoneBtn);
        clearDoneBtn.setEnabled(false);
        clearDoneBtn.setActionCommand(FileQueueHandler.CLEARDONE);
        clearDoneBtn.addActionListener(this);
        clearDoneBtn.setToolTipText("Clear all 'done' entries from the import queue.");
        importPanel.add(Box.createRigidArea(new Dimension(0,5)));
        importPanel.add(clearFailedBtn);
        clearFailedBtn.setEnabled(false);
        clearFailedBtn.setActionCommand(FileQueueHandler.CLEARFAILED);
        clearFailedBtn.addActionListener(this);
        clearFailedBtn.setToolTipText("Clear all 'failed' entries from the import queue.");
        importPanel.add(Box.createRigidArea(new Dimension(0,10)));
        importPanel.add(importBtn);
        importBtn.setEnabled(false);
        importBtn.setActionCommand(FileQueueHandler.IMPORT);
        importBtn.addActionListener(this);
        importBtn.setToolTipText("Begin importing files.");
        GuiCommonElements.enterPressesWhenFocused(importBtn);
        queuePanel.add(Box.createRigidArea(new Dimension(0,5)));
        queuePanel.add(importPanel);
        add(queuePanel);
    }
    
    /**
     * Set the progress 'max planes' for file at row
     * 
     * @param row - row in table
     * @param maxPlanes - max planes to set
     */
    public void setProgressInfo(int row, int maxPlanes)
    {
        this.row = row;
        this.maxPlanes = maxPlanes;
    }
 
    /**
     * Set progress of an 'added' row to 'pending'
     * 
     * @param row - row to set in file queue
     * @return return false if row was set for 'added' before attempt
     */
    public boolean setProgressPending(int row)
    {
        if (getTable().getValueAt(row, 2).equals("added"))
        {
            getTable().setValueAt("pending", row, 2); 
            return true;
        }
        return false;
            
    }
    
    /**
     * Set file status at row in 'invalid'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressInvalid(int row)
    {
        if (getTable().getValueAt(row, 2).equals("added"))
            getTable().setValueAt("invalid format", row, 2);    
    }
    
    /**
     * Set import progress of current row in file queue
     * 
     * @param count total number of files in series
     * @param series current file in series
     * @param step current plane (out of maxPlanes) in the file
     */
    public void setImportProgress(int count, int series, int step)
    {
        String text;
        if (count > 1)
            text = series + 1 + "/" + count + ": " + step + "/" + maxPlanes;
        else
            text = step + "/" + maxPlanes;
        getTable().setValueAt(text, row, 2);   
    }

    /**
     *Set progress for the file at row to 'failed'
     *
     * @param row in file queue to set status on
     */
    public void setProgressFailed(int row)
    {
     	getTable().setValueAt("failed", row, 2);
        failedFiles = true;
        getTable().fireTableDataChanged();
    }
    
    /**
     * Set progress for the file at row to 'unknown'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressUnknown(int row)
    {
        getTable().setValueAt("unreadable", row, 2);
        failedFiles = true;
        getTable().fireTableDataChanged();
    }    
        
    /**
     * Set progress for the file at row to 'prepping'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressPrepping(int row)
    {
        getTable().setValueAt("prepping", row, 2); 
    }

    /**
     * Set progress for the file at row to 'done'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressDone(int row)
    {
        getTable().setValueAt("done", row, 2);
        doneFiles = true;
        getTable().fireTableDataChanged();
    }

    /**
     * Set progress for the file at row to 'updating db'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressSaveToDb(int row)
    {
        getTable().setValueAt("updating db", row, 2);       
    }
    
    /**
     * Set progress for the file at row to 'overlays'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressOverlays(int row)
    {
        getTable().setValueAt("overlays", row, 2);       
    }
    
    /**
     * Set progress for the file at row to 'archiving'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressArchiving(int row)
    {
        getTable().setValueAt("archiving", row, 2);       
    }
    
    /**
     * Set progress for the file at row to 'thumbnailing'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressResettingDefaults(int row)
    {
        getTable().setValueAt("thumbnailing", row, 2);       
    }

    /**
     * Set progress for the file at row to 'analyzing'
     * 
     * @param row in file queue to set status on
     */
    public void setProgressAnalyzing(int row)
    {
        getTable().setValueAt("analyzing", row, 2); 
    }
        
    public int getMaximumPlanes()
    {
        return maxPlanes;
    }

    /**
     * Retrieves the import containers from the table.
     * 
     * @return ImportContainer
     */
    public ImportContainer[] getImportContainersFromTable() {

        int num = getTable().getRowCount();     
        ImportContainer[] importContainer = new ImportContainer[num];

        for (int i = 0; i < num; i++)
        {
            importContainer[i] = (ImportContainer) getTable().getValueAt(i, 3);
        }
        return importContainer;
    }

    /**
     * Fire property changes on action events for buttons and ui
     * 
     * @param event
     */
    public void actionPerformed(ActionEvent event)
    {
        Object src = event.getSource();
        if (src == addBtn)
            firePropertyChange(FileQueueHandler.ADD, false, true);
        if (src == removeBtn)
            firePropertyChange(FileQueueHandler.REMOVE, false, true);
        if (src == clearDoneBtn)
            firePropertyChange(FileQueueHandler.CLEARDONE, false, true);
        if (src == clearFailedBtn)
            firePropertyChange(FileQueueHandler.CLEARFAILED, false, true);
        if (src == importBtn)
        {
            getQueue().clearSelection();
            firePropertyChange(FileQueueHandler.IMPORT, false, true);
        }
        //if (src == refreshBtn)
        //firePropertyChange(FileQueueHandler.REFRESH, false, true);
    }
    

    /**
     * @param row
     */
    public void centerOnRow(int row)
    {
        getQueue().getSelectionModel().setSelectionInterval(row, row);
        Rectangle visibleRect = getQueue().getVisibleRect();
        int centerY = visibleRect.y + visibleRect.height/2;
        Rectangle cellRect = getQueue().getCellRect(row, 0, true);
        if (centerY < cellRect.y)
        {
            // need to scroll up
            cellRect.y = cellRect.y - visibleRect.y + centerY;
        }
        else
        {
            // need to scroll down
            cellRect.y = cellRect.y + visibleRect.y - centerY;                    
        }
        getQueue().scrollRectToVisible(cellRect);
    }
        
    public void update(IObservable importLibrary, ImportEvent event)
    {
        // TODO : Here we should check for "cancel" and if so
        // raise some form of exception. This is currently being
        // done in a similar way in ImportHandler with an anonymous
        // inner class.
        
        // TODO: all these setProgress methods could take a base
        // ImportEvent class PROGRESS_EVENT and then we wouldn't
        // need to do the instanceof's here.
        if (event instanceof ImportEvent.LOADING_IMAGE) {
            ImportEvent.LOADING_IMAGE ev = (ImportEvent.LOADING_IMAGE) event;
            setProgressPrepping(ev.index);
        }
        else if (event instanceof ImportEvent.LOADED_IMAGE) {
            ImportEvent.LOADED_IMAGE ev = (ImportEvent.LOADED_IMAGE) event;
            setProgressAnalyzing(ev.index);
        }
        else if (event instanceof ImportEvent.DATASET_STORED) {
            ImportEvent.DATASET_STORED ev = (ImportEvent.DATASET_STORED) event;
            setProgressInfo(ev.index, ev.size.imageCount);
        }
        else if (event instanceof ImportEvent.IMPORT_STEP) {
            ImportEvent.IMPORT_STEP ev = (ImportEvent.IMPORT_STEP) event;
            if (ev.step <= getMaximumPlanes()) 
            {   
                setImportProgress(ev.seriesCount, ev.series, ev.step);
            }
        }
        else if (event instanceof ImportEvent.IMPORT_DONE) {
            ImportEvent.IMPORT_DONE ev = (ImportEvent.IMPORT_DONE) event;
            setProgressDone(ev.index);
        }
        else if (event instanceof ImportEvent.IMPORT_ARCHIVING) {
            ImportEvent.IMPORT_ARCHIVING ev = (ImportEvent.IMPORT_ARCHIVING) event;
            setProgressArchiving(ev.index);
        }
        else if (event instanceof ImportEvent.BEGIN_SAVE_TO_DB) {
            ImportEvent.BEGIN_SAVE_TO_DB ev = (ImportEvent.BEGIN_SAVE_TO_DB) event;
        	setProgressSaveToDb(ev.index);
        }
        else if (event instanceof ImportEvent.IMPORT_OVERLAYS) {
            ImportEvent.IMPORT_OVERLAYS ev = (ImportEvent.IMPORT_OVERLAYS) event;
        	setProgressOverlays(ev.index);
        }
        else if (event instanceof ImportEvent.IMPORT_THUMBNAILING) {
            ImportEvent.IMPORT_THUMBNAILING ev = (ImportEvent.IMPORT_THUMBNAILING) event;
        	setProgressResettingDefaults(ev.index);
        }
        else if (event instanceof ImportEvent.IMPORT_QUEUE_STARTED)
        {
            importBtn.setText("Cancel");
            importing = true;
            // addBtn.setEnabled(false);
        }
        else if (event instanceof ImportEvent.IMPORT_QUEUE_DONE)
        {
            // addBtn.setEnabled(true);
            importBtn.setText("Import");
            importBtn.setEnabled(true);
            getQueue().setRowSelectionAllowed(true);
            removeBtn.setEnabled(true);
            if (failedFiles == true)
                clearFailedBtn.setEnabled(true);
            if (doneFiles == true)
                clearDoneBtn.setEnabled(true);
            importing = false;
            cancel = false;
            abort = false;
        }
        else if (event instanceof ImportEvent.GROUP_SET)
        {
        	ImportEvent.GROUP_SET ev = (ImportEvent.GROUP_SET) event;
        	updateGroupBtn(ev.groupName, ev.groupType);
        }
        
    }
	private void updateGroupBtn(String groupName, int groupLevel) {
    	groupBtn.setText(groupName);
    	IconManager icons = IconManager.getInstance();
    	Icon groupIcon = null;
    	
    	if (groupLevel == ImportEvent.GROUP_PUBLIC)
    		groupIcon = icons.getIcon(IconManager.PUBLIC_GROUP);
    	if (groupLevel == ImportEvent.GROUP_COLLAB_READ)
    		groupIcon = icons.getIcon(IconManager.READ_GROUP);
    	if (groupLevel == ImportEvent.GROUP_COLLAB_READ_LINK)
    		groupIcon = icons.getIcon(IconManager.READ_LINK_GROUP);
    	if (groupLevel == ImportEvent.GROUP_PRIVATE)
    		groupIcon = icons.getIcon(IconManager.PRIVATE_GROUP);
    	
    	groupBtn.setIcon(groupIcon);
    	groupBtn.setPreferredSize(new Dimension(160, 20));
    	
    	// TODO: For now disable the button
    	DefaultButtonModel model = new DefaultButtonModel()
    	{
			private static final long serialVersionUID = 1L;
			public void setArmed(boolean armed) {}
    		public void setPressed(boolean pressed) {}
    		public void setRollover(boolean rollover) {}
    	};
    	groupBtn.setModel(model);
    	groupBtn.invalidate();
    	groupBtn.setFocusable(false);
	}
	
	/**
     * Get the renderer used for rendering header cells
     * @return
     */
    public MyTableHeaderRenderer getHeaderCellRenderer()
    {
        return headerCellRenderer;
    }

    /**
     * Get the renderer used for rendering the file column cells
     * @return
     */
    public LeftDotRenderer getFileCellRenderer()
    {
        return fileCellRenderer;
    }

    /**
     * Get the renderer used for rendering the dataset/project column cells
     * @return
     */
    public CenterTextRenderer getDpCellRenderer()
    {
        return dpCellRenderer;
    }

    /**
     * Get the renderer used for rendering the status line column cells
     * @return
     */
    public CenterTextRenderer getStatusCellRenderer()
    {
        return statusCellRenderer;
    }
    
    /**
     * Set up queue table model
     * 
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    protected static class QueueTableModel extends DefaultTableModel implements TableModelListener {
        
        private static final long serialVersionUID = 1L;
        
        private String[] columnNames = {"Files in Queue", 
        		"Project/Dataset or Screen", "Status", "DatasetNum", "Path", 
        		"Archive", "ProjectNum", "UserPixels", "UserSpecifiedName"};

        /* (non-Javadoc)
         * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
         */
        public void tableChanged(TableModelEvent arg0) { }
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnCount()
         */
        public int getColumnCount() { return columnNames.length; }

        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#getColumnName(int)
         */
        public String getColumnName(int col) { return columnNames[col]; }
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
         */
        public boolean isCellEditable(int row, int col) { return false; }
        
        /**
         * Allow rows to be selected
         * 
         * @return
         */
        public boolean rowSelectionAllowed() { return true; }
    }
 
    /**
     * Set up queue table headers
     * 
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    private static class MyTableHeaderRenderer extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

        	if (table == null) return null;
        	
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
            
            setEnabled(table == null || table.isEnabled());
                        
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
    
// Internal Helper Classes
    
    /**
     * Set up left dot column for long file names
     * 
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    private static class LeftDotRenderer extends DefaultTableCellRenderer
    {   
    	private static final long serialVersionUID = 1L;
    	
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
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
            ImportContainer ic = (ImportContainer) getQueue().getModel().getValueAt(row, 3);
            setToolTipText(ic.getFile().getAbsolutePath());
            
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
            
            if (getQueue().getValueAt(row, 2).equals("done"))
            { this.setEnabled(false); } 
            else
            { this.setEnabled(true); }

            if (getQueue().getValueAt(row, 2).equals("failed"))
            { setForeground(Color.red);} 
            else if (getQueue().getValueAt(row, 2).equals("unreadable"))
            { setForeground(ETable.DARK_ORANGE);} 
            else
            { setForeground(null);}
            
            return this;
        }
    }
    
    /**
     * Set up center aligned column
     * 
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    private static class CenterTextRenderer extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.

        private static final long serialVersionUID = 1L;
        
        /* (non-Javadoc)
         * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
         */
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        	
            setFont(UIManager.getFont("TableCell.font"));
            setHorizontalAlignment(DefaultTableCellRenderer.CENTER);
            
            // Set tool tip if desired
            setToolTipText((String)value);
            
            if (getQueue().getValueAt(row, 2).equals("done"))
            { this.setEnabled(false); } 
            else
            { this.setEnabled(true); }

            if (getQueue().getValueAt(row, 2).equals("failed"))
            { setForeground(Color.red);} 
            else if (getQueue().getValueAt(row, 2).equals("unreadable"))
            { setForeground(ETable.DARK_ORANGE);} 
            else
            { setForeground(null);}
            
            // Since the renderer is a component, return itself
            return this;
        }
    }

    /**
     * Selection listener for cells
     * 
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    private static class SelectionListener implements ListSelectionListener {
    	
        JTable table;
        
        /**
         * It is necessary to pass in the table since it is not possible
         * to determine the table from the event's source
         * 
         * @param table
         */
        SelectionListener(JTable table) 
        {
            this.table = table;
        }
        
        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
        public void valueChanged(ListSelectionEvent e) 
        {
            // If cell selection is enabled, both row and column change events are fired
            if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) 
            {
                    dselectRows();
            } 
        }
        
        /**
         * Only select rows with a status of 'added' or 'pending'
         */
        private void dselectRows()
        {
            // Column selection changed
            int rows = getQueue().getRowCount();

            for (int i = 0; i < rows; i++ )
            {
                try
                {
                    if (!(getQueue().getValueAt(i, 2).equals("added") ||
                            getQueue().getValueAt(i, 2).equals("pending")) 
                            && table.getSelectionModel().isSelectedIndex(i))
                    {
                        table.getSelectionModel().removeSelectionInterval(i, i);
                    }
                } catch (ArrayIndexOutOfBoundsException e)
                {
                	log.error("Error deselecting rows in table.", e);
                }
            }
        }
    }
    
    /**
     * Main for testing (debugging only)
     * 
     * @param args
     * @throws Exception 
     */
    public static void main (String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        try 
        {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { 
        	System.err.println(laf + " not supported."); 
        }
        
        FileQueueTable q = new FileQueueTable(); 
        JFrame f = new JFrame();   
        f.getContentPane().add(q);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }

	/**
	 * @param table the table to set
	 */
	public static void setTable(QueueTableModel table) {
		FileQueueTable.table = table;
	}

	/**
	 * @return the table
	 */
	public static QueueTableModel getTable() {
		return table;
	}

	/**
	 * @param queue the queue to set
	 */
	public static void setQueue(ETable queue) {
		FileQueueTable.queue = queue;
	}

	/**
	 * @return the queue
	 */
	public static ETable getQueue() {
		return queue;
	}
}
