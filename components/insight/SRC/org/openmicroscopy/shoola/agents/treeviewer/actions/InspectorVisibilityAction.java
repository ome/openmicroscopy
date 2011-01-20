/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.InspectorVisibilityAction 
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
 * Show or hide the tree view.
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
public class InspectorVisibilityAction 
	extends TreeViewerAction
{

    /** The name of the action. */
    private static final String NAME_HIDE = "Hide Tree Viewer";
    
    /** The name of the action. */
    private static final String NAME_SHOW = "Show Tree Viewer";
    
    /** The description of the action. */
    private static final String DESCRIPTION_HIDE = "Hide the Tree Viewer."; 
    
    /** The description of the action. */
    private static final String DESCRIPTION_SHOW = "Show the Tree Viewer."; 
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public InspectorVisibilityAction(TreeViewer model)
	{
		super(model);
		setEnabled(true);
		name = NAME_HIDE;
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION_HIDE));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.INSPECTOR)); 
	}
	
    /**
     * Shows or hides the Tree Viewer.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    { 
    	if (NAME_HIDE.equals(name)) {
    		name = NAME_SHOW;
    		putValue(Action.SHORT_DESCRIPTION, 
    				UIUtilities.formatToolTipText(DESCRIPTION_SHOW));
    	} else {
    		name = NAME_HIDE;
    		putValue(Action.SHORT_DESCRIPTION, 
    				UIUtilities.formatToolTipText(DESCRIPTION_HIDE));
    	}
    	model.setInspectorVisibility();
    }
    
}
