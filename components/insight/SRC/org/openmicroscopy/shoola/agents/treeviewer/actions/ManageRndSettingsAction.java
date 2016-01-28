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
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteRndSettingsCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageTimeSet;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.WellSampleData;

/** 
 * Copies, pastes or resets the rendering settings depending on the
 * specified index.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ManageRndSettingsAction
    extends TreeViewerAction
{

    /** Identified the copy action. */
    public static final int COPY = 0;

    /** Identified the paste action. */
    public static final int PASTE = 1;

    /** Identified the reset action. */
    public static final int RESET = 2;

    /** Identified the min/max action. */
    public static final int SET_MIN_MAX = 3;

    /** Identified the set owner settings action. */
    public static final int SET_OWNER_SETTING = 4;

    /** The name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy";

    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY =
            "Copy the rendering settings.";

    /** The name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste and Save";

    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE =
            "Paste and Save the rendering settings.";

    /** The name of the action if the index is {@link #RESET}. */
    private static final String NAME_RESET = "Set Imported and Save";

    /** The description of the action if the index is {@link #RESET}. */
    private static final String DESCRIPTION_RESET =
            "Reset and Save the rendering settings created while importing.";

    /** The name of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String NAME_SET_MIN_MAX = "Set Min/Max";

    /** The description of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String DESCRIPTION_SET_MIN_MAX =
            "Set the Pixels Intensity interval to min/max for all channels.";

    /** The name of the action if the index is {@link #SET_OWNER_SETTING}. */
    private static final String NAME_SET_OWNER_SETTING = "Set Owner's and Save";

    /** 
     * The description of the action if the index is {@link #SET_OWNER_SETTING}.
     */
    private static final String DESCRIPTION_SET_OWNER_SETTING =
            "Reset to the Owner's rendering settings and save.";


    /** One of the constants defined by this class. */
    private int index;

    /** Helper reference to the icons manager. */
    private IconManager icons;

    /** 
     * Handles the time nodes.
     *
     * @param nodes The node to handle.
     */
    private void handleTreeTimeNode(TreeImageDisplay[] nodes)
    {
        int count = 0;
        TreeImageDisplay node;
        for (int i = 0; i < nodes.length; i++) {
            node = nodes[i];
            if (node.getNumberOfItems() > 0 && model.canAnnotate(node)) {
                count++;
            }
        }
        setEnabled(count == nodes.length);
    }

    /**
     * Checks if the passed index is supported.
     *
     * @param value The value to control.
     */
    private void checkIndex(int value)
    {
        switch (value) {
        case COPY:
            name = NAME_COPY;
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_COPY));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
            break;
        case PASTE:
            name = NAME_PASTE;
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
            break;
        case RESET:
            name = NAME_RESET;
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_RESET));
            putValue(Action.SMALL_ICON, 
                    icons.getIcon(IconManager.RND_UNDO));
            break;
        case SET_MIN_MAX:
            name = NAME_SET_MIN_MAX;
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_SET_MIN_MAX));
            putValue(Action.SMALL_ICON,
                    icons.getIcon(IconManager.RND_MIN_MAX));
            break;
        case SET_OWNER_SETTING:
            name = NAME_SET_OWNER_SETTING;
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(
                            DESCRIPTION_SET_OWNER_SETTING));
            putValue(Action.SMALL_ICON, 
                    icons.getIcon(IconManager.RND_OWNER));
            break;
        default:
            throw new IllegalArgumentException("Index not supported.");
        }
    }

    /**
     * Call-back to notify of a change of state in the currently selected 
     * browser.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser != null) 
            onDisplayChange(browser.getLastSelectedDisplay());
    }

    /**
     * Call-back to notify of a change in the currently selected display
     * in the currently selected
     * {@link org.openmicroscopy.shoola.agents.treeviewer.browser.Browser}.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        //Copy
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Object ho = selectedDisplay.getUserObject();
        Browser browser = model.getSelectedBrowser();
        if (ho == null || browser == null) {
            setEnabled(false);
            return;
        }
        TreeImageDisplay[] selected = browser.getSelectedDisplays();
        int count = 0;
        switch (index) {
        case COPY:
            if (selected.length > 1) setEnabled(false);
            else {
                if (ho instanceof ImageData || ho instanceof WellSampleData)
                    setEnabled(model.canAnnotate(ho));
                else setEnabled(false);
            }
            break;
        case PASTE:
            if (!model.hasRndSettings()) {
                setEnabled(false);
                return;
            }
            if (selectedDisplay instanceof TreeImageTimeSet) {
                handleTreeTimeNode(selected);
                return;
            }
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData || ho instanceof ScreenData ||
                    ho instanceof ProjectData ||
                    ho instanceof PlateAcquisitionData))
                setEnabled(false);
            else {
                DataObject data;
                for (int i = 0; i < selected.length; i++) {
                    data = (DataObject) selected[i].getUserObject();
                    if (model.canAnnotate(ho) &&
                            model.areSettingsCompatible(data.getGroupId()))
                        count++;
                }
                setEnabled(count == selected.length);
            }
            break;
        case RESET:
        case SET_MIN_MAX:
            if (selectedDisplay instanceof TreeImageTimeSet) {
                handleTreeTimeNode(selected);
                return;
            }
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData ||
                    ho instanceof PlateAcquisitionData))
                setEnabled(false);
            else {
                for (int i = 0; i < selected.length; i++) {
                    if (model.canAnnotate(selected[i].getUserObject()))
                        count++;
                }
                setEnabled(count == selected.length);
            }
            break;
        case SET_OWNER_SETTING:
            if (selectedDisplay instanceof TreeImageTimeSet) {
                handleTreeTimeNode(selected);
                return;
            }
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData ||
                    ho instanceof PlateAcquisitionData))
                setEnabled(false);
            else {
                Object object;
                for (int i = 0; i < selected.length; i++) {
                    object = selected[i].getUserObject();
                    if (model.canAnnotate(object))
                        count++;
                }
                setEnabled(count == selected.length);
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public ManageRndSettingsAction(TreeViewer model, int index)
    {
        super(model);
        setEnabled(false);
        name = NAME;
        icons = IconManager.getInstance();
        checkIndex(index);
        this.index = index;
    }

    /** 
     * Copies, pastes or resets the rendering settings for the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        PasteRndSettingsCmd cmd;
        switch (index) {
        case COPY:
            model.copyRndSettings(null);
            break;
        case RESET:
            cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.RESET);
            cmd.execute();
            break;
        case SET_MIN_MAX:
            cmd = new PasteRndSettingsCmd(model,
                    PasteRndSettingsCmd.SET_MIN_MAX);
            cmd.execute();
            break;
        case PASTE:
            cmd = new PasteRndSettingsCmd(model, PasteRndSettingsCmd.PASTE);
            cmd.execute();
            break;
        case SET_OWNER_SETTING:
            cmd = new PasteRndSettingsCmd(model,
                    PasteRndSettingsCmd.SET_OWNER);
            cmd.execute();
        }
    }

}
