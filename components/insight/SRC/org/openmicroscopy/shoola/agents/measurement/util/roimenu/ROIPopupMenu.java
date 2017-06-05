/*
 * org.openmicroscopy.shoola.agents.measurement.util.roimenu.ROIPopupMenu 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement.util.roimenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import omero.gateway.model.FolderData;

import org.openmicroscopy.shoola.agents.measurement.util.actions.ROIAction;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController;
import org.openmicroscopy.shoola.agents.measurement.util.roitable.ROIActionController.CreationActionType;
import org.openmicroscopy.shoola.agents.measurement.view.ROITable;
import org.openmicroscopy.shoola.util.roi.figures.ROIFigure;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * Displays options to manipulate a ROI.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROIPopupMenu
{	
	
	/** Text for the pop-up Menu -- not shown. */
	final static String POPUP_MENU_DESCRIPTION = "Manager Options";

	/** 
	 * Text for the ROI options -- parent of Split, merge, delete, propagate, 
	 * duplicate. 
	 * */
	final static String ROI_CREATION_OPTIONS = "ROI Management Options";
	
	/** 
	 * Text for the ROI stats options, the ability to call the roi stats:
	 * show intensity over time, project, etc.
	 * */
	final static String ROI_STATS_OPTIONS = "ROI Stats Options";
	
	/** The menubar which holds the menu items. */
	private JPopupMenu				popupMenu;
	
	/** The list of actions. */
	private List<ROIAction>			actions;
	
	/** The reference to the {@link ROITable}*/
	private ROITable table;
	
	/**
	 * Instantiate the popup menu
	 * @param table Reference to the {@link ROITable}
	 */
	public ROIPopupMenu(ROITable table)
	{
		this.table = table;
		actions = new ArrayList<ROIAction>();
		createPopupMenu();
	}
			
	/**
	 * Creates the menu which will allow the user to adjust the ROI properties.
	 * 
	 * @return The ROI control menu.
	 */
	private JMenu createROICreationOptions()
	{
		JMenu roiOptionsParent = new JMenu(ROI_CREATION_OPTIONS);
		ROIAction action;
		CreationActionType[] values = 
			ROIActionController.CreationActionType.values();
		for (int indexCnt = 0 ; indexCnt < values.length ; indexCnt++)
		{
			action = new ROIAction(table, values[indexCnt]);
			actions.add(action);
			popupMenu.add(new JMenuItem(action));
		}
		return roiOptionsParent;
	}

	/** Creates the popup menu. */
	private void createPopupMenu()
	{
		popupMenu = new JPopupMenu();
		createROICreationOptions();
	}

    /**
     * Enables/Disables the actions with respect to the selected objects
     * 
     * @param selection
     *            The selected objects
     */
    public void setActionsEnabled(Collection<Object> selection) {
        Iterator<ROIAction> j = actions.iterator();
        while (j.hasNext()) {
            ROIAction action = j.next();
            action.setEnabled(checkPermission(action.getCreationActionType(),
                    selection));
        }
    }
    
    /**
     * Checks if a specific action is enabled
     * 
     * @param action
     *            The action to check for
     * @return See above
     */
    public boolean isActionEnabled(CreationActionType action) {
        Iterator<ROIAction> j = actions.iterator();
        while (j.hasNext()) {
            ROIAction a = j.next();
            if (a.getCreationActionType() == CreationActionType.REMOVE_FROM_FOLDER)
                return a.isEnabled();
        }
        return false;
    }

    /**
     * Enable/Disable a specific action
     * 
     * @param action
     *            The action
     * @param enabled
     *            Pass <code>true</code> to enable, <code>false</code> to
     *            disable the action
     */
    public void enableAction(CreationActionType action, boolean enabled) {
        Iterator<ROIAction> j = actions.iterator();
        while (j.hasNext()) {
            ROIAction a = j.next();
            if (a.getCreationActionType() == CreationActionType.REMOVE_FROM_FOLDER)
                a.setEnabled(enabled);
        }
    }

    /**
     * Check a specific action should be enabled or disabled with respect to a
     * certain object selection
     * 
     * @param action
     *            The action to check for
     * @param selection
     *            The selected objects
     * @return <code>true</code> if the action should be enabled,
     *         <code>false</code> if the action should be disabled
     */
    private boolean checkPermission(CreationActionType action,
            Collection<Object> selection) {

        if (action == CreationActionType.CREATE_FOLDER) {
            if (selection.size() == 0)
                return table.canCreateFolder();
            if (selection.size() == 1) {
                Object obj = selection.iterator().next();
                if (obj instanceof FolderData) {
                    FolderData f = (FolderData) obj;
                    return table.canCreateFolder() && f.canEdit();
                }
                if (obj instanceof ROI)
                    return table.canCreateFolder() && ((ROI) obj).canEdit();
                if (obj instanceof ROIShape)
                    return table.canCreateFolder() && ((ROIShape) obj).getROI().canEdit();
            }
            else {
                for (Object obj : selection) {
                    if (!(obj instanceof ROI) && !(obj instanceof ROIShape))
                        return false;
                    if (obj instanceof ROI && !((ROI) obj).canEdit())
                        return false;
                    if (obj instanceof ROIShape
                            && !((ROIShape) obj).getROI().canEdit())
                        return false;
                }
                //only editable ROIs selected
                return table.canCreateFolder();
            }
        }
           
        boolean isFolderSelection = true;
        boolean isROISelection = true;

        for (Object obj : selection) {
            if (isROISelection
                    && !(obj instanceof ROI || 
                            obj instanceof ROIShape || 
                            obj instanceof ROIFigure)) {
                isROISelection = false;
            }
            if (isFolderSelection && !(obj instanceof FolderData)) {
                isFolderSelection = false;
            }
        }

        if (!(isFolderSelection ^ isROISelection))
            return false;

        boolean isInFolder = false;
        int delete = 0;
        int edit = 0;
        int link = 0;
        if (isFolderSelection) {
            Iterator<Object> i = selection.iterator();
            FolderData folder;
            while (i.hasNext()) {
                folder = (FolderData) i.next();
                if (folder.canEdit())
                    edit++;
                if (folder.canDelete())
                    delete++;
                if (folder.canLink())
                    link++;
            }
        } else {
            Iterator<Object> i = selection.iterator();
            Object obj;
            ROIShape shape;
            ROIFigure roi;
            while (i.hasNext()) {
                obj = i.next();
                if (obj instanceof ROI) {
                    boolean shapeEdit = true;
                    boolean shapeDel = true;
                    if (!isInFolder)
                        isInFolder = !table.findFolders((ROI) obj).isEmpty();
                    for (ROIShape s : ((ROI) obj).getShapes().values()) {
                        roi = s.getFigure();
                        if (shapeEdit && !roi.canEdit())
                            shapeEdit = false;
                        if (shapeDel && !roi.canDelete())
                            shapeDel = false;
                    }
                    if (shapeEdit) {
                        edit++;
                        link++;
                    }
                    if (shapeDel)
                        delete++;
                }
                if (obj instanceof ROIShape) {
                    if (!isInFolder)
                        isInFolder = !table.findFolders(
                                ((ROIShape) obj).getROI()).isEmpty();
                    shape = (ROIShape) obj;
                    roi = shape.getFigure();
                    if (!(roi.isReadOnly())) {
                        if (roi.canEdit()) {
                            edit++;
                            link++;
                        }
                        if (roi.canDelete())
                            delete++;
                    }
                }
                if (obj instanceof ROIFigure) {
                    if (!isInFolder)
                        isInFolder = !table.findFolders(
                                ((ROIFigure) obj).getROI()).isEmpty();
                    roi = (ROIFigure) obj;
                    if (!(roi.isReadOnly())) {
                        if (roi.canEdit()) {
                            edit++;
                            link++;
                        }
                        if (roi.canDelete())
                            delete++;
                    }
                }
            }
        }

        switch (action) {
        // ROI actions
        case ADD_TO_FOLDER:
            return isROISelection && link == selection.size();
        case DELETE:
            return isROISelection && delete == selection.size();
        case DUPLICATE:
            return isROISelection && edit == selection.size();
        case MERGE:
            return isROISelection && edit == selection.size();
        case PROPAGATE:
            return isROISelection && edit == selection.size();
        case REMOVE_FROM_FOLDER:
            return isROISelection && link == selection.size() && isInFolder;
        case SPLIT:
            return isROISelection && edit == selection.size();
        case TAG:
            return isROISelection && edit == selection.size();

            // Folder actions
        case CREATE_FOLDER:
            return isFolderSelection && link == 1 && selection.size() == 1;
        case DELETE_FOLDER:
            return isFolderSelection && delete == selection.size();
        case EDIT_FOLDER:
            return isFolderSelection && edit == 1 && selection.size() == 1;
        case MOVE_FOLDER:
            return isFolderSelection && link == selection.size();
        }

        return false;
    }

    /**
     * Returns the popup menu.
     * 
     * @return see above.
     */
    public JPopupMenu getPopupMenu() {
        return popupMenu;
    }
	
}