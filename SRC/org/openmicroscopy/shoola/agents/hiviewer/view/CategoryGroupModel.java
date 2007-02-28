/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.CategoryGroupModel
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

import org.openmicroscopy.shoola.agents.hiviewer.CategoryGroupLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;

/** 
 * A concrete Model for a CG/C/I hierarchy consisting of a single tree
 * rooted by a given Category Group.
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
class CategoryGroupModel
    extends HiViewerModel
{

    /** 
     * The id of the Category Groups that is the root of the CG/C/I tree
     * that this Model handles. 
     */
    private Set<Long>     ids;

    /**
     * Creates a new instance.
     * 
     * @param cgID The id of the Category Group that is the root of the CG/C/I
     *             tree that this Model will handle. 
     */
    CategoryGroupModel(long cgID) 
    {
        super();
        ids = new HashSet<Long>(1);
        ids.add(new Long(cgID));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ids The id of the Category Groups that is the root of the CG/C/I
     *             tree that this Model will handle. 
     */
    CategoryGroupModel(Set<Long> ids) 
    {
        super();
        this.ids = ids; 
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() 
    { 
        return HiViewer.CATEGORY_GROUP_HIERARCHY; 
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof CategoryGroupModel))
            return false;
        CategoryGroupModel cgm = (CategoryGroupModel) other;
        if (cgm.getHierarchyType() != getHierarchyType()) return false;
        if (cgm.ids.size() != ids.size()) return false;
        Iterator i = cgm.ids.iterator(), j;
        Long id;
        int index = ids.size();
        while (i.hasNext()) {
            id = (Long) i.next();
            j = ids.iterator();
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
        return new CategoryGroupLoader(component, ids, refresh);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new CategoryGroupModel(ids);
        model.setRootLevel(getExperimenter(), getUserGroupID());
        return model;
    }

}
