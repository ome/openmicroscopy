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
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
	
	/** 
	 * Builds and lays out the UI.
	 * 
	 * @param location The component displaying the option.
	 */
	private void buildGUI(JComponent location)
	{
		Container c = getContentPane();
		c.add(location, BorderLayout.CENTER);
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
		setResizable(false);
		setTitle(TITLE);
		initComponents();
		buildGUI(location);
		pack();
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
	
}
