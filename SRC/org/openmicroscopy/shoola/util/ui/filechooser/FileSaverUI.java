/*
 * org.openmicroscopy.shoola.util.ui.filechooser.FileSaverUI 
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.UIManager;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.IconManager;
import org.openmicroscopy.shoola.agents.imviewer.util.saver.ImgSaver;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The UI delegate.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class FileSaverUI
	implements ActionListener, PropertyChangeListener
{

	/** The tool tip of the <code>Approve</code> button. */
	static final String 			SAVE_AS = "Save the current file in" +
												" the specified format.";  
	
	/** The tool tip of the <code>Approve</code> button. */
	static final String 			LOAD = "Load the current file in" +
											" the specified format.";  
	
	/** 
	 * The size of the invisible components used to separate buttons
	 * horizontally.
	 */
	private static final Dimension	H_SPACER_SIZE = new Dimension(3, 10);

	/** Action id indicating to cancel and close. */
	private static final int		CANCEL = 0;

	/** Action id indicating to approve selection. */
	private static final int		APPROVE = 1;

	/** Action id indicating to create a new folder. */
	private static final int		NEW_FOLDER = 2;
	
	/** Reference to the {@link ImgSaver}. */
	private FileChooser                 model;

	/** Reference to the file chooser. */
	private CustomizedFileChooser		chooser;

	/** Box to save the current directory as default. */
	private JCheckBox					settings;

	/** 
	 * Replaces the <code>CancelButton</code> provided by the 
	 * {@link JFileChooser} class. 
	 */
	private JButton						cancelButton;

	/** 
	 * Replaces the <code>ApproveButton</code> provided by the 
	 * {@link JFileChooser} class. 
	 */
	private JButton						approveButton;
	
	/** 
	 * Replaces the <code>NewFolder</code> provided by the 
	 * {@link JFileChooser} class. 
	 */
	private JButton						newFolderButton;

	/** The panel hosting the buttons. */
	private JPanel						buttonPanel;
	
	/** Initializes the component composing the display. */
	private void initComponents()
	{
		chooser = new CustomizedFileChooser(model, this);
		settings = new JCheckBox();
		settings.setText("Set the current directory as default.");
		settings.setSelected(true);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand(""+CANCEL);
		newFolderButton = new JButton("New Folder");
		newFolderButton.setToolTipText(
    			UIUtilities.formatToolTipText("Create a new folder"));
		newFolderButton.addActionListener(this);
		newFolderButton.setActionCommand(""+NEW_FOLDER);
		
		switch (model.getDialogType()) {
			case FileChooser.SAVE:
				approveButton = new JButton("Save as");
				approveButton.setToolTipText(
						UIUtilities.formatToolTipText(SAVE_AS));
				break;
			case FileChooser.LOAD:
				approveButton = new JButton("Load");
				approveButton.setToolTipText(
						UIUtilities.formatToolTipText(LOAD));
		}
		chooser.setApproveButtonText(approveButton.getText());
		chooser.setApproveButtonToolTipText(approveButton.getToolTipText());
		approveButton.addActionListener(this);
		approveButton.setActionCommand(""+APPROVE);
		approveButton.setEnabled(false);
		model.getRootPane().setDefaultButton(approveButton);
	}

	/**
	 * Adds the passed button to the {@link #buttonPanel}.
	 * 
	 * @param button	The button to add.
	 * @param location	The location of the button.
	 */
	private void fillControlButtonsPanel(JButton button, int location)
	{
		buttonPanel.removeAll();
		if (button == null) {
			buttonPanel.add(cancelButton);
			buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
			buttonPanel.add(approveButton);
			return;
		}
		switch (location) {
			case FileChooser.LEFT:
				buttonPanel.add(button);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(cancelButton);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(approveButton);
				break;
			case FileChooser.CENTER:
				buttonPanel.add(cancelButton);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(button);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(approveButton);
				break;
			case FileChooser.RIGHT:
				buttonPanel.add(cancelButton);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(approveButton);
				buttonPanel.add(Box.createRigidArea(H_SPACER_SIZE));
				buttonPanel.add(button);
				break;
		}
	}
	
	/**
	 * Builds the tool bar.
	 * 
	 * @return See above
	 */
	private JPanel buildToolbar()
	{
		buttonPanel = new JPanel();
		buttonPanel.setBorder(null);
		fillControlButtonsPanel(null, -1);
		JPanel controls = new JPanel();
    	controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
    	controls.add(Box.createRigidArea(new Dimension(20, 5)));
    	controls.add(newFolderButton);
    	JPanel p = UIUtilities.buildComponentPanelRight(buttonPanel);
        p.setOpaque(true);
        controls.add(p);
        return controls;
	}

	/**
	 * Builds the UI component displaying the saving options.
	 * 
	 * @return See above.
	 */
	private JPanel buildSelectionPane()
	{
		JPanel p = new JPanel();
		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.X_AXIS));
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(result);
		p.add(UIUtilities.buildComponentPanel(settings));
		return p;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		JPanel controls = new JPanel();
		controls.setLayout(new BorderLayout(0, 0));
		if (!chooser.areControlButtonsShown())
			controls.add(buildToolbar(), BorderLayout.CENTER);
		controls.add(buildSelectionPane(), BorderLayout.SOUTH);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout(0, 0));
		p.add(chooser, BorderLayout.CENTER);
		p.add(controls, BorderLayout.SOUTH);
		IconManager im = IconManager.getInstance();
		Container c = model.getContentPane();
		c.setLayout(new BorderLayout(0, 0));
		TitlePanel tp = new TitlePanel(model.getTitle(), model.getNote(), 
				im.getIcon(IconManager.SAVE_BIG));

		c.add(tp, BorderLayout.NORTH);
		c.add(p, BorderLayout.CENTER);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = 
				UIManager.getLookAndFeel().getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				model.getRootPane().setWindowDecorationStyle(
						JRootPane.FILE_CHOOSER_DIALOG);
		}
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param save is this a save dialog.
	 * @param model Reference to the Model. Mustn't be <code>null</code>.
	 */
	FileSaverUI(FileChooser model)
	{ 
		if (model == null) throw new IllegalArgumentException("No model.");
		this.model = model;
		initComponents();
		buildGUI();
	}

	/**
	 * Set the current directory of the file chooser. 
	 * @param directory see above.
	 */
	void setCurrentDirectory(File directory)
	{
		chooser.setCurrentDirectory(directory);
	}

	/**
	 * Set the current file of the file chooser. 
	 * @param file see above.
	 */
	void setSelectedFile(File file)
	{
		chooser.setSelectedFile(file);
	}

	/**
	 * Returns the pathname of the current directory.
	 *
	 * @return  The directory path.
	 */
	File getCurrentDirectory()
	{ 
		return chooser.getCurrentDirectory(); 
	}

	/**
	 * Returns the pathname of the current file.
	 *
	 * @return  The file path.
	 */
	File getSelectedFile()
	{ 
		return chooser.getSelectedFile(); 
	}

	/**
	 * Returns <code>true</code> if the default folder is set when
	 * saving the file, <code>false</code> toherwise.
	 * 
	 * @return See above.
	 */
	boolean isSetDefaultFolder() { return settings.isSelected(); }

	/**
	 * Sets the <code>enabled</code> flag of not the <code>Save</code> and
	 * <code>Preview</code> options.
	 * 
	 * @param b The value to set.
	 */
	void setControlsEnabled(boolean b)
	{
		approveButton.setEnabled(b);
	}

	/**
     * Sets the text of the <code>Approve</code> button.
     * 
     * @param text The value to set.
     */
	void setApproveButtonText(String text)
	{
		approveButton.setText(text);
		chooser.setApproveButtonText(text);
	}
	
	/**
     * Sets the tooltip text of the <code>Approve</code> button.
     * 
     * @param text The value to set.
     */
	void setApproveButtonToolTipText(String text)
	{
		approveButton.setToolTipText(text);
		chooser.setToolTipText(text);
	}
	
	/**
     * Adds the passed button to add to the control.
     * 
     * @param button	The button to add.
     * @param location	The location of the button.
     */
    void addControlButton(JButton button, int location)
    {
    	if (button == null) 
    		throw new IllegalArgumentException("Button cannot be null.");
    	switch (location) {
			case FileChooser.LEFT:
			case FileChooser.RIGHT:
			case FileChooser.CENTER:
				break;
			default:
				throw new IllegalArgumentException("Location not supported.");
		}
    	fillControlButtonsPanel(button, location);
    	buttonPanel.repaint();
    }
    
	/**
	 * Reacts to click on buttons.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case CANCEL:
				chooser.cancelSelection();
				break;
			case APPROVE:
				chooser.approveSelection();
				break;
			case NEW_FOLDER:
				CreateFolderDialog d = new CreateFolderDialog(model);
				d.addPropertyChangeListener(
						CreateFolderDialog.CREATE_FOLDER_PROPERTY, this);
				d.pack();
				UIUtilities.centerAndShow(model, d);
				break;
		}
	}

	/**
	 * Listens to the property fired by the folder dialog.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = (String) evt.getNewValue();
		chooser.createFolder(name);
	}

}



