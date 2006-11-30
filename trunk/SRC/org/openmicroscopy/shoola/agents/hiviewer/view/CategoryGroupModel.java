/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.CategoryGroupModel
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
    private Set     ids;

    /**
     * Creates a new instance.
     * 
     * @param cgID The id of the Category Group that is the root of the CG/C/I
     *             tree that this Model will handle. 
     */
    CategoryGroupModel(long cgID) 
    {
        super();
        ids = new HashSet(1);
        ids.add(new Long(cgID));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param ids The id of the Category Groups that is the root of the CG/C/I
     *             tree that this Model will handle. 
     */
    CategoryGroupModel(Set ids) 
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
     * @see HiViewerModel#createHierarchyLoader()
     */
    protected DataLoader createHierarchyLoader()
    {
        return new CategoryGroupLoader(component, ids);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new CategoryGroupModel(ids);
        model.setRootLevel(getRootLevel(), getRootID());
        return model;
    }

}
