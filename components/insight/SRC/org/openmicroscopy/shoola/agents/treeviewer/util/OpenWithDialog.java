/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.OpenWithDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.env.data.model.ApplicationData;
import org.openmicroscopy.shoola.util.ui.TitlePanel;

/** 
 * Selects the application to open the document with.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OpenWithDialog 
	extends JDialog
	implements ActionListener
{

	/**
	 * Bound property indicating to open the document with the selected 
	 * application.
	 */
	public static final String OPEN_DOCUMENT_PROPERTY = "openDocument";
	
	/** The default title. */
	private static final String TITLE = "Choose Application";
	
	/** The default text. */
	private static final String TEXT = 
		"Choose an application to open the document ";
	
	/** The chooser. */
	private JFileChooser chooser;
	
	/** Opens the document with the selected application. */
	private void open()
	{
		try {
			ApplicationData data = new ApplicationData(chooser.getSelectedFile());
			firePropertyChange(OPEN_DOCUMENT_PROPERTY, null, data);
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/** Closes and disposes. */
	private void close()
	{
		setVisible(false);
		dispose();
	}
	
	/** 
	 * Initializes the components. 
	 * 
	 * @param directory The default directory.
	 */
	private void initComponents(String directory)
	{
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if (directory != null && directory.length() > 0)
			chooser.setCurrentDirectory(new File(directory));
		chooser.setApproveButtonText("Open");
		chooser.setApproveButtonToolTipText("Open the document with the " +
				"selected application");
		chooser.addActionListener(this);
	}
	
	/**
	 * Builds and lays out the UI.
	 * 
	 * @param name The name of the document to open.
	 */
	private void buildGUI(String name)
	{
		IconManager icons = IconManager.getInstance();
		TitlePanel tp = new TitlePanel(TITLE, TEXT+name, 
				icons.getIcon(IconManager.APPLICATION_48));
		Container c = getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		c.add(tp, BorderLayout.NORTH);
		c.add(chooser, BorderLayout.CENTER);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The parent of the dialog.
	 * @param directory The default directory.
	 * @param name 		The name of the document to open.
	 */
	public OpenWithDialog(JFrame parent, String directory, String name)
	{
		super(parent);
		initComponents(directory);
		buildGUI(name);
		pack();
	}

	/**
	 * Listens to events fired by the chooser.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		String name = e.getActionCommand();
		if (JFileChooser.APPROVE_SELECTION.equals(name)) open();
		else if (JFileChooser.CANCEL_SELECTION.equals(name)) close();
	}
	
}
