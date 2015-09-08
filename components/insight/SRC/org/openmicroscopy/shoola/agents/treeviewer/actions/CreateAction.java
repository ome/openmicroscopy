/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CreateCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Creates a new <code>DataObject</code> of the corresponding type.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * @since OME2.2
 */
public class CreateAction
    extends TreeViewerAction
{
    
	/** The default name of the action. */
	private static final String NAME = "New...";

	/** The name of the action for the creation of a <code>Dataset</code>. */
	private static final String NAME_DATASET = "New Dataset...";

	/** The name of the action for the creation of a <code>Tag</code>. */
	private static final String NAME_TAG = "New Tag...";

	/** The name of the action for the creation of a <code>Image</code>. */
	private static final String NAME_IMAGE = "New...";

	/** 
	 * Description of the action if the selected node is a <code>Tag Set</code>.
	 */
	private static final String DESCRIPTION_TAG = "Create a new Tag " +
	"and add it to the selected Tag Set.";
    
    /** 
     * Description of the action if the selected node is a <code>Project</code>.
     */
    private static final String DESCRIPTION_DATASET = "Create a new Dataset " +
    		"and add it to the selected Project.";

    /** 
     * Description of the action if the selected node is a <code>Image</code>.
     */
    private static final String DESCRIPTION_IMAGE = 
    	"Import the selected images.";
    
    /** Default Description of the action. */
    private static final String DESCRIPTION = "Create a new element.";
    
    /** The type of node to create. */
    private int nodeType;
    
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
	            setEnabled(false);
	            break;
	        default:
                onDisplayChange(browser.getLastSelectedDisplay());
	            break;
        }
    }
    
    /**
     * Modifies the name of the action and sets it enabled depending on
     * the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
        Browser browser = model.getSelectedBrowser();
        if (selectedDisplay == null || browser == null) {
            setEnabled(false);
            name = NAME;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
            return;
        }
        TreeImageDisplay[] nodes = browser.getSelectedDisplays();
        if (nodes.length > 1) {
        	 setEnabled(false);
             name = NAME;
             putValue(Action.SHORT_DESCRIPTION, 
                     UIUtilities.formatToolTipText(DESCRIPTION));
             return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof String || ho instanceof ExperimenterData) { // root
        	setEnabled(false);
        	name = NAME;
        	putValue(Action.SHORT_DESCRIPTION, 
        			UIUtilities.formatToolTipText(DESCRIPTION));
        } else if (ho instanceof ProjectData) {
            setEnabled(model.canLink(ho));
            name = NAME_DATASET; 
            nodeType = CreateCmd.DATASET;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
        } else if (ho instanceof ScreenData || ho instanceof DatasetData) {
        	setEnabled(false);
            name = NAME;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
        } else if (ho instanceof TagAnnotationData) {
        	String ns = ((TagAnnotationData) ho).getNameSpace();
        	if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(ns)) {
        		setEnabled(model.canAnnotate(ho));
            	nodeType = CreateCmd.TAG;
            	putValue(Action.SMALL_ICON, im.getIcon(IconManager.TAG));
            	name = NAME_TAG;
            	putValue(Action.SHORT_DESCRIPTION, 
            			UIUtilities.formatToolTipText(DESCRIPTION_TAG)); 
        	} else {
        		setEnabled(false);
                name = NAME;
                putValue(Action.SHORT_DESCRIPTION, 
                        UIUtilities.formatToolTipText(DESCRIPTION));
        	} 
        } else if (ho instanceof ImageData) {
        	setEnabled(false);
        	nodeType = CreateCmd.IMAGE;
            putValue(Action.SMALL_ICON, im.getIcon(IconManager.IMPORTER));
            name = NAME_IMAGE;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_IMAGE));
        } else {
            setEnabled(false);
            name = NAME;
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
        }
        description = (String) getValue(Action.SHORT_DESCRIPTION);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public CreateAction(TreeViewer model)
    {
        super(model);
        name = NAME;  
        putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        description = (String) getValue(Action.SHORT_DESCRIPTION);
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.CREATE));
    } 

    /**
     * Creates a {@link CreateCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
       CreateCmd cmd = new CreateCmd(model, nodeType);
       cmd.execute();
    }

}
