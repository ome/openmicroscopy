/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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

import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.ImageData;

/** 
 * Opens the document with an external application.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ViewOtherAction
	extends TreeViewerAction
{

	/** Name of the action. */
    private static final String NAME = "Other..."; 
    
	/** Description of the action. */
    private static final String DESCRIPTION = "Open the document with the " +
    		"selected application.";
    
    /** Description of the action. */
    private static final String DESCRIPTION_GENERAL = "Select the application" +
    		" to open the document.";
    
    /** The external application or <code>null</code>. */
    private ApplicationData data;
    
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
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof ImageData) {
        	setEnabled(true); //TODO: check if big image
        } else setEnabled(ho instanceof FileAnnotationData);
    }
    
	/**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param data  The component identifying the external application.
     */
    public ViewOtherAction(TreeViewer model, ApplicationData data)
    {
        super(model);
        name = NAME;
        this.data = data;
        if (data == null) {
        	putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_GENERAL));
        } else {
        	name = data.getApplicationName();
            putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION));
        	putValue(Action.SMALL_ICON, data.getApplicationIcon());
        	
        }
        Browser browser = model.getSelectedBrowser();
    	if (browser != null)
    		onDisplayChange(browser.getLastSelectedDisplay());
    }
    
    /**
     * Opens the document with the specified external application. 
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) { model.openWith(data); }
    
}
