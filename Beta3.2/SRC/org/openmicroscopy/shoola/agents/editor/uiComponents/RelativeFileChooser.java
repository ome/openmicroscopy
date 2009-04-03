 /*
 * uiComponents.RelativeFileChooser 
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
package org.openmicroscopy.shoola.agents.editor.uiComponents;

//Java imports

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.util.FilePathMethods;


/** 
 * A FileChooser dialog that lets users specify that the link should 
 * be Relative to a given file location. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class RelativeFileChooser 
	extends JDialog 
	implements ActionListener {
	
	/**
	 * A checkbox displayed at the bottom of the FileChooser.
	 */
	private JCheckBox relLinkCheckBox;
	
	/**
	 * The path to a directory that you want to link FROM
	 */
	private String originalDirectory;
	
	private boolean relativeLink;
	
	private JFileChooser fc;
	
	private String fileChooserState = JFileChooser.CANCEL_SELECTION;
	
	public RelativeFileChooser(String originalPath) {
		this(null, originalPath, null);
	}
	
	public RelativeFileChooser(JFrame frame, String originalDir,
			String currentFilePath) {
		
		super(frame);
		this.originalDirectory = originalDir;
		
		// Create a file chooser
		fc = new JFileChooser();
		fc.setDialogType(JFileChooser.OPEN_DIALOG);
		fc.addActionListener(this);
		
		if (currentFilePath != null)
			fc.setCurrentDirectory(new File(currentFilePath));
		
		relLinkCheckBox = new JCheckBox("Relative link");
		relLinkCheckBox.setToolTipText("<html>The link to the chosen file will " +
				"be 'relative' to the OMERO.editor file you are editing.<br>" +
				"Choose this option if the location of both files is likely " +
				"to change in a similar way (eg saved in the same folder).");
		relLinkCheckBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (relLinkCheckBox.isSelected() && (originalDirectory == null)){
					JOptionPane.showMessageDialog(RelativeFileChooser.this, 
							"The current file is not\n" +
						"saved, so a relative file path to the chosen file \n" +
						"cannot be determined.", "Current File Not Saved", 
						JOptionPane.ERROR_MESSAGE);
					relLinkCheckBox.setSelected(false);
				}
				relativeLink = relLinkCheckBox.isSelected();
			}});
		

		Container dialogPane = getContentPane();
		dialogPane.setLayout(new BorderLayout());
		dialogPane.add(fc, BorderLayout.CENTER);
		
		dialogPane.add(relLinkCheckBox, BorderLayout.SOUTH);
		
		fc.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}});
		pack();
		setModal(true);
		setLocationRelativeTo(this);
		setVisible(true);
	}
	
	/**
	 * This method takes an absolute file path, converts and saves it as a relative link. 
	 * Updating dataField with this attribute will notify this field, which will display it.
	 */
	public String getPath() {
		
		String filePath;
		File chosenFile = fc.getSelectedFile();
		if (chosenFile == null) return null;
		
		/*
		 * If the dialog was closed by any way other than the OK button,
		 * (which sets the fileChooserState to JFileChooser.APPROVE_SELECTION)
		 * then no file was chosen. Return null. 
		 */
		if (! fileChooserState.equals(JFileChooser.APPROVE_SELECTION)) {
			return null;
		}
		
		String chosenFilePath = chosenFile.getAbsolutePath();
		
		if (relativeLink) {
			/*
			 * Calculate what the relative path is, based on the location of 
			 * the chosen File . 
			 */
			filePath = FilePathMethods.getRelativePathFromAbsolutePath(
					originalDirectory, chosenFilePath);
			return filePath;
		}
		else {
			return chosenFilePath;
		}
	}
	
	public boolean isRelativeLink() {
		return relativeLink;
	}

	/**
	 * Listen for actionEvents from the FileChooser.
	 * Set the fileChooserState to the actionCommand.
	 * The ActionCommand will reveal whether this dialog was closed by 
	 * the "OK" or "Cancel" buttons.
	 */
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource().equals(fc)) {
			fileChooserState = e.getActionCommand();
		}
	}

}
