/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.CategoryComparator
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.Comparator;

import org.openmicroscopy.ds.st.Category;
import org.openmicroscopy.ds.st.CategoryGroup;

/**
 * A class that compares category and category group objects by their names.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class CategoryComparator implements Comparator
{
    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Object o1, Object o2)
    {
        if(!(o1 instanceof Category && o2 instanceof Category) &&
           !(o1 instanceof CategoryGroup && o2 instanceof CategoryGroup))
        {
            throw new ClassCastException("Invalid types to compare");
        }
        if(o1 == null || o2 == null)
        {
            throw new NullPointerException("Comparing null object");
        }
        
        if(o1 instanceof Category)
        {
            Category c1 = (Category)o1;
            Category c2 = (Category)o2;
            return c1.getName().compareTo(c2.getName());
        }
        else
        {
            CategoryGroup cg1 = (CategoryGroup)o1;
            CategoryGroup cg2 = (CategoryGroup)o2;
            return cg1.getName().compareTo(cg2.getName());
        }
    }

}
