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
import java.util.Timer;
import java.util.TimerTask;

import org.openmicroscopy.shoola.agents.browser.events.BrowserAction;
import org.openmicroscopy.shoola.agents.browser.events.BrowserActions;
import org.openmicroscopy.shoola.agents.browser.events.ReversibleBrowserAction;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;

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
     * The action to be executed when a left-click occurs on this icon.
     */
    protected BrowserAction leftAction;
    
    /**
     * The action to be executed when a right-click occurs on this icon.
     */
    protected BrowserAction rightAction;
    
    /**
     * The action to be executed when a mouseover occurs.
     */
    protected BrowserAction mouseOverAction;
    
    /**
     * The presentation (viewable) node... this is kept separate so an
     * icon can be either text, a glyph, or an image.
     */
    protected PNode presentationNode;
    
    /**
     * The event handler for the icon.
     */
    protected IconEventHandler eventHandler;
    
    // initialization method
    private void init()
    {
        eventHandler = new IconEventHandler();
    }
    
    
    /**
     * Constructs an icon with the specified image.
     * @param imageIcon The image to show in the icon.
     */
    public BIcon(BufferedImage imageIcon)
    {
        init();
        presentationNode = new PImage(imageIcon,true);
        addChild(presentationNode);
        presentationNode.setOffset(0,0);
        presentationNode.addInputEventListener(eventHandler);
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
        presentationNode.setOffset(0,0);
        presentationNode.addInputEventListener(eventHandler);
    }
    
    /**
     * Constructs an icon with a predefined node.  Overrides
     * click behavior for the child node.  In this manner,
     * supports image+text icons and glyph-based icons.
     * 
     * @param childNode
     */
    public BIcon(PNode childNode)
    {
        this.presentationNode = childNode;
        addChild(presentationNode);
        presentationNode.setOffset(0,0);
        presentationNode.addInputEventListener(eventHandler);
    }
    
    /**
     * Sets the action that occurs whenever a user left-clicks on the icon
     * to the specified response.
     * @param action What should happen when the user left-clicks the icon.
     */
    public void setLeftClickAction(BrowserAction action)
    {
        this.leftAction = action;
        // TODO: change event handler
    }
    
    /**
     * Sets the action that occurs whenever a user right-clicks on the icon
     * to the specified response.
     * @param action What should happen when the user right-clicks the icon.
     */
    public void setRightClickAction(BrowserAction action)
    {
        this.rightAction = action;
        // TODO: change event handler
    }
    
    /**
     * Sets the action that occurs whenever a user mouses over the icon to
     * the specified response.
     * @param action What should happen when the user mouses over the icon.
     */
    public void setMouseOverAction(BrowserAction action)
    {
        this.mouseOverAction = action;
        // TODO: change event handler
    }
    
    /**
     * The inner class that governs the user interaction with the icon.
     * 
     * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
     * <b>Internal version:</b> $Revision$ $Date$
     * @version 2.2
     * @since OME2.2
     */
    class IconEventHandler extends PBasicInputEventHandler
    {
        private BrowserAction leftClickAction = BrowserActions.NOOP_ACTION;
        private BrowserAction rightClickAction = BrowserActions.NOOP_ACTION;
        private BrowserAction mouseOverAction = BrowserActions.NOOP_ACTION;
        
        private int triggerTime = 500;
        private boolean pendingMouseOverAction = false;
        private boolean mouseOverActionExecuted = false;
        
        /**
         * Sets the left click action to the specified response.
         * 
         * @param action What to do on a left-click
         */
        public void setLeftClickAction(BrowserAction action)
        {
            if(action == null)
            {
                leftClickAction = BrowserActions.NOOP_ACTION;
            }
            else
            {
                leftClickAction = action;
            }
        }
        
        /**
         * Sets the right click action to the specified response.
         * 
         * @param action What do to on a right (ctrl) click
         */
        public void setRightClickAction(BrowserAction action)
        {
            if(action == null)
            {
                rightClickAction = BrowserActions.NOOP_ACTION;
            }
            else
            {
                rightClickAction = action;
            }
        }
        
        /**
         * Sets the mouse over action to the specified response.
         * Can be reversible.
         * 
         * @param action What to do on a mouse over.
         */
        public void setMouseOverAction(BrowserAction action)
        {
            if(action == null)
            {
                mouseOverAction = BrowserActions.NOOP_ACTION;
            }
            else
            {
                mouseOverAction = action;
            }
        }
        
        /**
         * Trigger on release (just like toolbars in Windows/Mac)
         */
        public void mouseReleased(PInputEvent e)
        {
            // TODO: verify that this works cross-platform
            if(e.isPopupTrigger())
            {
                rightClickAction.actionPerformed();
            }
            else
            {
                leftClickAction.actionPerformed();
            }
            e.setHandled(true);
        }
        
        /**
         * Sets the interval from when a user mouses over the icon for it to
         * occur.
         * 
         * @param millis
         */
        public void setMouseOverTriggerTime(int millis)
        {
            if(millis < 0)
            {
                millis = 0;
            }
            triggerTime = millis;
        }
        
        /**
         * Responds to a mouseEntered event.  Will execute the action bound
         * to this event handler's mouseEntered event if a mouseExited event
         * does not cancel it within the mouse over trigger time.
         * 
         * @param e The input event.
         */
        public void mouseEntered(PInputEvent e)
        {
            Timer timer = new Timer();
            TimerTask task = new TimerTask()
            {
                public void run()
                {
                    if(pendingMouseOverAction)
                    {
                        mouseOverAction.actionPerformed();
                        mouseOverActionExecuted = true;
                        pendingMouseOverAction = false;
                    }
                }
            };
            timer.schedule(task,triggerTime);
            e.setHandled(true);
        }
        
        /**
         * Responds to a mouse exited event.  If the bound action is
         * reversible, the action will be undone/cancelled.
         * 
         * @param e The event to respond to.
         */
        public void mouseExited(PInputEvent e)
        {
            pendingMouseOverAction = false;
            if(mouseOverActionExecuted)
            {
                if(mouseOverAction instanceof ReversibleBrowserAction)
                {
                    ((ReversibleBrowserAction)mouseOverAction).actionCancelled();
                }
            }
            e.setHandled(true);
        }
    }
}
