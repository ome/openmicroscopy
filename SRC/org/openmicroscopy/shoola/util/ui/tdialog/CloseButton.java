/*
 * org.openmicroscopy.shoola.util.ui.tdialog.CloseButton
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui.tdialog;


//Java imports
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;

/** 
 * The close button in the {@link TitleBar}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: 4695 $ $Date: 2006-12-15 17:08:05 +0000 (Fri, 15 Dec 2006) $)
 * </small>
 * @since OME2.2
 */
class CloseButton
    extends JButton
{
    
    /** 
     * Identifies the close action.
     * @see #setActionType(int) 
     */
    static final int    CLOSE_ACTION = 1;
    
    /** Tooltip text when the button repsents the close action. */
    static final String CLOSE_TOOLTIP = "Close";
       
    /** Creates a new instance. */
    CloseButton() 
    {
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        //Just to make sure button sz=icon sz.
        setMargin(new Insets(0, 0, 0, 0));  
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
    }
    
    /**
     * Sets the button to represent the specified action.
     * 
     * @param type One of the constants defined by this class.
     */
    void setActionType(int type)
    {
        switch (type) {
            case CLOSE_ACTION:
            	IconManager icons = IconManager.getInstance();
                setIcon(icons.getIcon(IconManager.CROSS));
                setRolloverIcon(icons.getIcon(IconManager.CROSS_OVER));
                setToolTipText(CLOSE_TOOLTIP);
        }
    }
    
    /** 
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#isFocusable()
     */
    public boolean isFocusable() { return false; }
    
    /** 
     * Overridden to make sure no focus is painted on top of the icon. 
     * @see JButton#requestFocus()
     */
    public void requestFocus() {}

}