/*
 * org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory
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

package org.openmicroscopy.shoola.agents.hiviewer.layout;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * A factory to create {@link Layout} objects.
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
public class LayoutFactory
{

    /** Identifies the <i>Squary</i> layout.*/
    public static final int     SQUARY_LAYOUT = 1;
    
    /** Identifies the <i>Tree</i> layout.*/
    public static final int     TREE_LAYOUT = 2;
    
    
    /**
     * Creates the specified layout.
     * 
     * @param type One of the constants defined by this class.
     * @return A layout object for the given layout <code>type</code>.
     * @throws IllegalArgumentException If <code>type</code> is not one of
     *          the constants defined by this class.
     */
    public static Layout createLayout(int type)
    {
        switch (type) {
            case SQUARY_LAYOUT:
                return new SquaryLayout();
            case TREE_LAYOUT:
                return new TreeLayout();
            default:
                throw new IllegalArgumentException("Unsupported layout type: "+
                                                    +type+".");
        }
    }
    
    /**
     * Returns the default layout.
     * 
     * @return See above.
     */
    public static Layout getDefaultLayout()
    { 
        return createLayout(SQUARY_LAYOUT); 
    }
    
}
