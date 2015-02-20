/*
 * org.openmicroscopy.shoola.util.ui.TooltipTableHeader 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.util.ui;

import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.openmicroscopy.shoola.util.CommonsLangUtils;


/** 
 * Creates a table header with tool tip.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class TooltipTableHeader extends JTableHeader {

	/** The tool tips. */
	private final List<String> toolTips;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model The table model the header is for.
	 * @param toolTips The list with the tool tips to display.
	 */
	public TooltipTableHeader(TableColumnModel model, List<String> toolTips)
	{
		super(model);
		if (toolTips == null)
			throw new IllegalArgumentException("No tool tips specified.");
		this.toolTips = toolTips;
	}
	
	/**
	 * Overridden to return the tool tip depending on the location of the mouse.
	 * @see JTableHeader#getToolTipText(MouseEvent)
	 */
	public String getToolTipText(MouseEvent e)
	{
		int col = columnAtPoint(e.getPoint());
		JTable table = getTable();
		if (table == null) return "";
		int modelCol = getTable().convertColumnIndexToModel(col);
		String retStr;
		try {
			retStr = toolTips.get(modelCol);
		} catch (Exception ex) {
			retStr = "";
		}
		if (CommonsLangUtils.isBlank(retStr))
			retStr = super.getToolTipText(e);
		return retStr;
	}
}
