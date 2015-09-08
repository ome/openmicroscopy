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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/**
 * Action to delete the selected nodes.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class BrowserDeleteAction 
	extends BrowserAction
{

	/** Name of the action. */
    private static final String NAME = "Delete";
    
	/** Description of the action. */
    private static final String DESCRIPTION = "Delete the selected elements.";
    
    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see BrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
    	 switch (model.getState()) {
             case Browser.LOADING_DATA:
             case Browser.LOADING_LEAVES:
             case Browser.COUNTING_ITEMS:  
                 setEnabled(false);
                 break;
             default:
                 onDisplayChange(model.getLastSelectedDisplay());
                 break;
         }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            setEnabled(false);
            return;
        }
        TreeImageDisplay[] selected;
        int count;
        boolean b;
        Object ho = selectedDisplay.getUserObject(); 
        if ((ho instanceof DatasetData) || (ho instanceof ProjectData) ||
        	(ho instanceof FileAnnotationData) ||
        	(ho instanceof TagAnnotationData) || 
        	(ho instanceof ScreenData) ||
        	(ho instanceof PlateData) ||
        	(ho instanceof PlateAcquisitionData)) {
        	selected = model.getSelectedDisplays();
        	count = 0;
        	b = false;
    		for (int i = 0; i < selected.length; i++) {
				b = model.canEdit(selected[i].getUserObject());
				if (b) count++;
			}
    		setEnabled(count == selected.length);
        } else if (ho instanceof ExperimenterData) {
        	if (model.getBrowserType() == Browser.ADMIN_EXPLORER) {
        		/*
        		setEnabled(true);
        		selected = model.getSelectedDisplays();
        		if (selected != null) {
        			TreeImageDisplay d;
        			ExperimenterData exp;
        			b = true;
        			for (int i = 0; i < selected.length; i++) {
        				d = selected[i];
        				exp = (ExperimenterData) d.getUserObject();
        				if (exp.getId() == 
        					TreeViewerAgent.getUserDetails().getId()) {
        					b = false;
        					break;
        				}
        			}
        			setEnabled(b);
        		}
        		*/
        		setEnabled(false);
        	} else setEnabled(false);
        } else if (ho instanceof GroupData) {
        	/*
        	if (model.getBrowserType() == Browser.ADMIN_EXPLORER) {
        		setEnabled(TreeViewerAgent.isAdministrator());
        	} else setEnabled(false);
        	*/
        	setEnabled(false);
        } else if (ho instanceof ImageData) {
        	count = 0;
        	b = false;
        	selected = model.getSelectedDisplays();
    		for (int i = 0; i < selected.length; i++) {
				b = model.canEdit(selected[i].getUserObject());
				if (b) count++;
			}
    		setEnabled(count == selected.length);
        } else setEnabled(false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public BrowserDeleteAction(Browser model)
	{
		super(model);
		setEnabled(true);
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.DELETE));
	}
	
    /**
     * Creates a {@link DeleteCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        DeleteCmd cmd = new DeleteCmd(model);
        cmd.execute();
    }

}
