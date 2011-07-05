/*
 * org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser 
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
package org.openmicroscopy.shoola.util.ui.filechooser;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

//Third-party libraries
import sun.awt.shell.ShellFolder;

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;


/** 
 * Sets a new file system view.
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
public class GenericFileChooser 
	extends JFileChooser
{

	/** The selection box used to set the directory.*/
	private JComboBox box;
	
	/** The listener to the box.*/
	private ActionListener listener;
	
	/** 
	 * Handles the file selection via the selection box.
	 * 
	 * @param f The file to handle.
	 */
	private void handleFileSelection(File f)
	{
		if (box == null || f == null) return;
		if ((f instanceof ShellFolder && ((ShellFolder) f).isLink()) ||
				f.getName().endsWith(".lnk")) {
			JOptionPane.showMessageDialog(null, "Cannot use shortcut " +
					"from selection box.");
			box.setSelectedItem(f.getParentFile());
		}
	}

	/** Initializes the component.*/
	private void initialize()
	{
		box = (JComboBox) UIUtilities.findComponent(this, JComboBox.class);
		if (box != null) {
			listener = new ActionListener() {
				
				public void actionPerformed(ActionEvent e) {
					JComboBox b = (JComboBox) e.getSource();
					if (b == box) {
						Object selected = b.getSelectedItem();
						if (selected != null && selected instanceof File)
							handleFileSelection((File) selected);
					}
				}
			};
			box.addActionListener(listener);
		}
	}

	/** Creates a new instance.*/
	public GenericFileChooser()
	{
		super();
		initialize();
	}

	/**
	 * Overridden to add/remove the listener
	 * @see JFileChooser#setCurrentDirectory(File)
	 */
	public void setCurrentDirectory(File directory)
	{
		try {
			if (box != null) box.removeActionListener(listener);
			super.setCurrentDirectory(directory);
			if (box != null) box.addActionListener(listener);
		} catch (Exception e) {
			setCurrentDirectory(directory.getParentFile());
		}
	}

}
