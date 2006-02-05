/*
 * ome.tools.lsid.LsidUtils
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
 * Written by:    Josh Moore <josh.moore@gmx.de>
 *
 *------------------------------------------------------------------------------
 */

// Java imports

// Third-party libraries

// Application-internal dependencies

package ome.tools.lsid;


public abstract class LsidUtils
{

    /** takes a field identifier as code-generated in each IObject class
     * and produces a back-end useable name.
     * 
     * TODO should change those fields from Strings to LSIDs with proper getters
     * to avoid this parsing overhead.
     * 
     * TODO possibly unused.
     */
    public static String parseField(String lsidProperty)
    {
        return lsidProperty.substring(lsidProperty.indexOf("_")+1);
    }
    
}
