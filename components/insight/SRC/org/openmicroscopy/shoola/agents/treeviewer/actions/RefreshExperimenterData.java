/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;

/** 
 * Reloads the experimenter data.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class RefreshExperimenterData
	extends TreeViewerAction
{

	/** Name of the action. */
    private static final String NAME = "Refresh User";
    
    /** Description of the action. */
    private static final String DESCRIPTION = "Refresh User's data.";
    
    /**
     * Sets the action enabled depending on the browser's type and 
     * the currently selected node. Sets the name of the action depending on 
     * the <code>DataObject</code> hosted by the currently selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Browser b = model.getSelectedBrowser();
        
        if (b != null && b.getSelectedDisplays().length > 1) {
        	setEnabled(false);
        	return;
        }
        Object ho = selectedDisplay.getUserObject();
        setEnabled(ho instanceof ExperimenterData);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public RefreshExperimenterData(TreeViewer model)
	{
		super(model);
		putValue(Action.NAME, NAME);
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.REFRESH)); 
	}
	
	/**
     * Reloads the experimenter's data.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Browser b = model.getSelectedBrowser();
        if (b != null) b.refreshExperimenterData();
    }
	
}
