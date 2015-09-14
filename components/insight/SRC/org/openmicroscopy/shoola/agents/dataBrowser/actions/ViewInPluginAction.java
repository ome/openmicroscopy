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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;

import java.awt.event.ActionEvent;

import javax.swing.Action;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.events.ViewInPluginEvent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.ImageData;

/** 
 * Opens the image using the specified viewer.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class ViewInPluginAction
	extends DataBrowserAction
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
     * Sets the action enabled depending on the currently selected display
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
    	if (node == null) {
            setEnabled(false);
            return;
        }
    	setEnabled(node.getHierarchyObject() instanceof ImageData);
    }
    
	/**
	 * Creates a new instance.
	 * 
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	public ViewInPluginAction(DataBrowser model, int plugin)
	{
		super(model);
		this.plugin = plugin;
		
		IconManager icons = IconManager.getInstance();
		switch (plugin) {
			case LookupNames.IMAGE_J:
				putValue(Action.NAME, NAME_IJ);
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_IJ));
		        putValue(Action.SMALL_ICON, 
		        		icons.getIcon(IconManager.VIEWER_IJ));
				break;
			case LookupNames.KNIME:
				putValue(Action.NAME, NAME_KNIME);
				putValue(Action.SHORT_DESCRIPTION, 
		                UIUtilities.formatToolTipText(DESCRIPTION_KNIME));
		        putValue(Action.SMALL_ICON, 
		        		icons.getIcon(IconManager.VIEWER_KNIME));
		}
	}
	
	/**
     * Views the selected images.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser browser = model.getBrowser();
    	if (browser == null) return;
    	ImageDisplay node = browser.getLastSelectedDisplay();
    	if (node == null) return;
    	Object object = node.getHierarchyObject();
    	if (DataBrowserAgent.runAsPlugin() >= 0) {
    		if (object instanceof ImageData) {
				ViewInPluginEvent event = new ViewInPluginEvent(
					model.getSecurityContext(),
					(DataObject) object, plugin);
				event.setDataObjects(browser.getSelectedDataObjects());
				DataBrowserAgent.getRegistry().getEventBus().post(event);
			}
    	}
    }

}
