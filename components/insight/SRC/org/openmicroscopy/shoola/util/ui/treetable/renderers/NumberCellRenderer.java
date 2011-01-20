/*
 * org.openmicroscopy.shoola.util.ui.treetable.renderers.NumberCellRenderer 
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
package org.openmicroscopy.shoola.util.ui.treetable.renderers;

//Java imports
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

//Third-party libraries

//Application-internal dependencies

/** 
 * Renders numerical values.
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
public class NumberCellRenderer
	extends DefaultTableCellRenderer
{
	
	/**
	 * Creates a new instance. Sets the opacity of the label to
	 * <code>true</code>.
	 * 
	 * @param alignment The alignment of the label being rendered.
	 */
	public NumberCellRenderer(int alignment)
	{
		setHorizontalAlignment(alignment);
		//setOpaque(true);
		setBorder(null);
	}
	
	/** Creates a new instance. */
	public NumberCellRenderer()
	{
		this(SwingConstants.CENTER);
	}

	/**
	 * Overridden to set the correct renderer.
	 * @see DefaultTableCellRenderer#getTableCellRendererComponent(JTable, 
	 * 								Object, boolean, boolean, int, int)
	 */
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column)
	{
		
		if (value != null) setText(value.toString());
		return this;
	}
	
}


