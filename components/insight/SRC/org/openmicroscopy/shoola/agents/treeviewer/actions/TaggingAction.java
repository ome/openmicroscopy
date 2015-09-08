/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;

/** 
 * Action to launch the available tags.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class TaggingAction 
	extends TreeViewerAction
{

	/** The default name of the action. */
    private static final String NAME = "Tag...";
    
    /** The default name of the action. */
    private static final String DESCRIPTION = "Add or remove tags.";
    
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
            case Browser.COUNTING_ITEMS:  
                setEnabled(false);
                break;
            default:
                onDisplayChange(browser.getLastSelectedDisplay());
        }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
        	setEnabled(false);
            return;
        }
        Browser browser = model.getSelectedBrowser();
        if (browser == null) {
        	setEnabled(false);
            return;
        } 
        Object ho = selectedDisplay.getUserObject(); 

        if (ho instanceof DatasetData || ho instanceof ProjectData ||
        	ho instanceof ImageData || ho instanceof ScreenData ||
			ho instanceof PlateData || ho instanceof PlateAcquisitionData) {
        	if (model.canAnnotate(ho)) {
        		List selected = browser.getSelectedDataObjects();
        		if (selected == null) setEnabled(false);
        		else {
        			List<Long> ids = new ArrayList<Long>();
            		Iterator i = selected.iterator();
            		DataObject data;
            		while (i.hasNext()) {
    					data = (DataObject) i.next();
    					if (!ids.contains(data.getGroupId()))
    						ids.add(data.getGroupId());
    				}
            		setEnabled(ids.size() == 1);
        		}
        	} else setEnabled(false);
        } else setEnabled(false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public TaggingAction(TreeViewer model)
	{
		super(model);
		name = NAME;  
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		description = (String) getValue(Action.SHORT_DESCRIPTION);
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.TAG));
	}
	
	/**
	 * Brings up the tagging wizard.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    {
    	model.showTagWizard();
    }
    
}
