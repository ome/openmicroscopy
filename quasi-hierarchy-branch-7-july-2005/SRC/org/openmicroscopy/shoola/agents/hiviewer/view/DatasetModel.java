/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.DatasetModel
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DatasetLoader;

/** 
 * A concrete Model for a P/D/I hierarchy consisting of a single tree
 * rooted by a given Dataset.
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
     * The id of the Dataset that is the root of the P/D/I tree that this 
     * Model handles.
     */
    private int     datasetID;
    
    
    /**
     * Creates a new instance.
     * 
     * @param datasetID The id of the Dataset that is the root of the P/D/I
     *                   tree that this Model will handle. 
     */
    DatasetModel(int datasetID) 
    {
        super();
        this.datasetID = datasetID; 
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
        return (dm.getHierarchyType() == getHierarchyType()) 
                && (dm.datasetID == datasetID);
    }

    /** 
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#createHierarchyLoader()
     */
    protected DataLoader createHierarchyLoader()
    {
        return new DatasetLoader(component, datasetID);
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        return new DatasetModel(datasetID);
    }

}
