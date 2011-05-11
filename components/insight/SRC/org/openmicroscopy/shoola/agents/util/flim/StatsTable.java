/*
 * org.openmicroscopy.shoola.agents.util.flim.StatsTable 
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
package org.openmicroscopy.shoola.agents.util.flim;

//Java imports
import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JTable;

//Third-party libraries

//Application-internal dependencies

/**
 * Component displaying the histogram.
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
public class StatsTable
	extends JPanel
{
	/** The number of rows in the table by default. */
	private final static int NUMROWS = 3;

	/** The number of columns in the table by default. */
	private final static int NUMCOLS = 3;
	
	/** The table the StatsTable aggregates. */
	JTable statsTable;
	
	/** The table model. */
	StatsTableModel tableModel;
	
	/**
	 * Instantiate the table.
	 */
	public StatsTable()
	{
		initComponents();
		buildUI();
	}
	
	/**
	 * Build the components.
	 */
	private void initComponents()
	{
		tableModel = new StatsTableModel(NUMROWS);
		statsTable = new JTable(tableModel);
	}

	/**
	 * Build the UI
	 */
	private void buildUI()
	{
		this.setLayout(new BorderLayout());
		this.add(statsTable, BorderLayout.CENTER);
	}
	
	/**
	 * Add new row to the table.
	 * @param data  See above.
	 */
	public void insertData(RowData data) 
	{
		tableModel.insertData(data);
	}
	
}
