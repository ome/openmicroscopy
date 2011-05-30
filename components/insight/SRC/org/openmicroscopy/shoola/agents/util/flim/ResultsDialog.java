/*
 * org.openmicroscopy.shoola.env.ui.ResultsDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.flim.resultstable.ResultsTable;


/** 
 * Display the ResultsTable with the associated buttons around it.
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
public class ResultsDialog
	extends JPanel 
	implements ActionListener
{
	
	/** The action to save the results. */
	protected final static int SAVEACTION = 0;
	
	/** The action to load results from csv file. */
	protected final static int LOADACTION = 1;
	
	/** The results wizard action. */
	protected final static int WIZARDACTION = 2;
	
	/** The clear table action. */
	protected final static int CLEARACTION = 3;

	/** The save button. */
	protected JButton saveButton;
	
	/** The save button. */
	protected JButton clearButton;
	
	/** The save button. */
	protected JButton removeButton;
	
	/** The wizard Button.*/
	protected JButton wizardButton;
	
	/** The load button. */
	protected JButton loadButton;
	
	/** The list of buttons and their visibility. */
	protected Map<JButton, Boolean> buttonVisibility;
	
	protected List<String> columnNames;
	
	protected ResultsTable resultsTable;
	
	ResultsDialog(List<String> columnNames)
	{
		this.columnNames = columnNames;
		initComponents();
		buildUI();
	}
	
	
	private void initComponents()
	{
		resultsTable = new ResultsTable(columnNames);
		createButtons();
	}
	
	
	
	/**
	 * Create the buttons on the table.
	 */
	protected void createButtons()
	{
		buttonVisibility = new LinkedHashMap<JButton, Boolean>();
		saveButton = createButton("Save", "Save results to CSV file.",SAVEACTION, true);
		loadButton = createButton("Load", "Load results from CSV file.",LOADACTION, true);
		clearButton = createButton("Clear", "Clear the table",CLEARACTION, true);
		wizardButton = createButton("Wizard", "Display the results wizard.",WIZARDACTION, true);
	}
	
	/**
	 * Create a button with name, tooltip and actionCommand, add table as listener to button.
	 * @param name See above.
	 * @param tooltop See above.
	 * @param actionCommand See above.
	 * @return See above.
	 */
	protected JButton createButton(String name, String tooltop, Integer actionCommand, boolean isVisible)
	{
		JButton button = new JButton(name);
		button.setToolTipText(tooltop);
		button.setActionCommand(actionCommand+"");
		button.addActionListener(this);
		buttonVisibility.put(button, isVisible);
		return button;
	}
	
	private void buildUI()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(resultsTable);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		Iterator<JButton> keyIterator = buttonVisibility.keySet().iterator();
		while(keyIterator.hasNext())
		{
			JButton button = keyIterator.next();
			if(buttonVisibility.get(button))
				buttonPanel.add(button);
		}
		add(buttonPanel);
	}


	/**
	 * Overrides {@link ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getActionCommand()==null) return;
		int command=Integer.parseInt(e.getActionCommand());
		
		switch(command)
		{
		case CLEARACTION:
			break;
		case SAVEACTION:
			break;
		case LOADACTION:
			break;
		case WIZARDACTION:
			break;
		}
	}
}
