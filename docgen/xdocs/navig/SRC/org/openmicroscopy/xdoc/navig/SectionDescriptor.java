/*
 * org.openmicroscopy.xdoc.navig.SectionDescriptor
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

package org.openmicroscopy.xdoc.navig;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * Holds the values of the attributes of a section tag in a 'doc.xml' file.
 * This is just a convenience class so that the payload of a section node
 * can be easily stored into a {@link javax.swing.tree.DefaultMutableTreeNode}. 
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
class SectionDescriptor
{

    /** The section name, that is the value of the 'name' attribute. */
    final String    name;
    
    /** 
     * Points to the section content.
     * This is the value of the <code>href</code> attribute.. 
     */
    final String    url;
    
    /**
     * Creates a new instance.
     * 
     * @param name 	The name of the section.
     * @param url	The value of the <code>href</code> attribute.
     */
    public SectionDescriptor(String name, String url)
    {
        if (name == null) throw new NullPointerException("No name.");
        if (url == null) throw new NullPointerException("No url.");
        this.name = name;
        this.url = url;
    }
    
    /** Overrides parent to return the section name. */
    public String toString() { return name; }

}
