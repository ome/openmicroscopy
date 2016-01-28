/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ViewInPluginCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.ImageData;

/** 
 * Opens the image using the specified viewer.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ViewInPlugin 
	extends TreeViewerAction
{

	/** Name of the action. */
    private static final String NAME_IJ = "View in ImageJ...";

    /** Description of the action. */
    private static final String DESCRIPTION_IJ = "View the selected image " +
    		"in ImageJ.";
    
    /** Name of the action. */
    private static final String NAME_KNIME = "View in KNIME...";

    /** Description of the action. */
    private static final String DESCRIPTION_KNIME = "View the selected " +
    		"image(s) in KNIME.";
    
    /** Indicate the plugin to open.*/
    private int plugin;
    
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
     * @param plugin The selected plug-in.
     */
    public ViewInPlugin(TreeViewer model, int plugin)
    {
        super(model);
        this.plugin = plugin;
        name = NAME;
        IconManager icons = IconManager.getInstance();
        switch (plugin) {
			case LookupNames.IMAGE_J:
			case LookupNames.IMAGE_J_IMPORT:
				name = NAME_IJ;
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_IJ));
		        putValue(Action.SMALL_ICON, 
		        		icons.getIcon(IconManager.VIEWER_IJ));
				break;
			case LookupNames.KNIME:
				name = NAME_KNIME;
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_KNIME));
		        putValue(Action.SMALL_ICON, 
		        		icons.getIcon(IconManager.VIEWER_KNIME));
		}
    }
    
    /**
     * Creates a  {@link ViewCmd} command to execute the action. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	ViewInPluginCmd cmd = new ViewInPluginCmd(model, plugin);
        cmd.execute();
    }
    
}
