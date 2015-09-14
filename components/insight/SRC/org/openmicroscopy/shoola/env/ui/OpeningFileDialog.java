/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.env.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.BevelBorder;


import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.FileAnnotationData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since OME3.0
 */
class OpeningFileDialog
	extends JDialog
	implements ActionListener
{

	/** Bound property indicating to save the file to disk .*/
	public static final String	SAVE_TO_DISK_PROPERTY = "saveToDisk";
	
	/** The default title of the window. */
	private static final String TITLE = "Opening ";
	
	/** ID indicating to cancel the action. */
	private static final int	CANCEL = 0;
	
	/** ID indicating to open or save to disk the file. */
	private static final int	OK = 1;
	
	/** The annotation this window is for. */
	private FileAnnotationData 	data;
	
	/** Button to close and dispose of the window. */
	private JButton				cancelButton;
	
	/** Button to download or view the file. */
	private JButton				okButton;
	
	/** Button to view the file. */
	private JRadioButton		openButton;
	
	/** Button to download the file. */
	private JRadioButton		saveButton;
	
	/** Reference to the icons manager. */
	private IconManager			icons;
	
	/** Sets the properties of the window. */
	private void setProperties()
	{
		setTitle(TITLE+data.getFileName());
		setModal(true);
	}
	
	/** Initializes the UI components. */
	private void initComponents()
	{
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		okButton = new JButton("OK");
		okButton.addActionListener(this);
		okButton.setActionCommand(""+OK);
		openButton = new JRadioButton("Open");
		saveButton = new JRadioButton("Save to Disk");
		ButtonGroup group = new ButtonGroup();
		group.add(openButton);
		group.add(saveButton);
		openButton.setSelected(true);
	}
	
	/**
	 * Builds and lays out the body of the dialog.
	 * 
	 * @return See above.
	 */
	private JPanel buildContent()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		JLabel l = UIUtilities.setTextFont("You have chosen to open");
		JPanel row = new JPanel();
		row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
		row.add(new JLabel(icons.getIcon(IconManager.DOCUMENT_12)));
		row.add(Box.createHorizontalStrut(5));
		row.add(UIUtilities.setTextFont(data.getFileName()));
		
		content.add(UIUtilities.buildComponentPanel(l));
		content.add(UIUtilities.buildComponentPanel(row));
		content.add(Box.createVerticalStrut(10));
		l = new JLabel("What should OMERO.insight do with this file?");
		content.add(UIUtilities.buildComponentPanel(l));
		content.add(buildSelectionPane());
		content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return content;
	}
	
	/**
	 * Builds the UI component presenting the choice i.e. download or view 
	 * the file.
	 * 
	 * @return See above.
	 */
	private JPanel buildSelectionPane()
	{
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(openButton);
		content.add(saveButton);
		JPanel p = UIUtilities.buildComponentPanel(content);
		p.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		return p;
	}
	
	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildBar()
	{
		JPanel bar = new JPanel();
		bar.setBorder(null);
		bar.add(cancelButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(okButton);
		return UIUtilities.buildComponentPanelRight(bar);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		Container c = getContentPane();
		c.add(buildContent(), BorderLayout.CENTER);
		c.add(buildBar(), BorderLayout.SOUTH);
	}
	
	/** Closes the window and disposes of it. */
	private void cancel()
	{
		setVisible(false);
		dispose();
	}
	
	/** 
	 * Fires a property to indicate to download the file and closes
	 * the window.
	 */
	private void downloadData()
	{
		firePropertyChange(SAVE_TO_DISK_PROPERTY, null, data);
		cancel();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param owner	The owner of the frame.
	 * @param icons	Reference to the icon manager.
	 * @param data	The objec to display. Mustn't be <code>null</code>.
	 */
	OpeningFileDialog(JFrame owner, IconManager icons, FileAnnotationData data)
	{
		super(owner);
		if (data == null)
			throw new IllegalArgumentException("No file to open.");
		this.icons = icons;
		this.data = data;
		setProperties();
		initComponents();
		buildGUI();
		pack();
	}

	/**
	 * Closes the window or save the file.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				cancel();
				break;
			case OK:
				downloadData();
		}
	}
	
}
