/*
 * org.openmicroscopy.shoola.env.data.util.IObjectComparator
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import java.util.Comparator;


//Third-party libraries

//Application-internal dependencies
import omero.model.IObject;

/** 
 * Holds information about instances of a given agent to save.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 3.0
 * @since 5.0-Beta2
 */
public class IObjectComparator
    implements Comparator<IObject>
{

    /** Flag indicating to sort in ascending order or not.*/
    private boolean ascending;
    
    /**
     * Creates a new instance. Sorts in descending order.
     */
    public IObjectComparator()
    {
        this(false);
    }

    /**
     * Creates a new instance.
     * 
     * @param ascending Pass <code>true</code> to sort in ascending order
     *                  <code>false</code> otherwise
     */
    public IObjectComparator(boolean ascending)
    {
        this.ascending = ascending;
    }

    /**
     * Compares the 2 objects.
     */
    public int compare(IObject o1, IObject o2)
    {
        if (o1 == null && o2 == null) return 0;
        else if (o1 == null) return -1;
        else if (o2 == null) return 1;
        long id1 = o1.getId().getValue();
        long id2 = o2.getId().getValue();
        int v = id2 > id1 ? 1 : (id2 < id1 ? -1 : 0);
        if (ascending) return -v;
        return v;
    }

}
