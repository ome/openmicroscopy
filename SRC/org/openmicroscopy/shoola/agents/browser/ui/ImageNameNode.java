/*
 * org.openmicroscopy.shoola.agents.browser.ui.ImageNameNode
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

import org.openmicroscopy.shoola.agents.browser.UIConstants;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;
import org.openmicroscopy.shoola.agents.browser.images.ThumbnailDataModel;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Tooltip-like node that displays the name of a thumbnail, and other
 * associated information.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since 2.2
 */
public class ImageNameNode extends PNode
                           implements MouseDownSensitive,
                                      MouseDragSensitive
{
    private Thumbnail t;
    private ThumbnailDataModel infoModel;
    private String displayString;
    
    public static Color backgroundColor = new Color(255,255,128);
    
    /**
     * Creates an image name node.  The trick is for the tooltip not to
     * get in the way of the thumbnail if the user selects it, so all
     * events that this node may capture (and it has to be a node, not just
     * an overlay, because drawing another thumbnail may supercede it, plus
     * you want the delay) must be passed down to the thumbnail.  This makes
     * implementation slightly more complicated.
     * 
     * @param t The thumbnail to base the image name node off of.
     */
    public ImageNameNode(Thumbnail t)
    {
        if(t == null)
        {
            return;
        }
        this.t = t;
        this.infoModel = t.getModel();
        this.displayString = createDisplayString();
        PText textNode = new PText(displayString);
        textNode.setOffset(2,2);
        setBounds(0,0,textNode.getWidth()+4,textNode.getHeight()+4);
        addChild(textNode);
    }
    
    // creates a fully descriptive image name.
    private String createDisplayString()
    {
        // handle screen case as well.
        StringBuffer imageName = new StringBuffer();
        imageName.append(infoModel.getName());
        String wellName = "";
        if(infoModel.getValue(UIConstants.WELL_KEY_STRING) != null)
        {
            wellName = "[Well " +
                       infoModel.getValue(UIConstants.WELL_KEY_STRING) +
                       "]";
            imageName.append(" ");
            imageName.append(wellName);
        }
        if(t.isMultipleThumbnail())
        {
            imageName.append(" (");
            imageName.append(t.getMultipleImageIndex()+1);
            imageName.append("/");
            imageName.append(t.getMultipleImageSize());
            imageName.append(")");
        }
        return imageName.toString();
    }
    
    /**
     * Paints the background.
     */
    public void paint(PPaintContext context)
    {
        Graphics2D g2 = context.getGraphics();
        g2.setPaint(backgroundColor);
        g2.fill(getBounds().getBounds2D());
    }
    
    /*
     * PASS EVENTS THROUGH TO THUMBNAIL.
     */
    
    public MouseDownActions getMouseDownActions()
    {
        return t.getMouseDownActions();
    }
    
    public MouseDragActions getMouseDragActions()
    {
        // TODO when thumbnail becomes drag sensitive, do the same
        return null;
    }
    
    public void respondStartDrag(PInputEvent e) {}
    public void respondDrag(PInputEvent e) {}
    public void respondEndDrag(PInputEvent e) {}
    
    public void respondMouseClick(PInputEvent event)
    {
        t.respondMouseClick(event);
        getParent().removeChild(this);
    }
    
    public void respondMouseDoubleClick(PInputEvent event)
    {
        t.respondMouseDoubleClick(event);
        getParent().removeChild(this);
    }
    
    public void respondMousePress(PInputEvent event)
    {
        t.respondMousePress(event);
        getParent().removeChild(this);
    }
    
    public void respondMouseRelease(PInputEvent event)
    {
        t.respondMouseRelease(event);
        getParent().removeChild(this);
    }

    // does nothing
    public void setMouseDownActions(MouseDownActions actions) {}
    public void setMouseDragActions(MouseDragActions actions) {}    
}
