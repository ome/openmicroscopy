/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.PinButton 
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
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * A button to pint the <code>Image</code>'s thumbnail.
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
class PinButton
	extends JButton
{
	
    /** Description of the button. */
    private final String DESCRIPTION = "Click to pin the thumbnail on desktop.";
    
    /** The node hosting this button. */
    private final ImageNode    parentNode;

    /** 
     * Creates a new instance. 
     * 
     * @param node The node hosting this button. Mustn't be <code>null</code>.
     */
    PinButton(ImageNode node)
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
        setIcon(im.getIcon(IconManager.PIN_SMALL));
        //setRolloverIcon(im.getIcon(IconManager.PIN_SMALL_OVER));
        setToolTipText(DESCRIPTION);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                parentNode.pinThumbnail();
            }
        });
    }
    
}
