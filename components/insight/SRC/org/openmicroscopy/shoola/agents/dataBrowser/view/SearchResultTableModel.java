/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.agents.dataBrowser.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/**
 * The Model for the {@link SearchResultTable}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class SearchResultTableModel extends DefaultTableModel {

    /** Defines the size of the thumbnail icons */
    private static final double THUMB_ZOOM_FACTOR = 0.5;

    /** Defines the format how the date is shown */
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "MMM dd, yyyy hh:mm a");

    /** The DataObjects shown in the table */
    private List<DataObject> data = new ArrayList<DataObject>();

    /** A reference to the DataBrowserModel */
    private AdvancedResultSearchModel model;

    /** The groups the user has access to */
    @SuppressWarnings("unchecked")
    private Collection<GroupData> groups = TreeViewerAgent
            .getAvailableUserGroups();

    /**
     * Creates a new instance
     * @param data The {@link DataObject}s which should be shown in the table
     * @param model Reference to the DataBrowserModel
     */
    public SearchResultTableModel(List<DataObject> data,
            AdvancedResultSearchModel model) {
        super(new String[] { "Type", "Name", "Date", "Group", " " }, data
                .size());
        this.data = data;
        this.model = model;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object result = "N/A";

        DataObject obj = data.get(row);

        switch (column) {
            case 0:
                result = getIcon(obj);
                break;
            case 1:
                result = getObjectName(obj);
                break;
            case 2:
                result = getDate(obj);
                break;
            case 3:
                result = getGroup(obj);
                break;
            case 4:
                result = obj;
                break;
        }

        return result;
    }

    /**
     * Get the creation date of the {@link DataObject}
     * @param obj
     * @return
     */
    private String getDate(DataObject obj) {

        try {
            return DATE_FORMAT.format(new Date(obj.getCreated().getTime()));
        } catch (Exception e) {
            return "N/A";
        }

    }

    /**
     * Get the group name the {@link DataObject} belongs to
     * @param obj
     * @return
     */
    private String getGroup(DataObject obj) {
        for (GroupData g : groups) {
            if (g.getId() == obj.getGroupId()) {
                return g.getName();
            }
        }
        return "[ID: " + obj.getGroupId() + "]";
    }

    /**
     * Get the {@link Icon} for the {@link DataObject}
     * @param obj
     * @return A general icon (e. g. for DataSets) or a thumbnail icon (for Images); a tranparent icon if the data type is not supported
     */
    private Icon getIcon(DataObject obj) {

        if (obj instanceof ImageData) {
            Thumbnail thumb = model.getThumbnail(obj);
            return thumb == null ? IconManager.getInstance().getIcon(
                    IconManager.IMAGE) : thumb.getIcon(THUMB_ZOOM_FACTOR);
        }

        else if (obj instanceof ProjectData) {
            return IconManager.getInstance().getIcon(IconManager.PROJECT);
        }

        else if (obj instanceof DatasetData) {
            return IconManager.getInstance().getIcon(IconManager.DATASET);
        }

        else if (obj instanceof ScreenData) {
            return IconManager.getInstance().getIcon(IconManager.DATASET);
        }

        else if (obj instanceof PlateData) {
            return IconManager.getInstance().getIcon(IconManager.DATASET);
        }

        return IconManager.getInstance().getIcon(IconManager.TRANSPARENT);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return Icon.class;
            case 1:
                return String.class;
            case 2:
                return String.class;
            case 3:
                return String.class;
            case 4:
                return DataObject.class;
            default:
                return String.class;
        }
    }

    /**
     * Get the name of the {@link DataObject}
     * @param obj
     * @return
     */
    private String getObjectName(DataObject obj) {
        String name = "";
        if (obj instanceof ImageData) {
            name = ((ImageData) obj).getName();
        } else if (obj instanceof DatasetData) {
            name = ((DatasetData) obj).getName();
        } else if (obj instanceof ProjectData) {
            name = ((ProjectData) obj).getName();
        } else if (obj instanceof ImageData) {
            name = ((ImageData) obj).getName();
        } else if (obj instanceof ScreenData) {
            name = ((ScreenData) obj).getName();
        } else if (obj instanceof PlateData) {
            name = ((PlateData) obj).getName();
        }
        return name;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 4;
    }

}
