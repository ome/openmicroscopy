
/*
 * org.openmicroscopy.shoola.agents.util.flim.resultstable.ResultsTableView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.agents.util.flim.resultstable;

//Java imports
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

//Third-party libraries
import org.jdesktop.swingx.JXTable;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.flim.util.ResultsCellRenderer;

/**
 * The View of the results table.
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
public class ResultsTableView 
extends JXTable
{
	/** The grouping of rows in the table. */
	int mod;
	
	ResultsTableView()
	{
		super();
	}
	
	/**
	 * Set the hightlighting mod of the table.
	 * @param mod The mod.
	 */
	public void setRowHighlightMod(int mod)
	{
		this.mod = mod;
	}
	
	/**
	 * Overridden to return a customized cell renderer.
	 * @see JXTable#getCellRenderer(int, int)
	 */
	public TableCellRenderer getCellRenderer(int row, int column) 
	{
        return new ResultsCellRenderer(mod);
    }
}

