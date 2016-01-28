/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.ImageData;

/** 
 * Send feedback about the selected image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class SendFeedbackAction 
	extends TreeViewerAction
{

	/** The name of the action. */
	private static final String NAME = "Send Feedback...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Send feedback about the " +
			"selected image.";
	
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
			setEnabled(false);
			return;
		}
		Browser browser = model.getSelectedBrowser();
        if (browser == null) {
        	setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject(); 
        TreeImageDisplay[] selected = browser.getSelectedDisplays();
        if (selected != null && selected.length > 1) {
        	setEnabled(false);
        	return;
        }
        setEnabled(ho instanceof ImageData);
	}
	
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public SendFeedbackAction(TreeViewer model)
	{
		super(model);
		name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.SEND_COMMENT));
	}
	
	/**
     * Sends comments about the selected image.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Browser browser = model.getSelectedBrowser();
    	if (browser == null) return;
    	TreeImageDisplay display = browser.getLastSelectedDisplay();
    	if (display == null) return;
    	Object object = display.getUserObject();
    	if (object instanceof ImageData) {
    		ImageData image = (ImageData) object;
    		String text = "Image Name: "+image.getName();
    		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
    		ExperimenterData exp = TreeViewerAgent.getUserDetails();
    		un.submitMessage(exp.getEmail(), text);
    	}
    }
	
}
