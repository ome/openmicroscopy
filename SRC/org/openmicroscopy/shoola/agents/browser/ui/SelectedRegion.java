/*
 * org.openmicroscopy.shoola.agents.browser.ui.SelectedRegion
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

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import org.openmicroscopy.shoola.agents.browser.events.MouseDownActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragActions;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A region that is used for selection, to interrupt dispatch on mouse
 * drag events from selected image thumbnails.  This is *sort* of a hack,
 * but it was the most efficient hack that I could come up with.
 * We'll see if selection proves to be a non-special case, that implies
 * that this class (or selection/canvas-local construction) should be
 * more generic.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
class SelectedRegion extends PNode implements MouseDownSensitive,
                                              MouseDragSensitive
{
    private Shape activePath;
    private MouseDownActions mouseDownActions;
    private MouseDragActions mouseDragActions;
    
    /**
     * Constructs a new selected region.
     *
     */
    public SelectedRegion()
    {
        activePath = new GeneralPath();
        mouseDownActions = new MouseDownActions();
        mouseDragActions = new MouseDragActions();
    }
    
    /**
     * Returns the selected shape that covers all selected thumbnails.
     * @return The shape over all selected thumbnails.
     */
    public Shape getSelectedRegion()
    {
       return activePath;
    }
    
    /**
     * Overrides the bounds constraints to pick up the entire object.
     * (This might not work)
     */
    public PBounds getBounds()
    {
        return new PBounds(activePath.getBounds2D());
    }
    
    /**
     * Overrides the setBounds method to be consistent with Piccolo
     * transformations.
     */
    public boolean setBounds(double x, double y, double width, double height)
    {
        if(!super.setBounds(x,y,width,height))
        {
            return false;
        }
        
        Rectangle2D bounds = activePath.getBounds2D();
        double widthScale = width/bounds.getWidth();
        double heightScale = width/bounds.getHeight();
        
        AffineTransform scaleTransform =
            AffineTransform.getScaleInstance(widthScale,heightScale);
        
        activePath = scaleTransform.createTransformedShape(activePath);
        
        double currentX = activePath.getBounds2D().getX();
        double currentY = activePath.getBounds2D().getY();
        
        AffineTransform shiftTransform =
            AffineTransform.getTranslateInstance(x-currentX,y-currentY);
        
        activePath = shiftTransform.createTransformedShape(activePath);
        return true;   
    }
    
    /**
     * Generate a region by specifying which thumbnails are
     * currently selected.  The region will blanket over these
     * thumbnails, and thus, will intercept mouse events first.
     * 
     * @param thumbnails The thumbnails to build a selected region over.
     */
    public void setSelected(Thumbnail[] thumbnails)
    {
        GeneralPath path = new GeneralPath();
        if(thumbnails == null || thumbnails.length == 0){
            activePath = path;
        }
        else
        {
            for(int i=0;i<thumbnails.length;i++)
            {
                path.append(thumbnails[i].getBounds().getBounds2D(),false);
            }
            activePath = path;
        }
    }
    
    /**
     * Overrides intersects so that they'll get the right mouse events.
     * @param bounds The rectangle to check
     */
    public boolean intersects(Rectangle2D bounds)
    {
        return activePath.intersects(bounds);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#getMouseDownActions()
     */
    public MouseDownActions getMouseDownActions()
    {
        return mouseDownActions;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#getMouseDragActions()
     */
    public MouseDragActions getMouseDragActions()
    {
        return mouseDragActions;
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondStartDrag(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondStartDrag(PInputEvent e)
    {
        PiccoloAction dragAction =
            mouseDragActions.getStartDragAction(PiccoloModifiers.getModifier(e));
        dragAction.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondDrag(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondDrag(PInputEvent e)
    {
        PiccoloAction dragAction =
            mouseDragActions.getDragAction(PiccoloModifiers.getModifier(e));
        dragAction.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#respondDrag(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondEndDrag(PInputEvent e)
    {
        PiccoloAction dragAction =
            mouseDragActions.getEndDragAction(PiccoloModifiers.getModifier(e));
        dragAction.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseClick(PInputEvent e)
    {
        PiccoloAction downAction =
            mouseDownActions.getMouseClickAction(PiccoloModifiers.getModifier(e));
        downAction.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseDoubleClick(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseDoubleClick(PInputEvent e)
    {
        PiccoloAction action =
            mouseDownActions.getDoubleClickAction(PiccoloModifiers.getModifier(e));
        action.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMousePress(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMousePress(PInputEvent e)
    {
        PiccoloAction downAction =
            mouseDownActions.getMousePressAction(PiccoloModifiers.getModifier(e));
        downAction.execute(e);
    }

    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#respondMouseRelease(edu.umd.cs.piccolo.event.PInputEvent)
     */
    public void respondMouseRelease(PInputEvent e)
    {
        PiccoloAction downAction =
            mouseDownActions.getMouseReleaseAction(PiccoloModifiers.getModifier(e));
        downAction.execute(e);
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive#setMouseDownActions(org.openmicroscopy.shoola.agents.browser.events.MouseDownActions)
     */
    public void setMouseDownActions(MouseDownActions actions)
    {
        if(actions != null)
        {
            mouseDownActions = actions;
        }
    }
    
    /**
     * @see org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive#setMouseDragActions(org.openmicroscopy.shoola.agents.browser.events.MouseDragActions)
     */
    public void setMouseDragActions(MouseDragActions actions)
    {
        if(actions != null)
        {
            mouseDragActions = actions;
        }
    }
}
