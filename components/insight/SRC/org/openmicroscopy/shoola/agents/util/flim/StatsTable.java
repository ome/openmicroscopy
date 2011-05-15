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
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.jdesktop.swingx.JXTable;

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
	extends JPanel implements ItemListener, ActionListener
{
	/** The number of rows in the table by default. */
	private final static int NUMROWS = 3;

	/** The number of columns in the table by default. */
	private final static int NUMCOLS = 3;
	
	/** The table the StatsTable aggregates. */
	private JXTable statsTable;
	
	/** The table model. */
	private StatsTableModel tableModel;

	/** Button to clear the statsTable.*/
	private JButton clearTableButton;
	
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
		statsTable = new JXTable(tableModel);
		clearTableButton = new JButton("clear");
		clearTableButton.setActionCommand("clear");
		clearTableButton.addActionListener(this);
		statsTable.getTableHeader().setReorderingAllowed(false);
		statsTable.getTableHeader().setVisible(true);
	}

	/**
	 * Build the UI
	 */
	private void buildUI()
	{
		double size[][] = {{0.8,0.2},{0.1, 0.1, 0.1, TableLayout.FILL}};
		this.setLayout(new TableLayout(size));
		this.add(new JScrollPane(statsTable), "0,0,0,3");
		this.add(clearTableButton,"1,1,1,1");
	}
	
	/**
	 * Add new row to the table.
	 * @param data  See above.
	 */
	public void insertData(RowData data) 
	{
		tableModel.insertData(data);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("clear"))
		{
			tableModel.clear();
		}
	}

	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

}
