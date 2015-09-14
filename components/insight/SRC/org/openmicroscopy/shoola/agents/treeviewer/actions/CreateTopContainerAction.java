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
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CreateCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.TagAnnotationData;

/**
 * Creates a top container either a project or a categoryGroup.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME2.2
 */
public class CreateTopContainerAction
    extends TreeViewerAction
{

    /** Indicates to create a <code>Project</code>. */
    public static final int PROJECT = CreateCmd.PROJECT;

    /** Indicates to create a <code>Dataset</code>. */
    public static final int DATASET = CreateCmd.DATASET;

    /** Indicates to create a <code>Tag</code>. */
    public static final int TAG = CreateCmd.TAG;

    /** Indicates to create a <code>Screen</code>. */
    public static final int SCREEN = CreateCmd.SCREEN;

    /** Indicates to create a <code>Tag SEt</code>. */
    public static final int TAG_SET = CreateCmd.TAG_SET;

    /** Indicates to create a <code>Group</code>. */
    public static final int GROUP = CreateCmd.GROUP;

    /** Indicates to create a <code>Experimenter</code>. */
    public static final int EXPERIMENTER = CreateCmd.EXPERIMENTER;

    /** The name of the action for the creation of a <code>Project</code>. */
    private static final String NAME = "New...";

    /** The name of the action for the creation of a <code>Project</code>. */
    private static final String NAME_PROJECT = "New Project...";

    /** The name of the action for the creation of a <code>Dataset</code>. */
    private static final String NAME_DATASET = "New Dataset...";

    /** The name of the action for the creation of a <code>Tag</code>. */
    private static final String NAME_TAG = "New Tag...";

    /** The name of the action for the creation of a <code>Tag Set</code>. */
    private static final String NAME_TAG_SET = "New Tag Set...";

    /** The name of the action for the creation of a <code>Screen</code>. */
    private static final String NAME_SCREEN = "New Screen...";

    /** The name of the action for the creation of a <code>Group</code>. */
    private static final String NAME_GROUP = "New Group...";

    /** The name of the action for the creation of a <code>Screen</code>. */
    private static final String NAME_EXPERIMENTER = "New User...";

    /** Description of the action for a <code>Tag</code> . */
    private static final String DESCRIPTION_TAG_SET = "Create a new Tag Set.";

    /** Description of the action for a <code>Tag</code> . */
    private static final String DESCRIPTION_TAG = "Create a new Tag.";

    /** Description of the action for a <code>Dataset</code> . */
    private static final String DESCRIPTION_DATASET = "Create a new Dataset.";

    /** Description of the action for a <code>Project</code> . */
    private static final String DESCRIPTION_PROJECT = "Create a new Project.";

    /** Description of the action for the creation of a <code>Screen</code>. */
    private static final String DESCRIPTION_SCREEN = "Create a new Screen.";

    /** Description of the action for the creation of a <code>Group</code>. */
    private static final String DESCRIPTION_GROUP = "Create a new Group.";

    /**
     * Description of the action for the creation of a <code>Experimenter</code>.
     */
    private static final String DESCRIPTION_EXPERIMENTER =
            "Create a new user.";

    /** The type of node to create. */
    private int nodeType;

    /** 
     * Indicates that the action was used in the top menu
     * if <code>true</code>.
     */
    private boolean fromTopMenu;

    /**
     * Checks if the passed value is supported.
     *
     * @param value The value to handle.
     */
    private void checkType(int value)
    {
        IconManager icons = IconManager.getInstance();
        switch (value) {
        case PROJECT:
            name = NAME_PROJECT;
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PROJECT));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_PROJECT));
            break;
        case DATASET:
            name = NAME_DATASET;
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DATASET));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_DATASET));
            break;
        case TAG:
            name = NAME_TAG;
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.TAG));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_TAG));
            break;
        case TAG_SET:
            name = NAME_TAG_SET;
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.TAG_SET));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_TAG_SET));
            break;
        case SCREEN:
            name = NAME_SCREEN;
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.SCREEN));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_SCREEN));
            break;
        case GROUP:
            name = NAME_GROUP;
            putValue(Action.SMALL_ICON, icons.getIcon(
                    IconManager.OWNER_GROUP));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_GROUP));
            break;
        case EXPERIMENTER:
            name = NAME_EXPERIMENTER;
            putValue(Action.SMALL_ICON,
                    icons.getIcon(IconManager.OWNER));
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(
                            DESCRIPTION_EXPERIMENTER));
            break;
        default:
            throw new IllegalArgumentException("Type not supported.");
        }
    }

    /** 
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) {
            setEnabled(false);
            return;
        }
        switch (browser.getState()) {
        case Browser.LOADING_DATA:
        case Browser.LOADING_LEAVES:
        case Browser.COUNTING_ITEMS:  
            setEnabled(false);
            break;
        default:
            if (browser.getBrowserType() != Browser.ADMIN_EXPLORER)
                setEnabled(true);
            else onDisplayChange(browser.getLastSelectedDisplay());
        }
    }

    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        setEnabled(false);
        if (nodeType == GROUP) {
            setEnabled(TreeViewerAgent.isAdministrator());
            return;
        }
        if (nodeType != EXPERIMENTER) {
            if (selectedDisplay != null) {
                Object ho = selectedDisplay.getUserObject();
                if (ho instanceof ExperimenterData) {
                    long id = TreeViewerAgent.getUserDetails().getId();
                    ExperimenterData exp = (ExperimenterData) ho;
                    setEnabled(exp.getId() == id);
                    return;
                }
                if (ho instanceof GroupData) {
                    setEnabled(model.getDisplayMode() ==
                            TreeViewer.GROUP_DISPLAY);
                    return;
                }
                setEnabled(model.canLink(ho));
            }
        } else {
            if (TreeViewerAgent.isAdministrator()) {
                Browser browser = model.getSelectedBrowser();
                if (browser == null || selectedDisplay == null) {
                    setEnabled(false);
                    return;
                } 
                Object ho = selectedDisplay.getUserObject();
                if (ho instanceof GroupData) {
                    TreeImageDisplay[] selected = browser.getSelectedDisplays();
                    setEnabled(selected.length == 1);
                } else setEnabled(false);
            }
        }
    }

    /**
     * Modifies the name of the action when a new browser is selected.
     * @see TreeViewerAction#onBrowserSelection(Browser)
     */
    protected void onBrowserSelection(Browser browser)
    {
        if (browser == null) setEnabled(false);
    }

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param nodeType The Type of node to create.
     *                 One of the constants defined by this class.
     */
    public CreateTopContainerAction(TreeViewer model, int nodeType)
    {
        super(model);
        fromTopMenu = false;
        checkType(nodeType);
        this.nodeType = nodeType;
        onBrowserSelection(model.getSelectedBrowser());
    }

    /**
     * Sets the flag indicating that the action was used in a top menu.
     *
     * @param fromTopMenu The value to set.
     */
    public void setFromTopMenu(boolean fromTopMenu)
    { 
        this.fromTopMenu = fromTopMenu;
    }

    /**
     * Creates a {@link CreateCmd} command to execute the action.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (nodeType == -1) return;
        boolean withParent = false;

        Browser browser = model.getSelectedBrowser();
        if (browser != null) {
            int n = browser.getSelectedDisplays().length;
            if (n == 1) {
                TreeImageDisplay node = browser.getLastSelectedDisplay();
                Object uo = node.getUserObject();
                switch (nodeType) {
                case DATASET:
                    if (uo instanceof ProjectData)
                        withParent = model.canLink(uo);
                    break;
                case EXPERIMENTER:
                    if (uo instanceof ExperimenterData)
                        withParent = false;
                    break;
                case TAG:
                    if (uo instanceof TagAnnotationData) {
                        TagAnnotationData tag = (TagAnnotationData) uo;
                        String ns = tag.getNameSpace();
                        if (ns != null &&
                                TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                                        ns));
                        withParent = model.canLink(tag);
                    }
                }
            }
        }
        if (!fromTopMenu) withParent = false;
        CreateCmd cmd = new CreateCmd(model, nodeType);
        cmd.setWithParent(withParent);
        cmd.execute();
    }

}
