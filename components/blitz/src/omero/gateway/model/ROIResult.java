/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package omero.gateway.model;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hosts the results of a call loading the ROI.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ROIResult
{

    /** The collection of rois. */
    private Collection<ROIData> rois;

    /** The collection of folders */
    private Collection<FolderData> folders;
    
    /** The ID of the original file */
    private long fileID;

    /** The result associated to the ROI. */
    private Object result;

    /**
     * Creates a new instance.
     *
     * @param rois The collection of ROIs.
     */
    public ROIResult(Collection<ROIData> rois)
    {
        this(rois, -1, null);
    }

    /**
     * Creates a new instance.
     *
     * @param rois The collection of ROIs.
     * @param fileID The id of the file.
     */
    public ROIResult(Collection<ROIData> rois, long fileID)
    {
        this(rois, fileID, null);
    }

    /**
     * Creates a new instance.
     *
     * @param rois The collection of ROIs.
     * @param fileID The id of the file.
     * @param result The result table associated to the ROIS.
     */
    public ROIResult(Collection<ROIData> rois, long fileID, Object result)
    {
        this.rois = rois;
        this.fileID = fileID;
        this.result = result;
    }

    /**
     * Sets the results.
     *
     * @param result The value to set.
     */
    public void setResult(Object result) { this.result = result; }

    /**
     * Returns the collection of ROIs.
     *
     * @return See above.
     */
    public Collection<ROIData> getROIs() { return rois; }

    /**
     * Returns the id of the file.
     *
     * @return See above.
     */
    public long getFileID() { return fileID; }

    /**
     * Returns the result.
     *
     * @return See above.
     */
    public Object getResult() { return result; }

    /**
     * Sets the available folders
     * 
     * @param folders
     *            The folders
     */
    public void setFolders(Collection<FolderData> folders) {
        Map<Long, FolderData> folderById = new HashMap<Long, FolderData>();
        for (FolderData f : folders) {
            folderById.put(f.getId(), f);
        }

        if (rois != null) {
            for (ROIData roi : rois) {
                for (FolderData folder : roi.getFolders()) {
                    FolderData initFolder = folderById.get(folder.getId());
                    folder.setFolder(initFolder.asFolder());
                }
            }
        }

        this.folders = folderById.values();
    }

    /**
     * Get the available folders
     * 
     * @return See above.
     */
    public Collection<FolderData> getFolders() {
        return folders == null ? Collections.EMPTY_LIST
                : new ArrayList<FolderData>(folders);
    }
    
}
