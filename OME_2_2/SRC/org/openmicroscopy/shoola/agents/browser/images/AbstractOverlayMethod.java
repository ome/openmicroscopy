/*
 * org.openmicroscopy.shoola.agents.browser.images.AbstractOverlayMethod
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
package org.openmicroscopy.shoola.agents.browser.images;

/**
 * Abstract class version of OverlayMethod (that handles referential equality
 * such that everything else in the browser system works).  Does not actually
 * specify an overlay method; only specifies how equality and hash codes are
 * handled (as there should be only a given amount of paint methods in a
 * system, and they are only differentiated by their procedures, not their
 * inherent values)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class AbstractOverlayMethod implements OverlayMethod
{
    /**
     * Here's the super cheap hashCode hack.  Increments on new() to maintain
     * the invariant that two objects with equals() will have the same
     * hashCode.  Optimizes so that two different ones will have different
     * hashCodes.
     */
    protected static int numericalMarker = 0;
    
    /**
     * Distinguishing hash code.
     */
    protected int myMarker;
    
    protected String displayNodeType;
    
    /**
     * Constructor which increments the numerical marker so that the
     * equals/hashCode() invariant is maintained.
     *
     */
    protected AbstractOverlayMethod(String type)
    {
        // assign marker for hash code, increment
        // likelihood of Integer.MAX_VALUE PaintMethods: I don't envy the
        // JVM that will be handling that kind of nonsense.
        myMarker = numericalMarker++;
        if(type == null)
        {
            throw new IllegalArgumentException("node type cannot be null");
        }
        this.displayNodeType = type;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.images.OverlayMethod#getDisplayNodeType()
     */
    public String getDisplayNodeType()
    {
        return displayNodeType;
    }

    
    /**
     * Overrides the default behavior for equals() in favor of referential
     * equality, as the only things that will differentiate OverlayMethods are
     * their paint() procedures, not inherent values.
     */
    public boolean equals(Object o)
    {
        return(o == this);
    }
    
    /**
     * Overrides hashCode() to optimize for differentiation within
     * Collections.  The static variable increments this hash code for
     * every new OverlayMethod created.
     */
    public int hashCode()
    {
        return myMarker;
    }
}
