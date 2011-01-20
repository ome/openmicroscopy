/*
 * org.openmicroscopy.shoola.agents.measurement.util.RendererUtils 
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
package org.openmicroscopy.shoola.util.ui.treetable.util;


//Java imports
import java.awt.Component;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Collection of constants used by the various cell renderer.
 * and static methods.
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
public class OMETreeTableRenderUtils
{
	
	/**
	 * Sets the color of the component depending on the value of the
	 * passed row index.
	 * 
	 * @param c				The component to handle.
	 * @param selectedRow	The index of the selected row.
	 * @param row			The index of the current row.
	 */
	public static void setRowColor(Component c, int selectedRow, int row)
	{
		if (c == null) return;
		if (selectedRow == row) {
			c.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
			c.setForeground(UIUtilities.FOREGROUND_COLOUR);
		} else {
			if (row % 2 == 0)
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
			else
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			c.setForeground(UIUtilities.FOREGROUND_COLOUR);
		}
	}
	
	/**
	 * Sets the color of the component depending on the value of the
	 * selected boolean.
	 * 
	 * @param c				The component to handle.
	 * @param selected 		The row is selected.
	 * @param row			The index of the current row.
	 */
	public static void setRowColor(Component c, boolean selected, int row)
	{
		if (c == null) return;
		if (selected) 
		{
			c.setBackground(UIUtilities.SELECTED_BACKGROUND_COLOUR);
			c.setForeground(UIUtilities.FOREGROUND_COLOUR);
		} 
		else 
		{
			if (row % 2 == 0)
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
			else
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			c.setForeground(UIUtilities.FOREGROUND_COLOUR);
		}
	}
	
}
