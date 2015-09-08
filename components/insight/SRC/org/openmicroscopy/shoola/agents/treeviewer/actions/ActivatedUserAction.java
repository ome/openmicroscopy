/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
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
import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;

/** 
 * Indicates to activate or not a user.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ActivatedUserAction 
	extends TreeViewerAction
{

	/** The name of the action. */
    public static final String NAME = "Activated User";
    
    /** The description of the action. */
    private static final String DESCRIPTION = "If selected the user is " +
    		"active.";
    
    /** Icon to indicate that the user is activated. */
    private static final Icon ACTIVATED_ICON;
    
    /** Icon to indicate that the user is not activated. */
    private static final Icon NOT_ACTIVATED_ICON;
    
    static {
    	IconManager icons = IconManager.getInstance();
    	ACTIVATED_ICON = icons.getIcon(IconManager.OWNER);
    	NOT_ACTIVATED_ICON = icons.getIcon(IconManager.OWNER_NOT_ACTIVE);
    }
    
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
            	if (browser.getBrowserType() != Browser.ADMIN_EXPLORER)
            		setEnabled(false);
            	else
            		onDisplayChange(browser.getLastSelectedDisplay());
        }
    }

    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (!TreeViewerAgent.isAdministrator()) {
            setEnabled(false);
            return;
        }
        Browser browser = model.getSelectedBrowser();
        if (browser == null || selectedDisplay == null ||
            browser.getBrowserType() != Browser.ADMIN_EXPLORER) {
            setEnabled(false);
            return;
        }
        TreeImageDisplay[] nodes = browser.getSelectedDisplays();
        if (nodes.length > 1) setEnabled(false);
        else {
            if (selectedDisplay.getUserObject() instanceof ExperimenterData) {
                ExperimenterData exp = (ExperimenterData)
                        selectedDisplay.getUserObject();
                ExperimenterData user = model.getUserDetails();
                setEnabled(exp.getId() != user.getId() &&
                       !model.isSystemUser(exp.getId()));
                if (exp.isActive())
                    putValue(Action.SMALL_ICON, ACTIVATED_ICON);
                else putValue(Action.SMALL_ICON, NOT_ACTIVATED_ICON);
            } else setEnabled(false);
        }
    }

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
    public ActivatedUserAction(TreeViewer model)
    {
        super(model);
        name = NAME;
		putValue(Action.SHORT_DESCRIPTION, 
				UIUtilities.formatToolTipText(DESCRIPTION));
		IconManager im = IconManager.getInstance();
		putValue(Action.SMALL_ICON, im.getIcon(IconManager.TRANSPARENT));
    } 
    
    /**
     * Displays a modal dialog.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {}
 
}
