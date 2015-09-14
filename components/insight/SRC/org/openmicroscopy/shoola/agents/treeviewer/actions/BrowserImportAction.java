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

import org.openmicroscopy.shoola.agents.events.importer.LoadImporter;
import org.openmicroscopy.shoola.agents.events.treeviewer.BrowserSelectionEvent;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

/**
 * Action to import images.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class BrowserImportAction 
	extends BrowserAction
{

    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
	    	 case Browser.LOADING_DATA:
	         case Browser.LOADING_LEAVES:
	             setEnabled(false);
	             break;
	         default:
	        	 onDisplayChange(model.getLastSelectedDisplay());
         }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
    	int t = model.getBrowserType();
    	if (t == Browser.PROJECTS_EXPLORER || t == Browser.SCREENS_EXPLORER) {
    		if (selectedDisplay == null) {
            	setEnabled(true);
                return;
            }
            Object ho = selectedDisplay.getUserObject();
            if (ho == null) setEnabled(true);
            else {
            	/*
            	TreeImageDisplay[] nodes = model.getSelectedDisplays();
                if (nodes != null && nodes.length > 1) {
                	setEnabled(false);
                } else {
                	if (ho instanceof ProjectData || ho instanceof DatasetData ||
                			ho instanceof ScreenData) 
                		setEnabled(model.isUserOwner(ho));
                	else setEnabled(true);
                }
                */
            	if (ho instanceof ProjectData || ho instanceof DatasetData ||
            			ho instanceof ScreenData || ho instanceof ImageData) 
            		setEnabled(model.canEdit(ho));
            	else if (ho instanceof ExperimenterData && 
            			t != Browser.ADMIN_EXPLORER) {
            		ExperimenterData exp = TreeViewerAgent.getUserDetails();
            		setEnabled(exp.getId() == ((ExperimenterData) ho).getId());
            	} else setEnabled(true);
            }
    	} else setEnabled(false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public BrowserImportAction(Browser model)
	{
		super(model);
		switch (model.getBrowserType()) {
			case Browser.SCREENS_EXPLORER:
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(
								ImportAction.DESCRIPTION_SCREEN));
				break;
			case Browser.PROJECTS_EXPLORER:
			default:
				putValue(Action.SHORT_DESCRIPTION, 
						UIUtilities.formatToolTipText(
								ImportAction.DESCRIPTION_DATASET));
				break;
		}
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.IMPORTER));
	}
	
    /**
     * Brings up the importer dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	TreeImageDisplay display = model.getLastSelectedDisplay();
    	if (display != null) {
    		Object o = display.getUserObject();
        	if (o instanceof ImageData) {
        		TreeImageDisplay p = display.getParentDisplay();
        		if (p == null) return;
        		display = p;
        	}
    	}
    	
    	LoadImporter event = null;
    	int type = BrowserSelectionEvent.PROJECT_TYPE;
    	switch (model.getBrowserType()) {
			case Browser.SCREENS_EXPLORER:
				type = BrowserSelectionEvent.SCREEN_TYPE;
    	}
    	event = new LoadImporter(display, type);
    	event.setGroup(model.getSecurityContext(display).getGroupID());
    	//long id = TreeViewerAgent.getUserDetails().getId();
    	//event.setObjects(model.getNodesForUser(id, display));
    	EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
    	bus.post(event);
    }
    
}
