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
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Manages the object i.e. either copy, paste, cut or remove.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
public class ManageObjectAction
	extends DataBrowserAction
	implements PropertyChangeListener
{

    /** Identified the copy action. */
    public static final int COPY = 0;

    /** Identified the paste action. */
    public static final int PASTE = 1;

    /** Identified the remove action. */
    public static final int REMOVE = 2;

    /** Identified the cut action. */
    public static final int CUT = 3;

    /** The default name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY_LINK = "Copy Link";

    /** The description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY_LINK = "Copy link(s) to the selected element(s) into the clipboard.";

    /** Alternative name of the action if the index is {@link #COPY}. */
    private static final String NAME_COPY = "Copy";

    /** Alternative description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = "Copy the selected element(s) into the clipboard.";
    
    /** The default name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE_LINK = "Paste Link";

    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE_LINK = "Paste link(s) from the clipboard.";
    
    /** Alternative name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE = "Paste";

    /** Alternative description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE = "Paste element(s) from the clipboard.";

    /** The default name of the action if the index is {@link #REMOVE}. */
    private static final String NAME_REMOVE = "Delete";

    /** The description of the action if the index is {@link #REMOVE}. */
    private static final String DESCRIPTION_REMOVE = "Delete the selected elements.";

    /** The default name of the action if the index is {@link #CUT}. */
    private static final String NAME_CUT_LINK = "Cut Link";

    /** The description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT_LINK = "Cut the selected link(s).";
    
    /** Alternative name of the action if the index is {@link #CUT}. */
    private static final String NAME_CUT = "Cut";

    /** Alternative description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT = "Cut the selected element(s).";

    /** The system group to check.*/
    private static final String[] KEYS = {GroupData.SYSTEM, GroupData.GUEST};

    /** One of the constants defined by this class. */
    private int index;

    /** Helper reference to the icons manager. */
    private IconManager icons;

    /**
     * Checks if the passed index is supported.
     * 
     * @param value The value to control.
     */
    private void checkIndex(int value)
    {
        switch (value) {
        case COPY:
            putValue(Action.NAME, NAME_COPY_LINK);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_COPY_LINK));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
            break;
        case PASTE:
            putValue(Action.NAME, NAME_PASTE_LINK);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_PASTE_LINK));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
            break;
        case REMOVE:
            putValue(Action.NAME, NAME_REMOVE);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_REMOVE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.REMOVE));
            break;
        case CUT:
            putValue(Action.NAME, NAME_CUT_LINK);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_CUT_LINK));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.CUT));
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
     * Adapt the name and description of this action with respect to the
     * selected browser
     * 
     * @param browserType
     *            The type of the {@link Browser}
     */
    private void adaptActionNameDescription(int browserType) {
        if (browserType == DataBrowser.GROUP) {
            switch (index) {
                case CUT:
                    putValue(Action.NAME, NAME_CUT);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT));
                    break;
                case COPY:
                    putValue(Action.NAME, NAME_COPY);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_COPY));
                    break;
                case PASTE:
                    putValue(Action.NAME, NAME_PASTE);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
            }
        } else {
            switch (index) {
                case CUT:
                    putValue(Action.NAME, NAME_CUT_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT_LINK));
                    break;
                case COPY:
                    putValue(Action.NAME, NAME_COPY_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_COPY_LINK));
                    break;
                case PASTE:
                    putValue(Action.NAME, NAME_PASTE_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_PASTE_LINK));
            }
        }
    }
    
    /**
     * Sets the action enabled depending on the currently selected display.
     * @see DataBrowserAction#onDisplayChange(ImageDisplay)
     */
    protected void onDisplayChange(ImageDisplay node)
    {
        if (node == null) {
            setEnabled(false);
            return;
        }
        Browser browser = model.getBrowser();
        if (browser == null) {
            setEnabled(false);
            return;
        }
        
        adaptActionNameDescription(model.getType());
        
        Object ho = node.getHierarchyObject();
        Class<?> klass = model.hasDataToCopy();
        Collection<DataObject> selected = browser.getSelectedDataObjects();
        Iterator<DataObject> i;
        int count = 0;
        switch (index) {
        case COPY:
            if (ho instanceof DatasetData || ho instanceof ImageData ||
                    ho instanceof PlateData) {
                i = selected.iterator();
                while (i.hasNext()) {
                    if (model.canLink(i.next())) count++;
                }
                setEnabled(count == selected.size());
            } else if (ho instanceof ExperimenterData) {
                setEnabled(model.getType() == DataBrowser.GROUP);
            } else setEnabled(false);
            break;
        case PASTE:
            if (klass == null) {
                setEnabled(false);
                return;
            }
            if (ho instanceof ProjectData) {
                if (DatasetData.class.equals(klass)) {
                    i = selected.iterator();
                    while (i.hasNext()) {
                        if (model.canLink(i.next())) count++;
                    }
                    setEnabled(count == selected.size());
                } else setEnabled(false);
            } else if (ho instanceof ScreenData) {
                if (PlateData.class.equals(klass)) {
                    i = selected.iterator();
                    while (i.hasNext()) {
                        if (model.canLink(i.next())) count++;
                    }
                    setEnabled(count == selected.size());
                } else setEnabled(false);
            } else if (ho instanceof DatasetData) {
                if (ImageData.class.equals(klass)) {
                    i = selected.iterator();
                    while (i.hasNext()) {
                        if (model.canLink(i.next())) count++;
                    }
                    setEnabled(count == selected.size());
                } else setEnabled(false);
            } else setEnabled(false);
            break;
        case REMOVE:
            if (ho instanceof ExperimenterData) {
                setEnabled(false);
            } else if (ho instanceof ProjectData
                    || ho instanceof DatasetData
                    || ho instanceof ImageData || ho instanceof ScreenData) {
                i = selected.iterator();
                while (i.hasNext()) {
                    if (model.canDelete(i.next())) count++;
                }
                setEnabled(count == selected.size());
            } else setEnabled(false);
            break;
        case CUT:
            Object parent = model.getParentOfNodes();
            if ((ho instanceof DatasetData && parent instanceof ProjectData)
                    || (ho instanceof ImageData && 
                            (parent instanceof DatasetData ||
                                    parent instanceof TagAnnotationData))
                                    || (ho instanceof PlateData && 
                                            parent instanceof ScreenData)) {
                i = selected.iterator();
                while (i.hasNext()) {
                    if (model.canLink(i.next())) count++;
                }
                setEnabled(count == selected.size());
            } else if (ho instanceof ExperimenterData) {
                setEnabled(false);
                if (model.getType() == DataBrowser.GROUP) {
                    if (parent instanceof GroupData) {
                        GroupData g = (GroupData) parent;
                        Boolean b = null;
                        for (int j = 0; j < KEYS.length; j++) {
                            if (model.isSystemGroup(g.getId(), KEYS[j])) {
                                ExperimenterData user = model.getCurrentUser();
                                ExperimenterData exp = (ExperimenterData) ho;
                                b = exp.getId() != user.getId() &&
                                        !model.isSystemUser(exp.getId(),
                                                KEYS[j]);
                                break;
                            }
                        }
                        if (b != null) setEnabled(b);
                        else setEnabled(true);
                    }
                }
            } else {
                setEnabled(false);
            }
        }
    }

    /** Handles the property change.*/
    private void handlePropertyChange()
    {
        Browser browser = model.getBrowser();
        if (browser != null) {
            onDisplayChange(browser.getLastSelectedDisplay());
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index	One of the management constants defined by this class.
     */
    public ManageObjectAction(DataBrowser model, int index)
    {
        super(model);
        setEnabled(false);
        icons = IconManager.getInstance();
        checkIndex(index);
        this.index = index;
        model.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                String name = evt.getPropertyName();
                if (DataBrowser.COPY_RND_SETTINGS_PROPERTY.equals(name) ||
                        DataBrowser.ITEMS_TO_COPY_PROPERTY.equals(name) || 
                        DataBrowser.SELECTION_UPDATED_PROPERTY.equals(name)) {
                    handlePropertyChange();
                }
            }
        });
    }

    /**
     * Copies, pastes, cuts or removes the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        switch (index) {
        case COPY:
            model.copy();
            break;
        case PASTE:
            model.paste();
            break;
        case REMOVE:
            model.remove();
            break;
        case CUT:
            model.cut();
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
        if (DataBrowser.COPY_RND_SETTINGS_PROPERTY.equals(name) ||
                DataBrowser.ITEMS_TO_COPY_PROPERTY.equals(name) || 
                DataBrowser.SELECTION_UPDATED_PROPERTY.equals(name) ||
                Browser.SELECTED_DATA_BROWSER_NODE_DISPLAY_PROPERTY.equals(name) ||
                Browser.SELECTED_DATA_BROWSER_NODES_DISPLAY_PROPERTY.equals(name)) {
            Browser browser = model.getBrowser();
            if (browser != null) {
                onDisplayChange(browser.getLastSelectedDisplay());
            }
        }
    }

}
