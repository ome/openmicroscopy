/*
 * org.openmicroscopy.shoola.agents.browser.ui.BrowserViewEventDispatcher
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

import org.openmicroscopy.shoola.agents.browser.events.MouseDownSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseDragSensitive;
import org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PPickPath;

/**
 * Contains the event dispatching code for the BrowserView.  Makes the
 * BrowserView code a little less dense.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public final class BrowserViewEventDispatcher
{
    private static PBasicInputEventHandler defaultClickHandler =
        new PBasicInputEventHandler()
        {
            public void mouseClicked(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseDownSensitive)
                    {
                        ((MouseDownSensitive)node).respondMouseClick(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        
            public void mouseEntered(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                    !e.isHandled())
                {
                    if(node instanceof MouseOverSensitive)
                    {
                        ((MouseOverSensitive)node).respondMouseEnter(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
            
            public void mouseExited(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseOverSensitive)
                    {
                        ((MouseOverSensitive)node).respondMouseExit(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        };
        
    private static PDragSequenceEventHandler defaultDragHandler =
        new PDragSequenceEventHandler()
        {       
            public void startDrag(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseDragSensitive)
                    {
                        setIsDragging(true);
                        ((MouseDragSensitive)node).respondStartDrag(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        
            public void drag(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseDragSensitive)
                    {
                        ((MouseDragSensitive)node).respondDrag(e);
                        e.setHandled(true);
                    }
                    else
                    {
                        pickPath.popNode(node);
                    }
                }
            }
        
            public void endDrag(PInputEvent e)
            {
                PPickPath pickPath = e.getPath();
                PNode node;
                while((node = pickPath.getPickedNode()) != null &&
                      !e.isHandled())
                {
                    if(node instanceof MouseDragSensitive)
                    {
                        setIsDragging(false);
                        ((MouseDragSensitive)node).respondEndDrag(e);
                        e.setHandled(true);
                    }
                }
            }
        };
    
    /**
     * Returns the handler responsible for dispatching mouse over/down
     * events to the subcomponents within the view below.
     * 
     * @return See above.
     */
    public static PBasicInputEventHandler getDefaultMouseHandler()
    {
        return defaultClickHandler;
    }
    
    /**
     * Returns the handler responsible for dispatching mouse drag events to
     * the subcomponents within the view below.
     * @return
     */
    public static PDragSequenceEventHandler getDefaultDragHandler()
    {
        defaultDragHandler.setMinDragStartDistance(4);
        return defaultDragHandler;
    }
}
