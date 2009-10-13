package ome.formats.importer.gui;

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

import layout.TableLayout;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ETable;
import ome.formats.importer.util.ErrorContainer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ErrorTable
    extends JPanel
    implements ActionListener, PropertyChangeListener, IObserver, IObservable, MouseListener
{
    /** Logger for this class */
    private static Log log = LogFactory.getLog(ErrorTable.class);

    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    private static final long serialVersionUID = 1L;
    public ErrorTableModel table = new ErrorTableModel();
    public ETable eTable = new ETable(table);

    GuiCommonElements gui;
    
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

    private ArrayList<ErrorContainer> errors;

    private boolean failedFiles = false;
    
    private CheckboxRenderer cbr;
    private CheckboxCellEditor cbe;

    private JPanel progressPanel;
    
    private JProgressBar bytesProgressBar; // byte progress for one file
    private JProgressBar filesProgressBar; // number of files in set (1 of 10 for example)
    
    private Thread runThread;

    public ErrorTable(GuiCommonElements gui)
    {   
        this.gui = gui;
        
        // set to layout that will maximize on resizing
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        this.setOpaque(false);
        
        // Main Panel containing all elements  
        // Set up the main panel layout
        double mainTable[][] =
                {{5, 200, 140, TableLayout.FILL, 180, 5}, // columns
                { 5, TableLayout.PREFERRED, TableLayout.FILL, 5, 29, 5}}; // rows
        
        mainPanel = gui.addMainPanel(this, mainTable, 0,0,0,0, debug); 
                
        String message = "All errors accumulated during your import are collected here, " +
                "allowing you to review and send us feedback on the problem. " +
                "To help us, you can upload them to us by selecting the \"Upload\" checkbox " +
                "besides each error.";

        
        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(mainPanel, message, "1,1,4,0", debug);
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
        
        progressPanel = gui.addPlanePanel(mainPanel, progressTable, debug);
        
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
       
        cancelBtn = gui.addButton(mainPanel, "Cancel", 'c', "Cancel sending", "2,4,l,c", debug);
        cancelBtn.addActionListener(this);
        
        cancelBtn.setVisible(false);
        
        sendBtn = gui.addButton(mainPanel, "Send Feedback", 's', "Send your errors to the OMERO team", "4,4,r,c", debug);
        sendBtn.addActionListener(this);
        sendBtn.setEnabled(false);
        
        this.add(mainPanel);
    }

    public void propertyChange(PropertyChangeEvent e)
    {
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendBtn)
        {
            notifyObservers(new ImportEvent.ERRORS_SEND());
        }
        if (e.getSource() == cancelBtn)
        {
            enableCancelBtn(false);
            notifyObservers(new ImportEvent.ERRORS_UPLOAD_CANCELLED());
        }
    } 

    public void mouseClicked(MouseEvent e) {}


    public void mouseEntered(MouseEvent e) {}


    public void mouseExited(MouseEvent e) {}


    public void mousePressed(MouseEvent e) {}


    public void mouseReleased(MouseEvent e)
    {
        if (e.getSource() == cbe.checkbox)
        {
           cbe.stopCellEditing();
        }
    }

    public void update(IObservable importLibrary, ImportEvent event)
    {

    }
        
    // Observable methods    
    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }
    
    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);
        
    }

    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer:observers)
        {
            observer.update(this, event);
        }
    }


    public void updateProgress(int rowIndex, int file, int value)
    {
        
    }
    
    public void initProgress(int rowIndex, int files)
    {
        
    }
    
    
    public ArrayList<ErrorContainer> getErrors() {
        return errors;
    }


    public void setErrors(ArrayList<ErrorContainer> errors) {
        this.errors = errors;
    }
    
    public void fireTableDataChanged()
    {
        table.fireTableDataChanged();
    }
    
    public void addRow(Vector<Object> rowData)
    {
        table.addRow(rowData);
        sendBtn.setEnabled(true);
    }
    
    public void setProgressSending(int row)
    {
        table.setValueAt(1, row, 3);
        failedFiles  = false;
        table.fireTableDataChanged();
        progressPanel.setVisible(true);
        cancelBtn.setVisible(true); 
        invalidate();
    } 
    
    public void setProgressDone(int row)
    {
        table.setValueAt(20, row, 3);
        failedFiles  = false;
    }
    
    public void setFilesProgress(int value)
    {
        filesProgressBar.setValue(value);
    }
    
    public void setFilesInSet(int value)
    {
        filesProgressBar.setMaximum(value);
    }
    
    public void setBytesProgress(int value)
    {
        bytesProgressBar.setValue(value);
    }

    public void setBytesFileSize(int value) {
        bytesProgressBar.setMaximum(value);
    }
    
    public void enableSendBtn(boolean enabled)
    {
        sendBtn.setEnabled(enabled);
    }


    public void enableCancelBtn(boolean b) {
        if (b)
        {   
            cancelBtn.setText("Cancel");
            cancelBtn.setEnabled(b);
        }
        else
        {
            cancelBtn.setText("Cancelling...");
            cancelBtn.setEnabled(b);
        }
    }


    public void setCancelBtnCancelled() {
        cancelBtn.setText("Cancelled");
    }


    public void setSendBtnEnable(boolean b) {
        sendBtn.setEnabled(b);
    }


    public void setCancelBtnVisible(boolean b)
    {
        cancelBtn.setVisible(b);
    }
    
    //
    // Inner classes
    //
    
    class ErrorTableModel 
        extends DefaultTableModel 
        implements TableModelListener 
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

    class MyTableHeaderRenderer 
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

    class LeftTableHeaderRenderer 
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
        setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
        setText(value.toString());
        setOpaque(true);
            
        // Set tool tip if desired
        setToolTipText((String)value);
         
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


    class TextLeftRenderer
        extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.
    
        private static final long serialVersionUID = 1L;
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            setFont(UIManager.getFont("TableCell.font"));
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
    
    class TextCellCenter
        extends DefaultTableCellRenderer 
    {
        // This method is called each time a column header
        // using this renderer needs to be rendered.
    
        private static final long serialVersionUID = 1L;
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
 
    class StatusRenderer 
    extends DefaultTableCellRenderer 
    {
        private static final long serialVersionUID = 1L;

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
    
    class CheckboxRenderer 
    extends JCheckBox 
    implements TableCellRenderer
    {
        private static final long serialVersionUID = 1L;

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
    
    class CheckboxCellEditor
    extends AbstractCellEditor
    implements TableCellEditor   
    {
        private static final long serialVersionUID = 1L;
        JCheckBox checkbox;
        
        public CheckboxCellEditor(JCheckBox checkbox)
        {
            this.checkbox = checkbox;
        }

        public Object getCellEditorValue() {
            return Boolean.valueOf(checkbox.isSelected());
        }
        
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
