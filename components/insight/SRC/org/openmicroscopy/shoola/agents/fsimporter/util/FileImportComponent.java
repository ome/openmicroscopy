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
import java.awt.event.MouseListener;
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
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.measurement.MeasurementAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.ImageData;
import pojos.PlateData;

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
	implements ActionListener, ChangeListener, PropertyChangeListener
{

	/** Indicates that the container is of type <code>Project</code>. */
	public static final int PROJECT_TYPE = 0;
	
	/** Indicates that the container is of type <code>Screen</code>. */
	public static final int SCREEN_TYPE = 1;
	
	/** Indicates that the container is of type <code>Dataset</code>. */
	public static final int DATASET_TYPE = 2;
	
	/** Indicates that no container specified. */
	public static final int NO_CONTAINER = 3;
	
	/** 
	 * Bound property indicating that the error to submit is selected or not. 
	 */
	public static final String SUBMIT_ERROR_PROPERTY = "submitError";
	
	/** Bound property indicating to browse the node. */
	public static final String BROWSE_PROPERTY = "browse";
	
	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** Color used to indicate that a file could not be imported. */
	private static final Color		ERROR_COLOR = Color.red;
	
	/** Text to indicate to view the image. */
	private static final String VIEW_TEXT = "View";
	
	/** Text to indicate to view the image. */
	private static final String BROWSE_TEXT = "Browse";
	
	/** Text to indicate to view the image. */
	private static final String NOT_VIEW_TEXT = "Image not viewable";
	
	/** The number of extra labels for images to add. */
	private static final int NUMBER = 3;
	
	/** One of the constants defined by this class. */
	private int				type;
	
	/** The file to import. */
	private File 			file;
	
	/** The component indicating the progress of the import. */
	private JXBusyLabel 	busyLabel;
	
	/** The component displaying the file name. */
	private JPanel			namePane;

	/** The component displaying the result. */
	private JLabel			resultLabel;

	/** The component displaying the imported image. */
	private ThumbnailLabel	imageLabel;
	
	/** Keeps track of the extra images if any. */
	private List<ThumbnailLabel> imageLabels;
	
	/** The component displaying the status of the import. */
	private JLabel			status;

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
	
	/** The mouse adapter to view the image. */
	private MouseAdapter adapter;

	/** Flag indicating to use the folder as container. */
	private boolean folderAsContainer;
	
	/** Button to delete the imported image. */
	private JButton deleteButton;
	
	/** Deletes the image that was imported but cannot be viewed. */
	private void deleteImage()
	{
		List<DeletableObject> l = new ArrayList<DeletableObject>();
		
		if (image instanceof ThumbnailData) {
			l.add(new DeletableObject(((ThumbnailData) image).getImage())); 
		} else if (image instanceof ImageData) {
			l.add(new DeletableObject((DataObject) image)); 
		}
		if (l.size() == 0) return;
		IconManager icons = IconManager.getInstance();
		DeleteActivityParam p = new DeleteActivityParam(
				icons.getIcon(IconManager.APPLY_22), l);
		p.setFailureIcon(icons.getIcon(IconManager.DELETE_22));
		UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
		un.notifyActivity(p);
		//the row enabled
		deleteButton.setEnabled(false);
		errorBox.setEnabled(false);
		fileNameLabel.setEnabled(false);
		resultLabel.setEnabled(false);
		imageLabel.setEnabled(false);
	}
	
	/** Initializes the components. */
	private void initComponents()
	{
		adapter = new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{ 
				if (e.getClickCount() == 2) {
					if (image instanceof ThumbnailData) {
						ThumbnailData data = (ThumbnailData) image;
						EventBus bus = 
							ImporterAgent.getRegistry().getEventBus();
						bus.post(new ViewImage(new ViewImageObject(
								data.getImage()), null));
					} else if (image instanceof ImageData) {
						ImageData data = (ImageData) image;
						EventBus bus = 
							ImporterAgent.getRegistry().getEventBus();
						bus.post(new ViewImage(new ViewImageObject(
								data), null));
					} else if (image instanceof PlateData) {
						firePropertyChange(BROWSE_PROPERTY, null, image);
					}
				}
			}
		};
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		busyLabel = new JXBusyLabel(SIZE);
		busyLabel.setVisible(true);
		busyLabel.setBusy(false);
		
		namePane = new JPanel();
		namePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		IconManager icons = IconManager.getInstance();
		Icon icon;
		if (file.isFile()) icon = icons.getIcon(IconManager.IMAGE);
		else icon = icons.getIcon(IconManager.DIRECTORY);
		imageLabel = new ThumbnailLabel(icon);
		imageLabel.addPropertyChangeListener(this);
		imageLabels = new ArrayList<ThumbnailLabel>();
		ThumbnailLabel label;
		for (int i = 0; i < NUMBER; i++) {
			label = new ThumbnailLabel();
			label.setVisible(false);
			imageLabels.add(label);
		}
		fileNameLabel = new JLabel(file.getName());
		namePane.add(imageLabel);
		Iterator<ThumbnailLabel> j = imageLabels.iterator();
		while (j.hasNext()) {
			namePane.add(j.next());
		}
		namePane.add(Box.createHorizontalStrut(4));
		namePane.add(fileNameLabel);
		namePane.add(Box.createHorizontalStrut(10));
		resultLabel = new JLabel();
		//control = busyLabel;
		errorBox = new JCheckBox("Mark to Send");
		errorBox.setOpaque(false);
		errorBox.setToolTipText("Mark the file to send to the development " +
				"team.");
		errorBox.setVisible(false);
		errorBox.setSelected(true);
		statusLabel = new StatusLabel();
		statusLabel.addPropertyChangeListener(this);
		deleteButton = new JButton(icons.getIcon(IconManager.DELETE));
		deleteButton.setToolTipText("Delete the image");
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setVisible(false);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		add(namePane);
		add(busyLabel);
		add(resultLabel);
		add(statusLabel);
		add(errorBox);
		add(deleteButton);
	}
	
	/**
	 * Sets the text of the {@link #resultLabel}.
	 * 
	 * @param text The string to set.
	 */
	private void setStatusText(String text)
	{
		if (text == null) text = "";
		text = text.trim();
		if (text.length() == 0) resultLabel.setText(statusLabel.getErrorText());
		else resultLabel.setText(text);
	}
	
	/** 
	 * Attaches the listeners to the newly created component.
	 * 
	 * @param c The component to handle.
	 */
	private void attachListeners(FileImportComponent c)
	{
		PropertyChangeListener[] listeners = getPropertyChangeListeners();
		if (listeners != null && listeners.length > 0) {
			for (int j = 0; j < listeners.length; j++) {
				c.addPropertyChangeListener(listeners[j]);
			}
		}
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
		p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = new FileImportComponent((File) entry.getKey(), 
					folderAsContainer);
			c.setType(getType());
			attachListeners(c);
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
	 * @param file The file to import.
	 * @param folderAsContainer Pass <code>true</code> if the passed file
	 * 							has to be used as a container, 
	 * 							<code>false</code> otherwise.
	 */
	public FileImportComponent(File file, boolean folderAsContainer)
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified.");
		this.file = file;
		if (file.isFile()) folderAsContainer = false;
		this.folderAsContainer = folderAsContainer;
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
	 * Sets the result of the import.
	 * 
	 * @param status Flag indicating the status of the import.
	 * @param image  The image.
	 */
	public void setStatus(boolean status, Object image)
	{
		this.image = image;	
		busyLabel.setBusy(status);
		busyLabel.setVisible(false);
		if (image instanceof ImageData) {
			ImageData img = (ImageData) image;
			Exception error = null;
			try {
				img.getDefaultPixels();
			} catch (Exception e) {
				error = e;
			}
			if (error != null) {
				fileNameLabel.setForeground(ERROR_COLOR);
				resultLabel.setForeground(ERROR_COLOR);
				setStatusText(NOT_VIEW_TEXT);
				resultLabel.setToolTipText(
						UIUtilities.formatExceptionForToolTip(error));
				errorBox.setVisible(true);
				errorBox.addChangeListener(this);
				deleteButton.setVisible(true);
				deleteButton.addActionListener(this);
			} else {
				imageLabel.setData(img);
				resultLabel.setText(VIEW_TEXT);
				resultLabel.setForeground(UIUtilities.HYPERLINK_COLOR);
				resultLabel.setToolTipText(ThumbnailLabel.IMAGE_LABEL_TOOLTIP);
				resultLabel.setEnabled(false);
				resultLabel.setVisible(true);
				fileNameLabel.addMouseListener(adapter);
				resultLabel.addMouseListener(adapter);
				addMouseListener(adapter);
			}
		} else if (image instanceof ThumbnailData) {
			ThumbnailData thumbnail = (ThumbnailData) image;
			if (thumbnail.isValidImage()) {
				imageLabel.setThumbnail((ThumbnailData) image);
				statusLabel.setVisible(false);
				fileNameLabel.addMouseListener(adapter);
				addMouseListener(adapter);
				resultLabel.setVisible(true);
			} else {
				fileNameLabel.setForeground(ERROR_COLOR);
				resultLabel.setForeground(ERROR_COLOR);
				setStatusText(NOT_VIEW_TEXT);
				resultLabel.setToolTipText(
						UIUtilities.formatExceptionForToolTip(
								thumbnail.getError()));
				errorBox.setVisible(true);
				errorBox.addChangeListener(this);
				deleteButton.setVisible(true);
				deleteButton.addActionListener(this);
			}

		} else if (image instanceof PlateData) {
			imageLabel.setData((PlateData) image);
			resultLabel.setText(BROWSE_TEXT);
			resultLabel.setForeground(UIUtilities.HYPERLINK_COLOR);
			resultLabel.setToolTipText(ThumbnailLabel.PLATE_LABEL_TOOLTIP);
			resultLabel.setEnabled(false);
			resultLabel.setVisible(true);
			fileNameLabel.addMouseListener(adapter);
			resultLabel.addMouseListener(adapter);
		} else if (image instanceof List) {
			statusLabel.setVisible(false);
			List list = (List) image;
			int m = list.size();
			ThumbnailData thumb = (ThumbnailData) list.get(0);
			imageLabel.setThumbnail(thumb);
			list.remove(0);
			ThumbnailLabel label = imageLabels.get(0);
			label.setVisible(true);
			thumb = (ThumbnailData) list.get(0);
			label.setThumbnail(thumb);
			list.remove(0);
			if (list.size() > 0) {
				label = imageLabels.get(1);
				label.setVisible(true);
				thumb = (ThumbnailData) list.get(0);
				label.setThumbnail(thumb);
				list.remove(0);
				int n = statusLabel.getSeriesCount()-m;
				if (n > 0) {
					label = imageLabels.get(2);
					Font f = label.getFont();
					label.setFont(f.deriveFont(f.getStyle(), f.getSize()-2));
					label.setVisible(true);
					String value = "... "+n+" more";
					label.setText(value);
				}
			}
			resultLabel.setVisible(true);
			//control = resultLabel;
		} else if (image instanceof Boolean) {
			if (file.isDirectory()) setStatusText("Folder imported");
			else setStatusText("File not valid");
		} else {
			if (!status) {
				statusLabel.setVisible(false);
				resultLabel.setToolTipText("");
				resultLabel.setEnabled(false);
				if (image == null) setStatusText(null);
				else if (image instanceof String) {
					setStatusText((String) image);
				} else if (image instanceof ImportException) {
					fileNameLabel.setForeground(ERROR_COLOR);
					resultLabel.setForeground(ERROR_COLOR);
					ImportException ie = (ImportException) image;
					setStatusText(ie.getMessage());
					resultLabel.setToolTipText(
							UIUtilities.formatExceptionForToolTip(ie));
					errorBox.setVisible(true);
					errorBox.addChangeListener(this);
				}
			}
		}
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
				Iterator<FileImportComponent> i = components.values().iterator();
				FileImportComponent fc;
				l = new ArrayList<FileImportComponent>();
				List<FileImportComponent> list;
				while (i.hasNext()) {
					fc = i.next();
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
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToSend()
	{
		if (file.isFile()) {
			if (errorBox.isVisible())
				return errorBox.isEnabled() && errorBox.isSelected();
			return false;
		}
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend()) 
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if refresh whole tree, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasToRefreshTree()
	{
		if (file.isFile()) {
			if (errorBox.isVisible() || deleteButton.isVisible())
				return false;
			switch (type) {
				case PROJECT_TYPE:
				case NO_CONTAINER:
					return true;
				default:
					return false;
			}
		}
		if (components == null) return false;
		if (folderAsContainer && type != PROJECT_TYPE) {
			Iterator<FileImportComponent> i = components.values().iterator();
			while (i.hasNext()) {
				if (i.next().toRefresh()) 
					return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Returns <code>true</code> if some files were imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean toRefresh()
	{
		if (file.isFile()) {
			if (deleteButton.isVisible()) return false;
			else if (errorBox.isVisible())
				return !(errorBox.isEnabled() && errorBox.isSelected());
			return true;
		}
		if (components == null) return false;
		Iterator<FileImportComponent> i = components.values().iterator();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend()) 
				count++;
		}
		return components.size() != count;
	}

	/** Indicates the import has been cancelled. */
	public void cancelLoading()
	{
		boolean isBusy = busyLabel.isBusy();
		if (file.isFile() && isBusy) {
			statusLabel.setText("cancelled");
		}
		if (components == null) return;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			i.next().cancelLoading();
		}
	}
	
	/**
	 * Sets the type. 
	 * 
	 * @param type One of the constants defined by this class.
	 */
	public void setType(int type) { this.type = type; }
	
	/**
	 * Returns the supported type. One of the constants defined by this class.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }
	
	/**
	 * Overridden to make sure that all the components have the correct 
	 * background.
	 * @see JPanel#setBackground(Color)
	 */
	public void setBackground(Color color)
	{
		if (busyLabel != null) busyLabel.setBackground(color);
		if (namePane != null) {
			namePane.setBackground(color);
			for (int i = 0; i < namePane.getComponentCount(); i++) 
				namePane.getComponent(i).setBackground(color);
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
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl == statusLabel && busyLabel != null) {
				busyLabel.setBusy(true);
			}
		} else if (StatusLabel.FILE_IMPORTED_PROPERTY.equals(name)) {
			Object[] results = (Object[]) evt.getNewValue();
			File f = (File) results[0];
			if (f.getAbsolutePath().equals(file.getAbsolutePath()))
				setStatus(false, results[1]);
		} else if (StatusLabel.FILE_RESET_PROPERTY.equals(name)) {
			file = (File) evt.getNewValue();
			fileNameLabel.setText(file.getName());
		} else if (ThumbnailLabel.BROWSE_PLATE_PROPERTY.equals(name)) {
			firePropertyChange(BROWSE_PROPERTY, evt.getOldValue(), 
					evt.getNewValue());
		}
	}

	/**
	 * Sends a property when the error box is selected or not.
	 * @see ChangeListener#stateChanged(ChangeEvent)
	 */
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == errorBox) {
			boolean b = errorBox.isSelected();
			firePropertyChange(SUBMIT_ERROR_PROPERTY, !b, b);
		}
	}
	
	/**
	 * Deletes the image if the image cannot be viewed.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { deleteImage(); }
	
}
