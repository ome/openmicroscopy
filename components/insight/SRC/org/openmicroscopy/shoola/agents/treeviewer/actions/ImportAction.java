/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ImportAction 
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CreateCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import com.mdl.dbbrowser.client.SetSortDialog;

import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Action to import the images.
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
public class ImportAction
	extends TreeViewerAction
{

    /** The description of the action. */
    static final String DESCRIPTION_DATASET = 
    	"Launch the Importer. Import data to Project, Datasets...";
    
    /** The description of the action. */
    static final String DESCRIPTION_SCREEN = 
    	"Launch the Importer. Import data to Screen...";
    
	/** The name of the action. */
    private static final String NAME = "Import...";
    
    /** Flag indicating not to select any node. */
    private boolean noNode;
    
    /**
     * Sets the description of the action depending on the active browser.
     * 
     * @param browserType The type of browser.
     */
    private void setActionDescription(int browserType)
    {
    	if (browserType == Browser.SCREENS_EXPLORER)
    		putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_SCREEN));
    	else 
			putValue(Action.SHORT_DESCRIPTION, 
					UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
    }
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) return;
        switch (browser.getState()) {
	        case Browser.LOADING_DATA:
	        case Browser.LOADING_LEAVES:
	        //case Browser.COUNTING_ITEMS:  
	            setEnabled(false);
	            break;
	        default:
	        	onDisplayChange(browser.getLastSelectedDisplay());
        }
    }
    
    /** 
     * Sets the description of this action depending on the browser 
     * {@link Browser}.
     * @see TreeViewerAction#onBrowserSelection(Browser)
     */
    protected void onBrowserSelection(Browser browser)
    {
    	int type = Browser.PROJECTS_EXPLORER;
    	if (browser == null) {
    		if (TreeViewerAgent.isSPWFirst()) 
    			type = Browser.SCREENS_EXPLORER;
    	}
    	if (browser.getBrowserType() == Browser.SCREENS_EXPLORER)
    		type = Browser.SCREENS_EXPLORER;
    	setActionDescription(type);
    }
    
    /**
     * Sets the action enabled depending on the selected node.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
    	if (noNode) {
    		setEnabled(true);
    		return;
    	}
    	Browser browser = model.getSelectedBrowser();
    	setEnabled(false);
        if (browser == null) 
        	return;
        if (selectedDisplay == null) {
        	setEnabled(true);
        	return;
        }
        TreeImageDisplay[] nodes = browser.getSelectedDisplays();
        if (nodes != null && nodes.length > 1) {
        	setEnabled(false);
        	return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof ProjectData || ho instanceof ScreenData || 
        		ho instanceof DatasetData)
        	setEnabled(model.isUserOwner(ho));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param noNode Pass <code>true</code> if no nodes need to be specified,
     * 				 <code>false</code> otherwise.
     */
	public ImportAction(TreeViewer model, boolean noNode)
	{
		super(model);
		this.noNode = noNode;
		name = NAME;
		int type = Browser.PROJECTS_EXPLORER;
		if (TreeViewerAgent.isSPWFirst()) 
			type = Browser.SCREENS_EXPLORER;
		setActionDescription(type);
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.IMPORTER));
	}

	/**
     * Creates a {@link CreateCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       CreateCmd cmd = new CreateCmd(model, CreateCmd.IMAGE);
       cmd.setWithParent(!noNode);
       cmd.execute();
    }
    
}
