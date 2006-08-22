/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.AnnotatedButton
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;


//Java imports
import java.awt.Insets;
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
class AnnotatedButton
    extends JButton
{

    /**
     * Creates a new instance.
     */
    AnnotatedButton()
    {
        setBorder(BorderFactory.createEmptyBorder());  //No border around icon.
        setMargin(new Insets(0, 0, 0, 0));//Just to make sure button sz=icon sz.
        setOpaque(false);  //B/c button=icon.
        setFocusPainted(false);  //Don't paint focus box on top of icon.
        setRolloverEnabled(true);
        IconManager im = IconManager.getInstance();
        setIcon(im.getIcon(IconManager.ANNOTATED_SMALL));
        setRolloverIcon(im.getIcon(IconManager.ANNOTATED_SMALL_OVER));
    }
    
}
