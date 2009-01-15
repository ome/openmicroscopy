/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.DeletableTableNode 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.util.ui.treetable.model.OMETreeNode;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class DeletableTableNode 
	extends OMETreeNode
{

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
	 * Overridden to return the correct value depending on the column
	 * @see OMETreeNode#getValueAt(int)
	 */
	public Object getValueAt(int column)
	{
		Object node = getUserObject();
		if (!(node instanceof DeletableObject)) {
			switch (column) {
				case NotDeletedObjectDialog.TYPE_COL: return "";
				case NotDeletedObjectDialog.ID_COL: return -1;
				case NotDeletedObjectDialog.NAME_COL: return "";
			}
		}
			
		DataObject object = ((DeletableObject) node).getObjectToDelete();
		switch (column) {
			case NotDeletedObjectDialog.TYPE_COL:
				if (object instanceof ImageData) return "Image";
				if (object instanceof DatasetData) return "Dataset";
				if (object instanceof ProjectData) return "Project";
				if (object instanceof ScreenData) return "Screen";
				if (object instanceof PlateData) return "Plate";
				if (object instanceof FileAnnotationData) return "File";
				return "";
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
				if (object instanceof FileAnnotationData)
		        	return ((FileAnnotationData) object).getFileName();
				if (object instanceof ImageData)
					return EditorUtil.getPartialName(
							((ImageData) object).getName());
				return "";
		}
		return null;
	}
	
}
