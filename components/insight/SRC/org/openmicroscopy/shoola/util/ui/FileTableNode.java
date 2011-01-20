/*
 * org.openmicroscopy.shoola.util.ui.FileTableNode
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;


//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies

/**
 * Element to display the status of the upload of a file.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class FileTableNode 
	extends JPanel
{

	/** The text if no exception are specified. */
	private static final String NO_EXCEPTION = "No exception specified.";
	
	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** The file to send. */
	private File 		file;
	
	/** The exception to send. */
	private Exception 	exception;
	
	/** The component displaying the status of the commit. */
	private JXBusyLabel status;
	
	/** Box to select the file. If user changes his/her mind. */
	private JCheckBox	selected;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(status);
		add(Box.createHorizontalStrut(5));
		add(selected);
		add(new JSeparator(JSeparator.VERTICAL));
		add(new JLabel(file.getName()));
		add(Box.createHorizontalStrut(15));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file The file to display.
	 * @param exception The associated exception.
	 */
	FileTableNode(File file, Exception exception)
	{
		if (file == null)
			throw new IllegalArgumentException("No file");
		this.file = file;
		this.exception = exception;
		status = new JXBusyLabel(SIZE);
		selected = new JCheckBox("Send file");
		selected.setSelected(true);
		//status.setBusy(true);
		buildGUI();
	}
	
	/**
	 * Returns <code>true</code> if the file has to be sent, 
	 * <code>false</code> otherwise.
	 * 
	 * @return
	 */
	boolean isSelected() { return selected.isSelected(); }
	
	/**
	 * Returns the file hosting by the node.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/**
	 * Returns the exception hosting by the node.
	 * 
	 * @return See above.
	 */
	public Exception getException() { return exception; }
	
	/**
	 * Sets the status of the post.
	 * 
	 * @param busy Pass <code>true</code> to indicate an on-going post,
	 * 			   <code>false</code> when it is done.
	 */
	public void setStatus(boolean busy)
	{
		status.setBusy(busy);
		if (!busy) status.setText("done");
	}

	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		if (status != null) status.setBackground(color);
		if (selected != null) selected.setBackground(color);
		super.setBackground(color);
	}
	
}
