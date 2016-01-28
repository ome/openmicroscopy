/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ProjectData;

/**
 *  Adds existing objects to the selected <code>DataObject</code>.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *      <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class AddAction
    extends TreeViewerAction
{

    /** The default name of the action. */
    private static final String NAME = "Add Existing...";

    /** The name of the action to add existing <code>Datasets</code>. */
    private static final String NAME_DATASET = "Add Existing Dataset...";

    /** The name of the action to add existing <code>Images</code>. */
    private static final String NAME_IMAGE = "Add Existing Image...";

    /** The name of the action to add existing <code>Users</code>. */
    private static final String NAME_USER = "Edit group membership...";

    /** Description of the action. */
    private static final String DESCRIPTION = "Add existing elements to the " +
            "selected container.";

    /** Description of the action. */
    private static final String DESCRIPTION_DATASET =
            "Add existing datasets to the selected project.";

    /** Description of the action. */
    private static final String DESCRIPTION_IMAGE =
            "Add existing images to the selected dataset.";

    /** Description of the action. */
    private static final String DESCRIPTION_USER =
            "Add/Remove existing users to/from the selected group.";

    /**
     * Modifies the name of the action and sets it enabled depending on
     * the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
        if (selectedDisplay == null) {
            setEnabled(false);
            putValue(Action.NAME, NAME);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        if (ho instanceof String || ho instanceof ExperimenterData) { // root
            setEnabled(false);
            putValue(Action.NAME, NAME);
        } else if (ho instanceof ProjectData) {
            setEnabled(model.canLink(ho));
            putValue(Action.NAME, NAME_DATASET);
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
        } else if (ho instanceof DatasetData) {
            setEnabled(model.canLink(ho));
            putValue(Action.NAME, NAME_IMAGE);
            putValue(Action.SHORT_DESCRIPTION, 
                    UIUtilities.formatToolTipText(DESCRIPTION_IMAGE));
        } else if (ho instanceof GroupData) {
            setEnabled(false);
            GroupData g = (GroupData) ho;
            if (TreeViewerAgent.isAdministrator() || model.isLeaderOfGroup(g)) {
                boolean multipleNodesSelected = false;
                Browser browser = this.model.getSelectedBrowser();
                TreeImageDisplay[] array = browser.getSelectedDisplays();
                if (array != null && array.length > 1) {
                    multipleNodesSelected = true;
                }
                setEnabled(!multipleNodesSelected);
            }
            putValue(Action.NAME, NAME_USER);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_USER));
        } else {
            setEnabled(false);
            putValue(Action.NAME, NAME);
        }
        name = (String) getValue(Action.NAME);
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public AddAction(TreeViewer model)
    {
        super(model);
        putValue(Action.NAME, NAME);
        name = (String) getValue(Action.NAME);
        putValue(Action.SHORT_DESCRIPTION,
                UIUtilities.formatToolTipText(DESCRIPTION));
        IconManager im = IconManager.getInstance();
        putValue(Action.SMALL_ICON, im.getIcon(IconManager.ADD_EXISTING));
    }

    /**
     * Adds existing items to the currently selected node.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        Browser b = model.getSelectedBrowser();
        if (b == null) return;
        TreeImageDisplay d = b.getLastSelectedDisplay();
        if (d == null) return;
        Object ho = d.getUserObject();
        if (ho instanceof ProjectData || ho instanceof DatasetData ||
                ho instanceof GroupData)
            model.addExistingObjects((DataObject) ho);
    }

}
