/*
 * org.openmicroscopy.shoola.util.ui.FileTableNode
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;

//Application-internal dependencies

/**
 * Element to display the status of the upload of a file.
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
public class FileTableNode 
	extends JPanel
{

	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** The object hosting information about the file that failed to import. */
	private ImportErrorObject 		failure;
	
	/** The component displaying the status of the commit. */
	private JXBusyLabel status;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(status);
		add(new JSeparator(JSeparator.VERTICAL));
		if (failure.getFile() != null) {
		    add(new JLabel(failure.getFile().getName()));
		} else {
		    add(new JLabel("Log File ID:"+failure.getLogFileID()));
		}
		add(Box.createHorizontalStrut(15));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param failure The object hosting information about the file that failed 
	 * 				  to import.
	 */
	FileTableNode(ImportErrorObject failure)
	{
		if (failure == null)
			throw new IllegalArgumentException("No Object to send");
		this.failure = failure;
		status = new JXBusyLabel(SIZE);
		buildGUI();
	}
	
	/**
	 * Returns the file hosting by the node.
	 * 
	 * @return See above.
	 */
	public ImportErrorObject getFailure() { return failure; }
	
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
		super.setBackground(color);
	}
	
}
