/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.AnnotatedButton
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;

/** 
 * A button to indicate that the <code>Dataset</code> or <code>Image</code> has
 * been annotated.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class AnnotatedButton
	extends JButton
{

    /** Description of the button. */
    private final String DESCRIPTION = "Annotated object.";
    
    /** Description of the button. */
    private final String DESCRIPTION_FULL = "Annotated object. Click to view " +
            								"the annotation. ";
    
    /** The node hosting this button. */
    private final ImageNode    parentNode;
    
    /**
     *  
     * Creates a new instance. 
     * 
     * @param node 			The node hosting this button. 
     * 						Mustn't be <code>null</code>.
     * @param withListener 	Pass <code>true</code> to attach an action listener
     * 						<code>false</code> otherwise.
     */
    public AnnotatedButton(ImageNode node, boolean withListener)
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
        setIcon(im.getIcon(IconManager.ANNOTATED_SMALL));
        //setRolloverIcon(im.getIcon(IconManager.ANNOTATED_SMALL_OVER));
        if (withListener) {
        	setToolTipText(DESCRIPTION_FULL);
            addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e)
                {
                    parentNode.fireAnnotation();
                }
            
            });
        } else setToolTipText(DESCRIPTION);
        
    }
    
    /** 
     * Creates a new instance. 
     * 
     * @param node The node hosting this button.  Mustn't be <code>null</code>.
     */
    AnnotatedButton(ImageNode node)
    {
    	this(node, true);
    }
    
}
