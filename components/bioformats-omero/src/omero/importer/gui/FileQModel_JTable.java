package omero.importer.gui;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class FileQModel_JTable 
    extends DefaultTableModel 
    implements TableModelListener {
        
    private static final long serialVersionUID = 1L;
    private String[] columnNames = {"Files in Queue", "Project/Dataset", "Status", "FilePath", "DatasetNum", "Project", "Archive", "StatusEnum"};
    
    private int row;
    private int maxPlanes;
    public boolean failedFiles;
    public boolean doneFiles;

    public void tableChanged(TableModelEvent arg0) { }
    
    public int getColumnCount() { return columnNames.length; }

    public String getColumnName(int col) { return columnNames[col]; }
    
    public boolean isCellEditable(int row, int col) { return false; }
    
    public boolean rowSelectionAllowed() { return true; }
    
    public void setProgressInfo(int row, int maxPlanes)
    {
        this.row = row;
        this.maxPlanes = maxPlanes;
    }
 
    public void setProgressPending(int row)
    {
        if (getValueAt(row, 2).equals("added"))
        {
            setValueAt("pending", row, 2);  
        }
            
    }
    
    public void setProgressInvalid(int row)
    {
        if (getValueAt(row, 2).equals("added"))
            setValueAt("invalid format", row, 2);    
    }
    
        public void setImportProgress(int count, int series, int step)
    {
        String text;
        if (count > 1)
            text = series + 1 + "/" + count + ": " + step + "/" + maxPlanes;
        else
            text = step + "/" + maxPlanes;
        setValueAt(text, row, 2);   
    }

    public void setProgressFailed(int row)
    {
        setValueAt("failed", row, 2);
        failedFiles = true;
        fireTableDataChanged();
    }
    
    public void setProgressUnknown(int row)
    {
        setValueAt("unknown format", row, 2);
        failedFiles = true;
        fireTableDataChanged();
    }    
        
    public void setProgressPrepping(int row)
    {
        setValueAt("importing", row, 2); 
    }

    public void setProgressDone(int row)
    {
        setValueAt("done", row, 2);
        doneFiles = true;
        fireTableDataChanged();
    }
    
    public void setProgressArchiving(int row)
    {
        setValueAt("archiving", row, 2);       
    }
    
    public int getMaximum()
    {
        return maxPlanes;
    }
}

