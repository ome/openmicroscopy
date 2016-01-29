/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableCellRenderer 
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
package org.openmicroscopy.shoola.agents.measurement.util.roitable;


//Java imports
import java.awt.Component;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.util.roi.model.ROI;

import omero.gateway.model.FolderData;

/** 
 * Basic cell renderer displaying the icon associated to an ROI or an ROIshape.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ROITableCellRenderer 
	extends JLabel
	implements TreeCellRenderer
{

	/** Reference to the shape icon. */
	private static Icon SHAPE_ICON;
	
	/** Reference to the ROI icon. */
	private static Icon ROI_ICON;
	
	/** Reference to the ROI owned by other users icon. */
	private static Icon ROI_OTHER_OWNER_ICON;
	
	/** Reference to the Folder icon */
	private static Icon FOLDER_ICON;
	
	static {
		IconManager icons = IconManager.getInstance();
		SHAPE_ICON = icons.getIcon(IconManager.ROISHAPE);
		ROI_ICON = icons.getIcon(IconManager.ROISTACK);
		ROI_OTHER_OWNER_ICON = icons.getIcon(IconManager.ROISTACK_OTHER_OWNER);
		FOLDER_ICON = icons.getIcon(IconManager.LINE_16);
	}
	
	/** The identifier of the user currently logged in. */
	private long userID;
	
	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>false</code>.
	 */
	public ROITableCellRenderer()
	{
		setOpaque(false);
		userID = MeasurementAgent.getUserDetails().getId();
	}
	
	/**
	 * Sets the icon corresponding to the type of Object.
	 * @see TreeCellRenderer#getTreeCellRendererComponent(JTree, Object, 
	 * boolean, boolean, boolean, int, boolean)
	 */
	public Component getTreeCellRendererComponent(JTree tree, 
			Object value, boolean selected, boolean expanded, 
			boolean leaf, int row, boolean hasFocus)
	{
	    ROINode node = (ROINode) value;
		if (node.isROINode()) {
			ROI roi = (ROI) node.getUserObject();
			if (userID == roi.getOwnerID() || roi.getOwnerID() == -1)
				setIcon(ROI_ICON);
			else setIcon(ROI_OTHER_OWNER_ICON);
			TreeMap map = roi.getShapes();
			if (map == null) setText("[0]");
			else setText("["+map.size()+"]");
		} else if (node.isShapeNode()) {
			setIcon(SHAPE_ICON);
			setText("");
		} else if (node.isFolderNode()) {
		    FolderData folder = (FolderData) node.getUserObject();
		    setIcon(FOLDER_ICON);
            setText("Folder: "+folder.getName());
		}
		return this;
	}
	
}