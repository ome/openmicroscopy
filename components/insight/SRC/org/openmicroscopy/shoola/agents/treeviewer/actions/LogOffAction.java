/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.events.LogOff;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.GroupData;
/** 
 * Logs off from the current server.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class LogOffAction
	extends TreeViewerAction
{

	/** The name of the action. */
    public static final String NAME = "Switch User...";

    /** The description of the action. */
    public static final String DESCRIPTION = "Reconnect as another user.";

    /**
     * Enables the action if the browser is not ready.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
    	if (model.isImporting()) {
    		setEnabled(false);
    		return;
    	}
    	if (browser != null)
    		setEnabled(browser.getState() == Browser.READY);
    }

    /**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
    public LogOffAction(TreeViewer model)
    {
        super(model);
        name = NAME;
        putValue(Action.NAME, NAME);
		putValue(Action.SHORT_DESCRIPTION,
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.LOGIN));
    }

    /**
     * Logs off from the current server.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	Registry reg = TreeViewerAgent.getRegistry();
    	GroupData group = model.getSingleGroupDisplayed();
    	LogOff evt = new LogOff();
    	if (group != null)
    	    evt.setSecurityContext(new SecurityContext(group.getId()));
    	reg.getEventBus().post(evt);
    	reg.getUserNotifier().clearActivities();
    }

}
