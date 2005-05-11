/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.CategoryModel
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
import org.openmicroscopy.shoola.agents.hiviewer.CategoryLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;

/** 
 * A concrete Model for a CG/C/I hierarchy consisting of a single tree
 * rooted by a given Category.
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
     * The id of the Category that is the root of the CG/C/I tree
     * that this Model handles. 
     */
    private int     categoryID;
    
    
    /**
     * Creates a new instance.
     * 
     * @param categoryID The id of the Category that is the root of the CG/C/I
     *                   tree that this Model will handle. 
     */
    CategoryModel(int categoryID) 
    {
        super();
        this.categoryID = categoryID; 
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
        return (cm.getHierarchyType() == getHierarchyType()) 
                && (cm.categoryID == categoryID);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#createHierarchyLoader()
     */
    protected DataLoader createHierarchyLoader()
    {
        return new CategoryLoader(component, categoryID);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        return new CategoryModel(categoryID);
    }
    
}
