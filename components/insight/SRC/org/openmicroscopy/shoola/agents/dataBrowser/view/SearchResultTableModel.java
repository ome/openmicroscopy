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

import java.awt.FontMetrics;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
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
 * 
 * @since 5.0
 */
public class SearchResultTableModel extends DefaultTableModel {

    /** The name of the columns */
    public static final String[] COLUMN_NAMES = { "Type", "Name",
            "Date (Acquisition/Import)", "Group", " " };

    /**
     * The index of the column which contains the View buttons (i. e. the last
     * column)
     */
    public static final int VIEWBUTTON_COLUMN_INDEX = COLUMN_NAMES.length - 1;

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

    private SearchResultTable parent;
    
    /**
     * Creates a new instance
     * 
     * @param data
     *            The {@link DataObject}s which should be shown in the table
     * @param model
     *            Reference to the DataBrowserModel
     */
    public SearchResultTableModel(SearchResultTable parent, List<DataObject> data,
            AdvancedResultSearchModel model) {
        super(COLUMN_NAMES, data.size());
        this.parent = parent;
        this.data = data;
        this.model = model;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < 0 || row >= data.size())
            throw new ArrayIndexOutOfBoundsException(row
                    + " is not within the valid range of rows [0,"
                    + (data.size() - 1) + "]");

        DataObject obj = data.get(row);

        Object result = "--";

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
     * Get the acquisition/creation date of the {@link DataObject}
     * 
     * @param obj
     * @return
     */
    private String getDate(DataObject obj) {
        String aDate = "--";
        String iDate = "--";

        try {
            if (obj instanceof ImageData) {
                // just images have an acquisition date
                aDate = DATE_FORMAT.format(new Date(((ImageData) obj)
                        .getAcquisitionDate().getTime()));
            }
            iDate = DATE_FORMAT.format(new Date(obj.getCreated().getTime()));
        } catch (Exception e) {
            // if there is no date, date is invalid or the text conversion
            // goes wrong, just stick to '--' for the date
        }

        return aDate + "<br/>" + iDate;
    }

    /**
     * Get the group name the {@link DataObject} belongs to
     * 
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
     * 
     * @param obj
     * @return A general icon (e. g. for DataSets) or a thumbnail icon (for
     *         Images); a tranparent icon if the data type is not supported
     */
    private Icon getIcon(DataObject obj) {

        if (obj instanceof ImageData) {
            Thumbnail thumb = model.getThumbnail(obj);
            return thumb == null ? IconManager.getInstance().getIcon(
                    IconManager.IMAGE_48) : thumb.getIcon(THUMB_ZOOM_FACTOR);
        }

        else if (obj instanceof ProjectData) {
            return IconManager.getInstance().getIcon(IconManager.PROJECT_48);
        }

        else if (obj instanceof DatasetData) {
            return IconManager.getInstance().getIcon(IconManager.DATASET_48);
        }

        else if (obj instanceof ScreenData) {
            return IconManager.getInstance().getIcon(IconManager.SCREEN_48);
        }

        else if (obj instanceof PlateData) {
            return IconManager.getInstance().getIcon(IconManager.PLATE);
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
     * 
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
        }  else if (obj instanceof ScreenData) {
            name = ((ScreenData) obj).getName();
        } else if (obj instanceof PlateData) {
            name = ((PlateData) obj).getName();
        }
        
        
        FontMetrics fm = parent.getGraphics().getFontMetrics();
        int colWidth = parent.getColumn(1).getWidth();
        int textWidth = fm.stringWidth(name); 
        if (textWidth > colWidth) {
            int max = (int) ((double) colWidth / (double) textWidth * name
                    .length());
            // TODO: FontMetrics.stringWidth doesn't seem to work properly, but with 
            // an additional cut-off of 10 it seems to work fine for now.
            name = EditorUtil.truncate(name, max-10, true);
            textWidth = fm.stringWidth(name);
        }
        
        return name;
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == VIEWBUTTON_COLUMN_INDEX;
    }

}
