/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.DataAttributeDefinition
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

import java.util.*;

/**
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public interface DataAttributeDefinition
{
    /**
     * Return the common name of the DataAttribute.
     * @return The common name of the DataAttribute. (duh)
     */
    public String getAttributeTypeName();

    /**
     * Return a collection of element names.
     * @return The list of element names.
     */
    public Collection getElementNames();

    /**
     * Returns the element type associated with the specified key.
     * @param key The element's name.
     * @return The type of the specified element.
     */
    public DataElementType getElementType(String key);

    /**
     * Returns whether or not the attribute is an instance of
     * this definition.
     * 
     * @param attribute The DataAttribute to verify.
     * @return Whether or not attribute is an instance of this definition.
     */
    public boolean isInstance(DataAttribute attribute);

    /**
     * Creates an instance of the DataAttribute with the set of values.
     * Will return null (TODO: analyze this behavior) if an incorrect
     * specification of values is entered.
     * @param values The values to create the instance.
     * @return A DataAttribute representing those objects.
     */
    public DataAttribute getInstance(Map values);

}
