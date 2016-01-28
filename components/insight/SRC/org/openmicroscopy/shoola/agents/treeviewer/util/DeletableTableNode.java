/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ExperimenterData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.GroupData;
import omero.gateway.model.ImageData;
import omero.gateway.model.PlateData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.PlateAcquisitionData;
import omero.gateway.model.ScreenData;

/** 
 * UI representation of an object that could not be deleted.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
class DeletableTableNode 
	extends OMETreeNode
{

	/** Text corresponding to the <code>image</code> type. */
	static final String IMAGE_TYPE = "Image";
	
	/** Text corresponding to the <code>dataset</code> type. */
	static final String DATASET_TYPE = "Dataset";
	
	/** Text corresponding to the <code>project</code> type. */
	static final String PROJECT_TYPE = "Project";
	
	/** Text corresponding to the <code>screen</code> type. */
	static final String SCREEN_TYPE = "Screen";
	
	/** Text corresponding to the <code>plate</code> type. */
	static final String PLATE_TYPE = "Plate";
	
	/** Text corresponding to the <code>plate Acquisition</code> type. */
	static final String PLATE_ACQUISITION_TYPE = "Plate Run";
	
	/** Text corresponding to the <code>file</code> type. */
	static final String FILE_TYPE = "File";
	
	/** Text corresponding to the <code>group</code> type. */
	static final String GROUP_TYPE = "Group";
	
	/** Text corresponding to the <code>Experimenter</code> type. */
	static final String EXPERIMENTER_TYPE = "Experimenter";
	
	/**
	 * Creates a new instance.
	 * 
	 * @param refNode The node of reference. Mustn't be <code>null</code>;
	 */
	DeletableTableNode(Object refNode)
	{
		super(refNode);
		if (refNode == null)
			throw new IllegalArgumentException("No node specified.");
		//setAllowsChildren(false);
	}
	
	/**
	 * Returns one of the constants defined by this class if the object 
	 * is known otherwise returns an empty string.
	 * 
	 * @return See above.
	 */
	String getType()
	{
		Object node = getUserObject();
		if (!(node instanceof DataObject)) return "";
		DataObject object = (DataObject) node;
		if (object instanceof ImageData) return IMAGE_TYPE;
		if (object instanceof DatasetData) return DATASET_TYPE;
		if (object instanceof ProjectData) return PROJECT_TYPE;
		if (object instanceof ScreenData) return SCREEN_TYPE;
		if (object instanceof PlateData) return PLATE_TYPE;
		if (object instanceof FileAnnotationData) return FILE_TYPE;
		if (object instanceof PlateAcquisitionData) 
			return PLATE_ACQUISITION_TYPE;
		if (object instanceof GroupData) return GROUP_TYPE;
		if (object instanceof ExperimenterData) return EXPERIMENTER_TYPE;
		return "";
	}
	
	/**
	 * Overridden to return the correct value depending on the column
	 * @see OMETreeNode#getValueAt(int)
	 */
	public Object getValueAt(int column)
	{
		Object node = getUserObject();
		if (!(node instanceof DataObject)) {
			switch (column) {
				case NotDeletedObjectDialog.TYPE_COL: return "";
				case NotDeletedObjectDialog.ID_COL: return -1;
				case NotDeletedObjectDialog.NAME_COL: return "";
			}
		}
		DataObject object = (DataObject) node;
		switch (column) {
			case NotDeletedObjectDialog.TYPE_COL:
				return getType();
			case NotDeletedObjectDialog.ID_COL:
				return object.getId();
			case NotDeletedObjectDialog.NAME_COL:
				if (object instanceof DatasetData)
					return ((DatasetData) object).getName();
				if (object instanceof ProjectData)
					return ((ProjectData) object).getName();
				if (object instanceof ScreenData)
					return ((ScreenData) object).getName();
				if (object instanceof PlateData)
					return ((PlateData) object).getName();
				if (object instanceof PlateAcquisitionData)
					return ((PlateAcquisitionData) object).getLabel();
				if (object instanceof FileAnnotationData)
		        	return ((FileAnnotationData) object).getFileName();
				if (object instanceof ImageData)
					return EditorUtil.getPartialName(
							((ImageData) object).getName());
				if (object instanceof GroupData)
					return ((GroupData) object).getName();
				if (object instanceof ExperimenterData) {
					return EditorUtil.formatExperimenter(
							(ExperimenterData) object);
				}
				return "";
		}
		return null;
	}
	
}
