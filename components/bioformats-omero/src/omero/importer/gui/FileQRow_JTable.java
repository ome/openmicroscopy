package omero.importer.gui;

import java.io.File;
import java.util.Vector;

import ome.model.containers.Dataset;
import ome.model.containers.Project;
import omero.importer.engine.IFileQRow;


/**
 * @author Brian W. Loranger
 *
 * This class contains the structure for each row within the fileQ
 * (with each row having several columns).
 */
public class FileQRow_JTable
       extends Vector
       implements IFileQRow
{
    private static final long serialVersionUID = 1L;
    
    File        filePath;
    String      omeroName;
    Project     project;
    Dataset     dataset;
    Status      status;
    Boolean     archived;
    
    @SuppressWarnings("unchecked")
    public FileQRow_JTable(File filePath, String omeroName, Project project, Dataset dataset, Status status, Boolean archived)
    {
        if (filePath == null) throw new NullPointerException("filePath cannot be null");
        if (omeroName == null) throw new NullPointerException("omeroName cannot be null");
        if (project == null) throw new NullPointerException("project cannot be null");
        if (dataset == null) throw new NullPointerException("dataset cannot be null");
        if (status == null) throw new NullPointerException("status cannot be null");
        if (archived == null) throw new NullPointerException("archived cannot be null");
        
        this.filePath = filePath;
        this.omeroName = omeroName;
        this.project = project;
        this.dataset = dataset;
        this.status = status;
        this.archived = archived;
        
        String statusStr = getStatusString(status);
        String pdDisplayName = getPDdisplayName(project, dataset);
       
        add(omeroName);
        add(pdDisplayName);
        add(statusStr);
        add(filePath);
        add(project);
        add(dataset);
        add(archived);
        add(status);
    }
    
    public File getFilePath() { return filePath; }
    public String getOmeroName() { return omeroName; }
    public Project getProject() { return project; }
    public Dataset getDataset() { return dataset; }
    public Status getStatus() {return status; }
    public Boolean getArchived() { return archived; }
    
    public String getStatusString(Status status)
    {
        String statusStr = null;
        
        switch(status)
        {
            case ADDED:     statusStr = "Added";
                            break;
            case PENDING:   statusStr = "Pending";
                            break;
            case ACTIVE:    statusStr = "Added";
                            break;
            case FAILED:    statusStr = "Failed";
                            break;
            case DONE:      statusStr = "Done";
                            break;
            default:        statusStr = "Unknown";
                            break;   
        }
        return statusStr;
    }
    
    public String getPDdisplayName(Project project, Dataset dataset)
    {
        String pdDisplayName = null;
        if (project != null && dataset != null)
        {
            pdDisplayName = project.getName()+"/"+dataset.getName(); 
        }

        return pdDisplayName;
    }
}
