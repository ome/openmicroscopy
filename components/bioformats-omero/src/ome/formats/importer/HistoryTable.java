package ome.formats.importer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdesktop.swingx.JXDatePicker;

import layout.TableLayout;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.util.Actions;
import ome.formats.importer.util.ETable;
import ome.formats.importer.util.GuiCommonElements;


public class HistoryTable
    extends JPanel
    implements ActionListener, PropertyChangeListener, IObserver
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public HistoryTableModel table = new HistoryTableModel();
    public ETable eTable = new ETable(table);
    
    private static final String DATE_FORMAT = "yy/MM/dd";
    
    GuiCommonElements gui;
    
 // ----- Variables -----
    // Debug Borders
    Boolean debug = false;
    
    // Size of the add/remove/refresh buttons (which are square).
    int buttonSize = 40;
    
    // width of certain columns
    int statusWidth = 100;
    int dateWidth = 180;
    
    Main    viewer;
    

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
    JButton         clearBtn;
    
    public static HistoryDB db = null;
    long experimenterID;
    private OMEROMetadataStore store;
    JOutlookBar outlookBar = new JOutlookBar();

    JList todayList = new JList(outlookBar.today);
    JList yesterdayList = new JList(outlookBar.yesterday);
    JList thisWeekList = new JList(outlookBar.thisWeek);
    JList lastWeekList = new JList(outlookBar.lastWeek);
    JList thisMonthList = new JList(outlookBar.thisMonth);
    
    HistoryTable(Main viewer)
    {
        this.viewer = viewer;
        
        try {
            viewer.fileQueueHandler.addPropertyChangeListener(this);
            outlookBar.addPropertyChangeListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        db = HistoryDB.getHistoryDB();

        viewer.loginHandler.addObserver(this);
        db.addObserver(this);
        
        gui = new GuiCommonElements();
        
        // set to layout that will maximize on resizing
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setOpaque(false);
        
        // Main Panel containing all elements  
        // Set up the main panel layout
        double mainTable[][] =
                {{170, 10, TableLayout.FILL, 80}, // columns
                { 5, 30, 35, 40, TableLayout.FILL, 35, 5}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 0,0,0,0, debug); 

        // *****Side Panel****
        double topSideTable[][] = 
                {{TableLayout.FILL}, // columns
                {20, 20, 20, 20}}; // rows      
        
        topSidePanel = gui.addBorderedPanel(mainPanel, topSideTable, " Date Filter ", debug);
        
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
        
        outlookBar.addBar( "Today", outlookBar.getListPanel(todayList));
        outlookBar.addBar( "Yesterday", outlookBar.getListPanel(yesterdayList));
        outlookBar.addBar( "This Week", outlookBar.getListPanel(thisWeekList));
        outlookBar.addBar( "Last Week", outlookBar.getListPanel(lastWeekList));
        outlookBar.addBar( "This Month", outlookBar.getListPanel(thisMonthList));
        outlookBar.setVisibleBar(0);
        
        bottomSidePanel = gui.addBorderedPanel(mainPanel, bottomSideTable, " Quick Date ", debug);
        
        bottomSidePanel.add(outlookBar, "f,f");       
        
        clearBtn = gui.addIconButton(mainPanel, "Wipe History", clearIcon, 
                32, 110, (int)'S', "Click here to clear your history log.", "0,5,c,c", debug);   
        
        clearBtn.setActionCommand(Actions.CLEARHISTORY);
        clearBtn.addActionListener(this);
        
        // *****Top right most row containing search field and buttons*****
        searchField = gui.addTextField(mainPanel, "Name Filter: ", "*.*", 'N', 
                "Type in a file name to search for here.", "", 
                TableLayout.PREFERRED, "2,1, 0, 0", debug);

        searchBtn = gui.addButton(mainPanel, "Search", 'S', "Click here to search", "3,1,c,c", debug);
        
        searchBtn.setActionCommand(Actions.HISTORYSEARCH);
        searchBtn.addActionListener(this);
        
        // *****Middle right row containing the filter options*****
        // Since this panel has a different layout, use a new panel for it

        // Set up the filterTable layout
        double filterTable[][] =
                {{100, 70, 70, 70, 90, TableLayout.FILL}, // columns
                { 30 }}; // rows
        
        filterPanel = gui.addPlanePanel(mainPanel, filterTable, debug);     
        filterLabel = gui.addTextPane(filterPanel, "Status Filters: ", "0,0,r,c", debug);
        
        doneCheckBox = gui.addCheckBox(filterPanel, "Done", "1,0,l,c", debug);
        failedCheckBox = gui.addCheckBox(filterPanel, "Failed", "2,0,l,c", debug);
        invalidCheckBox = gui.addCheckBox(filterPanel, "Invalid", "3,0,l,c", debug);
        pendingCheckBox = gui.addCheckBox(filterPanel, "Pending", "4,0,1,c", debug);
        
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

        mainPanel.add(scrollPane, "2,3,3,5");
        mainPanel.add(bottomSidePanel, "0,4,0,0"); 
        mainPanel.add(topSidePanel, "0,0,0,3");
        mainPanel.add(filterPanel, "2,2,3,1");
        
        this.add(mainPanel);
    }
    
    private void getExperimenterID()
    {
        try {
            store = viewer.loginHandler.getMetadataStore();
            this.experimenterID = store.getExperimenterID();
        } catch (NullPointerException e)
        {
            this.experimenterID = -1;
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (src == searchBtn || src == doneCheckBox || src == failedCheckBox 
                || src == invalidCheckBox || src == pendingCheckBox)
            //System.err.println(fromDate.getDate());
            getFileQuery(-1, experimenterID, searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        if (src == clearBtn)
            ClearHistory();
    }

    private void ClearHistory()
    {
        String message = "This will delete your import history. \n" +
                "Are you sure you want to continue?";
        Object[] o = {"Yes", "No"};
        
        int result = JOptionPane.showOptionDialog(this, message, "Warning", -1,
                JOptionPane.WARNING_MESSAGE,null,o,o[1]);
        if (result == 0) //yes clicked
        {
            db.wipeUserHistory(experimenterID);
            updateOutlookBar();
            getFileQuery(-1, experimenterID, searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        }
    }
    
    class HistoryTableModel 
    extends DefaultTableModel 
    implements TableModelListener {
    
    private static final long serialVersionUID = 1L;
    private String[] columnNames = {"File Name", "Project/Dataset", "Import Date/Time", "Status"};

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
 
    class LeftDotRenderer 
    extends DefaultTableCellRenderer
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

    /**
     * @param args
     * @return 
     */
    
    public void getImportQuery(long ExperimenterID)
    {   
        try {
            ResultSet rs = db.getImportResults(db, "import_table", ExperimenterID);

            Vector<Object> row = new Vector<Object>();
            
            int count = table.getRowCount();
            for (int r = count - 1; r >= 0; r--)
            {
                table.removeRow(r);
            }
           
            // the result set is a cursor into the data.  You can only
            // point to one row at a time
            // assume we are pointing to BEFORE the first row
            // rs.next() points to next row and returns true
            // or false if there is no next row, which breaks the loop
            for (; rs.next(); ) {
                row.add(rs.getObject("date"));
                row.add(rs.getObject("status"));
                table.addRow(row);
            }
            rs.close();
            db.shutdown();
        } catch (SQLException ex3) {
            ex3.printStackTrace();
        } catch (NullPointerException ex4) {} // results are null
    }
    
    public void getFileQuery(int importID, long experimenterID, String string, Date from, Date to)
    {   
        try {
            ResultSet rs = db.getFileResults(db, "file_table", importID, experimenterID, string, 
                    doneCheckBox.isSelected(), failedCheckBox.isSelected(), invalidCheckBox.isSelected(),
                    pendingCheckBox.isSelected(), from, to);
            
            // the order of the rows in a cursor
            // are implementation dependent unless you use the SQL ORDER statement
            //ResultSetMetaData meta = rs.getMetaData();
            
            int count = table.getRowCount();
            for (int r = count - 1; r >= 0; r--)
            {
                table.removeRow(r);
            }
           
            // Format the current time.
            String dayString, hourString, datasetName= "", projectName = "";
            long oldDatasetID = 0, datasetID = 0, oldProjectID = 0, projectID = 0;
            
            // the result set is a cursor into the data.  You can only
            // point to one row at a time
            // assume we are pointing to BEFORE the first row
            // rs.next() points to next row and returns true
            // or false if there is no next row, which breaks the loop
            for (; rs.next() ;) {
                datasetID = rs.getLong("datasetID");
                projectID = rs.getLong("projectID");
                
                if (oldDatasetID != datasetID)
                {
                    oldDatasetID = datasetID;
                    datasetName = store.getDatasetName(rs.getLong("datasetID"));                    
                }
                
                if (oldProjectID != projectID)
                {
                    oldProjectID = projectID;
                    projectName = store.getProjectName(rs.getLong("projectID"));
                }
                
                dayString = db.day.format(rs.getObject("date"));
                hourString = db.hour.format(rs.getObject("date"));

                if (db.day.format(new Date()).equals(dayString))
                    dayString = "Today";
                
                if (db.day.format(db.getYesterday()).equals(dayString))
                {
                    dayString = "Yesterday";
                }
                
                Vector<Object> row = new Vector<Object>();
                row.add(rs.getObject("filename"));
                row.add(projectName + "/" + datasetName);
                row.add(dayString + " " + hourString);
                row.add(rs.getObject("status"));
                table.addRow(row);
                table.fireTableDataChanged();
            }
            
            rs.close();
            //db.shutdown();
        } catch (SQLException ex3) {
            ex3.printStackTrace();
        } catch (NullPointerException ex4) {
            ex4.printStackTrace();
        } // results are null
    }
    
    private void updateOutlookBar()
    {
        GregorianCalendar newCal = new GregorianCalendar( );
        int dayOfWeek = newCal.get( Calendar.DAY_OF_WEEK );
        int dayOfMonth = newCal.get( Calendar.DAY_OF_MONTH);
        
        DefaultListModel today = db.getImportListByDate(db.getDaysBefore(new Date(), 1), new Date());
        outlookBar.updatePanelList(todayList, outlookBar.today, today);

        DefaultListModel yesterday = db.getImportListByDate(new Date(), db.getYesterday());
        outlookBar.updatePanelList(yesterdayList, outlookBar.yesterday, yesterday);

        DefaultListModel thisWeek = db.getImportListByDate(db.getDaysBefore(new Date(), 1), db.getDaysBefore(new Date(), -(dayOfWeek)));
        outlookBar.updatePanelList(thisWeekList, outlookBar.thisWeek, thisWeek);

        DefaultListModel lastWeek = db.getImportListByDate(db.getDaysBefore(new Date(), -(dayOfWeek)), 
                db.getDaysBefore(new Date(), -(dayOfWeek+7)));
        outlookBar.updatePanelList(lastWeekList, outlookBar.lastWeek, lastWeek);
        
        DefaultListModel thisMonth = db.getImportListByDate(db.getDaysBefore(new Date(), 1), db.getDaysBefore(new Date(), -(dayOfMonth)));
        outlookBar.updatePanelList(thisMonthList, outlookBar.thisMonth, thisMonth);
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        //System.err.println(e.getPropertyName());
        String prop = e.getPropertyName();
        if (prop.equals("QUICK_HISTORY"))
            getQuickHistory((Integer)e.getNewValue());
        if (prop.equals("date"))
        {
            //System.err.println(fromDate.getDate());
            getFileQuery(-1, experimenterID, searchField.getText(), 
                    fromDate.getDate(), toDate.getDate());
        }
            
    }

    private void getQuickHistory(Integer importKey)
    {
       getFileQuery(importKey, experimenterID, null, null, null);
    }

    public void update(IObservable importLibrary, Object message, Object[] args)
    {
        //System.err.print("Update: " + message + "\n");
        getExperimenterID();
        if (experimenterID != -1 && message == "LOGGED_IN" || message == "QUICKBAR_UPDATE")
            {
                updateOutlookBar();
            }
    }   
    
}
