/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.LocationDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies

/** 
 * Displays the possible location of the imports.
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
class LocationDialog 
	extends JDialog
	implements ActionListener
{
	/** The message to display in the header. */
	private static final String MESSAGE_LOCATION = "Select where to import the data";

	/** The message to display in the header. */
	private static final String MESSAGE_GROUP = "Group";
	
	/** User has selected to add the files. */
	public final static int			ADD_OPTION = 1;

	/** User has selected to cancel. */
	public final static int			CANCEL_OPTION = 0;
	
	/** The title of the dialog.*/
	private static String TITLE = "Location selection";
	
	/** Component indicating to add to the queue.*/
	private JButton addButton;
	
	/** Component indicating to cancel the addition.*/
	private JButton cancelButton;
	
	/** Option chosen by the user.*/
	private int option;
	
	/** The component used to select the group. */
	private JComboBox groupSelection;
	
	/** Initializes the components.*/
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close and do not add the files to the " +
				"queue.");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL_OPTION);
		addButton = new JButton("Add to the Queue");
		addButton.setToolTipText("Add the files to the queue.");
		addButton.addActionListener(this);
		addButton.setActionCommand(""+ADD_OPTION);
		getRootPane().setDefaultButton(addButton);
	}
	
	/**
	 * Builds and lays out the UI.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolbar()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		bar.add(addButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(cancelButton);
		return bar;
	}
	
	
	private JPanel layoutMainPanel()
	{
		JPanel locationPane = new JPanel();
		locationPane.setLayout(new BorderLayout());
		
		JTabbedPane tabPane = new JTabbedPane();
		
		IconManager icons = IconManager.getInstance();
		
		Icon projectIcon = icons.getIcon(IconManager.PROJECT);
		JPanel projectPanel = createProjectPanel();
		
		Icon screenIcon = icons.getIcon(IconManager.PROJECT);
		JPanel screenPanel = createScreenPanel();

		tabPane.addTab("Projects", projectIcon, projectPanel,
		                  "Import settings for Projects");
		
		tabPane.addTab("Screens", screenIcon, screenPanel,
                "Import settings for Screens");
		
		JPanel groupPane = new JPanel();
		groupPane.add(UIUtilities.setTextFont(MESSAGE_GROUP), BorderLayout.WEST);
		groupPane.add(groupSelection, BorderLayout.CENTER);
		
		locationPane.add(groupPane, BorderLayout.NORTH);
		locationPane.add(tabPane, BorderLayout.CENTER);
		
		return locationPane;
	}
	
	private JPanel createProjectPanel() {
		JPanel projectPanel = new JPanel();
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));
		
		JPanel projectRow = createRow(null);
		projectRow.add(UIUtilities.setTextFont(PROJECT_TXT));
		projectRow.add(projectsBox);
		projectRow.add(addProjectButton);
		
		projectPanel.add(projectRow);
		projectPanel.add(Box.createVerticalStrut(8));
		
		JPanel dataSetRow = createRow(null);
		dataSetRow.add(UIUtilities.setTextFont(DATASET_TXT));
		dataSetRow.add(datasetsBox);
		dataSetRow.add(addDatasetButton);
		
		projectPanel.add(dataSetRow);
		projectPanel.add(new JSeparator());
		
		return projectPanel;
	}
	
	private JPanel createScreenPanel() {
		JPanel screenPanel = new JPanel();
		screenPanel.setLayout(new BoxLayout(screenPanel, BoxLayout.Y_AXIS));
		
		JPanel screenRow = createRow(null);
		screenRow.add(UIUtilities.setTextFont(SCREEN_TXT));
		screenRow.add(screensBox);
		screenRow.add(addScreenButton);

		screenPanel.add(screenRow);
		screenPanel.add(new JSeparator());
		
		return screenPanel;
	}
	
	/** 
	 * Builds and lays out the UI.
	 * 
	 * @param location The component displaying the option.
	 */
	private void buildGUI(JComponent location)
	{
		Container c = getContentPane();
		c.add(layoutMainPanel(), BorderLayout.CENTER);
		c.add(buildToolbar(), BorderLayout.SOUTH);
	}
	
	/** Closes the dialog.*/
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent The parent of the dialog.
	 * @param location The component displaying the option.
	 */
	LocationDialog(JFrame parent, JComponent location)
	{
		super(parent);
		setModal(true);
		setTitle(TITLE);
		initComponents();
		buildGUI(location);
		pack();
		
		int minHeight = this.getHeight();
		int minWidth = this.getWidth();
		Dimension minimumSize = new Dimension(minWidth, minHeight);
		
		setMinimumSize(minimumSize);
	}

    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @return The option selected by the user. 
     */
    int centerLocation()
    {
    	UIUtilities.centerAndShow(this);
    	return option;
    }
   
    /**
     * Shows the message box and returns the option selected by the user. 
     * 
     * @param location The location of the top-left corner of the dialog.
     * @return The option selected by the user. 
     */
    int showLocation(Point location)
    {
    	setLocation(location);
    	setVisible(true);
    	return option;
    }
    
	/**
	 * Sets the option.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		option = Integer.parseInt(e.getActionCommand());
		close();
	}

	public ImportLocationSettings getImportSettings() {
		// TODO Auto-generated method stub  21 Nov 2012 12:43:08 scott
		return null;
	}
	
}
