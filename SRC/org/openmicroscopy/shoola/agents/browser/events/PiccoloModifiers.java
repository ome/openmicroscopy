/*
 * org.openmicroscopy.shoola.agents.browser.events.PiccoloModifiers
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

import java.awt.Toolkit;

import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * Decouples the getModifiers() implementation for events from the Piccolo/
 * AWT implementation (as there is no Piccolo specification as to what
 * getModifiers() will return).  Also wraps the isPopupTrigger() in its own
 * bit.  This allows mapping from modifier combinations to actions.
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PiccoloModifiers
{
    /**
     * No modifiers flag.
     */
    public static int NORMAL = 0;
    
    /**
     * Popup modifier on flag.
     */
    public static int POPUP = 1;
    
    /**
     * Shift down flag.
     */
    public static int SHIFT_DOWN = 2;
    
    /**
     * Ctrl down flag (currently never selected w/POPUP)
     */
    public static int KEY_SHORTCUT = 4;
    
    /**
     * Alt down flag.
     */
    public static int ALT_DOWN = 8;
    
    /**
     * Convenience for alt-shift method.
     */
    public static int SHIFT_ALT_DOWN = SHIFT_DOWN | ALT_DOWN;
    
    /**
     * Convenience for shift-control/command method.
     */
    public static int SHIFT_SHORTCUT_DOWN = SHIFT_DOWN | KEY_SHORTCUT;
    
    /**
     * Convenience for alt-control/command method.
     */
    public static int ALT_SHORTCUT_DOWN = ALT_DOWN | KEY_SHORTCUT;
    
    /**
     * Discrete select flag (command for Macs, ctrl for UNIX/Windows)
     */
    public static int MOUSE_INDIV_SELECT = 16;
    
    private static int shortcutFlag =
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    /**
     * Gets a modifier from the specified PInputEvent.  Note that this
     * modifier will *never* return a modifier with any two of the KEY_SHORTCUT,
     * POPUP, and MOUSE_INDIV_SELECT bits selected.  The shift and alt
     * modifiers are the free variables.
     * 
     * @param e The input event to analyze.
     * @return A modifier with the modifier bits turned on.
     */
    public static int getModifier(PInputEvent e)
    {
        int modifier = NORMAL;
        
        // on mouse event
        if(e.isMouseEvent())
        {
            // use command (Mac)/control (UNIX/Win)
            if((e.getModifiers() & shortcutFlag) == shortcutFlag)
            {
                modifier = modifier | MOUSE_INDIV_SELECT;
            }
            // which cannot be used in conjunction w/popup trigger
            else if(e.isPopupTrigger())
            {
                modifier = modifier | POPUP;
            }
        }
        
        // on key event
        else if(e.isKeyEvent())
        {
            // use command (Mac)/control (UNIX/Win)
            if((e.getModifiers() & shortcutFlag) == shortcutFlag)
            {
                modifier = modifier | KEY_SHORTCUT;
            }
            // which cannot be used in conjunction w/popup trigger
            // (ctrl (macs), menu key (win))
            else if(e.isPopupTrigger())
            {
                modifier = modifier | POPUP;
            }
        }
        // shift's fair game for both
        if(e.isShiftDown())
        {
            modifier = modifier | SHIFT_DOWN;
        }
        // alt/option, I think, is also fair game for both.
        if(e.isAltDown())
        {
            modifier = modifier | ALT_DOWN;
        }
        
        // hope this works
        return modifier;
    }
}
