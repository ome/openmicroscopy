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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

//Third-party libraries
import info.clearthought.layout.TableLayout;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.measurement.IconManager;
import org.openmicroscopy.shoola.agents.util.flim.resultstable.ResultsTable;
import org.openmicroscopy.shoola.util.filter.file.ExcelFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;


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
	implements ActionListener, PropertyChangeListener
{
	/** The text property of the Load button, also used for visibility. */
	public final static String LOAD = "Load";

	/** The text property of the Load button, also used for visibility. */
	public final static String SAVE = "Save";
	
	/** The text property of the Load button, also used for visibility. */
	public final static String CLEAR = "Clear";
	
	/** The text property of the Load button, also used for visibility. */
	public final static String WIZARD = "Wizard";
	
	/** The value for laying out the buttons vertically. */
	public final static int VERTICAL = 0;
	
	/** The value for laying out the buttons horizontally.  */
	public final static int HORIZONTAL = 1;

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
	protected Map<String, Boolean> buttonVisibility;
	
	/** The columns to display. */
	protected List<String> columnNames;
	
	/** The results table that this dialog wraps. */
	protected ResultsTable resultsTable;
	
	/** The orientation of the buttons on the dialog. */
	protected int orient;
	
	/** The map of button names and buttons. */
	protected Map<String, JButton> buttonMap;
	
	/** Display the buttons as icons. */
	protected boolean asIcons;
	
	/** Filters used for the save options. */
	private static List<FileFilter> FILTERS;
	
	static 
	{
		FILTERS = new ArrayList<FileFilter>();
		FILTERS.add(new ExcelFilter());
	}
	
	/**
	 * Instatiate the dialog, with the visible column names.
	 * @param columnNames See above.
	 * @param orient The orientation of the buttons on the table.
	 */
	ResultsDialog(List<String> columnNames, int orient, boolean asIcons, Map<String, Boolean> buttonVisibility)
	{
		this.columnNames = columnNames;
		this.orient = orient;
		this.buttonVisibility = buttonVisibility;
		this.asIcons = asIcons;
		this.buttonMap = new HashMap<String, JButton>();
		initComponents();
		buildUI();
	}
	
	/**
	 * Initialise the components in the dialog.
	 */
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
		saveButton = createButton(SAVE, "Save results to CSV file.", IconManager.getInstance().
				getIcon(IconManager.SAVE_AS),SAVEACTION, true);
		loadButton = createButton(LOAD, "Load results from CSV file.",IconManager.getInstance().
				getIcon(IconManager.LOAD), LOADACTION, true);
		clearButton = createButton(CLEAR, "Clear the table",IconManager.getInstance().
				getIcon(IconManager.DELETE), CLEARACTION, true);
		wizardButton = createButton(WIZARD, "Display the results wizard.",IconManager.getInstance().
				getIcon(IconManager.WIZARD), WIZARDACTION, true);
		buttonMap.put(SAVE, saveButton);
		buttonMap.put(LOAD, loadButton);
		buttonMap.put(CLEAR, clearButton);
		buttonMap.put(WIZARD, wizardButton);
	}
	
	/**
	 * Create a button with name, tooltip and actionCommand, add table as listener to button.
	 * @param name See above.
	 * @param tooltip See above.
	 * @param actionCommand See above.
	 * @return See above.
	 */
	protected JButton createButton(String name, String tooltip, Icon icon, Integer actionCommand, boolean isVisible)
	{
		JButton button;
		if(asIcons)
		{
			button = new JButton(icon);
			UIUtilities.unifiedButtonLookAndFeel(button);
		}	
		else
			button = new JButton(name);
		button.setToolTipText(tooltip);
		button.setActionCommand(actionCommand+"");
		button.addActionListener(this);
		button.setIcon(icon);
		return button;
	}
	
	/**
	 * Build the UI for the dialog.
	 */
	private void buildUI()
	{
		double size[][];
		if(orient==VERTICAL)
		{
			size = new double[][]{{0.95,0.01,0.03},{TableLayout.PREFERRED, TableLayout.PREFERRED}};
			setLayout(new TableLayout(size));
			add(resultsTable,"0,0,0,1");
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
				Iterator<String> keyIterator = buttonVisibility.keySet().iterator();
			while(keyIterator.hasNext())
			{
				String buttonName = keyIterator.next();
				if(buttonVisibility.get(buttonName))
				{
					JButton button = buttonMap.get(buttonName);
					buttonPanel.add(button);
					if(orient==VERTICAL)
						buttonPanel.add(Box.createVerticalStrut(10));
				}
				
			}
			add(buttonPanel,"2,0,2,1");
		}
		else
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(resultsTable);
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new FlowLayout());
			Iterator<String> keyIterator = buttonVisibility.keySet().iterator();
			while(keyIterator.hasNext())
			{
				String buttonName = keyIterator.next();
				if(buttonVisibility.get(buttonName))
				{
					JButton button = buttonMap.get(buttonName);
					buttonPanel.add(button);
				}
				
			}
			add(buttonPanel);
		}
	
	}

	/**
	 * The save button has been pressed, show the save file dialog and then save data.
	 */
	private void saveAction()
	{
		
		FileChooser chooser = new FileChooser(null, FileChooser.SAVE, 
				"Save Results", "Saves the results", FILTERS);
		chooser.setCurrentDirectory(UIUtilities.getDefaultFolder());
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}

	/**
	 * The load button has been pressed, show the load file dialog and then load data.
	 */
	private void loadAction()
	{
		
		FileChooser chooser = new FileChooser(null, FileChooser.LOAD, 
				"Load Results", "Loads the results", FILTERS);
		chooser.setCurrentDirectory(UIUtilities.getDefaultFolder());
		chooser.addPropertyChangeListener(this);
		chooser.centerDialog();
	}
	
	/**
	 * Show the wizard to change selected columns.
	 */
	private void wizardAction()
	{
		resultsTable.showResultsWizard();
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
			resultsTable.clear();
			break;
		case SAVEACTION:
			saveAction();
			break;
		case LOADACTION:
			loadAction();
			break;
		case WIZARDACTION:
			wizardAction();
			break;
		}
	}

	/**
	 * Reacts to property fired by the filechooser. 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if(evt.getSource() instanceof FileChooser)
		{
			FileChooser theSource = (FileChooser)evt.getSource();
			if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) 
			{
				File[] files = (File[]) evt.getNewValue();
				File f = files[0];
				if(theSource.getChooserType()==FileChooser.SAVE)
					resultsTable.saveResults(f);
				else
					resultsTable.loadResults(f);
			}
		}
	}

	/** 
	 * Set the hightlighting scheme of the table.
	 * @param i See above.
	 */
	public void setRowHighlightMod(int i) 
	{
		resultsTable.setRowHighlightMod(i);
	}

	/**
	 * Inset data into the table, this is a key value pair, only columns which are visible will display data.
	 * @param rowData See above.
	 */
	public void insertData(Map<String, Object> rowData) 
	{
		resultsTable.insertData(rowData);
	}
	
	
}
