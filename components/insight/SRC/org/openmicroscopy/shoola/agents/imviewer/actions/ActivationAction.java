/*
 * org.openmicroscopy.shoola.agents.imviewer.actions.ActivationAction
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

package org.openmicroscopy.shoola.agents.imviewer.actions;




//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action attached to a menu item in the Windows menu.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Rev$ $LastChangedDate$)
 * </small>
 * @since OME2.2
 */
public class ActivationAction
    extends ViewerAction
{

    /** Description of the action. */
    private static final String DESCRIPTION = "View the selected image.";
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ActivationAction(ImViewer model)
    {
        super(model);
        setEnabled(true);
        putValue(Action.NAME, model.getViewTitle());
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        ImageIcon icon = model.getImageIcon();
        if (icon == null) {
            IconManager im = IconManager.getInstance();
            icon = im.getImageIcon(IconManager.VIEWER);
        }
        putValue(Action.SMALL_ICON, icon);
    }

    /** 
     * Activates the model. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        model.activate(null, -1, model.getDisplayMode(), -1);
    }

}
