/*
 * org.openmicroscopy.shoola.agents.classifier.AttributeComparator
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
package org.openmicroscopy.shoola.agents.classifier;

import java.util.Comparator;

import org.openmicroscopy.ds.DataException;
import org.openmicroscopy.ds.dto.Attribute;

/**
 * A comparator that compares Attributes by their ID (to enforce by-time-of
 * creation on results from the DB)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class AttributeComparator implements Comparator
{
    /**
     * Compares two Attributes.  Throws an exception if either is null or if
     * one is not an Attribute.  Otherwise, compares by the Attributes' IDs.
     */
    public int compare(Object arg0, Object arg1)
    {
        if(arg0 == null || arg1 == null)
        {
            throw new IllegalArgumentException("Comparing a null object");
        }
        if(!(arg0 instanceof Attribute) ||
           !(arg1 instanceof Attribute))
        {
            throw new IllegalArgumentException("Comparing non-Attributes");
        }
        Attribute a0 = (Attribute)arg0;
        Attribute a1 = (Attribute)arg1;
        
        // check data exception (attribute not yet added; goes last)
        boolean a0invalid = false;
        boolean a1invalid = false;
        try
        {
            int i = a0.getID();
        }
        catch(DataException ds)
        {
            a0invalid = true;
        }
        try
        {
            int i = a1.getID();
        }
        catch(DataException ds)
        {
            a1invalid = true;
        }
        
        if(!a0invalid && a1invalid)
        {
            return -1;
        }
        else if(a0invalid && a1invalid)
        {
            return 0;
        }
        else if(a0invalid && !a1invalid)
        {
            return 1;
        }
        
        // no, the are legit
        if(a0.getID() < a1.getID()) return -1;
        else if(a0.getID() == a1.getID()) return 0;
        else return 1;
    }
}
