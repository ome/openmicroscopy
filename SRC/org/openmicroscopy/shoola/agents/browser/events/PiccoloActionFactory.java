/*
 * org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory
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
 
package org.openmicroscopy.shoola.agents.browser.events;

import java.awt.geom.Rectangle2D;

import org.openmicroscopy.shoola.agents.browser.BrowserMode;
import org.openmicroscopy.shoola.agents.browser.BrowserModel;
import org.openmicroscopy.shoola.agents.browser.images.PaintMethod;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Factory classes for commonly used browser actions that require some form
 * of composition to work properly.  Which is like, all of them.  Whooooops.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PiccoloActionFactory
{
    /**
     * Generates a mode change action.
     * @param target The browser model to change.
     * @param mode The mode to change the model to.
     * @return The action which executes this, which can be applied to any
     *         node.
     */
    public static PiccoloAction getModeChangeAction(final BrowserModel target,
                                                    final String familyName,
                                                    final BrowserMode mode)
    {
        if(target == null || mode == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.setCurrentMode(familyName,mode);
            }

        };
        return action;
    }
    
    /**
     * Generates an add paint method action.
     * @param target The browser model to change.
     * @param method The (overlay) paint method to add.
     * @return The action which executes this paint method inclusion.
     */
    public static PiccoloAction getAddPaintMethodAction(final BrowserModel target,
                                                        final PaintMethod method)
    {
        if(target == null || method == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.addPaintMethod(method);
            }
        };
        return action;
    }
    
    /**
     * Generates a remove paint method action.
     * @param target The browser model to change.
     * @param method The (overlay) paint method to remove.
     * @return The action which executes this paint method exclusion.
     */
    public static PiccoloAction getRemovePaintMethodAction(final BrowserModel target,
                                                           final PaintMethod method)
    {
        if(target == null || method == null)
        {
            return null;
        }
        
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                target.removePaintMethod(method);
            }
        };
        return action;
    }
    
    /**
     * Generates a select thumbnail action that depends on the mode of the
     * @param target The browser model to tie this action to.
     * @return A select thumbnail action that will change the specified
     *         BrowserModel.
     */
    public static PiccoloAction getSelectThumbnailAction(final BrowserModel target)
    {
        if(target == null)
        {
            return null;
        }
        
        final BrowserMode selectionMode =
            target.getCurrentMode(BrowserModel.SELECT_MODE_NAME);
            
        PiccoloAction action = new PiccoloAction()
        {
            public void execute(PInputEvent e)
            {
                PNode node = e.getPickedNode();
                if(!(node instanceof Thumbnail))
                {
                    return;
                }
                Thumbnail t = (Thumbnail)node;
                
                // checks if command key is down
                if(PiccoloModifiers.getModifier(e) !=
                   PiccoloModifiers.MOUSE_INDIV_SELECT)
                {
                    target.deselectAllThumbnails();                
                }
                target.selectThumbnail(t);

                Rectangle2D bounds = t.getBounds().getBounds2D();
            }
        };
        return action;
    }
    
    /**
     * Creates an action, that, when executed, will trigger an opening of
     * this particular thumbnail.
     * @param t The thumbnail to open in the viewer.
     * @return A PiccoloAction that wraps the appropriate viewer trigger code
     *         in an execute() statement.
     */
    public static PiccoloAction getOpenInViewerAction(final Thumbnail t)
    {
        PiccoloAction action = new PiccoloAction()
        {
            public void execute()
            {
                // TODO write the open trigger code for this thumbnail.
            }
        };
        return action;
    }
}
