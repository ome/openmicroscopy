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
import java.util.Iterator;
import java.util.List;
import javax.swing.Action;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ActionCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CopyCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.CutCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.DeleteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PasteCmd;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

/** 
 * Cuts, copies and pastes objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ManageObjectAction
	extends TreeViewerAction
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
    private static final String DESCRIPTION_COPY_LINK =
            "Copy link(s) to the selected element(s) into the clipboard.";
    
    /** Alternative name of the action if the index is {@link #COPY} */
    private static final String NAME_COPY = "Copy";

    /** Alternative description of the action if the index is {@link #COPY}. */
    private static final String DESCRIPTION_COPY = "Copy the selected element(s) into the clipboard.";

    /** The default name of the action if the index is {@link #PASTE}. */
    private static final String NAME_PASTE_LINK = "Paste Link";
    
    /** The description of the action if the index is {@link #PASTE}. */
    private static final String DESCRIPTION_PASTE_LINK = "Paste link(s) from the clipboard.";
    
    /** Alternative default name of the action if the index is {@link #PASTE}. */
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
    
    /** Alternative default name of the action if the index is {@link #CUT}. */
    private static final String NAME_CUT = "Cut";

    /** Alternative description of the action if the index is {@link #CUT}. */
    private static final String DESCRIPTION_CUT = "Cut the selected element(s).";

    /** The system group to check.*/
    private static final String[] KEYS = {GroupData.SYSTEM, GroupData.GUEST};

    /** One of the constants defined by this class. */
    private int index;

    /** Helper reference to the icons manager. */
    private IconManager icons;

    /** The id of the user currently logged in.*/
    private long userID;

    /**
     * Checks if the passed index is supported.
     * 
     * @param value The value to control.
     */
    private void checkIndex(int value)
    {
        switch (value) {
        case COPY:
            name = NAME_COPY_LINK;
            putValue(Action.NAME, NAME_COPY_LINK);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_COPY_LINK));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.COPY));
            break;
        case PASTE:
            name = NAME_PASTE_LINK;
            putValue(Action.NAME, NAME_PASTE_LINK);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_PASTE_LINK));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.PASTE));
            break;
        case REMOVE:
            name = NAME_REMOVE;
            putValue(Action.NAME, NAME_REMOVE);
            putValue(Action.SHORT_DESCRIPTION,
                    UIUtilities.formatToolTipText(DESCRIPTION_REMOVE));
            putValue(Action.SMALL_ICON, icons.getIcon(IconManager.DELETE));
            break;
        case CUT:
            name = NAME_CUT_LINK;
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
     * Sets the action enabled depending on the state of the {@link Browser}.
     * @see TreeViewerAction#onBrowserStateChange(Browser)
     */
    protected void onBrowserStateChange(Browser browser)
    {
        if (browser == null) return;
        switch (browser.getState()) {
        case Browser.LOADING_DATA:
        case Browser.LOADING_LEAVES:
            setEnabled(false);
            break;
        default:
            onDisplayChange(browser.getLastSelectedDisplay());
            break;
        }
    }

    /**
     * Returns <code>true</code> if the pasting action is valid,
     * <code>false</code> otherwise.
     * 
     * @param ho The selected data object.
     * @param list The objects to copy.
     * @return See above.
     */
    private boolean isPasteValid(Object ho, List<DataObject>list)
    {
        Iterator<DataObject> i = list.iterator();
        DataObject os;
        int count = 0;
        while (i.hasNext()) {
            os = i.next();
            if (!EditorUtil.isTransferable(ho, os, userID)) return false;
            count++;
        }
        return count == list.size();
    }

    /**
     * Adapt the name and description of this action with respect to the
     * selected browser
     * 
     * @param browserType
     *            The type of the {@link Browser}
     */
    private void adaptActionNameDescription(int browserType) {
        if (browserType == Browser.ADMIN_EXPLORER) {
            switch (index) {
                case CUT:
                    name = NAME_CUT;
                    putValue(Action.NAME, NAME_CUT);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT));
                    break;
                case COPY:
                    name = NAME_COPY;
                    putValue(Action.NAME, NAME_COPY);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_COPY));
                    break;
                case PASTE:
                    name = NAME_PASTE;
                    putValue(Action.NAME, NAME_PASTE);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_PASTE));
            }
        } else {
            switch (index) {
                case CUT:
                    name = NAME_CUT_LINK;
                    putValue(Action.NAME, NAME_CUT_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities.formatToolTipText(DESCRIPTION_CUT_LINK));
                    break;
                case COPY:
                    name = NAME_COPY_LINK;
                    putValue(Action.NAME, NAME_COPY_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_COPY_LINK));
                    break;
                case PASTE:
                    name = NAME_PASTE_LINK;
                    putValue(Action.NAME, NAME_PASTE_LINK);
                    putValue(Action.SHORT_DESCRIPTION,
                            UIUtilities
                                    .formatToolTipText(DESCRIPTION_PASTE_LINK));
            }
        }
    }
    
    /**
     * Sets the action enabled depending on the selected type.
     * @see TreeViewerAction#onDisplayChange(TreeImageDisplay)
     */
    protected void onDisplayChange(TreeImageDisplay selectedDisplay)
    {
        if (selectedDisplay == null) {
            setEnabled(false);
            return;
        }
        Browser browser = model.getSelectedBrowser();
        if (browser == null) {
            setEnabled(false);
            return;
        }
        
        adaptActionNameDescription(browser.getBrowserType());
        
        Object ho = selectedDisplay.getUserObject();
        TreeImageDisplay[] selected;
        int count = 0;
        TreeImageDisplay parentDisplay = selectedDisplay.getParentDisplay();
        Object parent = null;
        if (parentDisplay != null) parent = parentDisplay.getUserObject();
        switch (index) {
        case PASTE:
            List<DataObject> list = model.getDataToCopy();
            if (list == null || list.size() == 0) {
                setEnabled(false);
                return;
            }
            if (ho instanceof ProjectData || ho instanceof ScreenData ||
                    ho instanceof DatasetData || ho instanceof GroupData ||
                    ho instanceof TagAnnotationData) {
                selected = browser.getSelectedDisplays();
                for (int i = 0; i < selected.length; i++) {
                    ho = selected[i].getUserObject();
                    if (isPasteValid(ho, list)) {
                        if (ho instanceof GroupData) {
                            count++;
                        } else {
                            if (model.canLink(ho)) count++;
                        }
                    }
                }
                setEnabled(count == selected.length);
            } else if (ho instanceof ExperimenterData ||
                    ho instanceof GroupData) {
                if (browser.getBrowserType() != Browser.ADMIN_EXPLORER) {
                    selected = browser.getSelectedDisplays();
                    for (int i = 0; i < selected.length; i++) {
                        ho = selected[i].getUserObject();
                        if (isPasteValid(ho, list)) count++;
                    }
                    setEnabled(count == selected.length);
                }
            } else setEnabled(false);
            break;
        case REMOVE:
            if (ho instanceof ProjectData || ho instanceof DatasetData ||
                    ho instanceof ScreenData || ho instanceof PlateData ||
                    ho instanceof PlateAcquisitionData ||
                    ho instanceof FileAnnotationData ||
                    ho instanceof TagAnnotationData ||
                    ho instanceof ImageData) {
                selected = browser.getSelectedDisplays();
                for (int i = 0; i < selected.length; i++) {
                    if (model.canDelete(selected[i].getUserObject())) 
                        count++;
                }
                setEnabled(count == selected.length);
            } else if (ho instanceof ExperimenterData) {
                if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
                    setEnabled(false);
                } else setEnabled(false);
            } else if (ho instanceof GroupData) {
                setEnabled(false); //TODO
            } else setEnabled(false);

            break;
        case COPY:
        case CUT:
            if (ho instanceof DatasetData || ho instanceof ImageData ||
                    ho instanceof PlateData) {
                selected = browser.getSelectedDisplays();
                for (int i = 0; i < selected.length; i++) {
                    if (model.canLink(selected[i].getUserObject()))
                        count++;
                }
                if (index == COPY) {
                    if (ho instanceof DatasetData) {
                        if (!(parent instanceof ProjectData)) {
                            setEnabled(false);
                            return;
                        }
                    } else if (ho instanceof ImageData) {
                        if (!(parent instanceof DatasetData ||
                                parent instanceof TagAnnotationData)) {
                            setEnabled(false);
                            return;
                        }
                    } else if (ho instanceof PlateData) {
                        if (!(parent instanceof ScreenData)) {
                            setEnabled(false);
                            return;
                        }
                    }
                }
                setEnabled(count == selected.length);
            } else if (ho instanceof ExperimenterData) {
                if (index == COPY) {
                    setEnabled(browser.getBrowserType() == Browser.ADMIN_EXPLORER);
                } else {
                    setEnabled(false);
                    if (browser.getBrowserType() == Browser.ADMIN_EXPLORER) {
                        if (parent instanceof GroupData) {
                            GroupData g = (GroupData) parent;
                            Boolean b = null;
                            for (int i = 0; i < KEYS.length; i++) {
                                if (model.isSystemGroup(g.getId(), KEYS[i])) {
                                    ExperimenterData user = model.getUserDetails();
                                    ExperimenterData exp = (ExperimenterData) ho;
                                    b = exp.getId() != user.getId() &&
                                            !model.isSystemUser(exp.getId(),
                                                    KEYS[i]);
                                    break;
                                }
                            }
                            if (b != null) setEnabled(b);
                            else setEnabled(true);
                        }
                    }
                }
            } else if (ho instanceof TagAnnotationData) {
                TagAnnotationData tag = (TagAnnotationData) ho;
                if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(
                        tag.getNameSpace()))
                    setEnabled(false);
                else {
                    selected = browser.getSelectedDisplays();
                    for (int i = 0; i < selected.length; i++) {
                        if (model.canAnnotate(selected[i].getUserObject()))
                            count++;
                    }
                    if (index == CUT) {
                        if (!(parent instanceof TagAnnotationData)) {
                            setEnabled(false);
                            return;
                        }
                    }
                    setEnabled(count == selected.length);
                }
            } else setEnabled(false);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     * @param index One of the constants defined by this class.
     */
    public ManageObjectAction(TreeViewer model, int index)
    {
        super(model);
        icons = IconManager.getInstance();
        checkIndex(index);
        this.index = index;
        userID = model.getUserDetails().getId();
    }

    /**
     * Copies, pastes, cuts or removes the selected objects.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        ActionCmd cmd = null;
        switch (index) {
        case COPY:
            cmd = new CopyCmd(model);
            break;
        case PASTE:
            cmd = new PasteCmd(model);
            break;
        case REMOVE:
            cmd = new DeleteCmd(model.getSelectedBrowser());
            break;
        case CUT:
            cmd = new CutCmd(model);
        }
        if (cmd != null) cmd.execute();
    }

}
