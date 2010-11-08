/*
 * org.openmicroscopy.shoola.agents.measurement.util.roitable.ROITableCellRenderer 
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
import javax.swing.Icon;
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
	private static Icon ROI__OTHER_OWNER_ICON;
	
	static {
		IconManager icons = IconManager.getInstance();
		SHAPE_ICON = icons.getIcon(IconManager.ROISHAPE);
		ROI_ICON = icons.getIcon(IconManager.ROISTACK);
		ROI__OTHER_OWNER_ICON = icons.getIcon(IconManager.ROISTACK_OTHER_OWNER);
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
		Object thisObject = ((ROINode) value).getUserObject();
		
		if (thisObject instanceof ROI) {
			ROI roi = (ROI) thisObject;
			if (userID == roi.getOwnerID())
				setIcon(ROI_ICON);
			else setIcon(ROI__OTHER_OWNER_ICON);
		} else if( thisObject instanceof ROIShape) setIcon(SHAPE_ICON);
		return this;
	}
	
}


