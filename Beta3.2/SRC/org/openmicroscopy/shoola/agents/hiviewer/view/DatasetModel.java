/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.DatasetModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DatasetLoader;

/** 
 * A concrete Model for a D/I hierarchy consisting of a single tree
 * rooted by given Datasets.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class DatasetModel
    extends HiViewerModel
{

    /** 
     * The id of the Datasets that is the root of the P/D/I tree that this 
     * Model handles.
     */
    private Set<Long> 		datasetsID;
    
    /**
     * Creates a new instance.
     * 
     * @param datasetID The id of the Dataset that is the root of the P/D/I
     *                   tree that this Model will handle. 
     */
    DatasetModel(long datasetID) 
    {
        super();
        datasetsID = new HashSet<Long>(1);
        datasetsID.add(new Long(datasetID)); 
    }
    
    /**
     * Creates a new instance.
     * 
     * @param datasetsID The id of the Datasets that is the root of the P/D/I
     *                   tree that this Model will handle. 
     */
    DatasetModel(Set<Long> datasetsID)
    {
    	super();
        this.datasetsID = datasetsID; 
    }
    
    /**
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return HiViewer.DATASET_HIERARCHY; }

    /**
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof DatasetModel)) return false;
        DatasetModel dm = (DatasetModel) other;
        if (dm.getHierarchyType() != getHierarchyType()) return false;
        if (dm.datasetsID.size() != datasetsID.size()) return false;
        Iterator i = dm.datasetsID.iterator(), j;
        Long id;
        int index = datasetsID.size();
        while (i.hasNext()) {
            id = (Long) i.next();
            j = datasetsID.iterator();
            while (j.hasNext()) {
                if (id.longValue() == ((Long) j.next()).longValue()) index--;
            }
        }
        return (index == 0);
    }

    /** 
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#createHierarchyLoader(boolean)
     */
    protected DataLoader createHierarchyLoader(boolean refresh)
    {
        return new DatasetLoader(component, datasetsID, refresh);
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new DatasetModel(datasetsID);
        model.setRootLevel(getExperimenter());
        return model;
    }

}
