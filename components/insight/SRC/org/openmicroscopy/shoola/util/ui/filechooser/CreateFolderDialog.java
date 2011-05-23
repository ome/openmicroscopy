/*
 * org.openmicroscopy.shoola.util.ui.filechooser.CreateFolderDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.filechooser;


//Java imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Replaces the <code>New Folder</code> dialog.
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
public class CreateFolderDialog 
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to create a new folder. */
	public static final String		CREATE_FOLDER_PROPERTY = "createFolder";
	
	/** Bound property indicating to cancel. */
	public static final String		CANCEL_FOLDER_PROPERTY = "cancelFolder";
	
	/** The default text displayed in the field. */
	private static final String		DEFAULT_NAME = "untitled folder";
	
	/** The default text. */
	private static final String		DEFAULT_TEXT = "New Folder";
	
	/** 
     * The size of the invisible components used to separate buttons
     * horizontally.
     */
    private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);
    
	/** Action id to cancel and close. */
	private static final int 		CANCEL = 0;
	
	/** Action id to create a new folder. */
	private static final int 		CREATE = 1;
	
	/** Button to cancel and close. */
	private JButton 	cancelButton;
	
	/** Button to create a new folder. */
	private JButton 	createButton;
	
	/** Field hosting the new name. */
	private JTextField	nameField;
	
	/** Fires an event with the name of the folder. */
	private void create()
	{
		String n = nameField.getText();
		if (n == null || n.trim().length() == 0) {
			return;
		}
		firePropertyChange(CREATE_FOLDER_PROPERTY, null, n);
		cancel();
	}
	
	/** Closes and disposes. */
	private void cancel()
	{
		setVisible(false);
		dispose();
		firePropertyChange(CANCEL_FOLDER_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		createButton = new JButton("Create");
		createButton.addActionListener(this);
		createButton.setActionCommand(""+CREATE);
		nameField = new JTextField();
		nameField.setText(DEFAULT_NAME);
		nameField.selectAll();
		addWindowListener(new WindowAdapter()
		{
			public void windowOpened(WindowEvent e) {
				nameField.requestFocus();
			} 
		});
		nameField.addKeyListener(new KeyAdapter() {
			
			/**
			 * Creates a new folder.
			 * @see KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) 
					create();
			}
		});
	}
	
	 /**
     * Builds the tool bar.
     * 
     * @return See above
     */
	private JPanel buildToolBar()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(cancelButton);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		bar.add(createButton);
		bar.add(Box.createRigidArea(H_SPACER_SIZE));
		JPanel p = UIUtilities.buildComponentPanelRight(bar);
        p.setOpaque(true);
        return p;
	}
	
	/** 
	 * Builds and lays out the UI. 
	 * 
	 * @param name The title and text.
	 */
	private void buildGUI(String name)
	{
		String s = "Name of "+name.toLowerCase()+":";
		JLabel label = new JLabel(s);
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(UIUtilities.buildComponentPanel(label));
		p.add(nameField);
		p.add(buildToolBar());
		getContentPane().add(UIUtilities.buildComponentPanel(p), 
							BorderLayout.CENTER);
	}
	
	/** 
	 * Initializes the widget. 
	 * 
	 * @param name The title and text.
	 */
	private void initialize(String name)
	{
		if (name == null || name.length() == 0) name = DEFAULT_TEXT;
		setTitle(name);
		setModal(true);
		setResizable(false);
		initComponents();
		buildGUI(name);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param text  The title and text.
	 */
	public CreateFolderDialog(JDialog owner, String text)
	{
		super(owner);
		initialize(text);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 * @param text  The title and text.
	 */
	public CreateFolderDialog(JFrame owner, String text)
	{
		super(owner);
		initialize(text);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 */
	public CreateFolderDialog(JDialog owner)
	{
		this(owner, DEFAULT_TEXT);
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param owner The owner of the dialog.
	 */
	public CreateFolderDialog(JFrame owner)
	{
		this(owner, DEFAULT_TEXT);
	}
	
	/**
	 * Sets the default text of the name area.
	 * 
	 * @param text The value to set.
	 */
	public void setDefaultName(String text)
	{
		if (text == null || text.trim().length() == 0) return;
		nameField.setText(text);
	}
	
	/**
	 * Reacts to action fired by buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				cancel();
				break;
			case CREATE:
				create();
		}
	}
	
}
