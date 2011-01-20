/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITreeTableCellRenderer 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.util.roi.model.ROI;
import org.openmicroscopy.shoola.util.roi.model.ROIShape;

/** 
 * Displays the icon corresponding to the shape of the ROI.
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
public class ROITreeTableCellRenderer
	extends JLabel
	implements TreeCellRenderer
{

	/** Helper reference to the Icon Manager. */
	private IconManager icons;
	
	/** The identifier of the user currently logged in. */
	private long userID;
	
	/**
	 * Creates a new instance. Sets the opacity of the label to 
	 * <code>true</code>.
	 */
	public ROITreeTableCellRenderer()
	{
		setOpaque(false);
		icons = IconManager.getInstance();
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

		Object thisObject = ((ROITreeNode) value).getUserObject();

		if (thisObject instanceof ROI) {
			ROI roi = (ROI) thisObject;
			if (userID == roi.getOwnerID() || roi.getOwnerID() == -1)
				setIcon(icons.getIcon(IconManager.ROISTACK));
			else setIcon(icons.getIcon(IconManager.ROISTACK_OTHER_OWNER));
		} else if (thisObject instanceof ROIShape)
			setIcon(icons.getIcon(IconManager.ROISHAPE));
		return this;
	}

}



