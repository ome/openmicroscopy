/*
 * org.openmicroscopy.shoola.agents.browser.images.OverlayNode
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
 * Simplified version of overlay nodes that have to respond to UI input
 * (such as mouse overs, mouse downs)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public abstract class OverlayNode extends ResponsiveNode
{
    protected String overlayNodeType;
    protected Thumbnail parentNode;
    
    /**
     * Default constructor
     * @param type The type to mark this node as.
     * @param parentNode The parent thumbnail of the node.
     * @throws IllegalArgumentException If either parameter is null.
     */
    public OverlayNode(String type, Thumbnail parentNode)
        throws IllegalArgumentException
    {
        if(type == null || parentNode == null)
        {
            throw new IllegalArgumentException("Null parameters");
        }
        overlayNodeType = type;
        this.parentNode = parentNode;
    }
    
    public String getOverlayType()
    {
        return overlayNodeType;
    }
    
    public void setOverlayType(String type)
    {
        if(type != null)
        {
            this.overlayNodeType = type;
        }
    }
    
    public Thumbnail getParentNode()
    {
        return parentNode;
    }
    
    public void setParentNode(Thumbnail parent)
    {
        if(parent != null)
        {
            this.parentNode = parent;
        }
    }
}
