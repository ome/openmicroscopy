/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.GenericDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;


//Java imports
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Modal dialog providing a <code>Save</code> and <code>Cancel</code> buttons.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class GenericDialog 
	extends JDialog
	implements ActionListener
{
	
	/** Bound property indicating to save the data. */
	public static final String		SAVE_GENERIC_PROPERTY = "saveGeneric";
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension  H_SPACER_SIZE = new Dimension(5, 10);
    
	/** Action ID to close the dialog. */
	private static final int 		CANCEL = 0;
	
	/** Action ID to save the data. */
	private static final int 		SAVE = 1;
	
	/** Button to close and dispose of the dialog. */
	private JButton cancelButton;
	
	/** Button to save. */
	private JButton saveButton;
	
	/** The parent embedded in this dialog. */
	private Object	parent;
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close and dispose.");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		saveButton = new JButton("Save");
		saveButton.setToolTipText("Save the changes.");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(""+SAVE);
		getRootPane().setDefaultButton(saveButton);
	}
	
	/** 
	 * Builds and lays out the buttons.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		//bar.setRollover(true);
		//bar.setBorder(null);
		//bar.setFloatable(false);
		bar.add(saveButton);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(cancelButton);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/**
	 * Builds and lays out the UI.
	 * 
	 * @param header The header of the dialog.
	 *  @param body  The main UI component of the dialog.
	 */
	private void buildGUI(TitlePanel header, JComponent body)
	{
		Container c = getContentPane();
		c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
		c.add(header, BorderLayout.NORTH);
		c.add(body, BorderLayout.CENTER);
		c.add(buildToolBar(), BorderLayout.SOUTH);
	}
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the frame.
	 * @param title The title of the frame.
	 */
	public GenericDialog(JFrame owner, String title)
	{
		super(owner);
		setTitle(title);
		setModal(true);
	}

	/**
	 * Initializes the dialog.
	 * 
	 * @param title The title displayed in the header.
	 * @param text  The text displayed in the header.
	 * @param icon  The icon displayed in the header.
	 * @param body  The main UI component of the dialog.
	 */
	public void initialize(String title, String text, Icon icon,
							JComponent body)
	{
		 initComponents();
		 buildGUI(new TitlePanel(title, text, icon), body);
		 setSize(400, 600);
	}

	/** 
	 * Sets the parent embedded in this dialog.
	 * 
	 * @param parent The value to set.
	 */
	public void setParent(Object parent) { this.parent = parent; }
	
	/**
	 * Closes or save the data.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
			switch (index) {
				case CANCEL:
					cancel();
					break;
				case SAVE:
					firePropertyChange(SAVE_GENERIC_PROPERTY, null, parent);
					cancel();
		}
	}
	
}
