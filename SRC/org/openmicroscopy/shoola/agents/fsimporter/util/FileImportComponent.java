/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent 
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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;


//Java imports
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.ImportErrorObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.image.geom.Factory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ImageData;

/** 
 * Component hosting the file to import and displaying the status of the 
 * import process.
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
public class FileImportComponent 
	extends JPanel
	implements PropertyChangeListener
{

	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** The border of the thumbnail label. */
	private static final Border		LABEL_BORDER = 
							BorderFactory.createLineBorder(Color.black, 1);
	
	/** Default text when a failure occurred. */
	private static final String		FAILURE_TEXT = "failed";
	
	/** The text displayed in the tool tip when the image has been imported. */
	private static final String		IMAGE_LABEL_TOOLTIP = 
		"Click on thumbnail to launch the viewer.";
	
	/** Color used to indicate that a file could not be imported. */
	private static final Color		ERROR_COLOR = Color.red;
	
	/** The file to import. */
	private File 			file;
	
	/** The component indicating the progress of the import. */
	private JXBusyLabel 	busyLabel;
	
	/** The component displaying the file name. */
	private JPanel			nameLabel;

	/** The component allowing to launch the viewer. */
	private JLabel			thumbLabel;

	/** The component displaying the imported image. */
	private JLabel			imageLabel;
	
	/** The component displaying the status of the import. */
	private JLabel			status;
	
	/** The default control. */
	private JComponent		control;
	
	/** The imported image. */
	private Object			image;
	
	/** The check box displayed if the import failed. */
	private JCheckBox		errorBox;
	
	/** Indicates the status of the on-going import. */
	private StatusLabel		statusLabel;
	
	/** The component displaying the name of the file. */
	private JLabel			fileNameLabel;
	
	/** Keep tracks of the components. */
	private Map<File, FileImportComponent> components;
	
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
		imageLabel = new JLabel(icon);
		imageLabel.addMouseListener(new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see ActionListener#actionPerformed(ActionEvent)
			 */
			public void mousePressed(MouseEvent e) {
				if (image instanceof ThumbnailData) {
					ThumbnailData thumb = (ThumbnailData) image;
					//parent.viewImage(thumb.getImage());
					EventBus bus = ImporterAgent.getRegistry().getEventBus();
					ViewImage evt = new ViewImage(thumb.getImage(), null);
					bus.post(evt);
				}
			}
		});
		fileNameLabel = new JLabel(file.getName());
		nameLabel.add(imageLabel);
		nameLabel.add(Box.createHorizontalStrut(4));
		nameLabel.add(fileNameLabel);
		nameLabel.add(Box.createHorizontalStrut(10));
		//Dimension d = nameLabel.getPreferredSize();
		//nameLabel.setPreferredSize(new Dimension(d.width, 35));
		thumbLabel = new JLabel();
		control = busyLabel;
		errorBox = new JCheckBox("Mark to Send");
		errorBox.setOpaque(false);
		errorBox.setToolTipText("Mark the file to send to the dev team.");
		errorBox.setVisible(false);
		statusLabel = new StatusLabel();
		if (file.isDirectory())
			statusLabel.addPropertyChangeListener(this);
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
	 * Adds the specified files to the list of import data.
	 * 
	 * @param files The files to import.
	 */
	private void insertFiles(Map<File, StatusLabel> files)
	{
		if (files == null || files.size() == 0) {
			statusLabel.setText("No files to import.");
			return;
		}
		components = new HashMap<File, FileImportComponent>();
		int n = files.size();
		String text = "Importing "+n+" file";
		if (n > 1) text += "s";
		statusLabel.setText(text);
		
		Entry entry;
		Iterator i = files.entrySet().iterator();
		FileImportComponent c;
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		int index = 0;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = new FileImportComponent((File) entry.getKey());
			c.setStatusLabel((StatusLabel) entry.getValue());
			if (index%2 == 0)
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
			else 
				c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
			components.put((File) entry.getKey(), c);
			p.add(c);
			index++;
		}
		removeAll();
		JXTaskPane pane = EditorUtil.createTaskPane(file.getName()+": "+text);
		IconManager icons = IconManager.getInstance();
		pane.setIcon(icons.getIcon(IconManager.DIRECTORY));
		Font font = pane.getFont();
		pane.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
		pane.add(p);
		double[][] size = {{TableLayout.FILL}, {TableLayout.PREFERRED}};
		setLayout(new TableLayout(size));
		add(pane, "0, 0");
		validate();
		repaint();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param file 		The file to import.
	 */
	public FileImportComponent(File file)
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified.");
		this.file = file;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Replaces the initial status label.
	 * 
	 * @param label The value to replace.
	 */
	void setStatusLabel(StatusLabel label)
	{
		statusLabel = label;
		statusLabel.addPropertyChangeListener(this);
		buildGUI();
		revalidate();
		repaint();
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
		} else if (image instanceof ThumbnailData) {
			ThumbnailData thumb = (ThumbnailData) image;
			ImageIcon icon = new ImageIcon(Factory.magnifyImage(0.25, 
					thumb.getThumbnail()));
			imageLabel.setToolTipText(IMAGE_LABEL_TOOLTIP);
			imageLabel.setIcon(icon);
			imageLabel.setBorder(LABEL_BORDER);
			if (icon != null)
				imageLabel.setPreferredSize(new Dimension(icon.getIconWidth(), 
						icon.getIconHeight()));
			statusLabel.setVisible(false);
			control = thumbLabel;
		} else if (image instanceof Boolean) {
			setStatusText("Folder imported");
			return;
		} else {
			if (!status) {
				statusLabel.setVisible(false);
				thumbLabel.setToolTipText("");
				thumbLabel.setEnabled(false);
				if (image == null) setStatusText(null);
				else if (image instanceof String) {
					setStatusText((String) image);
				} else if (image instanceof ImportException) {
					fileNameLabel.setForeground(ERROR_COLOR);
					thumbLabel.setForeground(ERROR_COLOR);
					ImportException ie = (ImportException) image;
					setStatusText(ie.getMessage());
					thumbLabel.setToolTipText(
							UIUtilities.printErrorText(ie.getCause()));
					errorBox.setSelected(true);
				}
				control = thumbLabel;
			} else control = busyLabel;
		}
		if (!file.isDirectory())
			buildGUI();
		revalidate();
		repaint();
	}
	
	/**
	 * Returns the files that failed to import.
	 * 
	 * @return See above.
	 */
	public List<FileImportComponent> getImportErrors()
	{
		List<FileImportComponent> l = null;
		if (file.isFile()) {
			if (errorBox != null && errorBox.isVisible()) {
				if (errorBox.isSelected() && errorBox.isEnabled() && 
						image instanceof Exception) {
					ImportErrorObject object = new ImportErrorObject(file, 
							(ImportException) image);
					l = new ArrayList<FileImportComponent>();
					l.add(this);
					return l;
				}
			}
		} else {
			if (components != null) {
				Entry entry;
				Iterator i = components.entrySet().iterator();
				FileImportComponent fc;
				l = new ArrayList<FileImportComponent>();
				List<FileImportComponent> list;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					fc = (FileImportComponent) entry.getValue();
					list = fc.getImportErrors();
					if (list != null && list.size() > 0)
						l.addAll(list);
				}
			}
		}
		return l;
	}
	
	/**
	 * Returns the import error object.
	 * 
	 * @return See above.
	 */
	public ImportErrorObject getImportErrorObject()
	{
		if (errorBox == null || !errorBox.isVisible()) return null;
		if (!errorBox.isEnabled()) return null;
		ImportErrorObject object = new ImportErrorObject(file, 
				(ImportException) image);
		object.setReaderType(statusLabel.getReaderType());
		object.setUsedFiles(statusLabel.getUsedFiles());
		return object;
	}
	
	/** Indicates that the file has been sent. */
	public void markAsSent()
	{
		if (errorBox != null) {
			errorBox.setEnabled(false);
			errorBox.setText("Sent");
			repaint();
		}
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
	 * Listens to property fired by the <code>StatusLabel</code>.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (StatusLabel.FILES_SET_PROPERTY.equals(name)) {
			insertFiles((Map<File, StatusLabel>) evt.getNewValue());
		} else if (StatusLabel.FILE_IMPORT_STARTED_PROPERTY.equals(name)) {
			if (busyLabel != null) busyLabel.setBusy(true);
		} else if (StatusLabel.FILE_IMPORTED_PROPERTY.equals(name)) {
			Object[] results = (Object[]) evt.getNewValue();
			File f = (File) results[0];
			if (f.getAbsolutePath().equals(file.getAbsolutePath()))
				setStatus(false, results[1]);
			
		}
	}
	
}
