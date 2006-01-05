/*
 * org.openmicroscopy.shoola.env.data.views.calls.DataObjectSaver
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data.views.calls;


//Java imports

//Third-party libraries

//Application-internal dependencies
import java.util.ArrayList;

import org.openmicroscopy.shoola.env.data.DataManagementService;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.DatasetSummary;
import org.openmicroscopy.shoola.env.data.model.ProjectData;
import org.openmicroscopy.shoola.env.data.model.ProjectSummary;
import org.openmicroscopy.shoola.env.data.views.BatchCall;
import org.openmicroscopy.shoola.env.data.views.BatchCallTree;
import pojos.DataObject;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class DataObjectSaver
    extends BatchCallTree
{

    /** Indicates to create a <code>DataObject</code>. */
    public static final int CREATE = 0;
    
    /** Indicates to update the <code>DataObject</code>. */
    public static final int UPDATE = 1;
    
    /** The save call. */
    private BatchCall       saveCall;
    
    /** The result of the call. */
    private DataObject      result;
    
    /** Tempo. */
    private DataObject      objectToSave;
    
    //TODO: remove asap.
    private ProjectData daToProjectData(pojos.ProjectData data)
    {
        ProjectData d = new ProjectData();
        d.setName(data.getName());
        d.setDescription(data.getDescription());
        return d;
    }
    
    private pojos.ProjectData summaryToDataObject(ProjectSummary s)
    {
        pojos.ProjectData data = new pojos.ProjectData();
        data.setId(s.getID());
        data.setName(s.getName());
        data.setDescription(((pojos.ProjectData) objectToSave).getDescription());
        return data;
    }
    
    private DatasetData daToDatasetData(pojos.DatasetData data)
    {
        DatasetData d = new DatasetData();
        d.setName(data.getName());
        d.setDescription(data.getDescription());
        return d;
    }
    
    private pojos.DatasetData summaryToDataObject(DatasetSummary s)
    {
        pojos.DatasetData data = new pojos.DatasetData();
        data.setId(s.getID());
        data.setName(s.getName());
        data.setDescription(((pojos.DatasetData) objectToSave).getDescription());
        return data;
    }
    
    /**
     * Checks if the specified index is supported.
     * 
     * @param i The index to check.
     */
    private void checkIndex(int i)
    {
        switch (i) {
            case CREATE:
            case UPDATE:
                return;
            default:
                throw new IllegalArgumentException("Index not supported");   
        }
    }
    
    /**
     * Creates a {@link BatchCall} to create the specified {@link DataObject}.
     * 
     * @param userObject The <code>DataObject</code> to create.
     * @param parentID The Id of the parent.
     * @return The {@link BatchCall}.
     */
    private BatchCall create(final DataObject userObject, final int parentID)
    {
        return new BatchCall("Create Data object.") {
            public void doCall() throws Exception
            {
                DataManagementService dms = context.getDataManagementService();
                //tempo
                if (userObject instanceof pojos.ProjectData) {
                    ProjectData data = 
                        daToProjectData((pojos.ProjectData) userObject);
                    result = summaryToDataObject(dms.createProject(data));
                } else if (userObject instanceof pojos.DatasetData) {
                    DatasetData data = 
                        daToDatasetData((pojos.DatasetData) userObject);
                    ProjectSummary parent = new ProjectSummary();
                    parent.setID(parentID);
                    ArrayList l = new ArrayList(1);
                    l.add(parent);
                    result = summaryToDataObject(
                                    dms.createDataset(l, null, data));
                }
            }
        };
    }
    
    /**
     * Creates a {@link BatchCall} to update the specified {@link DataObject}.
     * 
     * @param userObject The <code>DataObject</code> to update.
     * @return The {@link BatchCall}.
     */
    private BatchCall update(final DataObject userObject)
    {
        return new BatchCall("Update Data object.") {
            public void doCall() throws Exception
            {
                
            }
        };
    }
    
    /**
     * Adds the {@link #saveCall} to the computation tree.
     * @see BatchCallTree#buildTree()
     */
    protected void buildTree() { add(saveCall); }

    /**
     * TODO: modified code.
     */
    protected Object getResult()
    {
        return result;
    }

    /**
     * Creates a new instance.
     * 
     * @param userObject    The {@link DataObject} to create or update.
     *                      Mustn't be <code>null</code>.
     * @param index         One of the constants defined by this class.
     * @param parentID      The ID of the parent. The value is <code>-1</code>
     *                      if there is no parent.
     */
    public DataObjectSaver(DataObject userObject, int index, int parentID)
    {
        if (userObject == null)
            throw new IllegalArgumentException("No DataObject.");
        checkIndex(index);
        objectToSave = userObject; //tempo
        if (index == CREATE) {
            if ((userObject instanceof pojos.DatasetData) ||
                    (userObject instanceof pojos.CategoryData)) {
                if (parentID == -1) 
                    throw new IllegalArgumentException("ParentID not valid.");
            }
        }
        switch (index) {
            case CREATE:
                saveCall = create(userObject, parentID);
                break;
            case UPDATE:
                saveCall = update(userObject);
        }
    }
  
    
}
