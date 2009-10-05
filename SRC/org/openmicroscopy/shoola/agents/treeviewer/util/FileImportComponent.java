/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.FileImportComponent 
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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.ImportManager;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;

import pojos.ImageData;

/** 
 * Component hosting the file to import and displaying the status of the 
 * import process.
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
public class FileImportComponent 
	extends JPanel
	implements ChangeListener
{

	/** Bound property indicating to the file that failed to import. */
	public static final String		SEND_FILE_PROPERTY = "sendFile";
	
	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** The border of the thumbnail label. */
	private static final Border		LABEL_BORDER = 
							BorderFactory.createLineBorder(Color.black, 1);
	
	/** Default text when a failure occurred. */
	private static final String		FAILURE_TEXT = "failed";
	
	/** The file to import. */
	private File 			file;
	
	/** The component indicating the progress of the import. */
	private JXBusyLabel 	busyLabel;
	
	/** The component displaying the file name. */
	private JPanel			nameLabel;

	/** The component allowing to launch the viewer. */
	private JLabel			thumbLabel;

	/** The component displaying the status of the import. */
	private JLabel			status;
	
	/** The default control. */
	private JComponent		control;
	
	/** The manager. */
	private ImportManager	parent;
	
	/** The imported image. */
	private Object			image;
	
	/** The check box displayed if the import failed. */
	private JCheckBox		errorBox;
	
	/** Indicates the status of the on-going import. */
	private StatusLabel		statusLabel;
	
	/** Initializes the components. */
	private void initComponents()
	{
		setLayout(new FlowLayout(FlowLayout.LEFT));
		busyLabel = new JXBusyLabel(SIZE);
		busyLabel.setVisible(true);
		busyLabel.setBusy(false);
		
		nameLabel = new JPanel();
		nameLabel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		IconManager icons = IconManager.getInstance();
		Icon icon;
		if (file.isFile()) icon = icons.getIcon(IconManager.IMAGE);
		else icon = icons.getIcon(IconManager.DIRECTORY);
		nameLabel.add(new JLabel(icon));
		nameLabel.add(Box.createHorizontalStrut(4));
		nameLabel.add(new JLabel(file.getName()));
		nameLabel.add(Box.createHorizontalStrut(10));
		//Dimension d = nameLabel.getPreferredSize();
		//nameLabel.setPreferredSize(new Dimension(d.width, 35));
		thumbLabel = new JLabel();
		thumbLabel.setToolTipText("Click on thumbnail to launch the viewer.");
		thumbLabel.addMouseListener(new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void mousePressed(MouseEvent e) {
				if (image instanceof ThumbnailData) {
					ThumbnailData thumb = (ThumbnailData) image;
					parent.viewImage(thumb.getImage());
				}
			}
		});
		control = busyLabel;
		errorBox = new JCheckBox("Send file");
		errorBox.setOpaque(false);
		errorBox.setToolTipText("Select the file to send.");
		errorBox.addChangeListener(this);
		errorBox.setVisible(false);
		statusLabel = new StatusLabel();
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		add(nameLabel);
		add(control);
		if (statusLabel.isVisible())
			add(statusLabel);
		if (image instanceof ImportException) {
			errorBox.setSelected(true);
			errorBox.setVisible(true);
			add(errorBox);
		}
	}
	
	/**
	 * Sets the text of the {@link #thumbLabel}.
	 * 
	 * @param text The string to set.
	 */
	private void setStatusText(String text)
	{
		if (text == null) text = "";
		text = text.trim();
		if (text.length() == 0) thumbLabel.setText(FAILURE_TEXT);
		else thumbLabel.setText(text);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent 	The manager.
	 * @param file 		The file to import.
	 */
	public FileImportComponent(ImportManager parent, File file)
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified.");
		this.parent = parent;
		this.file = file;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the components displaying the status of an on-going import.
	 * 
	 * @return See above.
	 */
	public StatusLabel getStatus() { return statusLabel; }
	
	/**
	 * Returns the components hosting the name of the file.
	 * 
	 * @return See above.
	 */
	public JPanel getNameLabel() { return nameLabel; }
	
	/**
	 * Sets the id of the image to view.
	 * 
	 * @param status The value to set.
	 * @param image The image.
	 */
	public void setStatus(boolean status, Object image)
	{
		this.image = image;	
		busyLabel.setBusy(status);
		if (image instanceof ImageData) {
			thumbLabel.setText("Preview not available");
			thumbLabel.setToolTipText("");
			thumbLabel.setEnabled(false);
			control = thumbLabel;
			statusLabel.setVisible(false);
			//control = viewButton;
		} else if (image instanceof ThumbnailData) {
			ThumbnailData thumb = (ThumbnailData) image;
			ImageIcon icon = new ImageIcon(thumb.getThumbnail());
			thumbLabel.setIcon(icon);
			thumbLabel.setBorder(LABEL_BORDER);
			if (icon != null)
				thumbLabel.setPreferredSize(new Dimension(icon.getIconWidth(), 
						icon.getIconHeight()));
			statusLabel.setVisible(false);
			control = thumbLabel;
		} else {
			if (!status) {
				statusLabel.setVisible(false);
				thumbLabel.setToolTipText("");
				thumbLabel.setEnabled(false);
				if (image == null) setStatusText(null);
				else if (image instanceof String) {
					setStatusText((String) image);
				} else if (image instanceof Map) {
					Map m = (Map) image;
					if (m == null || m.size() == 0)
						setStatusText("Folder imported");
				} else if (image instanceof ImportException) {
					ImportException ie = (ImportException) image;
					setStatusText(ie.getMessage());
					errorBox.setSelected(true);
				}
				control = thumbLabel;
			} else control = busyLabel;
		}
		buildGUI();
		revalidate();
		repaint();
	}
	
	/**
	 * Returns <code>true</code> if the error box is selected,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSelected()
	{
		if (errorBox != null && errorBox.isVisible())
			return errorBox.isSelected();
		return errorBox.isSelected();
	}
	
	/**
	 * Returns the file that needs to be imported.
	 * 
	 * @return See above.
	 */
	public File getOriginalFile() { return file; }
	
	/**
	 * Returns the exception associated to the import;
	 * 
	 * @return See above.
	 */
	public Exception getImportException()
	{
		if (image instanceof Exception)
			return (Exception) image;
		return null;
	}
	
	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		if (busyLabel != null) busyLabel.setBackground(color);
		if (nameLabel != null) {
			nameLabel.setBackground(color);
			for (int i = 0; i < nameLabel.getComponentCount(); i++) 
				nameLabel.getComponent(i).setBackground(color);
		}
		if (status != null) status.setBackground(color);
		super.setBackground(color);
	}

	/**
	 * Indicates that the error box has been selected or unselected.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (errorBox != null)
			firePropertyChange(SEND_FILE_PROPERTY, !errorBox.isSelected(), 
					errorBox.isSelected());
	}

}
