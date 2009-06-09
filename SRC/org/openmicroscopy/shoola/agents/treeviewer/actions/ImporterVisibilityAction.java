/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ImporterVisibilityAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.actions;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;
//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Action to show or hide the importer view.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class ImporterVisibilityAction 	
	extends TreeViewerAction
{
	
    /** The description of the action. */
    private static final String DESCRIPTION_HIDE = "Hide the Import view."; 
    
    /** The description of the action. */
    private static final String DESCRIPTION_SHOW = "Show the Import Viewer."; 
    
    /** Reference to the icons manager. */
    private IconManager icons;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public ImporterVisibilityAction(TreeViewer model)
	{
		super(model);
		setEnabled(true);
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION_HIDE));
		icons = IconManager.getInstance();
		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.BACKWARD_NAV)); 
	}
	
    /**
     * Shows or hides the Importer.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	if (model.setImporterVisibility()) {
    		putValue(Action.SHORT_DESCRIPTION, 
    				UIUtilities.formatToolTipText(DESCRIPTION_HIDE));
    		putValue(Action.SMALL_ICON, 
    				icons.getIcon(IconManager.BACKWARD_NAV)); 
    	} else {
    		putValue(Action.SHORT_DESCRIPTION, 
    				UIUtilities.formatToolTipText(DESCRIPTION_SHOW));
    		putValue(Action.SMALL_ICON, icons.getIcon(IconManager.FORWARD_NAV)); 
    	}
    }
    
}
