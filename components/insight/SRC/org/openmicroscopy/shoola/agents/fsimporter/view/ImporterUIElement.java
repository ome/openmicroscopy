/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUIElement 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.CheckSumDialog;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ImportStatus;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.Environment;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.model.DownloadAndLaunchActivityParam;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.RotationIcon;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.FileAnnotationData;
import pojos.FilesetData;
import pojos.ProjectData;
import pojos.ScreenData;

/** 
 * Component displaying an import.
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
class ImporterUIElement 
	extends ClosableTabbedPaneComponent
{
	
	/** Description of the component. */
	private static final String DESCRIPTION = 
		"Closing will cancel imports that have not yet started.";
	
	/** Text indicating to only show the failure.*/
	private static final String SHOW_FAILURE = "Show Failed";
	
	/** Text indicating to show all the imports.*/
	private static final String SHOW_ALL = "Show All";
	
	/** The columns for the layout of the {@link #entries}. */
	private static final double[] COLUMNS = {TableLayout.FILL};
	
	/** The default size of the icon. */
	private static final Dimension ICON_SIZE = new Dimension(16, 16);
	
	/** Icon used when the import is completed successfully. */
	private static final Icon IMPORT_SUCCESS;
	
	/** Icon used when the import failed. */
	private static final Icon IMPORT_FAIL;
	
	/** Icon used when the import is completed. */
	private static final Icon IMPORT_PARTIAL;
	
	static {
		IconManager icons = IconManager.getInstance();
		IMPORT_SUCCESS = icons.getIcon(IconManager.APPLY);
		IMPORT_FAIL = icons.getIcon(IconManager.DELETE);
		IMPORT_PARTIAL = icons.getIcon(IconManager.APPLY_CANCEL);
	}
	
	/** The message to display in the header.*/
	private static final String MESSAGE = 
			"When upload is complete, the import\n" +
			"window and OMERO session can be closed.\n" +
			"Reading will continue on the server.";
	
	/** The object hosting information about files to import. */
	private ImportableObject object;
	
	/** The components to lay out. */
	private LinkedHashMap<String, FileImportComponent>	components;

	/** Component hosting the entries. */
	private JPanel	entries;

	/** The number of cancellation.*/
	private int countCancelled;
	
	/** The number of uploaded files.*/
	private int countUploaded;
	
	/** The number of files/folder imported. */
	private int countImported;
	
	/** The number of files uploaded. */
	private int countUploadFailure;
	
	/** 
	 * The number of failures that occurred during scanning, uploading
	 * or processing.
	 */
	private int countFailure;
	
	/** The total number of files or folder to import. */
	private int totalToImport;
	
	/** The size of the import. */
	private long sizeImport;
	
	/** The component displaying the size the import. */
	private JLabel sizeLabel;
	
	/** The component displaying the number of files to import. */
	private JLabel numberOfImportLabel;

	/** Label for report display */
	private JLabel reportLabel;

	/** Label for import size display */
	private JLabel importSizeLabel;
			
	/** The identifier of the component. */
	private int id;
	
	/** The collection of folders' name used as dataset. */
	private Map<JLabel, Object> foldersName;

	/** Reference to the view. */
	private ImporterUI view;
	
	/** Reference to the controller. */
	private ImporterControl controller;
	
	/** Reference to the controller. */
	private ImporterModel model;

	/** The type of container to handle. */
	private int type;
	
	/** The existing containers. */
	private List<DataObject> existingContainers;

	/** The busy label. */
	private JXBusyLabel busyLabel;
	
	/**The icon used to indicate an on-going import.*/
	private RotationIcon rotationIcon;
	
	/** Controls used to filter the result.*/
	private JButton filterButton;
	
	/** Flag indicating if the upload has started or not.*/
	private boolean uploadStarted;
	
	/**
	 * Returns the object found by identifier.
	 * 
	 * @param data The object to handle.
	 * @param result The collection of element to check.
	 * @return See above.
	 */
	private DataObject getObjectFromID(DataObject data, Collection result)
	{
		Iterator i = result.iterator();
		DataObject object;
		while (i.hasNext()) {
			object = (DataObject) i.next();
			if (object.getClass().equals(data.getClass()) 
					&& object.getId() == data.getId()) {
				return object;
			}
		}
		return null;
	}
	
	/**
	 * Returns the object found by name.
	 * 
	 * @param data The object to handle.
	 * @param result The collection of element to check.
	 * @return See above.
	 */
	private DataObject getObject(DataObject data, Collection result)
	{
		String name = "";
		if (data instanceof ProjectData) {
			name = ((ProjectData) data).getName();
		} else if (data instanceof ScreenData) {
			name = ((ScreenData) data).getName();
		} else if (data instanceof DatasetData) {
			name = ((DatasetData) data).getName();
		}
		Iterator i = result.iterator();
		DataObject object;
		String n = "";
		while (i.hasNext()) {
			object = (DataObject) i.next();
			if (object.getClass().equals(data.getClass())) {
				if (object instanceof ProjectData) {
					n = ((ProjectData) object).getName();
				} else if (object instanceof ScreenData) {
					n = ((ScreenData) object).getName();
				} else if (object instanceof DatasetData) {
					n = ((DatasetData) object).getName();
				}
				if (n.equals(name)) return object;
			}
		}
		return null;
	}
	
	/** Sets the text of indicating the number of imports. */
	private void setNumberOfImport()
	{
		StringBuffer buffer = new StringBuffer();
		int n = countUploaded-countUploadFailure-countCancelled;
		if (n < 0) n = 0;
		buffer.append(n);
		buffer.append(" out of ");
		buffer.append(totalToImport);
		buffer.append(" uploaded");
		numberOfImportLabel.setText(buffer.toString());
	}
	
	/**
	 * Browses the specified object.
	 * 
	 * @param data The object to handle.
	 * @param node The node hosting the object to browse or <code>null</code>.
	 */ 
	private void browse(Object data, Object node)
	{
		EventBus bus = ImporterAgent.getRegistry().getEventBus();
		if (data instanceof TreeImageDisplay || data instanceof DataObject) {
			bus.post(new BrowseContainer(data, node));
		} else if (data instanceof FileImportComponent) {
			FileImportComponent fc = (FileImportComponent) data;
			if (fc.getContainerFromFolder() != null)
				bus.post(new BrowseContainer(fc.getContainerFromFolder(), 
						node));
		}
	}

	/**
	 * Displays only the failures or all the results.
	 */
	private void filterFailures()
	{
		String v = filterButton.getText();
		if (SHOW_FAILURE.equals(v)) {
			filterButton.setText(SHOW_ALL);
			layoutEntries(true);
		} else {
			filterButton.setText(SHOW_FAILURE);
			layoutEntries(false);
		}
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		filterButton = new JButton(SHOW_FAILURE);
		filterButton.setEnabled(false);
		filterButton.addActionListener(new ActionListener() {
			
			/**
			 * Filters or not the import that did not fail.
			 */
			public void actionPerformed(ActionEvent evt) {
				filterFailures();
			}
		});
		sizeImport = 0;
		busyLabel = new JXBusyLabel(ICON_SIZE);
		numberOfImportLabel = UIUtilities.createComponent(null);
		foldersName = new LinkedHashMap<JLabel, Object>();
		countUploadFailure = 0;
		countFailure = 0;
		countUploaded = 0;
		addPropertyChangeListener(controller);
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		components = new LinkedHashMap<String, FileImportComponent>();
		List<ImportableFile> files = object.getFiles();
		FileImportComponent c;
		
		Iterator<ImportableFile> i = files.iterator();
		ImportableFile importable;
		type = -1;
		List<Object> containers = object.getRefNodes();
		if (containers != null && containers.size() > 0) {
			Iterator<Object> j = containers.iterator();
			TreeImageDisplay node;
			Object h;
			while (j.hasNext()) {
				node = (TreeImageDisplay) j.next();
				h = node.getUserObject();
				if (h instanceof DatasetData) {
					type = FileImportComponent.DATASET_TYPE;
				} else if (h instanceof ScreenData) {
					type = FileImportComponent.SCREEN_TYPE;
				} else if (h instanceof ProjectData) {
					type = FileImportComponent.PROJECT_TYPE;
				}
				break;
			}
		} else {
			type = FileImportComponent.NO_CONTAINER;
		}
		JLabel l;
		boolean single = model.isSingleGroup();
		FileObject f;
		while (i.hasNext()) {
			importable = i.next();
			f = (FileObject) importable.getFile();
			c = new FileImportComponent(importable,
					!controller.isMaster(), single, getID(), object.getTags());
			c.setType(type);
			c.addPropertyChangeListener(controller);
			c.addPropertyChangeListener(new PropertyChangeListener() {
				
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (FileImportComponent.BROWSE_PROPERTY.equals(name)) {
						List<Object> refNodes = object.getRefNodes();
						Object node = null;
						if (refNodes != null && refNodes.size() > 0)
							node = refNodes.get(0);
						browse(evt.getNewValue(), node);
					} else if (
						FileImportComponent.IMPORT_FILES_NUMBER_PROPERTY.equals(
								name)) {
						//-1 to remove the entry for the folder.
						Integer v = (Integer) evt.getNewValue()-1;
						totalToImport += v;
						setNumberOfImport();
					} else if (FileImportComponent.LOAD_LOGFILEPROPERTY.equals(
							name)) {
						FileImportComponent fc = (FileImportComponent)
								evt.getNewValue();
						if (fc == null) return;
						long logFileID = fc.getStatus().getLogFileID();
						if (logFileID <= 0) {
							FilesetData data = fc.getStatus().getFileset();
							if (data == null) return;
							model.fireImportLogFileLoading(data.getId(),
									fc.getIndex());
						} else downloadLogFile(logFileID);
					} else if (
						FileImportComponent.RETRIEVE_LOGFILEPROPERTY.equals(
							name)) {
						FilesetData data = (FilesetData) evt.getNewValue();
						if (data != null)
							model.fireImportLogFileLoading(data.getId(), id);
					} else if (
						FileImportComponent.CHECKSUM_DISPLAY_PROPERTY.equals(
							name)) {
						StatusLabel label = (StatusLabel) evt.getNewValue();
						CheckSumDialog d = new CheckSumDialog(view, label);
						UIUtilities.centerAndShow(d);
					} else if (FileImportComponent.RETRY_PROPERTY.equals(name)) {
						controller.retryUpload(
								(FileImportComponent) evt.getNewValue());
					} else if (
						FileImportComponent.CANCEL_IMPORT_PROPERTY.equals(name)) {
						controller.cancel(
								(FileImportComponent) evt.getNewValue());
					}
				}
			});
			if (f.isDirectory()) {
				if (importable.isFolderAsContainer()) {
					l = new JLabel(f.getName());
					foldersName.put(l, c);
				}
			} else {
				if (importable.isFolderAsContainer()) {
					String name = f.getParentName();
					//first check if the name is already there.
					Entry<JLabel, Object> entry;
					Iterator<Entry<JLabel, Object>>
					k = foldersName.entrySet().iterator();
					boolean exist = false;
					while (k.hasNext()) {
						entry = k.next();
						l = entry.getKey();
						if (l.getText().equals(name)) {
							exist = true;
							break;
						}
					}
					if (name == null) {
					    name = f.getName();
					}
					if (!exist) {
						foldersName.put(new JLabel(name), c);
					}
				}
			}
			importable.setStatus(c.getStatus());
			components.put(c.toString(), c);
		}
		totalToImport = files.size();
	}
	
	/**
	 * Downloads the log file.
	 * 
	 * @param logFileID
	 */
	void downloadLogFile(long logFileID)
	{
		if (logFileID < 0) return;
		Environment env = (Environment) 
				ImporterAgent.getRegistry().lookup(
						LookupNames.ENV);
		String path = env.getOmeroFilesHome();
		File f = new File(path, "importLog_"+logFileID);
		DownloadAndLaunchActivityParam
		activity = new DownloadAndLaunchActivityParam(logFileID,
				DownloadAndLaunchActivityParam.ORIGINAL_FILE,
				f, null);
		activity.setUIRegister(false);
		UserNotifier un =
				ImporterAgent.getRegistry().getUserNotifier();
		un.notifyActivity(model.getSecurityContext(), activity);
	}
	
	/** 
	 * Builds a row.
	 * 
	 * @return See above.
	 */
	private JPanel createRow()
	{
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setBackground(UIUtilities.BACKGROUND_COLOR);
		return p;
	}
	
	/**
	 * Make the JTextArea represent a multiline label.
	 * 
	 * @param textArea The component to handle.
	 */
	private void makeLabelStyle(JTextArea textArea)
	{
		if (textArea == null) return;
		textArea.setEditable(false);
		textArea.setCursor(null);
		textArea.setOpaque(false);
		textArea.setFocusable(false);
	}
	
	/** 
	 * Builds and lays out the header.
	 * 
	 * @return See above.
	 */
	private JPanel buildHeader()
	{
		sizeLabel = UIUtilities.createComponent(null);
		sizeLabel.setText(FileUtils.byteCountToDisplaySize(sizeImport));
		reportLabel = UIUtilities.setTextFont("Report:", Font.BOLD);
		importSizeLabel = UIUtilities.setTextFont("Import Size:", Font.BOLD);
		double[][] design = new double[][]{
					{TableLayout.PREFERRED},
					{TableLayout.PREFERRED, TableLayout.PREFERRED}
				};
		TableLayout layout = new TableLayout(design);
		JPanel detailsPanel = new JPanel(layout);
		detailsPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		JPanel p = createRow();
		p.add(reportLabel);
		p.add(numberOfImportLabel);
		detailsPanel.add(p, "0, 0");
		p = createRow();
		p.add(importSizeLabel);
		p.add(sizeLabel);
		detailsPanel.add(p, "0, 1");
		
		JPanel middlePanel = new JPanel();
		middlePanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		middlePanel.add(filterButton);
		
    	JTextArea description = new JTextArea(MESSAGE);
    	makeLabelStyle(description);
    	description.setBackground(UIUtilities.BACKGROUND_COLOR);
    	
		JPanel descriptionPanel = new JPanel();
		descriptionPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
		descriptionPanel.add(description);
		
		JPanel header = new JPanel();
		header.setBackground(UIUtilities.BACKGROUND_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		header.setLayout(new BorderLayout());
		
		header.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
		header.add(detailsPanel, BorderLayout.WEST);
		header.add(middlePanel, BorderLayout.CENTER);
		header.add(descriptionPanel, BorderLayout.EAST);
		header.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
		
		return header;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		layoutEntries(false);
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		setLayout(new BorderLayout(0, 0));
		add(buildHeader(), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
	}
	
	/** 
	 * Lays out the entries.
	 * 
	 * @param failure Pass <code>true</code> to display the failed import only,
	 * <code>false</code> to display all the entries.
	 */
	private void layoutEntries(boolean failure)
	{
		entries.removeAll();
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		int index = 0;
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		if (failure) {
			while (i.hasNext()) {
				entry = i.next();
				fc = entry.getValue();
				if (fc.hasComponents()) {
					addRow(layout, index, entry.getValue());
					fc.layoutEntries(failure);
					index++;
				} else {
					if (fc.hasImportFailed()) {
						addRow(layout, index, entry.getValue());
						index++;
					}
				}
			}
		} else {
			while (i.hasNext()) {
				entry = i.next();
				fc = entry.getValue();
				addRow(layout, index, fc);
				fc.layoutEntries(failure);
				index++;
			}
		}
		
		entries.revalidate();
		repaint();
		setNumberOfImport();
	}
	
	/**
	 * Adds a new row.
	 * 
	 * @param layout The layout.
	 * @param index	 The index of the row.
	 * @param c		 The component to add.
	 */
	private void addRow(TableLayout layout, int index, FileImportComponent c)
	{
		layout.insertRow(index, TableLayout.PREFERRED);
		if (index%2 == 0)
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		else 
			c.setBackground(UIUtilities.BACKGROUND_COLOUR_ODD);
		entries.add(c, new TableLayoutConstraints(0, index));
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 * @param view Reference to the model. Mustn't be <code>null</code>.
	 * @param id The identifier of the component.
	 * @param index The index of the component.
	 * @param name The name of the component.
	 * @param object the object to handle. Mustn't be <code>null</code>.
	 */
	ImporterUIElement(ImporterControl controller, ImporterModel model,
			ImporterUI view, int id, int index, String name,
			ImportableObject object)
	{
		super(index, name, DESCRIPTION);
		if (object == null) 
			throw new IllegalArgumentException("No object specified.");
		if (controller == null)
			throw new IllegalArgumentException("No Control.");
		if (model == null)
			throw new IllegalArgumentException("No Model.");
		if (view == null)
			throw new IllegalArgumentException("No View.");
		this.controller = controller;
		this.model = model;
		this.view = view;
		this.id = id;
		this.object = object;
		initialize();
		buildGUI();
	}

	/**
	 * Returns the identifier of the component.
	 * 
	 * @return See above.
	 */
	int getID() { return id; }
	
	/**
	 * Returns the formatted result.
	 * 
	 * @param f The imported file.
	 * @return See above.
	 */
	Object getFormattedResult(ImportableFile f)
	{
		FileImportComponent c = components.get(f.toString());
		if (c == null) return null;
		ImportErrorObject object = c.getImportErrorObject();
		if (object != null) return object;
		
		return null;
	}
	

	/**
	 * Sets the result of the import for the specified file.
	 * 
	 * @param f The imported file.
	 * @param result The result.
	 * @result Returns the formatted result or <code>null</code>.
	 */
	Object uploadComplete(ImportableFile f, Object result)
	{
		FileImportComponent c = components.get(f.toString());
		return uploadComplete(c, result);
	}
	
	/**
	 * Sets the result of the import for the specified file.
	 * 
	 * @param f The imported file.
	 * @param result The result.
	 * @param index The index corresponding to the component
	 * @result Returns the formatted result or <code>null</code>.
	 */
	Object uploadComplete(FileImportComponent c, Object result)
	{
		if (c == null) return null;
		c.uploadComplete(result);
		FileObject file = c.getFile();
		Object r = null;
		if (file.isFile()) {
			countUploaded++;
			sizeImport += c.getImportSize();
			sizeLabel.setText(FileUtils.byteCountToDisplaySize(sizeImport));
			//handle error that occurred during the scanning or upload.
			//Check that the result has not been set.
			//if (!c.hasResult()) {
			if (result instanceof Exception) {
				r = new ImportErrorObject(file.getTrueFile(), (Exception) result,
						c.getGroupID());
				if (c.hasResult()) return null;
				setImportResult(c, result);
			} else if (result instanceof Boolean) {
				Boolean b = (Boolean) result;
				if (!b && c.isCancelled()) {
					countUploaded--;
					if (isDone() && rotationIcon != null)
						rotationIcon.stopRotation();
				} else
				setImportResult(c, result);
			} else {
				if (c.isCancelled()) {
					if (result == null) {
						countCancelled++;
						countImported++;
						if (isDone() && rotationIcon != null)
							rotationIcon.stopRotation();
					} else {
						countCancelled--;
						countUploaded--;
					}
				}
			}
			//}
		} else {//empty folder
			if (result instanceof Exception) {
				countUploaded++;
				//Check if no files
				if (!c.hasComponents()) {
					countImported++;
					countUploadFailure++;
					c.setStatus(result);
				}
				if (isDone() && rotationIcon != null)
					rotationIcon.stopRotation();
			} else if (result instanceof Boolean) {
				Boolean b = (Boolean) result;
				if (!b && c.isCancelled()) {
					countUploaded++;
					countCancelled++;
					countImported++;
					if (isDone() && rotationIcon != null)
						rotationIcon.stopRotation();
				}
			}
		}
		setNumberOfImport();
		setClosable(isUploadComplete());
		return r;
	}
	
	/**
	 * Sets the result of the import for the specified file.
	 * 
	 * @param fc The component hosting the file to import.
	 * @param result The result.
	 * @result Returns the formatted result or <code>null</code>.
	 */
	void setImportResult(FileImportComponent fc, Object result)
	{
		if (fc == null) return;
		FileObject file = fc.getFile();
		if (file.isFile()) {
			fc.setStatus(result);
			countImported++;
			if (fc.isCancelled() && result != null &&
				!(result instanceof Boolean))
				countImported--;
			if (isDone() && rotationIcon != null)
				rotationIcon.stopRotation();
			if (fc.hasUploadFailed()) {
				countUploadFailure++;
				sizeImport -= fc.getImportSize();
				sizeLabel.setText(FileUtils.byteCountToDisplaySize(sizeImport));
			}
			if (fc.hasImportFailed()) countFailure++;
			setNumberOfImport();
			setClosable(isDone());
			filterButton.setEnabled(countFailure > 0 &&
					countFailure != totalToImport);
		} else { //empty folder
			if (result instanceof Exception) {
				fc.setStatus(result);
				countImported++;
				countFailure++;
				countUploadFailure++;
				if (isDone() && rotationIcon != null)
					rotationIcon.stopRotation();
				setNumberOfImport();
				setClosable(isDone());
			}
		}
	}

	/**
	 * Returns <code>true</code> if the upload is finished, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUploadComplete() { return countUploaded == totalToImport; }
	
	/**
	 * Returns <code>true</code> if the import is finished, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isDone() { return countImported == totalToImport; }
	
	/**
	 * Returns <code>true</code> if there is one remaining import,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isLastImport() { return countImported == (totalToImport-1); }
	
	/** 
	 * Indicates that the import has started. 
	 * 
	 * @param component The component of reference of the rotation icon.
	 */
	Icon startImport(JComponent component)
	{
		uploadStarted = true;
		setClosable(false);
		busyLabel.setBusy(true);
		repaint();
		return new RotationIcon(busyLabel.getIcon(), component, true);
	}
	
	/**
	 * Returns <code>true</code> if the import has started, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasStarted() { return uploadStarted; }
	
	/**
	 * Manually sets the uploadStarted flag
	 * @param uploadStarted
	 */
	void setUploadStarted(boolean uploadStarted) {
	    this.uploadStarted = uploadStarted;
	}
	
	/**
	 * Returns <code>true</code> if the component has imports in the queue that
	 * have not yet started or been cancelled, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasImportToCancel()
	{
	    for (final FileImportComponent fic : components.values()) {
	    	if (fic.hasImportToCancel()) {
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getMarkedFiles()
	{
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		List<FileImportComponent> l;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			l = fc.getImportErrors();
			if (l != null && l.size() > 0)
				list.addAll(l);
		}
		return list;
	}
	
	/**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getFilesToReupload()
	{
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		List<FileImportComponent> l;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			l = fc.getFilesToReupload();
			if (!CollectionUtils.isEmpty(l))
				list.addAll(l);
		}
		return list;
	}
	
	/**
	 * Returns the object to import.
	 * 
	 * @return See above.
	 */
	ImportableObject getData() { return object; }
	
	/**
	 * Returns the existing containers.
	 * 
	 * @return See above.
	 */
	List<DataObject> getExistingContainers()
	{
		if (existingContainers != null) return existingContainers;
		existingContainers = new ArrayList<DataObject>();
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		Map<Long, DatasetData> datasets = new HashMap<Long, DatasetData>();
		Map<Long, DataObject> projects = new HashMap<Long, DataObject>();
		Map<Long, DataObject> screens = new HashMap<Long, DataObject>();
		DatasetData d;
		DataObject object;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			d = fc.getDataset();
			if (d != null && d.getId() > 0)
				datasets.put(d.getId(), d);
			object = fc.getDataObject();
			if (object instanceof ScreenData && object.getId() > 0)
				screens.put(object.getId(), object);
			if (object instanceof ProjectData && object.getId() > 0) {
				if (d != null && d.getId() <= 0)
					projects.put(object.getId(), object);
			}
		}
		existingContainers.addAll(datasets.values());
		existingContainers.addAll(projects.values());
		existingContainers.addAll(screens.values());
		return existingContainers;
	}

	/**
	 * Sets the import log file for each import component.
	 *
	 * @param data Collection of file annotations linked to the file set.
	 * @param id The id of the file set.
	 */
	void setImportLogFile(Collection<FileAnnotationData> data, long id)
	{
	    if (CollectionUtils.isEmpty(data)) return;
	    Entry<String, FileImportComponent> entry;
	    Iterator<Entry<String, FileImportComponent>>
	    i = components.entrySet().iterator();
	    FileImportComponent fc;
	    Iterator<FileAnnotationData> j;
	    FileAnnotationData fa;
	    while (i.hasNext()) {
	        entry = i.next();
	        fc = entry.getValue();
	        if (fc.getIndex() == id) {
	            j = data.iterator();
	            while (j.hasNext()) {
	                fa = j.next();
	                if (FileAnnotationData.LOG_FILE_NS.equals(
	                        fa.getNameSpace())) {
	                    downloadLogFile(fa.getFileID());
	                    break;
	                }
	            }
	        }
	    }
	}

	/**
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToSend()
	{
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			if (fc.hasFailuresToSend())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if files to reimport, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToReimport()
	{
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			if (fc.hasFailuresToReimport())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if files to re-upload, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToReupload()
	{
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			if (fc.hasFailuresToReupload())
				return true;
		}
		return false;
	}
	
	/** Indicates that the import has been cancelled. */
	void cancelLoading()
	{
		if (components == null || components.size() == 0) return;
		Iterator<FileImportComponent> i = components.values().iterator();
		while (i.hasNext()) {
			i.next().cancelLoading();
		}
	}

	/**
	 * Returns <code>true</code> if the view should be refreshed,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasToRefreshTree()
	{
		Entry<String, FileImportComponent> entry;
		Iterator<Entry<String, FileImportComponent>>
		i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = i.next();
			fc = entry.getValue();
			if (fc.hasToRefreshTree())
				return true;
		}
		return false;
	}
	
	/**
	 * Returns the icon indicating the status of the import.
	 * 
	 * @return See above.
	 */
	Icon getImportIcon()
	{ 
		if (isDone()) {
			Iterator<Entry<String, FileImportComponent>>
			i = components.entrySet().iterator();
			FileImportComponent fc;
			Entry<String, FileImportComponent> entry;
			int failure = 0;
			ImportStatus v;
			while (i.hasNext()) {
				entry = i.next();
				fc = entry.getValue();
				v = fc.getImportStatus();
				if (v == ImportStatus.PARTIAL)
					return IMPORT_PARTIAL;
				if (v == ImportStatus.FAILURE) failure++;
			}
			if (failure == components.size()) return IMPORT_FAIL;
			else if (failure > 0) return IMPORT_PARTIAL;
			return IMPORT_SUCCESS;
		}
		return busyLabel.getIcon();
	}
	
	/** Invokes when the import is finished. */
	void onImportEnded()
	{ 
		busyLabel.setBusy(false);
		setClosable(true);
	}

	/**
	 * Resets the containers in the file to load.
	 * 
	 * @param result The containers to reset.
	 */
	void resetContainers(Collection result)
	{
		if (result == null || result.size() == 0) return;
		List<ImportableFile> files = getData().getFiles();
		if (files == null || files.size() == 0) return;
		Iterator<ImportableFile> i = files.iterator();
		ImportableFile f;
		DataObject parent;
		DatasetData dataset;
		DataObject data;
		ProjectData p;
		DatasetData r;
		while (i.hasNext()) {
			f = i.next();
			parent = f.getParent();
			dataset = f.getDataset();
			if (parent != null) {
				if (parent.getId() <= 0) { //new project or screen
					data = getObject(parent, result);
					r = null;
					if (dataset != null) {
						if (dataset.getId() <= 0) {
							//data is a project
							r = dataset;
							p = (ProjectData) data;
							r = (DatasetData) 
								getObject(dataset, p.getDatasets());
						}
					} 
					f.setLocation(data, r);
				} else { //was already created.
					if (dataset != null) {
						if (dataset.getId() <= 0) {
							data = getObjectFromID(parent, result);
							//data is a project
							r = dataset;
							p = (ProjectData) data;
							r = (DatasetData) 
								getObject(dataset, p.getDatasets());
							f.setLocation(data, r);
						}
					}
				}
			} else { //no parent
				if (dataset != null) {
					if (dataset.getId() <= 0) {
						//data is a project
						r = dataset;
						r = (DatasetData) 
							getObject(dataset, result);
						f.setLocation(null, r);
					}
				}
			}
		}
	}

}
