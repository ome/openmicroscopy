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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
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
	extends DataBrowserAction
{

	/** The name of the action. */
	private static final String NAME = "Send Feedback...";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Send feedback about the " +
			"selected image.";
	
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
    	Browser browser = model.getBrowser();
    	Collection l = browser.getSelectedDisplays();
    	if (l != null && l.size() == 1) {
    		Object object = node.getHierarchyObject();
    		setEnabled(object instanceof ImageData);
    	} else setEnabled(false);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
	public SendFeedbackAction(DataBrowser model)
	{
		super(model);
		putValue(Action.NAME, NAME);
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.SEND_COMMENT));
	}
	
	/**
	 * Posts an event to create a new experiment.
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
    public void actionPerformed(ActionEvent e)
    { 
    	Browser browser = model.getBrowser();
    	Collection l = browser.getSelectedDisplays();
    	if (l != null && l.size() == 1) {
    		Iterator i = l.iterator();
    		ImageDisplay node;
    		Object object;
    		UserNotifier un = DataBrowserAgent.getRegistry().getUserNotifier();
    		ExperimenterData exp = DataBrowserAgent.getUserDetails();
    		ImageData image;
    		String text;
    		while (i.hasNext()) {
				node = (ImageDisplay) i.next();
				object = node.getHierarchyObject();
				if (object instanceof ImageData) {
		    		image = (ImageData) object;
		    		text = "Image Name: "+image.getName();
		    		un.submitMessage(exp.getEmail(), text);
		    	}
			}
    	}
    }
    
}
