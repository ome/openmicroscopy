/*
 * org.openmicroscopy.shoola.agents.browser.heatmap.HeatMapMode
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
package org.openmicroscopy.shoola.agents.browser.heatmap;

import org.openmicroscopy.ds.dto.Attribute;

/**
 * Specifies a retrieval process for attribute data.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface HeatMapMode
{
    /**
     * Computes a particular value over the specified elements of the
     * list of attributes.  For example, given a set of StackMaximum
     * attributes, compute some value based on the TheMean element.
     *  
     * @param attributes The list of attributes to use for computation.
     * @param elementName The element over which to find a scalar value.
     * @return Some value related to the values of all the elements.
     * @throws IllegalArgumentException If elementName is an invalid element
     *         name, or points to a non-numeric data value.
     */
    public double computeValue(Attribute[] attributes, String elementName)
        throws IllegalArgumentException;
    
    /**
     * Indicate this mode as a string.
     * @return The string representation/characterization of this mode.
     */
    public String toString();
}
