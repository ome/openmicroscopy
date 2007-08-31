/*
 * omero.importer.engine.FileQRow
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2007 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package omero.importer.engine;

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
public class FileQRow
       extends Vector
       implements IFileQRow
{
    private static final long serialVersionUID = 1L;
    
    File        filePath;
    String      omeroName;
    Dataset     dataset;
    Status      status;
    Boolean     archived;
    
    @SuppressWarnings("unchecked")
    public FileQRow(File filePath, String omeroName, Dataset dataset, Status status, Boolean archived)
    {
        if (filePath == null) throw new NullPointerException("filePath cannot be null");
        if (omeroName == null) throw new NullPointerException("omeroName cannot be null");
        if (dataset == null) throw new NullPointerException("dataset cannot be null");
        if (status == null) throw new NullPointerException("status cannot be null");
        if (archived == null) throw new NullPointerException("archived cannot be null");
        
        this.filePath = filePath;
        this.omeroName = omeroName;
        this.dataset = dataset;
        this.status = status;
        this.archived = archived;
        
        String statusStr = getStatusString(status);
        
        add(omeroName);
        add(statusStr);
        add(filePath);
        add(dataset);
        add(archived);
        add(status);
    }
    
    public File getFilePath() { return filePath; }
    public String getOmeroName() { return omeroName; }
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
            case ACTIVE:    statusStr = "Active";
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
