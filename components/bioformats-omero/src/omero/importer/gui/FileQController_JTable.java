package omero.importer.gui;

import omero.importer.engine.IFileQController;


public class FileQController_JTable
    implements IFileQController
{

    FileQModel_JTable model = null;
    
    public FileQController_JTable(FileQModel_JTable model)
    {
        this.model = model;
        
    }

    public void addRow(FileQRow_JTable row)
    {
        model.addRow(row);
        model.fireTableDataChanged();
    }

    public void deleteRow(int index)
    {
        model.removeRow(index);
        model.fireTableDataChanged();
    }

    public FileQRow_JTable getRow(int index)
    {
        return (FileQRow_JTable) model.getDataVector().elementAt(index);
    }

    public Integer getSize()
    {
        return model.getRowCount();
    }
    
    public void setProgressInfo(int row, int maxPlanes)
    {
        model.setProgressInfo(row, maxPlanes);
    }
 
    public void setProgressPending(int row)
    {
        model.setProgressPending(row);
            
    }
    
    public void setProgressInvalid(int row)
    {
        model.setProgressInvalid(row);  
    }
    
        public void setImportProgress(int count, int series, int step)
    {
        model.setImportProgress(count, series, step);
    }

    public void setProgressFailed(int row)
    {
        model.setProgressFailed(row);
    }
    
    public void setProgressUnknown(int row)
    {
        model.setProgressUnknown(row);
    }    
        
    public void setProgressPrepping(int row)
    {
        model.setProgressPrepping(row);
    }

    public void setProgressDone(int row)
    {
        model.setProgressDone(row);
    }
    
    public void setProgressArchiving(int row)
    {
        model.setProgressArchiving(row);     
    }
    
    public int getMaximum()
    {
        return model.getMaximum();
    }
}
