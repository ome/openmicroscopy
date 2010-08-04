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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ETable;
import omero.ServerError;
import omero.model.Dataset;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingx.JXDatePicker;


/**
 * @author Brian W. Loranger
 *
 */
public class HistoryTable
    extends JPanel
    implements ActionListener, PropertyChangeListener, IObserver, IObservable, ListSelectionListener
{
	/** Logger for this class */
	private static Log log = LogFactory.getLog(HistoryTable.class);
	
    final ArrayList<IObserver> observers = new ArrayList<IObserver>();

    public HistoryTableModel table = new HistoryTableModel();
    public ETable eTable = new ETable(table);
    
    private static final String DATE_FORMAT = "yy/MM/dd";
    
 // ----- Variables -----
    // Debug Borders
    Boolean debug = false;
    
    // Size of the add/remove/refresh buttons (which are square).
    int buttonSize = 40;
    
    // width of certain columns
    int statusWidth = 100;
    int dateWidth = 180;

    // Add graphic for add button
    String searchIcon = "gfx/add.png";
    // Remove graphics for remove button
    String clearIcon = "gfx/nuvola_editdelete16.png";
    
    JPanel                  mainPanel;
    JPanel                  topSidePanel;
    JPanel                  bottomSidePanel;
    JPanel                  filterPanel;
    
    JTextPane               sideLabel;

    JLabel                  fromLabel;
    JLabel                  toLabel;
    
    JXDatePicker            fromDate;
    JXDatePicker            toDate;
    
    JTextField              searchField;
    
    JTextPane               filterLabel;
    JCheckBox               doneCheckBox;
    JCheckBox               failedCheckBox;
    JCheckBox               invalidCheckBox;
    JCheckBox               pendingCheckBox;
    
    JButton         searchBtn;
    JButton         reimportBtn;
    JButton         clearBtn;
    
    /**
     * THIS SHOULD NOT BE VISIBLE!
     */
    //final HistoryDB db;
    final HistoryTableStore db;
    
    private final GuiImporter viewer;
    private final HistoryTaskBar historyTaskBar = new HistoryTaskBar();

    JList todayList = new JList(historyTaskBar.today);
    JList yesterdayList = new JList(historyTaskBar.yesterday);
    JList thisWeekList = new JList(historyTaskBar.thisWeek);
    JList lastWeekList = new JList(historyTaskBar.lastWeek);
    JList thisMonthList = new JList(historyTaskBar.thisMonth);
    private boolean unknownProjectDatasetFlag;

    /**
     * Create history table
     * 
     * @param viewer- GuiImporter parent
     */
    HistoryTable(GuiImporter viewer)
    {
        this.viewer = viewer;
        try {
            historyTaskBar.addPropertyChangeListener(this);
        } catch (Exception ex) {
        	log.error("Exception adding property change listener.", ex);
        }

        HistoryTableStore db = null;
        //HistoryDB db = null;
        try {
        	db = new HistoryTableStore();
        	db.addObserver(this);
        } catch (Exception e) {
            db = null;
            log.error("Could not start history DB.", e);
            if (HistoryDB.alertOnce == false)
            {
                JOptionPane.showMessageDialog(null,
                    "We were not able to connect to the history DB.\n" +
                    "In the meantime, you will still be able to use \n" +
                    "the importer, but the history feature will be disable.",
                    "Warning",
                    JOptionPane.ERROR_MESSAGE);
                HistoryDB.alertOnce = true;
            }
        }
        
        this.db = db;
        
        // set to layout that will maximize on resizing
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setOpaque(false);
        
        // Main Panel containing all elements  
        // Set up the main panel layout
        double mainTable[][] =
                {{170, 10, TableLayout.FILL, 80}, // columns
                { 5, 30, 35, 40, TableLayout.FILL, 35, 5}}; // rows
        
        mainPanel = GuiCommonElements.addMainPanel(this, mainTable, 0,0,0,0, debug); 

        // *****Side Panel****
        double topSideTable[][] = 
                {{TableLayout.FILL}, // columns
                {20, 20, 20, 20}}; // rows      
        
        topSidePanel = GuiCommonElements.addBorderedPanel(mainPanel, topSideTable, " Date Filter ", debug);
        
        String[] dateFormats = new String[1];
        dateFormats[0] = DATE_FORMAT;
        
        fromDate = new JXDatePicker();
        fromDate.setToolTipText("Pick a from date.");
        //fromDate.getEditor().setEditable(false);
        //fromDate.setEditable(false);
        fromDate.setFormats(dateFormats);

        toDate = new JXDatePicker();
        toDate.setToolTipText("Pick a to date.");
        //toDate.getEditor().setEditable(false);
        //toDate.setEditable(false);
        toDate.setFormats(dateFormats);
        
        fromLabel = new JLabel("From (yy/mm/dd):");
        
        topSidePanel.add(fromLabel, "0,0");
        topSidePanel.add(fromDate, "0,1");

        toLabel = new JLabel("To (yy/mm/dd):");
        
        topSidePanel.add(toLabel, "0,2");
        topSidePanel.add(toDate, "0,3");
        
        double bottomSideTable[][] = 
        {{TableLayout.FILL}, // columns
        {TableLayout.FILL}}; // rows 
        
        historyTaskBar.addTaskPane( "Today", historyTaskBar.getList(todayList));
        historyTaskBar.addTaskPane( "Yesterday", historyTaskBar.getList(yesterdayList));
        historyTaskBar.addTaskPane( "This Week", historyTaskBar.getList(thisWeekList));
        historyTaskBar.addTaskPane( "Last Week", historyTaskBar.getList(lastWeekList));
        historyTaskBar.addTaskPane( "This Month", historyTaskBar.getList(thisMonthList));
        
        bottomSidePanel = GuiCommonElements.addBorderedPanel(mainPanel, bottomSideTable, " Quick Date ", debug);

        /*
        JPanel taskPanel = new JPanel( new BorderLayout() );
        JScrollPane taskScrollPane = new JScrollPane();
        taskScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        taskScrollPane.getViewport().add(historyTaskBar);
        taskPanel.add(taskScrollPane);
        
        bottomSidePanel.add(taskPanel, "f,f");
        taskPanel.validate();
        */
        
        
        bottomSidePanel.add(historyTaskBar, "0,0");  
        
        clearBtn = GuiCommonElements.addIconButton(mainPanel, "Wipe History", clearIcon, 
                130, 32, (int)'S', "Click here to clear your history log.", "0,5,C,C", debug);   
        
        clearBtn.setActionCommand(HistoryHandler.CLEARHISTORY);
        clearBtn.addActionListener(this);
        
        // *****Top right most row containing search field and buttons*****
        searchField = GuiCommonElements.addTextField(mainPanel, "Name Filter: ", "*.*", 'N', 
                "Type in a file name to search for here.", "", 
                TableLayout.PREFERRED, "2,1, 0, 0", debug);

        searchBtn = GuiCommonElements.addButton(mainPanel, "Search", 'S', "Click here to search", "3,1,C,C", debug);
        
        searchBtn.setActionCommand(HistoryHandler.HISTORYSEARCH);
        searchBtn.addActionListener(this);
        
        // *****Middle right row containing the filter options*****
        // Since this panel has a different layout, use a new panel for it

        // Set up the filterTable layout
        double filterTable[][] =
                {{100, 70, 70, 70, 90, TableLayout.FILL}, // columns
                { 30 }}; // rows
        
        filterPanel = GuiCommonElements.addPlanePanel(mainPanel, filterTable, debug);     
        filterLabel = GuiCommonElements.addTextPane(filterPanel, "Status Filters: ", "0,0,r,c", debug);
        
        doneCheckBox = GuiCommonElements.addCheckBox(filterPanel, "Done", "1,0,L,C", debug);
        failedCheckBox = GuiCommonElements.addCheckBox(filterPanel, "Failed", "2,0,L,C", debug);
        invalidCheckBox = GuiCommonElements.addCheckBox(filterPanel, "Invalid", "3,0,L,C", debug);
        pendingCheckBox = GuiCommonElements.addCheckBox(filterPanel, "Pending", "4,0,L,C", debug);
        
        // Default filters to 'on'
        doneCheckBox.setSelected(true);
        failedCheckBox.setSelected(true);
        invalidCheckBox.setSelected(true);
        pendingCheckBox.setSelected(true);
        
        doneCheckBox.addActionListener(this);
        failedCheckBox.addActionListener(this);
        invalidCheckBox.addActionListener(this);
        pendingCheckBox.addActionListener(this);
                
       // *****Bottom right most row containing the history table*****
        TableColumnModel cModel =  eTable.getColumnModel();
        
        // *** remove last 4 rows from display ***
        TableColumn hiddenColumn = cModel.getColumn(6);
        cModel.removeColumn(hiddenColumn);
        hiddenColumn = cModel.getColumn(5);
        cModel.removeColumn(hiddenColumn);
        hiddenColumn = cModel.getColumn(4);
        cModel.removeColumn(hiddenColumn);
        
        MyTableHeaderRenderer myHeader = new MyTableHeaderRenderer();
        
        // Create a custom header for the table
        cModel.getColumn(0).setHeaderRenderer(myHeader);
        cModel.getColumn(1).setHeaderRenderer(myHeader);
        cModel.getColumn(2).setHeaderRenderer(myHeader);  
        cModel.getColumn(3).setHeaderRenderer(myHeader); 

        cModel.getColumn(0).setCellRenderer(new LeftDotRenderer());
        cModel.getColumn(1).setCellRenderer(new TextCellCenter());
        cModel.getColumn(2).setCellRenderer(new TextCellCenter());
        cModel.getColumn(3).setCellRenderer(new TextCellCenter());   
        
        // Set the width of the status column
        TableColumn statusColumn = eTable.getColumnModel().getColumn(3);
        statusColumn.setPreferredWidth(statusWidth);
        statusColumn.setMaxWidth(statusWidth);
        statusColumn.setMinWidth(statusWidth);

        // Set the width of the status column
        TableColumn dateColumn = eTable.getColumnModel().getColumn(2);
        dateColumn.setPreferredWidth(dateWidth);
        dateColumn.setMaxWidth(dateWidth);
        dateColumn.setMinWidth(dateWidth);
                
        // Add the table to the scollpane
        JScrollPane scrollPane = new JScrollPane(eTable);

        reimportBtn = GuiCommonElements.addButton(filterPanel, "Reimport", 'R', "Click here to reimport selected images", "5,0,R,C", debug);
        reimportBtn.setEnabled(false);
        
        reimportBtn.setActionCommand(HistoryHandler.HISTORYREIMPORT);
        reimportBtn.addActionListener(this);
             
     // Handle the listener
		ListSelectionModel selectionModel = this.eTable.getSelectionModel();
		selectionModel.addListSelectionListener( this );

        
        mainPanel.add(scrollPane, "2,3,3,5");
        mainPanel.add(bottomSidePanel, "0,4,0,0"); 
        mainPanel.add(topSidePanel, "0,0,0,3");
        mainPanel.add(filterPanel, "2,2,3,1");
        
        this.add(mainPanel);
    }
 
    /**
     * Clear the history table of all data
     */
    private void ClearHistory()
    {
        String message = "This will delete your import history. \n" +
                "Are you sure you want to continue?";
        Object[] o = {"Yes", "No"};
        
        int result = JOptionPane.showOptionDialog(this, message, "Warning", -1,
                JOptionPane.WARNING_MESSAGE,null,o,o[1]);
        if (result == 0) //yes clicked
        {
            try {
				db.wipeDataSource(getExperimenterID());
			} catch (ServerError e) {
		        log.error("exception.", e);
			}
            updateOutlookBar();
            getItemQuery(-1, getExperimenterID(), searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        }
    }
    
   
    /**
     * Display base table query in table based on experimenter's id
     * 
     * @param ExperimenterID
     */
    @SuppressWarnings("unchecked")
	public void getBaseQuery(Long ExperimenterID)
    {   
        try {
        	
            int count = table.getRowCount();
            for (int r = count - 1; r >= 0; r--)
            {
                table.removeRow(r);
            }
           
            Vector<Object> query = db.getBaseQuery(ExperimenterID);
            int returnedRows = query.size();
            
            for (int i = 0; i < returnedRows; i++)
            {
            	Vector<Object> queryRow = (Vector<Object>) query.get(i);
            	Vector<Object> row = new Vector<Object>();
            	 
            	row.add(new Date((Long) queryRow.get(0)));
            	row.add((String) queryRow.get(1));
            	table.addRow(row);
            }
            
        } catch (NullPointerException npe) {
        	
        } // results are null
        catch (Exception e) {
        	log.error("exception.", e);
        }
    }
    
    /**
     * Do an item query and return results to table
     * 
     * @param importID - base ID
     * @param experimenterID - experimenter's id
     * @param queryString - query string for search
     * @param from - from date of search 
     * @param to - to date of search
     */
    @SuppressWarnings("unchecked")
	public void getItemQuery(long importID, long experimenterID, String queryString, Date from, Date to)
    {   
    	// Format the current time.
        String dayString, hourString, objectName= "", projectName = "", pdsString = "", fileName = "", filePath = "", status = "";
        long oldObjectID = 0L, objectID = 0L, oldProjectID = 0L, projectID = 0L, importTime = 0L;
        
        try {        	                                  
            for (int r = table.getRowCount() - 1; r >= 0; r--)
            {
                table.removeRow(r);
            }
            
            Vector<Object> query = db.getItemQuery(importID, experimenterID, queryString, from, to);
            int returnedRows = query.size();

            for (int i = 0; i < returnedRows; i++)
            {

            	Vector<Object> queryRow = (Vector<Object>) query.get(i);
            	
            	fileName = (String) queryRow.get(0);
            	importTime = (Long) queryRow.get(1);
            	status = (String) queryRow.get(2);
            	filePath = (String) queryRow.get(3);
            	objectID = (Long) queryRow.get(4);
            	projectID = (Long) queryRow.get(5);
            	
            	if (oldObjectID != objectID)
            	{
            		oldObjectID = objectID;
            		if (projectID != 0)
            		{
                        try {
                            objectName = getStore().getTarget(Dataset.class, objectID).getName().getValue();
                        } catch (Exception e)
                        {
                            objectName = "unknown";
                            displayAccessError();
                        } 
                        
                        if (oldProjectID != projectID)
                        {
                            oldProjectID = projectID;
                            try {
                                projectName = getStore().getProject(projectID).getName().getValue();
                            } catch (Exception e)
                            {
                                projectName = "unknown";
                                displayAccessError();
                            }
                        }
                    	
                        pdsString = projectName + "/" + objectName;
            		}
                    else
                    {
                        try {
                            objectName = getStore().getTarget(Screen.class, objectID).getName().getValue();
                        } catch (Exception e)
                        {
                            objectName = "unknown";
                            displayAccessError();
                        }   
                        
                        pdsString = objectName;
                    }
            	}
            	
                dayString = db.day.format(new Date(importTime));
                hourString = db.hour.format(new Date(importTime));

                if (db.day.format(new Date()).equals(dayString))
                    dayString = "Today";
                
                if (db.day.format(db.getYesterday()).equals(dayString))
                {
                    dayString = "Yesterday";
                }
                
                Vector<Object> row = new Vector<Object>();
                row.add(fileName);
                row.add(pdsString);
                row.add(dayString + " " + hourString);
                row.add(status);
                row.add(filePath);
                row.add(objectID);
                row.add(projectID);
                table.addRow(row);
                table.fireTableDataChanged();
                unknownProjectDatasetFlag = false;
            }
            
        } catch (NullPointerException npe) {
        	log.error("Null pointer exception.", npe);
        } // results are null
        catch (Exception e) {
        	log.error("exception.", e);
        }
    }
        
    /**
     * Display an access error the db is inaccessible
     */
    private void displayAccessError()
    {
        if (unknownProjectDatasetFlag) return;
        
        unknownProjectDatasetFlag = true;
        JOptionPane.showMessageDialog(null,
                "We were not able to retrieve the project/dataset for\n" +
                "one or more of the imports in this history selection.\n" +
                "The most likely cause is that the original project or\n" +
                "dataset was deleted.\n\n" +
                "As a result, the imported items in question cannot be\n" +
                "reimported automatically using the \"reimport\" button.\n\n" +
                "Click OK to continue.",
                "Warning",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Update the outlook bar with base data for appropriate dates
     */
    public void updateOutlookBar()
    {
        GregorianCalendar newCal = new GregorianCalendar( );
        int dayOfWeek = newCal.get( Calendar.DAY_OF_WEEK );
        int dayOfMonth = newCal.get( Calendar.DAY_OF_MONTH);
        
        DefaultListModel today = db.getBaseTableDataByDate(db.getDaysBefore(new Date(), -1), new Date());
        historyTaskBar.updateList(todayList, historyTaskBar.today, today);

        DefaultListModel yesterday = db.getBaseTableDataByDate(db.getYesterday(), new Date());
        historyTaskBar.updateList(yesterdayList, historyTaskBar.yesterday, yesterday);

        DefaultListModel thisWeek = db.getBaseTableDataByDate(db.getDaysBefore(new Date(), -(dayOfWeek)), db.getDaysBefore(new Date(), 1));
        historyTaskBar.updateList(thisWeekList, historyTaskBar.thisWeek, thisWeek);

        DefaultListModel lastWeek = db.getBaseTableDataByDate(db.getDaysBefore(new Date(), -(dayOfWeek+7)), db.getDaysBefore(new Date(), -(dayOfWeek)));
        historyTaskBar.updateList(lastWeekList, historyTaskBar.lastWeek, lastWeek);
        
        DefaultListModel thisMonth = db.getBaseTableDataByDate(db.getDaysBefore(new Date(), -(dayOfMonth)), db.getDaysBefore(new Date(), 1));
        historyTaskBar.updateList(thisMonthList, historyTaskBar.thisMonth, thisMonth);
    }

    /**
     * Retrieve base history using importkey
     * 
     * @param importKey - import key
     */
    private void getQuickHistory(Integer importKey)
    {
    		getItemQuery(importKey, getExperimenterID(), null, null, null);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        Object src = event.getSource();
        if (src == searchBtn || src == doneCheckBox || src == failedCheckBox 
                || src == invalidCheckBox || src == pendingCheckBox)
            getItemQuery(-1, getExperimenterID(), searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        if (src == clearBtn)
            ClearHistory();
        if (src == reimportBtn)
        {
            notifyObservers(new ImportEvent.REIMPORT());
        }
    }


    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();
        if (prop.equals("QUICK_HISTORY"))
            getQuickHistory((Integer)e.getNewValue());
        if (prop.equals("date"))
        {
            getItemQuery(-1, getExperimenterID(), searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        }
            
    }

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (this.eTable.getSelectedRowCount() > 0)
			reimportBtn.setEnabled(true);
		else
			reimportBtn.setEnabled(false);
	}
    
    // TODO: get rid of this
    /**
     * Return the OMERO Metadata Store
     * 
     * @return - OMEROMetadataStore
     */
    private OMEROMetadataStoreClient getStore() {
        return viewer.getLoginHandler().getMetadataStore();
    }
    
    /**
     * Return experiementer's id from store
     * 
     * @return - getStore().getExperimenterID()
     */
    private long getExperimenterID() {
        return getStore().getExperimenterID();
    }
    
    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    public void update(IObservable importLibrary, ImportEvent event)
    {
        long experimenterID = getExperimenterID();
        if (experimenterID != -1 && event instanceof ImportEvent.LOGGED_IN
                || event instanceof ImportEvent.QUICKBAR_UPDATE)
            {
        		if (db.historyEnabled) updateOutlookBar();
            }
    }

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
     * @author Brian W. Loranger
     *
     */
    static class HistoryTableModel extends DefaultTableModel implements TableModelListener 
    {
        
        private static final long serialVersionUID = 1L;
        private String[] columnNames = {"File Name", "Project/Dataset or Screen", "Import Date/Time", "Status", "FilePath", "DatasetID", "ProjectID"};
    
        /* (non-Javadoc)
         * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
         */
        public void tableChanged(TableModelEvent arg0) {}
        
        /** Always allow rows to be selected
         * 
         * @return - true 
         */
        public boolean rowSelectionAllowed() { return true; }
        
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
    }

    /**
     * @author Brian W. Loranger
     *
     */
    public class MyTableHeaderRenderer extends DefaultTableCellRenderer 
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
     * Create left dot aligned text cell for table
     * 
     * @author Brian W. Loranger
     *
     */
    class LeftDotRenderer extends DefaultTableCellRenderer
    {
        private static final long serialVersionUID = 1L;
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
            /*if (table.getValueAt(row, 2).equals("done"))
            { this.setEnabled(false);} 
            else
            { this.setEnabled(true); }
            */
            return this;
        }
    }

    /**
     * Create centered aligned text cell for table
     * 
     * @author Brian W. Loranger
     *
     */
    public class TextCellCenter extends DefaultTableCellRenderer 
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
            //setToolTipText((String)value);
            
            /*if (table.getValueAt(row, 2).equals("done") || 
                    table.getValueAt(row, 2).equals("failed"))
            { this.setEnabled(false); } 
            else
            { this.setEnabled(true); }
            */
            // Since the renderer is a component, return itself
            return this;
        }
    }    
}
