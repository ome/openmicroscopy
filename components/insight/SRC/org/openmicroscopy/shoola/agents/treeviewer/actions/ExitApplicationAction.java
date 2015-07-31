/*
 * org.openmicroscopy.shoola.agents.treeviewer.actions.ExitpplicationAction
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
import javax.swing.AbstractAction;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.GroupData;

/** 
 * Exit the application.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ExitApplicationAction
    extends TreeViewerAction
{

    /** The name of the action. */
    public static final String NAME = "Quit the application...";
    
    /** The description of the action. */
    public static final String DESCRIPTION = "Exit the application.";
    
    /** The name of the action. */
    public static final String NAME_AS_PLUGIN = "Quit the plugin...";
    
    /** The description of the action. */
    public static final String DESCRIPTION_AS_PLUGIN = "Exit the plugin.";
    
    /** 
     * Sets the action enabled to <code>true</code>.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onDisplayMode() { setEnabled(true); }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public ExitApplicationAction(TreeViewer model)
    {
        super(model);
        setEnabled(true);
        if (!TreeViewerAgent.isRunAsPlugin()) {
            putValue(Action.NAME, NAME);
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION));
        } else {
            putValue(Action.NAME, NAME_AS_PLUGIN);
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_AS_PLUGIN));
        }
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.EXIT_APPLICATION));
    }
    
    /** 
     * Closes the application.
     * @see AbstractAction#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
    	model.cancel();
    	EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
    	ExitApplication a = new ExitApplication(false);
    	GroupData group = model.getSelectedGroup();
    	if (group != null)
    		a.setSecurityContext(new SecurityContext(group.getId()));
        bus.post(a);
    }

}
