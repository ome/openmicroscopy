/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Browser;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.dataBrowser.view.DataBrowser;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
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
 * @since OME3.0
 */
public class ManageRndSettingsAction
    extends DataBrowserAction
    implements PropertyChangeListener
{

    /** Identified the copy action. */
    public static final int COPY = 0;

    /** Identified the paste action. */
    public static final int PASTE = 1;

    /** Identified the reset action. */
    public static final int RESET = 2;

    /** Identified the <code>Set Min max</code> action. */
    public static final int SET_MIN_MAX = 3;

    /** Identified the <code>Set Owner</code> action. */
    public static final int SET_OWNER = 4;

    /** The default name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy";

    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY =
            "Copy the rendering settings.";

    /** The default name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste and Save";

    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE =
            "Paste and Save the rendering settings.";

    /** The default name of the action if the index is {@link #RESET}. */
    private static final String NAME_RESET = "Set Imported and Save";

    /** The description of the action if the index is {@link #RESET}. */
    private static final String DESCRIPTION_RESET =
            "Reset and Save the rendering settings created while importing.";

    /** The default name of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String NAME_SET_MIN_MAX = "Set Min/Max";

    /** The description of the action if the index is {@link #SET_MIN_MAX}. */
    private static final String DESCRIPTION_SET_MIN_MAX =
            "Set the Pixels Intensity interval to min/max for all channels.";

    /** The name of the action if the index is {@link #SET_OWNER}. */
    private static final String NAME_SET_OWNER = "Set Owner's and Save";

    /** 
     * The description of the action if the index is {@link #SET_OWNER}.
     */
    private static final String DESCRIPTION_SET_OWNER =
            "Reset to the Owner's rendering settings and save.";

    /** Helper reference to the icons manager. */
    private IconManager icons;

    /** One of the constants defined by this class. */
    private int index;

    /**
     * Checks if the passed index is supported.
     *
     * @param value The value to control.
     */
    private void checkIndex(int value)
    {
        switch (value) {
        case COPY:
            putValue(Action.NAME, NAME_COPY);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_COPY));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
            break;
        case PASTE:
            putValue(Action.NAME, NAME_PASTE);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
            break;
        case RESET:
            putValue(Action.NAME, NAME_RESET);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_RESET));
            putValue(Action.SMALL_ICON,
                    icons.getIcon(IconManager.RND_REDO));
            break;
        case SET_MIN_MAX:
            putValue(Action.NAME, NAME_SET_MIN_MAX);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_SET_MIN_MAX));
            putValue(Action.SMALL_ICON,
                    icons.getIcon(IconManager.RND_MIN_MAX));
            break;
        case SET_OWNER:
            putValue(Action.NAME, NAME_SET_OWNER);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(
                            DESCRIPTION_SET_OWNER));
            putValue(Action.SMALL_ICON,
                    icons.getIcon(IconManager.RND_OWNER));
            break;
        default:
            throw new IllegalArgumentException("Index not supported.");
        }
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
     * Sets the action enabled depending on the currently selected display.
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
        Browser browser = model.getBrowser();
        if (node == null || browser == null) {
            setEnabled(false);
            return;
        }
        Object ho = node.getHierarchyObject();
        Collection<DataObject> selected = browser.getSelectedDataObjects();
        Iterator<DataObject> i;
        int count = 0;
        Object obj;
        switch (index) {
        case COPY:
            if (selected.size() > 1) setEnabled(false);
            else {
                if (ho instanceof WellSampleData || ho instanceof ImageData)
                    setEnabled(model.canAnnotate(ho));
                else setEnabled(false);
            }
            break;
        case PASTE:
            if (!model.hasRndSettings()) {
                setEnabled(false);
                return;
            }
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData || ho instanceof PlateAcquisitionData
                    || ho instanceof WellSampleData))
                setEnabled(false);
            else {
                if (ho instanceof PlateData ||
                        ho instanceof ImageData || ho instanceof DatasetData ||
                        ho instanceof PlateAcquisitionData || ho instanceof WellSampleData) {
                    i = selected.iterator();
                    DataObject data;
                    while (i.hasNext()) {
                        obj = i.next();
                        data = (DataObject) obj;
                        if (model.canAnnotate(obj) &&
                                model.areSettingsCompatible(data.getGroupId()))
                            count++;
                    }
                    setEnabled(count == selected.size());
                } else setEnabled(true);
            }
            break;
        case RESET:
        case SET_MIN_MAX:
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData ||
                    ho instanceof PlateAcquisitionData || ho instanceof WellSampleData))
                setEnabled(false);
            else {
                i = selected.iterator();
                while (i.hasNext()) {
                    obj = i.next();
                    if (model.canAnnotate(obj)) count++;
                }
                setEnabled(count == selected.size());
            }
            break;
        case SET_OWNER:
            if (!(ho instanceof ImageData || ho instanceof DatasetData ||
                    ho instanceof PlateData ||
                    ho instanceof PlateAcquisitionData || ho instanceof WellSampleData))
                setEnabled(false);
            else {
                i = selected.iterator();
                while (i.hasNext()) {
                    obj = i.next();
                    if (model.canAnnotate(obj))
                        count++;
                }
                setEnabled(count == selected.size());
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index	One of the management constants defined by this class.
     */
    public ManageRndSettingsAction(DataBrowser model, int index)
    {
        super(model);
        setEnabled(false);
        icons = IconManager.getInstance();
        checkIndex(index);
        this.index = index;
        model.addPropertyChangeListener(this);
    }

    /**
     * Copies, pastes or resets the rendering settings for the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        switch (index) {
        case COPY:
            model.copyRndSettings();
            break;
        case PASTE:
            model.pasteRndSettings();
            break;
        case RESET:
            model.resetRndSettings();
            break;
        case SET_MIN_MAX:
            model.setOriginalSettings();
            break;
        case SET_OWNER:
            model.setOwnerSettings();
        }
    }

    /**
     * Reacts to property changes in the {@link DataBrowser}.
     * Sets the enabled flag.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        String name = evt.getPropertyName();
        if (DataBrowser.COPY_ITEMS_PROPERTY.equals(name) ||
                DataBrowser.RND_SETTINGS_TO_COPY_PROPERTY.equals(name) ||
                Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(
                        name) ||
                        Browser.SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY.equals(
                                name) ||
                                DataBrowser.SELECTION_UPDATED_PROPERTY.equals(name)) {
            Browser browser = model.getBrowser();
            if (browser != null)
                onDisplayChange(browser.getLastSelectedDisplay());
        }
    }

}
