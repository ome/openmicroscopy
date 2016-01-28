/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee. All rights reserved.
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
import javax.swing.Action;
import javax.swing.Icon;

import org.openmicroscopy.shoola.agents.dataBrowser.DataBrowserAgent;
import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;

/** 
 * Indicates to activate or not a user.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0-Beta2
 */
public class ActivatedUserAction
    extends DataBrowserAction
{

    /** The name of the action. */
    public static final String NAME = "Activated User";

    /** The description of the action. */
    private static final String DESCRIPTION = "If selected the user is " +
            "active.";

    /** Icon to indicate that the user is activated. */
    public static final Icon ACTIVATED_ICON;

    /** Icon to indicate that the user is not activated. */
    public static final Icon NOT_ACTIVATED_ICON;

    static {
        IconManager icons = IconManager.getInstance();
        ACTIVATED_ICON = icons.getIcon(IconManager.OWNER);
        NOT_ACTIVATED_ICON = icons.getIcon(IconManager.OWNER_NOT_ACTIVE);
    }

    /**
     * Call-back to notify a change of state.
     * @see DataBrowserAction#onStateChange()
     */
    protected void onStateChange()
    {
        Browser browser = model.getBrowser();
        if (browser != null)
            onDisplayChange(browser.getLastSelectedDisplay());
    }

    /**
     * Sets the action enabled depending on the selected type.
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
        if (!DataBrowserAgent.isAdministrator()) {
            setEnabled(false);
            return;
        }
        Browser browser = model.getBrowser();
        if (browser == null || node == null ||
                model.getType() != DataBrowser.GROUP) {
            setEnabled(false);
            return;
        }
        Collection<ImageDisplay> nodes = browser.getSelectedDisplays();
        if (nodes.size() > 1) setEnabled(false);
        else {
            if (node.getHierarchyObject() instanceof ExperimenterData) {
                ExperimenterData exp = (ExperimenterData)
                        node.getHierarchyObject();
                ExperimenterData user = DataBrowserAgent.getUserDetails();
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
    public ActivatedUserAction(DataBrowser model)
    {
        super(model);
        putValue(Action.NAME, NAME);
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
