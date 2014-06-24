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
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.table.DefaultTableModel;

import org.openmicroscopy.shoola.agents.dataBrowser.IconManager;
import org.openmicroscopy.shoola.agents.dataBrowser.browser.Thumbnail;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

public class SearchResultTableModel extends DefaultTableModel {

    List<DataObject> data = new ArrayList<DataObject>();

    final DateFormat df = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

    AdvancedResultSearchModel model;

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

    private String getDate(DataObject obj) {

        try {
            return df.format(new Date(obj.getCreated().getTime()));
        } catch (Exception e) {
            return "N/A";
        }

    }

    private String getGroup(DataObject obj) {
        return "" + obj.getGroupId();
    }

    public Icon getIcon(DataObject obj) {

        if (obj instanceof ImageData) {
//            Thumbnail thumb = model.getThumbnail(obj.getId());
//            return thumb.getIcon() == null ? IconManager.getInstance().getIcon(
//                    IconManager.IMAGE) : thumb.getIcon();
            return IconManager.getInstance().getIcon(IconManager.IMAGE); 
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

    public String getIconString(DataObject obj) {
        if (obj instanceof ImageData) {
            return "[Thumbnail]";
        } else if (obj instanceof ProjectData) {
            return "[Icon Project]";
        } else if (obj instanceof DatasetData) {
            return "[Icon Dataset]";
        } else if (obj instanceof ScreenData) {
            return "[Icon Screen]";
        } else if (obj instanceof PlateData) {
            return "[Icon Plate]";
        }
        return "N/A";
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
