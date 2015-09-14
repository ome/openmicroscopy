/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.event.ActionEvent;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ImageData;

/** 
 * Views the selected image(s).
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta3
 */
public class ViewImageAction 
	extends TreeViewerAction
{

	/** Name of the action. */
    private static final String NAME = "View...";

    /** Description of the action. */
    private static final String DESCRIPTION = "View the selected image.";
    
    /**
     * Sets the action enabled depending on the browser's type and 
     * the currently selected node. Sets the name of the action depending on 
     * the <code>DataObject</code> hosted by the currently selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null || 
        		selectedDisplay.getParentDisplay() == null ||
        		selectedDisplay instanceof TreeImageTimeSet) {
            setEnabled(false);
            return;
        }
        setEnabled(selectedDisplay.getUserObject() instanceof ImageData);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ViewImageAction(TreeViewer model)
    {
        super(model);
        name = NAME;
        IconManager icons = IconManager.getInstance();
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        putValue(Action.SMALL_ICON, icons.getIcon(IconManager.VIEWER)); 
    }
    
    /**
     * Creates a  {@link ViewCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       ViewCmd cmd = new ViewCmd(model, true);
       cmd.execute();
    }
    
}
