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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Specifies an icon (action when selected/moused over) in the browser UI,
 * and stores the events bound to it.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class BIcon extends PNode
{   
    /**
     * The presentation (viewable) node... this is kept separate so an
     * icon can be either text, a glyph, or an image.
     */
    protected PNode presentationNode;
    
    protected int vertBuffer = 2; // border buffer (vertical)
    protected int horizBuffer = 2; // border buffer (horiz)
    protected int minWidth = 16; // minimum icon width
    protected int minHeight = 16; // minimum icon height
    
    // initialization method
    private void init()
    {
        // do nothing for now
    }
    
    // centers the node inside the icon, especially if it is smaller than
    // the minimum icon dimensions
    private void placeNode(PNode presentationNode)
    {
        if(presentationNode == null)
        {
            // do nothing
            return;
        }
        double offsetX, offsetY;
        double nodeWidth = presentationNode.getWidth();
        double nodeHeight = presentationNode.getHeight();
        
        // in each case, this code centers the presentation node
        // the node is too skinny (like me)
        if(nodeWidth < minWidth-horizBuffer*2)
        {
            offsetX = (minWidth-nodeWidth)/2;
            setBounds(getX(),getY(),minWidth,getHeight());
        }
        else
        {
            offsetX = horizBuffer;
            setBounds(getX(),getY(),(horizBuffer*2)+nodeWidth,getHeight());
        }
        
        // the node is short (Andrea?)
        if(nodeHeight < minHeight-vertBuffer*2)
        {
            offsetY = (minHeight-nodeHeight)/2;
            setBounds(getX(),getY(),getWidth(),minHeight);
        }
        else
        {
            offsetY = vertBuffer;
            setBounds(getX(),getY(),getWidth(),(vertBuffer*2)+nodeHeight);
        }
        
        // now do the centering
        presentationNode.setOffset(offsetX,offsetY);
        
    }
    
    /**
     * Constructs an icon with the specified image.
     * @param imageIcon The image to show in the icon.
     */
    public BIcon(Image imageIcon)
    {
        init();
        presentationNode = new PImage(imageIcon,true);
        addChild(presentationNode);
        placeNode(presentationNode);
    }
    
    /**
     * Constructs a text icon with the specified string.
     * @param text The string to show in the icon.
     */
    public BIcon(String text)
    {
        init();
        presentationNode = new PText(text);
        // TODO set font
        addChild(presentationNode);
        placeNode(presentationNode);
    }
    
    /**
     * Constructs an icon with a predefined node.  Overrides
     * click behavior for the child node.  In this manner,
     * supports image+text icons and glyph-based icons.
     * 
     * @param childNode The node to embed in this icon.
     */
    public BIcon(PNode childNode)
    {
        this.presentationNode = childNode;
        addChild(presentationNode);
        placeNode(presentationNode);
    }
    
    /**
     * Paints the icon (paints a border around it)
     */
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        g2.setColor(new Color(153,153,153));
        g2.draw(getBounds().getBounds2D());
    }
}
