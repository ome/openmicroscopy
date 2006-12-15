/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.ProjectModel
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

//Third-party libraries

//Application-internal dependencies
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ProjectLoader;

/** 
 * A concrete Model for a P/D/I hierarchy consisting of a single tree
 * rooted by a given Project.
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
class ProjectModel
    extends HiViewerModel
{

    /** 
     * The id of the Projects that is the root of the P/D/I tree that this 
     * Model handles.
     */
    private Set     projectsID;
    
    /**
     * Creates a new instance.
     * 
     * @param projectID 	The id of the Projects that is the root of the P/D/I
     *                  	tree that this Model will handle. 
     */
    ProjectModel(long projectID) 
    {
        super();
        projectsID = new HashSet(1);
        projectsID.add(new Long(projectID)); 
    }
    
    /**
     * Creates a new instance.
     * 
     * @param projectsID 	The id of the Projects that is the root of the P/D/I
     *                  	tree that this Model will handle. 
     */
    ProjectModel(Set projectsID) 
    {
        super();
        this.projectsID = projectsID; 
    }
    
    /** 
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return HiViewer.PROJECT_HIERARCHY; }

    /** 
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof ProjectModel)) return false;
        ProjectModel pm = (ProjectModel) other;
        if (pm.getHierarchyType() != getHierarchyType()) return false;
        if (pm.projectsID.size() != projectsID.size()) return false;
        Iterator i = pm.projectsID.iterator(), j;
        Long id;
        int index = projectsID.size();
        while (i.hasNext()) {
            id = (Long) i.next();
            j = projectsID.iterator();
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
        return new ProjectLoader(component, projectsID, refresh);
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new ProjectModel(projectsID);
        model.setRootLevel(getRootLevel(), getRootID());
        return model;
    }

}
