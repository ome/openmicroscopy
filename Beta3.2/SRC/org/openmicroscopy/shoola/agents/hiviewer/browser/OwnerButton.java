/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.OwnerButton 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.hiviewer.browser;



//Java imports
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class OwnerButton 
	extends JButton
{

	/** Description of the button. */
    private final String DESCRIPTION = "Indicates that you are the owner.";
    
    /** The node hosting this button. */
    private final ImageNode    parentNode;

    /** 
     * Creates a new instance. 
     * 
     * @param node The node hosting this button. Mustn't be <code>null</code>.
     */
    OwnerButton(ImageNode node)
    {
    	if (node == null) throw new IllegalArgumentException("No node");
        parentNode = node;
        setContentAreaFilled(false);
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        setMargin(new Insets(0, 0, 0, 0));//Just to make sure button sz=icon sz.
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        //setRolloverEnabled(true);
        IconManager im = IconManager.getInstance();
        setIcon(im.getIcon(IconManager.OWNER_SMALL));
        setToolTipText(DESCRIPTION);
    }
    
}
