/*
 * org.openmicroscopy.shoola.agents.browser.events.MouseOverSensitive
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

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Specifies a class or node sensitive to mouse over events.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public interface MouseOverSensitive
{
    /**
     * Return the action executed when the mouse first travels over the
     * object.
     * @return See above.
     */
    public PiccoloAction getMouseEnterAction();
    
    /**
     * Return the action executed when the mouse exits over the
     * object.
     * @return See above.
     */
    public PiccoloAction getMouseExitAction();
    
    /**
     * Return the action executed when the mouse hovers over the object
     * (inclusion TBD)
     * @return See above.
     */
    public PiccoloAction getMouseHoverAction();
    
    /**
     * Sets the action executed when the mouse first travels over the
     * object to the specified action.
     * @param action See above.
     */
    public void setMouseEnterAction(PiccoloAction action);
    
    /**
     * Sets the action executed when the mouse exits the
     * object to the specified action.
     * @param action See above.
     */
    public void setMouseExitAction(PiccoloAction action);
    
    /**
     * Sets the action executed when the mouse is hovering over the
     * object to the specified action.
     * @param action See above.
     */
    public void setMouseHoverAction(PiccoloAction action);
    
    /**
     * Respond to a mouse over.
     * @param e The parameters of the mouse enter.
     */
    public void mouseEntered(PInputEvent e);
    
    /**
     * Respond to a mouse exit.
     * @param e The parameters of the mouse exit.
     */
    public void mouseExited(PInputEvent e);
}
