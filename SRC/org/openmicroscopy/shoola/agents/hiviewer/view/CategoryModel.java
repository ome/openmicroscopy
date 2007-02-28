/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.CategoryModel
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
import org.openmicroscopy.shoola.agents.hiviewer.CategoryLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;

/** 
 * A concrete Model for a C/I hierarchy consisting of a single tree
 * rooted by given Categories.
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
class CategoryModel
    extends HiViewerModel
{

    /** 
     * The id of the Categores that is the root of the CG/C/I tree
     * that this Model handles. 
     */
    private Set<Long>     categoriesID;
    
    /**
     * Creates a new instance.
     * 
     * @param categoryID The id of the Category that is the root of the CG/C/I
     *                   tree that this Model will handle. 
     */
    CategoryModel(long categoryID) 
    {
        super();
        categoriesID = new HashSet<Long>(1);
        categoriesID.add(new Long(categoryID)); 
    }
    
    /**
     * Creates a new instance.
     * 
     * @param categoriesID The id of the Categories that is the root of the C/I
     *                   	tree that this Model will handle. 
     */
    CategoryModel(Set<Long> categoriesID) 
    {
        super(); 
        this.categoriesID = categoriesID;
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return HiViewer.CATEGORY_HIERARCHY; }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof CategoryModel)) return false;
        CategoryModel cm = (CategoryModel) other;
        if (cm.getHierarchyType() != getHierarchyType()) return false;
        if (cm.categoriesID.size() != categoriesID.size()) return false;
        Iterator i = cm.categoriesID.iterator(), j;
        Long id;
        int index = categoriesID.size();
        while (i.hasNext()) {
            id = (Long) i.next();
            j = categoriesID.iterator();
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
        return new CategoryLoader(component, categoriesID, refresh);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new CategoryModel(categoriesID);
        model.setRootLevel(getExperimenter(), getUserGroupID());
        return model;
    }
    
}
