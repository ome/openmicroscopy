/*
 * org.openmicroscopy.shoola.agents.measurement.view.ROITableModel 
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
package org.openmicroscopy.shoola.agents.measurement.view;

import java.util.Vector;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.openmicroscopy.shoola.agents.measurement.util.ROINode;

//Java imports

//Third-party libraries

//Application-internal dependencies

/**
 * 
 * The ROITableModel is the model for the ROITable class
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
class ROITableModel 
	extends DefaultTreeTableModel
{
	/** ROI ID Column no for the wizard. */
	public static final int				ROIID_COLUMN = 0;

	/** Time point Column no for the wizard. */
	public static final int				TIME_COLUMN = 1;
	
	/** Z-Section Column no for the wizard. */
	public static final int				Z_COLUMN = 2;

	/** Type Column no for the wizard. */
	public static final int				SHAPE_COLUMN = 3;

	/** Annotation Column no for the wizard. */
	public static final int				ANNOTATION_COLUMN = 4;

	/** Visible Column no for the wizard. */
	public static final int				VISIBLE_COLUMN = 5;

	/**
	 * Set the model to use ROI nodes and columns as a vector.
	 * @param node root node for model.
	 * @param columns column names.
	 */
	ROITableModel(ROINode node, Vector columns)
	{
		super(node, columns);
	}
	
	
	 public boolean isEditable(int column) {
            	switch (column)
    			{
    				case 0:
    					return false;
    				case ROIID_COLUMN+1:
    					return false;
    				case TIME_COLUMN+1:
    					return false;
    				case Z_COLUMN+1:
    					return false;
    				case SHAPE_COLUMN+1:
    					return false;
    				case ANNOTATION_COLUMN+1:
    					return true;
    				case VISIBLE_COLUMN+1:
    					return false;
    				default:
    					return false;
    			}
      }
	
	 public Class<?> getColumnClass(int column) 
	{
		switch (column)
		{
			case 0:
				return ROINode.class;
			case ROIID_COLUMN+1:
				return Long.class;
			case TIME_COLUMN+1:
				return Long.class;
			case Z_COLUMN+1:
				return Long.class;
			case SHAPE_COLUMN+1:
				return String.class;
			case ANNOTATION_COLUMN+1:
				return String.class;
			case VISIBLE_COLUMN+1:
				return Boolean.class;
			default:
				return null;
		}
	}
}
	


