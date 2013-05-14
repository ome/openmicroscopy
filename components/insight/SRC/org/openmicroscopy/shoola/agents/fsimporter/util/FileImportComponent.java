/*
 * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
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
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.ThumbnailData;
import org.openmicroscopy.shoola.env.data.util.SecurityContext;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Component hosting the file to import and displaying the status of the 
 * import process.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
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

	/** The value indicating that the import was successful. */
	public static final int SUCCESS = 0;
	
	/** The value indicating that the import was partially successful. */
	public static final int PARTIAL = 1;
	
	/** The value indicating that the import was not successful. */
	public static final int FAILURE = 2;
	
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
	
	/** Bound property indicating to display the error.*/
	public static final String DISPLAY_ERROR_PROPERTY = "displayError";
	
	/** Bound property indicating to cancel the import.*/
	public static final String CANCEL_IMPORT_PROPERTY = "cancelImport";
	
	/** Bound property indicating to browse the node. */
	public static final String BROWSE_PROPERTY = "browse";
	
	/** Bound property indicating to increase the number of files to import. */
	public static final String IMPORT_FILES_NUMBER_PROPERTY = "importFilesNumber";
	
	/**
	 * Bound property indicating to the import of the file has been cancelled,
	 * failed or successful.
	 */
	public static final String IMPORT_STATUS_CHANGE_PROPERTY = "importStatusChange";
	
	/**
	 * Bound property indicating to load the content of the log file.
	 */
	public static final String LOAD_LOGFILEPROPERTY = "loadLogfile";

	/** The default size of the busy label. */
	private static final Dimension SIZE = new Dimension(16, 16);
	
	/** Color used to indicate that a file could not be imported. */
	private static final Color		ERROR_COLOR = Color.red;
	
	/** Text to indicate to view the image. */
	private static final String VIEW_TEXT = "View";
	
	/** Text to indicate to view the image. */
	private static final String BROWSE_TEXT = "Browse";
	
	/** Text to indicate to view the image. */
	private static final String PYRAMID_TEXT = "Building pyramid, please wait";
	
	/** Text to indicate that the thumbnail cannot be created */
	private static final String IMAGE_CREATION_ERROR_TEXT = 
		"Cannot create thumbnail";
	
	/** Text to indicate that the compression level is not supported. */
	private static final String COMPRESSION_ERROR_TEXT = 
		"Compression not supported";
	
	/** Text to indicate that library is missing. */
	private static final String MISSING_LIB_ERROR_TEXT = 
		"Missing Library";
	
	/** Text to indicate that the file is not accessible. */
	private static final String NO_SPACE_ERROR_TEXT = "No more space on Disk";
	
	/** Text to indicate that the file is not accessible. */
	private static final String FILE_ON_TAPE_ERROR_TEXT = "File on Tape";
	
	/** Text to indicate that a checksum mismatch occurred during import. */
	private static final String CHECKSUM_MISMATCH_TEXT =
			"File integrity error during upload.";
	
	/** Tool tip text to indicate to browse the container. */
	private static final String BROWSE_CONTAINER_TOOLTIP = "Click to browse.";

	/** Text to indicate to browse the container. */
	private static final String BROWSE_CONTAINER_TEXT = "Browse container";

	/** Text to indicate that the folder has been imported. */
	private static final String FOLDER_IMPORTED_TEXT = "Folder Importer";
	
	/** Text to indicate that the file, after scanning is not valid. */
	private static final String FILE_NOT_VALID_TEXT = "File Not Valid";

	/** The number of extra labels for images to add. */
	private static final int NUMBER = 3;

	/** Action id to delete the image. */
	private static final int DELETE_ID = 0;
	
	/** Action id to cancel the import before it starts. */
	private static final int CANCEL_ID = 1;
	
	/** Action id to browse the container. */
	private static final int BROWSE_ID = 2;

	/** Text indicating where the images where imported. */
	private static final String TEXT_IMPORTED = "Imported to:";

	/** One of the constants defined by this class. */
	private int type;
	
	/** The file to import. */
	private File file;
	
	/** The component indicating the progress of the import. */
	private JXBusyLabel busyLabel;
	
	/** The component displaying the file name. */
	private JPanel namePane;

	/** The component displaying the result. */
	private JLabel resultLabel;
	
	/** The component displaying the imported image. */
	private ThumbnailLabel imageLabel;
	
	/** Keeps track of the extra images if any. */
	private List<ThumbnailLabel> imageLabels;

	/** The imported image. */
	private Object image;
	
	/** The check box displayed if the import failed. */
	private JCheckBox errorBox;
	
	/** The button indicating that an error occurred. */
	private JButton errorButton;
	
	/** Indicates the status of the on-going import. */
	private StatusLabel statusLabel;
	
	/** The component displaying the name of the file. */
	private JLabel fileNameLabel;
	
	/** Keep tracks of the components. */
	private Map<File, FileImportComponent> components;
	
	/** The mouse adapter to view the image. */
	private MouseAdapter adapter;

	/** Flag indicating to use the folder as container. */
	private boolean folderAsContainer;
	
	/** Button to delete the imported image. */
	private JButton deleteButton;
	
	/** Button to download the import log file */
	private JButton importLogButton;
	
	/** The data object corresponding to the folder. */
	private DataObject containerFromFolder;
	
	/** Button to browse the container. */
	private JLabel	browseButton;
	
	/** Button to cancel the import for that file. */
	private JLabel cancelButton;
	
	/** The node where to import the folder. */
	private DataObject data;
	
	/** The dataset if any. */
	private DatasetData dataset;
	
	/** The node of reference if any. */
	private Object refNode;
	
	/** The container displaying where it was imported. */
	private JLabel containerLabel;
	
	/** The object where the data have been imported.*/
	private DataObject containerObject;
	
	/** Flag indicating to show/hide the container label. */
	private boolean showContainerLabel;
	
	/** The component used when importing a folder. */
	private JXTaskPane pane;
	
	/** The parent of the node. */
	private FileImportComponent parent;
	
	/** The total number of files to import. */
	private int totalFiles;
	
	/** The value indicating the number of imports in the folder. */
	private int importCount;
	
	/** The error to show if any.*/
	private Throwable exception;
	
	/** Flag indicating that no container specified.*/
	private boolean noContainer;
	
	/** 
	 * Flag indicating that the container hosting the imported image
	 * can be browsed or not depending on how the import is launched.
	 */
	private boolean browsable;
	
	/** Set to <code>true</code> if attempt to re-import.*/
	private boolean reimported;
	
	/** Indicates that the file has been re-imported.*/
	private JLabel reimportedLabel;
	
	/** Flag indicating that the file should be reimported.*/
	private boolean toReImport;
	
	/** The group in which to import the file.*/
	private GroupData group;

	/** The user that will own the data being imported */
	private ExperimenterData user;
	
	/** The component displaying the group and user where the data is imported. */
	private JLabel groupUserLabel;
	
	/** Flag indicating the the user is member of one group only.*/
	private boolean singleGroup;

	/** The log file if any to load.*/
	private FileAnnotationData logFile;

	/** The button displayed the various options post if the import worked.*/
	private JButton actionMenuButton;
	
	private JPopupMenu menu;
	
	private int resultIndex = -1;
	
	
	private JPopupMenu createActionMenu()
	{
		if (menu != null) return menu;
		menu = new JPopupMenu();
		switch(resultIndex) {
		case FAILURE:
			menu.add(new JMenuItem(new AbstractAction("Submit") {
	            public void actionPerformed(ActionEvent e) {
	            	// TODO: submit the failure
	            }
	        }));
			break;
		case SUCCESS:
			menu.add(new JMenuItem(new AbstractAction("In Full Viewer") {
	            public void actionPerformed(ActionEvent e) {
	            	launchFullViewer();
	            }
	        }));
			menu.add(new JMenuItem(new AbstractAction("In Data Browser") {
	            public void actionPerformed(ActionEvent e) {
	            	// TODO: link to data browser
	            }
	        }));
		}
		
		menu.add(new JMenuItem(new AbstractAction("Import Log") {
            public void actionPerformed(ActionEvent e) {
            	// TODO: submit the failure
            }
        }));
		menu.add(new JMenuItem(new AbstractAction("Checksum") {
            public void actionPerformed(ActionEvent e) {
            	// TODO: submit the failure
            }
        }));
		return menu;
	}
	/** Indicates that the import was successful or if it failed.*/
	private void formatResult()
	{
		resultLabel.setVisible(true);
		IconManager icons = IconManager.getInstance();
		if (hasImportFailed()) {
			resultLabel.setIcon(icons.getIcon(IconManager.DELETE));
			
			actionMenuButton.setVisible(true);
			actionMenuButton.setForeground(Color.RED);
			actionMenuButton.setText("Failed");
			
			resultIndex = FAILURE;
		} else {
			if (!statusLabel.isMarkedAsCancel()) {
				resultLabel.setIcon(icons.getIcon(IconManager.APPLY));
				actionMenuButton.setVisible(true);
				actionMenuButton.setForeground(UIUtilities.HYPERLINK_COLOR);
				actionMenuButton.setText("View");
				
				resultIndex = SUCCESS;
			}
		}
	}
	
	/**
	 * Returns the formatted result.
	 * 
	 * @return See above.
	 */
	private Object getFormattedResult()
	{
		if (image == null) return null;
		ImportErrorObject o = getImportErrorObject();
		if (o != null) return o;
		if (image instanceof ImageData || image instanceof ThumbnailData ||
			image instanceof PlateData || image instanceof List)
			return image;
		return null;
	}
	
	/**
	 * Logs the exception.
	 * 
	 * @param e The error to log.
	 */
	private void logException(Exception e)
	{
		String s = "Error during import: ";
        LogMessage msg = new LogMessage();
        msg.print(s);
        msg.print(e);
		ImporterAgent.getRegistry().getLogger().warn(this, msg);
	}
	
	/** Displays the error box at the specified location.
	 * 
	 * @param p The location where to show the box.
	 */
	private void showError(Point p)
	{
		if (exception == null) return;
		firePropertyChange(DISPLAY_ERROR_PROPERTY, null, exception);
	}
	
	/** Sets the text indicating the number of import. */
	private void setNumberOfImport()
	{
		if (pane == null) return;
		String end = " file";
		if (totalFiles > 1) end +="s";
		String text = file.getName()+": "+importCount+" of "+totalFiles+end;
		pane.setTitle(text);
	}
	
	/** Browses the node or the data object. */
	private void browse()
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		Object d = dataset;
		if (dataset == null || data instanceof ScreenData) d = data;
		if (d == null) return;
		bus.post(new BrowseContainer(d, null));
	}
	
	/** Indicates that the file will not be imported. 
	 * 
	 * @param fire	Pass <code>true</code> to fire a property,
	 * 				<code>false</code> otherwise.
	 */
	private void cancel(boolean fire)
	{
		if (busyLabel.isBusy() && !statusLabel.isCancellable())
			return;
		busyLabel.setBusy(false);
		busyLabel.setVisible(false);
		statusLabel.markedAsCancel();
		cancelButton.setEnabled(false);
		cancelButton.setVisible(false);
		if (image == null && file.isFile())
			firePropertyChange(IMPORT_STATUS_CHANGE_PROPERTY, null, PARTIAL);
		if (fire)
			firePropertyChange(CANCEL_IMPORT_PROPERTY, null, this);
	}
	
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
		//UserNotifier un = MeasurementAgent.getRegistry().getUserNotifier();
		//TODO: review
		//un.notifyActivity(p);
		//the row enabled
		deleteButton.setEnabled(false);
		errorBox.setEnabled(false);
		fileNameLabel.setEnabled(false);
		resultLabel.setEnabled(false);
		imageLabel.setEnabled(false);
	}
	
	/**
	 * Launches the full viewer for the selected item.
	 */
	private void launchFullViewer()
	{
		ViewImage evt;
		int plugin = ImporterAgent.runAsPlugin();
		if (image instanceof ThumbnailData) {
			ThumbnailData data = (ThumbnailData) image;
			EventBus bus = 
				ImporterAgent.getRegistry().getEventBus();
			if (data.getImage() != null) {
				evt = new ViewImage(
						new SecurityContext(group.getId()),
						new ViewImageObject(
						data.getImage()), null);
				evt.setPlugin(plugin);
				bus.post(evt);
			}
		} else if (image instanceof ImageData) {
			ImageData data = (ImageData) image;
			EventBus bus = 
				ImporterAgent.getRegistry().getEventBus();
			if (data != null) {
				evt = new ViewImage(
						new SecurityContext(group.getId()),
						new ViewImageObject(data), null);
				evt.setPlugin(plugin);
				bus.post(evt);
			}
		} else if (image instanceof PlateData) {
			firePropertyChange(BROWSE_PROPERTY, null, image);
		}
	}
	/** Initializes the components. */
	private void initComponents()
	{
		actionMenuButton = new JButton();
		actionMenuButton.setVisible(false);
		actionMenuButton.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ev) {
				int x = 0;
				int y = actionMenuButton.getY()
				           + actionMenuButton.getHeight();
				JPopupMenu popup = createActionMenu();
				popup.show(actionMenuButton,x,y);
			}
		});
		
		reimportedLabel = new JLabel("Reimported");
		reimportedLabel.setVisible(false);
		showContainerLabel = true;
		adapter = new MouseAdapter() {
			
			/**
			 * Views the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{ 
				if (e.getClickCount() == 1) {
					launchFullViewer();
				}
			}
		};
		
		setLayout(new FlowLayout(FlowLayout.LEFT));
		busyLabel = new JXBusyLabel(SIZE);
		busyLabel.setVisible(false);
		busyLabel.setBusy(false);
		
		cancelButton = new JLabel("Cancel");
		cancelButton.setForeground(UIUtilities.HYPERLINK_COLOR);
		cancelButton.addMouseListener(new MouseAdapter() {
			
			/**
			 * Browses the object the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				Object src = e.getSource();
				if (e.getClickCount() == 1 && src instanceof JLabel) {
					cancel(true);
				}
			}
		});
		cancelButton.setVisible(true);
		importLogButton = new JButton("Import Log");
		importLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				firePropertyChange(LOAD_LOGFILEPROPERTY, null, logFile);
			}
		});
		importLogButton.setVisible(false);
		browseButton = new JLabel(BROWSE_CONTAINER_TEXT);
		if (browsable) {
			browseButton.setToolTipText(BROWSE_CONTAINER_TOOLTIP);
			browseButton.setForeground(UIUtilities.HYPERLINK_COLOR);
			browseButton.addMouseListener(new MouseAdapter() {
				
				/**
				 * Browses the object the image.
				 * @see MouseListener#mousePressed(MouseEvent)
				 */
				public void mousePressed(MouseEvent e)
				{
					Object src = e.getSource();
					if (e.getClickCount() == 1 && src instanceof JLabel) {
						browse();
					}
				}
			});
		}
		
		browseButton.setVisible(false);
		
		containerLabel = new JLabel();
		containerLabel.setVisible(false);
		String text = String.format("Group: %s, Owner: %s", 
				group.getName(), user.getUserName());
		groupUserLabel = new JLabel(text);
		
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
		errorButton = new JButton("Failed");
		errorButton.setForeground(ERROR_COLOR);
		errorButton.addMouseListener(new MouseAdapter() {
			
			/** 
			 * Displays the error dialog at the specified location.
			 * @see MouseAdapter#mouseReleased(MouseEvent) 
			 */
			public void mouseReleased(MouseEvent e) {
				showError(e.getPoint());
			}
			
		});
		errorButton.setVisible(false);
		
		statusLabel = new StatusLabel();
		statusLabel.addPropertyChangeListener(this);
		deleteButton = new JButton(icons.getIcon(IconManager.DELETE));
		deleteButton.setActionCommand(""+DELETE_ID);
		deleteButton.setToolTipText("Delete the image");
		UIUtilities.unifiedButtonLookAndFeel(deleteButton);
		deleteButton.setVisible(false);
		image = null;
	}

	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
		add(namePane);
		add(statusLabel);
		
		add(Box.createHorizontalStrut(15));
		add(busyLabel);
		add(resultLabel);
		add(cancelButton);
		add(actionMenuButton);
		//TODO:
		
		//add(errorButton);
		//add(errorBox);
		//add(deleteButton);
		
		
		//add(containerLabel);
		//add(browseButton);
		//add(groupUserLabel);
		//add(reimportedLabel);
		//add(importLogButton);
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
		if (files == null || files.size() == 0) return;
		components = new HashMap<File, FileImportComponent>();
		totalFiles = files.size();
		String text = "Importing "+totalFiles+" file";
		if (totalFiles > 1) text += "s";
		statusLabel.setText(text);
		
		Entry<File, StatusLabel> entry;
		Iterator<Entry<File, StatusLabel>> i = files.entrySet().iterator();
		FileImportComponent c;
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		int index = 0;
		p.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		File f;
		DatasetData d = dataset;
		Object node = refNode;
		if (folderAsContainer) {
			node = null;
			d = new DatasetData();
			d.setName(file.getName());
		}
		while (i.hasNext()) {
			entry = i.next();
			f = entry.getKey();
			c = new FileImportComponent(f, folderAsContainer, browsable, group, user,
					singleGroup);
			if (f.isFile()) {
				c.setLocation(data, d, node);
				c.setParent(this);
			}
			c.showContainerLabel(showContainerLabel);
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
		pane = EditorUtil.createTaskPane("");
		pane.setCollapsed(false);
		setNumberOfImport();

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
	 * @param browsable Flag indicating that the container can be browsed or not.
	 * @param group The group in which to import the file.
	 * @param user The user that will own the data being imported
	 * @param singleGroup Passes <code>true</code> if the user is member of 
	 * only one group, <code>false</code> otherwise.
	 */
	public FileImportComponent(File file, boolean folderAsContainer, boolean
			browsable, GroupData group, ExperimenterData user, boolean singleGroup)
	{
		if (file == null)
			throw new IllegalArgumentException("No file specified.");
		if (group == null)
			throw new IllegalArgumentException("No group specified.");
		this.file = file;
		this.group = group;
		this.user = user;
		this.singleGroup = singleGroup;
		importCount = 0;
		this.browsable = browsable;
		this.folderAsContainer = folderAsContainer;
		initComponents();
		buildGUI();
	}
	
	/**
	 * Returns the file hosted by this component.
	 * 
	 * @return See above.
	 */
	public File getFile() { return file; }
	
	/**
	 * Sets the location where to import the files.
	 * 
	 * @param data The data where to import the folder or screening data.
	 * @param dataset The dataset if any.
	 * @param refNode The node of reference.
	 */
	public void setLocation(DataObject data, DatasetData dataset, 
			Object refNode)
	{
		this.data = data;
		this.dataset = dataset;
		this.refNode = refNode;
		if (refNode != null && refNode instanceof TreeImageDisplay) {
			TreeImageDisplay n = (TreeImageDisplay) refNode;
			Object ho = n.getUserObject();
			if (ho instanceof DatasetData) {
				containerLabel.setText(TEXT_IMPORTED);
				browseButton.setText(((DatasetData) ho).getName());
				containerObject = (DataObject) ho;
			} else if (ho instanceof ProjectData) {
				containerLabel.setText(TEXT_IMPORTED);
				browseButton.setText(((ProjectData) ho).getName());
				containerObject = (DataObject) ho;
			} else if (ho instanceof ScreenData) {
				containerLabel.setText(TEXT_IMPORTED);
				browseButton.setText(((ScreenData) ho).getName());
				containerObject = (DataObject) ho;
			}
			return;
		}
		if (dataset != null) {
			containerLabel.setText(TEXT_IMPORTED);
			browseButton.setText(dataset.getName());
			containerObject = dataset;
			return;
		}
		if (data != null && data instanceof ScreenData) {
			containerLabel.setText(TEXT_IMPORTED);
			browseButton.setText(((ScreenData) data).getName());
			containerObject = data;
		}
	}
	
	public void setImportLogFile(Collection<FileAnnotationData> data, long id) {
		long fileSetID = getFileSetID(image);
		if (id != fileSetID || data == null) return;
		Iterator<FileAnnotationData> i = data.iterator();
		FileAnnotationData fa;
		while (i.hasNext()) {
			fa = i.next();
			//Check name space
			if (FileAnnotationData.LOG_FILE_NS.equals(fa.getNameSpace())) {
				logFile = fa;
				break;
			}
		}
		if (logFile == null) return;
		importLogButton.setVisible(true);
	}
	
	private long getFileSetID(Object data)
	{
		ImageData img = null;
		if (data instanceof ImageData) {
			img = (ImageData) data;
		} else if (data instanceof ThumbnailData) {
			ThumbnailData thumb = (ThumbnailData) data;
			img = thumb.getImage();
		} else if (data instanceof List) {
			if (!((List) data).isEmpty()) {
				return getFileSetID(((List) data).get(0));
			}
		}
		if (img == null) return -1;
		return img.asImage().getFileset().getId().getValue();
	}
	
	/**
	 * Returns the dataset or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DatasetData getDataset() { return dataset; }
	
	/**
	 * Returns the object or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public DataObject getDataObject() { return data; }
	
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
	
	/** Increases the number of imports. */
	void increaseNumberOfImport()
	{
		importCount++;
		setNumberOfImport();
	}
	
	/** 
	 * Sets the parent of the component.
	 * 
	 * @param parent The value to set.
	 */
	void setParent(FileImportComponent parent)
	{
		this.parent = parent;
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
	 * @param image The image.
	 */
	public Object setStatus(boolean status, Object image)
	{
		this.image = image;	
		busyLabel.setBusy(false);
		busyLabel.setVisible(false);
		cancelButton.setVisible(false);
		importCount++;
		if (parent != null) parent.increaseNumberOfImport();
		if (image instanceof ImageData) {
			ImageData img = (ImageData) image;
			Exception error = null;
			try {
				img.getDefaultPixels();
			} catch (Exception e) {
				logException(e);
				error = e;
				toReImport = true;
			}
			if (error != null) {
				exception = error;
				fileNameLabel.setForeground(ERROR_COLOR);
				resultLabel.setVisible(false);
				errorButton.setToolTipText(
						UIUtilities.formatExceptionForToolTip(error));
				errorButton.setVisible(true);
				errorBox.setVisible(true);
				errorBox.addChangeListener(this);
				deleteButton.setVisible(true);
				deleteButton.addActionListener(this);
			} else {
				imageLabel.setData(img);
				if (!browsable) imageLabel.setToolTipText("");
				fileNameLabel.addMouseListener(adapter);
				//resultLabel.addMouseListener(adapter);
				//addMouseListener(adapter);
				showContainerLabel =
					(dataset != null || containerFromFolder != null);
				if (noContainer) {
					browseButton.setVisible(false);
					containerLabel.setVisible(false);
				} else {
					browseButton.setVisible(showContainerLabel);
					containerLabel.setVisible(showContainerLabel);
				}
			}
		} else if (image instanceof ThumbnailData) {
			ThumbnailData thumbnail = (ThumbnailData) image;
			if (thumbnail.isValidImage()) {
				imageLabel.setData(thumbnail);
				fileNameLabel.addMouseListener(adapter);
				//addMouseListener(adapter);
				if (!browsable) imageLabel.setToolTipText("");
				if (thumbnail.requirePyramid() != null 
						&& thumbnail.requirePyramid().booleanValue()) {
						imageLabel.setToolTipText(PYRAMID_TEXT);
				}
				showContainerLabel = 
					(dataset != null || containerFromFolder != null);
				if (noContainer) {
					browseButton.setVisible(false);
					containerLabel.setVisible(false);
				} else {
					browseButton.setVisible(showContainerLabel);
					containerLabel.setVisible(showContainerLabel);
				}
			} else {
				fileNameLabel.setForeground(ERROR_COLOR);
				errorButton.setVisible(false);
				errorBox.setVisible(false);
				groupUserLabel.setVisible(!singleGroup);
				logException(thumbnail.getError());
			}
		} else if (image instanceof PlateData) {
			imageLabel.setData((PlateData) image);
			groupUserLabel.setVisible(!singleGroup);
			fileNameLabel.addMouseListener(adapter);
			showContainerLabel = containerObject instanceof ScreenData;
			if (noContainer || !browsable) {
				browseButton.setVisible(false);
				containerLabel.setVisible(false);
			} else {
				browseButton.setVisible(showContainerLabel);
				containerLabel.setVisible(showContainerLabel);
			}
		} else if (image instanceof List) {
			groupUserLabel.setVisible(!singleGroup);
			List<ImageData> list = new ArrayList<ImageData>((List) image);
			int m = list.size();
			imageLabel.setData(list.get(0));
			list.remove(0);
			ThumbnailLabel label = imageLabels.get(0);
			label.setVisible(true);
			label.setData(list.get(0));
			list.remove(0);
			if (list.size() > 0) {
				label = imageLabels.get(1);
				label.setVisible(true);
				label.setData(list.get(0));
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
			showContainerLabel = true;
			if (noContainer) {
				browseButton.setVisible(false);
				containerLabel.setVisible(false);
			} else {
				browseButton.setVisible(showContainerLabel);
				containerLabel.setVisible(showContainerLabel);
			}
		} else if (image instanceof Boolean) {
			if (!statusLabel.isMarkedAsCancel()) {
				cancelButton.setVisible(false);
				if (statusLabel.isMarkedAsDuplicate()) {
					setStatusText(StatusLabel.DUPLICATE);
				} else {
					statusLabel.setVisible(false);
					if (file.isFile()) {
						setStatusText(FILE_NOT_VALID_TEXT);
						this.image = new ImportException(FILE_NOT_VALID_TEXT);
						exception = (ImportException) this.image;
						errorButton.setVisible(true);
						errorBox.setVisible(true);
						errorBox.addChangeListener(this);
					}
				}
			}
		} else {
			if (!status) {
				statusLabel.setVisible(false);
				if (image == null) setStatusText(null);
				else if (image instanceof String) {
					setStatusText((String) image);
				} else if (image instanceof Exception) {
					Exception e = (Exception) image;
					fileNameLabel.setForeground(ERROR_COLOR);
					toReImport = false;
					errorButton.setToolTipText(
							UIUtilities.formatExceptionForToolTip(e));
					exception = e;
					logException(e);
					errorButton.setVisible(false);
					cancelButton.setVisible(false);
					if (e instanceof ImportException) {
						toReImport = true;
					} else {
						errorButton.setVisible(true);
						errorBox.setVisible(true);
						errorBox.addChangeListener(this);
					}
				}
			}
		}
		repaint();
		formatResult();
		return getFileSetID(this.image);
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
					l = new ArrayList<FileImportComponent>();
					l.add(this);
					return l;
				}
			}
		} else {
			if (components != null) {
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
				(Exception) image);
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
	 * Returns <code>true</code> if the import has failed, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasImportFailed()
	{
		//if (file.isFile()) return errorBox.isVisible();
		return errorBox.isVisible();
	}
	
	/**
	 * Returns <code>true</code> if the import has been cancelled,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isCancelled()
	{
		return statusLabel.isMarkedAsCancel();
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
		if (components == null) {
			if (errorBox.isVisible())
				return errorBox.isEnabled() && errorBox.isSelected();
			return false;
		}
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToSend()) 
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if file to reimport, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasFailuresToReimport()
	{
		if (file.isFile()) {
			//if (errorButton.isVisible() && !reimported)
			//	return true;
			return (toReImport && !reimported);
		}
		if (components == null) {
			return false;
		}
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			if (i.next().hasFailuresToReimport()) 
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the folder has components added,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean hasComponents()
	{
		return components != null && components.size() > 0;
	}
	
	/**
	 * Returns one of the following constants: {@link #SUCCESS}, 
	 * {@link #PARTIAL} or {@link #FAILURE}.
	 * 
	 * @return See above.
	 */
	public int getImportStatus()
	{
		if (file.isFile()) {
			if (errorBox.isVisible()) return FAILURE;
			return SUCCESS;
		}
		if (components == null || components.size() == 0) {
			if (image instanceof Boolean) {
				if (file.isDirectory()) {
					if  (isCancelled()) return SUCCESS;
					if (errorBox.isVisible()) return FAILURE;
					return SUCCESS;
				} else {
					if (!StatusLabel.DUPLICATE.equals(resultLabel.getText()))
						return FAILURE;
				}
			} else {
				if (errorBox.isVisible()) return FAILURE;
			}
			return SUCCESS;
		}
			
		Iterator<FileImportComponent> i = components.values().iterator();
		int n = components.size();
		int count = 0;
		while (i.hasNext()) {
			if (i.next().getImportStatus() == FAILURE) 
				count++;
		}
		if (count == n) return FAILURE;
		if (count > 0) return PARTIAL;
		return SUCCESS;
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
		cancel(false);
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
	 * Returns <code>true</code> if the folder has been converted into a
	 * container, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isFolderAsContainer() { return folderAsContainer; }
	
	/**
	 * Returns the object corresponding to the folder.
	 * 
	 * @return See above.
	 */
	public DataObject getContainerFromFolder() { return containerFromFolder; }
	
	/**
	 * Returns <code>true</code> if the extension of the specified file
	 * is a HCS files, <code>false</code> otherwise.
	 * 
	 * @param f The file to handle.
	 * @return See above.
	 */
	public boolean isHCSFile()
	{
		if (isFolderAsContainer()) return false;
		return ImportableObject.isHCSFile(file);
	}
	
	/**
	 * Sets the flag indicating to show or hide the container where the file
	 * has been imported.
	 * 
	 * @param show  Pass <code>true</code> to show, <code>false</code>
	 * 				otherwise.
	 */
	public void showContainerLabel(boolean show)
	{
		showContainerLabel = show;
	}
	
	/**
	 * Returns <code>true</code> if the file has already been marked for
	 * re-import, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public List<FileImportComponent> getReImport()
	{
		List<FileImportComponent> l = null;
		if (file.isFile()) {
			if (toReImport && !reimported) {
				l = new ArrayList<FileImportComponent>();
				l.add(this);
				return l;
			}
		} else {
			if (components != null) {
				Iterator<FileImportComponent> i = components.values().iterator();
				FileImportComponent fc;
				l = new ArrayList<FileImportComponent>();
				List<FileImportComponent> list;
				while (i.hasNext()) {
					fc = i.next();
					list = fc.getReImport();
					if (list != null && list.size() > 0)
						l.addAll(list);
				}
			}
		}
		return l;
	}
	
	/**
	 * Sets to <code>true</code> to mark the file for reimport.
	 * <code>false</code> otherwise.
	 * 
	 * @param Pass <code>true</code> to mark the file for reimport.
	 * <code>false</code> otherwise.
	 */
	public void setReimported(boolean reimported)
	{ 
		this.reimported = reimported;
		reimportedLabel.setVisible(true);
		repaint();
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
			if (isCancelled()) {
				busyLabel.setBusy(false);
				busyLabel.setVisible(false);
				return;
			}
			Map<File, StatusLabel> files = (Map<File, StatusLabel>)
				evt.getNewValue();
			insertFiles(files);
			firePropertyChange(IMPORT_FILES_NUMBER_PROPERTY, null, files.size());
		} else if (StatusLabel.FILE_IMPORT_STARTED_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			if (sl == statusLabel && busyLabel != null) {
				busyLabel.setBusy(true);
				busyLabel.setVisible(true);
				cancelButton.setVisible(sl.isCancellable());
			}
		} else if (StatusLabel.CANCELLABLE_IMPORT_PROPERTY.equals(name)) {
			StatusLabel sl = (StatusLabel) evt.getNewValue();
			cancelButton.setVisible(sl.isCancellable());
		} else if (StatusLabel.FILE_IMPORTED_PROPERTY.equals(name)) {
			Object[] results = (Object[]) evt.getNewValue();
			File f = (File) results[0];
			if (f.getAbsolutePath().equals(file.getAbsolutePath())) {
				Object result = setStatus(false, results[1]);
				firePropertyChange(StatusLabel.FILE_IMPORTED_PROPERTY, null,
						result);
				if (f.isFile()) {
					EventBus bus = ImporterAgent.getRegistry().getEventBus();
					ImportStatusEvent event;
					//Always assume true
					event = new ImportStatusEvent(true, null, result);
					bus.post(event);
					if (hasImportFailed())
						firePropertyChange(IMPORT_STATUS_CHANGE_PROPERTY, null,
								FAILURE);
					else if (isCancelled())
						firePropertyChange(IMPORT_STATUS_CHANGE_PROPERTY, null,
							PARTIAL);
					else firePropertyChange(IMPORT_STATUS_CHANGE_PROPERTY, null,
							SUCCESS);
				}
			}
		} else if (StatusLabel.FILE_RESET_PROPERTY.equals(name)) {
			file = (File) evt.getNewValue();
			fileNameLabel.setText(file.getName());
		} else if (ThumbnailLabel.BROWSE_PLATE_PROPERTY.equals(name)) {
			firePropertyChange(BROWSE_PROPERTY, evt.getOldValue(), 
					evt.getNewValue());
		} else if (StatusLabel.CONTAINER_FROM_FOLDER_PROPERTY.equals(name)) {
			containerFromFolder = (DataObject) evt.getNewValue();
			if (containerFromFolder instanceof DatasetData) {
				containerLabel.setText(TEXT_IMPORTED);
				browseButton.setText(
						((DatasetData) containerFromFolder).getName());
				containerObject = containerFromFolder;
			} else if (containerFromFolder instanceof ScreenData) {
				containerLabel.setText(TEXT_IMPORTED);
				browseButton.setText(
						((ScreenData) containerFromFolder).getName());
				containerObject = containerFromFolder;
			}
		} else if (StatusLabel.NO_CONTAINER_PROPERTY.equals(name)) {
			containerLabel.setText("");
			noContainer = true;
		} else if (StatusLabel.DEBUG_TEXT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		} else if (StatusLabel.FILESET_UPLOADED_PROPERTY.equals(name)) {
			List<String> checksums = (List<String>) evt.getOldValue();
			Map<Integer, String> failingChecksums =
					(Map<Integer, String>) evt.getNewValue();
			
			//TODO: Format and handle checksums.
			/*
			UIUtilities.formatStringListToTable(
					columnNames, formatChecksums(fue.srcFiles, fue.checksums,
							fue.failingChecksums)));
			firePropertyChange(FILESET_UPLOADED_PROPERTY, fue.checksums,
					fue.failingChecksums);
					*/
			// Add code to query for log file and create a download button
		} else if (ThumbnailLabel.VIEW_IMAGE_PROPERTY.equals(name)) {
			//use the group
			SecurityContext ctx = new SecurityContext(group.getId());
			EventBus bus = ImporterAgent.getRegistry().getEventBus();
			ImageData img = (ImageData) evt.getNewValue();
			bus.post(new ViewImage(ctx, new ViewImageObject(img), null));
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
	public void actionPerformed(ActionEvent e)
	{ 
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case DELETE_ID:
				deleteImage();
				break;
			case CANCEL_ID:
				cancel(true);
		}
	}
	
	/**
	 * Returns the name of the file and group's id and user's id.
	 * @see #toString();
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append(getFile().getAbsolutePath());
		if (group != null)
			buf.append("_"+group.getId());
		if (user != null)
			buf.append("_"+user.getId());
		return buf.toString();
	}
}
