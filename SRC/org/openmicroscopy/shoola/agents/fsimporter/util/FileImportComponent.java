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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
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
	
	/** Default text when a failure occurred. */
	private static final String		FAILURE_TEXT = "failed";
	
	/** Color used to indicate that a file could not be imported. */
	private static final Color		ERROR_COLOR = Color.red;
	
	/** The number of extra labels for images to add. */
	private static final int NUMBER = 3;
	
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
	
	/** The mouse adapter to view the image. */
	private MouseAdapter adapter;

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
						bus.post(new ViewImage(data.getImage(), null));
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
		control = busyLabel;
		errorBox = new JCheckBox("Mark to Send");
		errorBox.setOpaque(false);
		errorBox.setToolTipText("Mark the file to send to the development " +
				"team.");
		errorBox.setVisible(false);
		statusLabel = new StatusLabel();
		statusLabel.addPropertyChangeListener(this);
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		add(namePane);
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
	 * Sets the text of the {@link #resultLabel}.
	 * 
	 * @param text The string to set.
	 */
	private void setStatusText(String text)
	{
		if (text == null) text = "";
		text = text.trim();
		if (text.length() == 0) resultLabel.setText(FAILURE_TEXT);
		else resultLabel.setText(text);
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
	 * Sets the result of the import.
	 * 
	 * @param status Flag indicating the status of the import.
	 * @param image  The image.
	 */
	public void setStatus(boolean status, Object image)
	{
		this.image = image;	
		busyLabel.setBusy(status);
		if (image instanceof ImageData) {
			resultLabel.setText("Preview not available");
			resultLabel.setToolTipText("");
			resultLabel.setEnabled(false);
			control = resultLabel;
			statusLabel.setVisible(false);
		} else if (image instanceof ThumbnailData) {
			imageLabel.setThumbnail((ThumbnailData) image);
			statusLabel.setVisible(false);
			fileNameLabel.addMouseListener(adapter);
			addMouseListener(adapter);
			control = resultLabel;
		} else if (image instanceof List) {
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
			statusLabel.setVisible(false);
			control = resultLabel;
		} else if (image instanceof Boolean) {
			setStatusText("Folder imported");
			return;
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
					ie.printStackTrace();
					String s = UIUtilities.printErrorText(ie.getCause());
					String[] values = s.split("\n");
					//Display the first 20 lines
					String[] lines = values;
					int n = 20;
					if (values.length > 20) {
						lines = new String[n];
						for (int i = 0; i < lines.length; i++) {
							lines[i] = values[i];
						}
					}
					resultLabel.setToolTipText(
							UIUtilities.formatToolTipText(lines));
					errorBox.setSelected(true);
				}
				control = resultLabel;
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
		}
	}
	
}
