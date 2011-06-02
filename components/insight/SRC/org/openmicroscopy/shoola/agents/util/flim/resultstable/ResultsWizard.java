
/*
 * org.openmicroscopy.shoola.agents.util.flim.resultstable.ResultsWizard 
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
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

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
class ResultsWizard
	extends JDialog 
	implements ActionListener
{

	/** Action command ID to add a columns to the result table. */
	private static final int ADD = 0;

	/** Action command ID to remove a columns from the result table. */
	private static final int REMOVE = 1;

	/** Action command ID to add all columns to the result table. */
	private static final int ADD_ALL = 2;

	/** Action command ID to remove all columns from the result table. */
	private static final int REMOVE_ALL = 3;

	/** Action command ID to Accept the current columns selection. */
	private static final int ACCEPT = 4;

	/** Action command ID to cancel the wizard. */
	private static final int CANCEL = 5;

	/** Action command ID to reset the current columns selection. */
	private static final int RESET = 6;

	/** Collection of the currently selected columns. */
	private List<String>			currentColumns;

	/** Collection of all the possible columns. */
	private List<String>			allColumns;

	/** The original selection of columns, used by the reset button. */
	private List<String>  			originalColumns;

	/** The list box showing the current columns */
	private JList					currentColumnsList;

	/** The list box showing the remaining columns */
	private JList					remainingColumnsList;

	/** 
	 * The button to move a columns from the remaining columns to current columns. 
	 */
	private JButton 				addColumnButton;

	/** 
	 * The button to move a columns from the current columns to remaining columns. 
	 */
	private JButton 				removeColumnButton;

	/** The button to move all columns to the current columns. */
	private JButton 				addAllColumnsButton;

	/** The button to move all columns to the remaining columns. */
	private JButton 				removeAllColumnsButton;

	/** The button to accept current selection. */
	private JButton 				acceptButton;

	/** The button to accept current selection. */
	private JButton 				resetButton;

	/** The button to cancel current selection. */
	private JButton 				cancelButton;

	/** Initializes the components composing the display. */
	private void initComponents()
	{
		currentColumnsList = new JList();
		remainingColumnsList = new JList();
		createButtons();
		addActionListeners();
	}

	/**
	 * Resets the current selection to the original selection set when wizard 
	 * called.
	 */
	private void resetSelection()
	{
		currentColumns.clear();
		for (String column : originalColumns)
			currentColumns.add(column);
	}

	/**
	 * Creates a copy of the original selection set so it can be reset by user. 
	 */
	private void createOriginalSelection()
	{
		originalColumns = new ArrayList<String>();
		for (String column : currentColumns)
			originalColumns.add(column);
	}

	/** Adds action listeners to the buttons. */
	private void addActionListeners()
	{
		addColumnButton.setActionCommand(""+ADD);
		addColumnButton.addActionListener(this);
		addAllColumnsButton.setActionCommand(""+ADD_ALL);
		addAllColumnsButton.addActionListener(this);
		removeColumnButton.setActionCommand(""+REMOVE);
		removeColumnButton.addActionListener(this);
		removeAllColumnsButton.setActionCommand(""+REMOVE_ALL);
		removeAllColumnsButton.addActionListener(this);
		acceptButton.setActionCommand(""+ACCEPT);
		acceptButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);
		resetButton.setActionCommand(""+RESET);
		resetButton.addActionListener(this);
	}

	/**
	 * This method is invoked when the user clicks the addColumnButton.
	 * It adds a column to the list and then sorts the list to maintain order. 
	 */
	private void addColumn()
	{
		if (remainingColumnsList.getSelectedIndex() == -1)
			return;
		int [] indexes = remainingColumnsList.getSelectedIndices();
		DefaultListModel model = 
			(DefaultListModel) remainingColumnsList.getModel();
		String columnName;
		for (int i = 0 ; i < indexes.length ; i++)
		{
			columnName = (String) model.getElementAt(indexes[i]);
			currentColumns.add(columnName);
		}
		sortCurrentColumns();
		populateCurrentColumnsList();
		populateRemainingColumnsPanel();
	}

	/**
	 * Sorts the selection list of the user so that the order is preseved between
	 * pairs of attributes (CentreX, and CentreY).
	 */
	private void sortCurrentColumns()
	{
		ArrayList<String> 
		sortedList = new ArrayList<String>();
		for (String column : allColumns)
		{
			for (String currentColumn : currentColumns)
			{
				if (currentColumn.equals(column))
					sortedList.add(currentColumn);
			}
		}
		currentColumns.clear();
		currentColumns.addAll(sortedList);	
	}

	/** Adds all the columns to the list. */
	private void addAllColumns()
	{
		currentColumns.clear();
		for (String column: allColumns)
			currentColumns.add(column);
		populateCurrentColumnsList();
		populateRemainingColumnsPanel();
	}

	/** Removes a single column from the list. */
	private void removeColumn()
	{
		if (currentColumnsList.getSelectedIndex() == -1) return;
		DefaultListModel model = (DefaultListModel)
		currentColumnsList.getModel();
		int [] indexes = currentColumnsList.getSelectedIndices();
		String columnName;
		for (int i = 0 ; i < indexes.length ; i++)
		{
			columnName = (String) model.getElementAt(indexes[i]);
			for (String column : currentColumns)
			{
				if (column.equals(columnName))
				{
					currentColumns.remove(column);
					break;
				}
			}
		}
		sortCurrentColumns();	
		populateCurrentColumnsList();
		populateRemainingColumnsPanel();
	}

	/** Removes all columns from the list. */
	private void removeAllColumns()
	{
		currentColumns.clear();
		populateCurrentColumnsList();
		populateRemainingColumnsPanel();
	}

	/** 
	 * The user has clicked the cancel button, resets selection to the original
	 * selection and closes the window. 
	 */
	private void cancelButtonClicked()
	{
		this.resetSelection();
		acceptButtonClicked();
	}

	/** The user has accepted the new selection, close window. */
	private void acceptButtonClicked()
	{
		setVisible(false);
		this.dispose();
	}

	/** Resets the selection to the original selection. */
	private void resetButtonClicked()
	{
		this.resetSelection();
		populateCurrentColumnsList();
		populateRemainingColumnsPanel();
	}

	/** Creates all the buttons in the UI. */
	private void createButtons()
	{
		addColumnButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.RIGHT_ARROW_22));
		removeColumnButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.LEFT_ARROW_22));
		addAllColumnsButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.DOUBLE_RIGHT_ARROW_22));
		removeAllColumnsButton = new JButton(IconManager.getInstance().
				getIcon(IconManager.DOUBLE_LEFT_ARROW_22));
		acceptButton = new JButton("Accept");
		cancelButton = new JButton("Cancel");
		resetButton = new JButton("Reset");
		getRootPane().setDefaultButton(acceptButton);
	}

	/** Builds and lays out the UI. */
	private void buildUI()
	{
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
		JPanel leftPanel = createRemainingColumnsPanel();
		JPanel buttonPanel = createButtonPanel();
		JPanel rightPanel = createCurrentColumnsPanel();
		container.add(Box.createHorizontalStrut(10));
		container.add(leftPanel);
		container.add(Box.createHorizontalStrut(20));
		container.add(buttonPanel);
		container.add(Box.createHorizontalStrut(20));
		container.add(rightPanel);
		container.add(Box.createHorizontalStrut(10));

		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel("Results Wizard", 
				"Select the values you wish to record.",	
				icons.getIcon(IconManager.WIZARD_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(tp, BorderLayout.NORTH);
		c.add(container, BorderLayout.CENTER);
		c.add(createControlPanel(), BorderLayout.SOUTH);
	}

	/**
	 * Creates the control panel which has the buttons for the accepting, 
	 * cancelling and resetting of selections.
	 * 
	 * @return See above.
	 */
	private JPanel createControlPanel()
	{
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new FlowLayout());
		controlPanel.add(resetButton);
		controlPanel.add(acceptButton);
		controlPanel.add(cancelButton);
		return controlPanel;
	}

	/**
	 * Creates the remaining columns panel, which shows the columns which have
	 * not been selected.
	 * 
	 * @return See above.
	 */
	private JPanel createRemainingColumnsPanel()
	{
		JPanel columnsPanel = new JPanel();
		columnsPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(remainingColumnsList);
		columnsPanel.add(new JLabel("Unused Values:"), BorderLayout.NORTH);
		columnsPanel.add(pane, BorderLayout.CENTER);
		populateRemainingColumnsPanel();
		return columnsPanel;
	}

	/**
	 * Creates the central button panel hosting the add and remove selection 
	 * buttons.
	 * 
	 * @return See above.
	 */
	private JPanel createButtonPanel()
	{
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
		buttonPanel.add(Box.createVerticalStrut(30));
		buttonPanel.add(addColumnButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeColumnButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(addAllColumnsButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(removeAllColumnsButton);
		buttonPanel.add(Box.createVerticalStrut(10));
		return buttonPanel;
	}

	/**
	 * Creates the current columns panel, which shows the columns which have
	 * been selected.
	 * 
	 * @return See above.
	 */
	private JPanel createCurrentColumnsPanel()
	{
		JPanel columnsPanel = new JPanel();
		columnsPanel.setLayout(new BorderLayout());
		JScrollPane pane = new JScrollPane(currentColumnsList);
		columnsPanel.add(new JLabel("Selected Values:"), 
				BorderLayout.NORTH);
		columnsPanel.add(pane, BorderLayout.CENTER);
		populateCurrentColumnsList();
		return columnsPanel;
	}

	/** Updates the currentColumns list box. */
	private void populateCurrentColumnsList()
	{
		DefaultListModel listModel = new DefaultListModel();
		for (String column : currentColumns)
			listModel.addElement(column);
		currentColumnsList.setModel(listModel);
	}

	/** Updates the remaining columns list box. */
	private void populateRemainingColumnsPanel()
	{
		DefaultListModel listModel = new DefaultListModel();

		boolean found;
		for (String allColumn : allColumns)
		{
			found = false;
			for (String currentColumn : currentColumns)
				if (currentColumn.
						equals(allColumn))
					found = true;
			if (!found)
				listModel.addElement(allColumn);
		}
		remainingColumnsList.setModel(listModel);
	}

	/**
	 * Creates a new instance. 
	 * 
	 * @param cColumns The initial list of columns.
	 * @param aColumns The initial list of columns.
	 */
	ResultsWizard(List<String> cColumns, 
			List<String> aColumns)
	{
		currentColumns = cColumns;
		allColumns = aColumns;
		createOriginalSelection();
		initComponents();
		buildUI();
		setAlwaysOnTop(true);
		setModal(true);
	}

	/**
	 * Get the selected columns.
	 * @return See above.
	 */
	public List<String> getSelectedColumns()
	{
		return currentColumns;
	}
	
	/**
	 * Reacts to event fired by the various controls.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
		int id = -1;
		try
		{
			id = Integer.parseInt(evt.getActionCommand());
			switch (id)
			{
			case ADD:
				addColumn();
				break;
			case ADD_ALL:
				addAllColumns();
				break;
			case REMOVE:
				removeColumn();
				break;
			case REMOVE_ALL:
				removeAllColumns();
				break;
			case ACCEPT:
				acceptButtonClicked();
				break;
			case CANCEL:
				cancelButtonClicked();
				break;
			case RESET:
				resetButtonClicked();
				break;
			default:
				break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

}


