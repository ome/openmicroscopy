/*
 * org.openmicroscopy.shoola.agents.browser.ui.BIcon
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
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.image.BufferedImage;

import org.openmicroscopy.shoola.agents.browser.BrowserAction;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;

/**
 * Specifies an icon (action when selected) in the browser UI.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BIcon extends PNode
{   
    /**
     * The action to be executed when a left-click occurs on this icon.
     */
    protected BrowserAction leftAction;
    
    /**
     * The action to be executed when a right-click occurs on this icon.
     */
    protected BrowserAction rightAction;
    
    /**
     * The presentation (viewable) node... this is kept separate so an
     * icon can be either text, a glyph, or an image.
     */
    protected PNode presentationNode;
    
    /**
     * Constructs an icon with the specified image.
     * @param imageIcon The image to show in the icon.
     */
    public BIcon(BufferedImage imageIcon)
    {
        presentationNode = new PImage(imageIcon,true);
        addChild(presentationNode);
        presentationNode.setOffset(0,0);
    }
    
    /**
     * Constructs a text icon with the specified string.
     * @param text The string to show in the icon.
     */
    public BIcon(String text)
    {
        presentationNode = new PText(text);
        // TODO set font
        addChild(presentationNode);
        presentationNode.setOffset(0,0);
    }
    
    /**
     * Sets the action that occurs whenever a user left-clicks on the icon
     * to the specified response.
     * @param action What should happen when the user left-clicks the icon.
     */
    public void setLeftClickAction(BrowserAction action)
    {
        this.leftAction = action;
    }
    
    /**
     * Sets the action that occurs whenever a user right-clicks on the icon
     * to the specified response.
     * @param action What should happen when the user right-clicks the icon.
     */
    public void setRightClickAction(BrowserAction action)
    {
        this.rightAction = action;
    }
}
