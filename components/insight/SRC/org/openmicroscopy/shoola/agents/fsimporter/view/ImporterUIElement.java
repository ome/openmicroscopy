/*
 * org.openmicroscopy.shoola.agents.fsimporter.view.ImporterUIElement 
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
package org.openmicroscopy.shoola.agents.fsimporter.view;


//Java imports
import java.awt.BorderLayout;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

//Third-party libraries
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.importer.BrowseContainer;
import org.openmicroscopy.shoola.agents.events.importer.ImportStatusEvent;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponent;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

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
	
	/** The object hosting information about files to import. */
	private ImportableObject object;
	
	/** The components to lay out. */
	private LinkedHashMap<String, FileImportComponent>	components;

	/** Component hosting the entries. */
	private JPanel	entries;

	/** The number of files/folder imported. */
	private int countImported;
	
	/** The number of files imported. */
	private int countFilesImported;
	
	/** The number of files imported. */
	private int countCancelled;
	
	/** The number of files imported. */
	private int countFailure;
	
	/** The total number of files or folder to import. */
	private int totalToImport;
	
	/** The total number of files to import.*/
	private int totalFilesToImport;
	
	/** The time when the import started. */
	private long startImport;
	
	/** The component displaying the duration of the import. */
	private JLabel timeLabel;
	
	/** The component displaying the number of files to import. */
	private JLabel numberOfImportLabel;
	
	/** The identifier of the component. */
	private int id;
	
	/** The collection of folders' name used as dataset. */
	private Map<JLabel, Object> foldersName;
	
	/** Flag indicating that the images will be added to the default dataset. */
	private boolean orphanedFiles;
	
	/** Reference to the controller. */
	private ImporterControl controller;
	
	/** The components displaying the components. */
	private Map<JLabel, Object> containerComponents;
	
	/** The components displaying the components. */
	private Map<JLabel, Object> topContainerComponents;
	
	/** Flag indicating to refresh the topContainer. */
	private boolean topContainerToRefresh;
	
	/** The listener associated to the folder. */
	private MouseAdapter folderListener;
	
	/** The listener associated to the container. */
	private MouseAdapter containerListener;
	
	/** The type of container to handle. */
	private int type;
	
	/** The existing containers. */
	private List<DataObject> existingContainers;

	/** The busy label. */
	private JXBusyLabel		busyLabel;
	
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
		String n = "";
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
		buffer.append(" out of ");
		buffer.append(totalFilesToImport);
		buffer.append(" imported: "+countFilesImported);
		if (countCancelled > 0)
			buffer.append(" cancelled: "+countCancelled);
		if (countFailure > 0)
			buffer.append(" failed: "+countFailure);
		//numberOfImportLabel.setText(countImported+" of "+totalToImport);
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
	 * Returns the label hosting the passed object.
	 * 
	 * @param data The object to handle.
	 * @return See above.
	 */
	private JLabel createNameLabel(Object data)
	{
		JLabel label = new JLabel();
		boolean browse = false;
		String name = "";
		if (data instanceof DatasetData) {
			browse = true;
			name = ((DatasetData) data).getName();
		} else if (data instanceof ScreenData) {
			browse = true;
			name = ((ScreenData) data).getName();
		} else if (data instanceof ProjectData) {
			browse = true;
			name = ((ProjectData) data).getName();
		}
		label.setEnabled(browse);
		if (browse) {
			label.setBackground(UIUtilities.BACKGROUND_COLOR);
			label.setToolTipText("Browse when import completed.");
		}
		label.setText(name);
		return label;
	}
	
	/** Initializes the components. */
	private void initialize()
	{
		busyLabel = new JXBusyLabel(ICON_SIZE);
		
		numberOfImportLabel = UIUtilities.createComponent(null);
		containerComponents = new LinkedHashMap<JLabel, Object>();
		topContainerComponents = new LinkedHashMap<JLabel, Object>();
		foldersName = new LinkedHashMap<JLabel, Object>();
		folderListener = new MouseAdapter() {
			
			/**
			 * Browses the object the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				Object src = e.getSource();
				if (e.getClickCount() == 1 && src instanceof JLabel) {
					browse(foldersName.get((JLabel) src), null);
				}
			}
		};
		containerListener = new MouseAdapter() {
			
			/**
			 * Browses the object the image.
			 * @see MouseListener#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				Object src = e.getSource();
				if (e.getClickCount() == 1 && src instanceof JLabel) {
					browse(containerComponents.get((JLabel) src), null);
				}
			}
		};
		countImported = 0;
		countCancelled = 0;
		countFailure = 0;
		countFilesImported = 0;
		//setClosable(true);
		addPropertyChangeListener(controller);
		entries = new JPanel();
		entries.setBackground(UIUtilities.BACKGROUND);
		components = new LinkedHashMap<String, FileImportComponent>();
		List<ImportableFile> files = object.getFiles();
		FileImportComponent c;
		File f;
		Iterator<ImportableFile> i = files.iterator();
		orphanedFiles = false;
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
		int count = 0;
		while (i.hasNext()) {
			importable = i.next();
			f = (File) importable.getFile();
			c = new FileImportComponent(f, importable.isFolderAsContainer(),
					!controller.isMaster());
			c.setLocation(importable.getParent(), importable.getDataset(), 
					importable.getRefNode());
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
						totalFilesToImport += v;
						setNumberOfImport();
					} else if (
						FileImportComponent.IMPORT_STATUS_CHANGE_PROPERTY.equals(
								name)) {
						Integer v = (Integer) evt.getNewValue();
						switch (v) {
							case FileImportComponent.FAILURE:
								countFailure++;
								break;
							case FileImportComponent.PARTIAL:
								countCancelled++;
								break;
							default:
								countFilesImported++;
						}
						setNumberOfImport();
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
					String name = f.getParentFile().getName();
					//first check if the name is already there.
					Entry entry;
					Iterator k = foldersName.entrySet().iterator();
					boolean exist = false;
					while (k.hasNext()) {
						entry = (Entry) k.next();
						l = (JLabel) entry.getKey();
						if (l.getText().equals(name)) {
							exist = true;
							break;
						}
					}
					if (!exist) {
						foldersName.put(new JLabel(name), c);
					}
				}
				if (!c.isHCSFile()) count++;
			}
			importable.setStatus(c.getStatus());
			components.put(f.getAbsolutePath(), c);
		}
		List<DataObject> objects = getExistingContainers();
		int n = objects.size();
		if (n == 1) { //only one.
			DataObject o = objects.get(0);
			containerComponents.put(createNameLabel(o), o);
			Iterator<FileImportComponent> j = components.values().iterator();
			while (j.hasNext()) {
				j.next().showContainerLabel(false);
			}
		} else if (n == 0) {
			if (foldersName.size() == 1) {
				Iterator<FileImportComponent> j = components.values().iterator();
				while (j.hasNext()) {
					j.next().showContainerLabel(false);
				}
			}
		}
		totalToImport = files.size();
		totalFilesToImport = files.size();
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
	 * Builds and lays out the header.
	 * 
	 * @return See above.
	 */
	private JPanel buildHeader()
	{
		JPanel header = new JPanel();
		header.setBackground(UIUtilities.BACKGROUND_COLOR);
		header.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
		
		JLabel label = UIUtilities.setTextFont("Report:", Font.BOLD);
		JPanel row = createRow();
		row.add(label);
		row.add(numberOfImportLabel);
		header.add(row);
		row = createRow();
		label = UIUtilities.setTextFont("Import Time:", Font.BOLD);
		timeLabel = UIUtilities.createComponent(null);
		timeLabel.setText(UIUtilities.formatShortDateTime(null));
    	row.add(label);
    	row.add(timeLabel);
    	header.add(row);
    	Collection<TagAnnotationData> tags = object.getTags();
		if (tags != null && tags.size() > 0) {
			row = createRow();
			label = UIUtilities.setTextFont("Images Tagged with: ", Font.BOLD);
			JLabel value = UIUtilities.createComponent(null);
			StringBuffer buffer = new StringBuffer();
			Iterator<TagAnnotationData> i = tags.iterator();
			int index = 0;
			int n = tags.size()-1;
			while (i.hasNext()) {
				buffer.append((i.next()).getTagValue());
				if (index < n) buffer.append(", ");
				index++;
			}
			value.setText(buffer.toString());
			row.add(label);
			row.add(value);
			header.add(row);
		}
		/*
		header.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0, 2, 2, 0);
		c.gridy = 0;
		c.gridx = 0;
		JLabel label = UIUtilities.setTextFont(
				"The number of files/folders imported:", Font.BOLD);
		JLabel value;
    	header.add(label, c);
    	c.gridx = c.gridx+2;
    	header.add(numberOfImportLabel, c);
    	c.gridy++;
    	c.gridx = 0;
    	label = UIUtilities.setTextFont("Import Time:", Font.BOLD);
		startImport = System.currentTimeMillis();
		timeLabel = UIUtilities.createComponent(null);
		timeLabel.setText(UIUtilities.formatShortDateTime(null));
    	header.add(label, c);
    	c.gridx = c.gridx+2;
    	header.add(timeLabel, c);
    	c.gridy++; 	
    	c.gridx = 0;
    	int n;
		Collection<TagAnnotationData> tags = object.getTags();
		if (tags != null && tags.size() > 0) {
			label = UIUtilities.setTextFont("Images Tagged with: ", Font.BOLD);
			value = UIUtilities.createComponent(null);
			StringBuffer buffer = new StringBuffer();
			Iterator<TagAnnotationData> i = tags.iterator();
			int index = 0;
			n = tags.size()-1;
			while (i.hasNext()) {
				buffer.append((i.next()).getTagValue());
				if (index < n) buffer.append(", ");
				index++;
			}
			value.setText(buffer.toString());
			header.add(label, c);
	    	c.gridx = c.gridx+2;
	    	header.add(value, c);
	    	c.gridy++; 	
	    	c.gridx = 0;
		}
		*/
		topContainerToRefresh = topContainerToRefresh();
		JPanel content = UIUtilities.buildComponentPanel(header);
		content.setBackground(UIUtilities.BACKGROUND_COLOR);
		return content;
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		layoutEntries();
		JScrollPane pane = new JScrollPane(entries);
		pane.setOpaque(false);
		pane.setBorder(new LineBorder(Color.LIGHT_GRAY));
		setLayout(new BorderLayout(0, 0));
		add(buildHeader(), BorderLayout.NORTH);
		add(pane, BorderLayout.CENTER);
	}
	
	/** Lays out the entries. */
	private void layoutEntries()
	{
		entries.removeAll();
		TableLayout layout = new TableLayout();
		layout.setColumn(COLUMNS);
		entries.setLayout(layout);
		int index = 0;
		Entry entry;
		
		FileImportComponent c;
		Iterator i = components.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			c = (FileImportComponent) entry.getValue();
			addRow(layout, index, c);
			index++;
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
		//entries.add(c.getNameLabel(), "0, "+index);
		entries.add(c, "0, "+index+"");
	}
	
	/**
	 * Returns <code>true</code> if some files were imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean toRefresh()
	{
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
			if (fc.toRefresh()) 
				return true;
		}
		return false;
	}
	
	/**
	 * Returns <code>true</code> if the top container has to be refreshed,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean topContainerToRefresh()
	{
		List<DataObject> l = getExistingContainers();
		if (l == null || l.size() == 0) return false;
		DataObject object = l.get(0);
		if (!(object instanceof ProjectData)) return false;
		//DatasetData d = getData().getDefaultDataset();
		//if (d != null && d.getId() <= 0) return true;
		Iterator<FileImportComponent> i = components.values().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			fc = i.next();
			if (fc.isFolderAsContainer()) 
				return true;
		}
		return false;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param controller Reference to the control. Mustn't be <code>null</code>.
	 * @param id The identifier of the component.
	 * @param index The index of the component.
	 * @param name The name of the component.
	 * @param object the object to handle. Mustn't be <code>null</code>.
	 */
	ImporterUIElement(ImporterControl controller, int id, int index, 
			String name, ImportableObject object)
	{
		super(index, name, DESCRIPTION);
		if (object == null) 
			throw new IllegalArgumentException("No object specified.");
		if (controller == null)
			throw new IllegalArgumentException("No controller."); 
		this.controller = controller;
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
	 * Sets the result of the import for the specified file.
	 * 
	 * @param f The file to import.
	 * @param result The result.
	 */
	void setImportedFile(File f, Object result)
	{
		FileImportComponent c = components.get(f.getAbsolutePath());
		if (c != null) {
			c.setStatus(false, result);
			countImported++;
			if (f.isFile()) {
				if (c.hasImportFailed()) countFailure++;
				else if (!c.isCancelled()) countFilesImported++;
			}
			if (f.isDirectory() && !c.hasComponents() && 
					c.isCancelled()) countCancelled++;
			setNumberOfImport();
			setClosable(isDone());
			if (isDone()) {
				Iterator<JLabel> i = containerComponents.keySet().iterator();
				JLabel label;
				boolean toRefresh = toRefresh();
				if (toRefresh) {
					while (i.hasNext()) {
						label = i.next();
						if (label.isEnabled()) {
							label.setForeground(UIUtilities.HYPERLINK_COLOR);
							label.addMouseListener(containerListener);
						}
					}
				}
				if (topContainerToRefresh) {
					i = topContainerComponents.keySet().iterator();
					while (i.hasNext()) {
						label = i.next();
						if (label.isEnabled())
							label.setForeground(UIUtilities.HYPERLINK_COLOR);
					}
				}
				if (foldersName.size() > 0) {
					Entry entry;
					Iterator k = foldersName.entrySet().iterator();
					FileImportComponent fc;
					Object value;
					while (k.hasNext()) {
						entry = (Entry) k.next();
						label = (JLabel) entry.getKey();
						value = entry.getValue();
						if (value instanceof FileImportComponent) {
							fc = (FileImportComponent) value;
							if (fc.toRefresh()) {
								label.setForeground(UIUtilities.HYPERLINK_COLOR);
								label.addMouseListener(folderListener);
							}
						} else {
							label.setForeground(UIUtilities.HYPERLINK_COLOR);
							label.addMouseListener(folderListener);
						}
					}
				}
				long duration = System.currentTimeMillis()-startImport;
				String text = timeLabel.getText();
				String time = UIUtilities.calculateHMS((int) (duration/1000));
				timeLabel.setText(text+" Duration: "+time);
				if (!controller.isMaster()) {
					EventBus bus = ImporterAgent.getRegistry().getEventBus();
					ImportStatusEvent event;
					if (toRefresh) {
						event = new ImportStatusEvent(false, 
								getExistingContainers());
					} else {
						event = new ImportStatusEvent(false, null);
					}
					event.setToRefresh(hasToRefreshTree());
					bus.post(event);
				}
			}
		}
	}
	
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
	
	/** Indicates that the import has started. */
	void startImport()
	{ 
		startImport = System.currentTimeMillis();
		setClosable(false);
		busyLabel.setBusy(true);
		repaint();
	}
	
	/**
	 * Returns <code>true</code> if the import has started, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasStarted() { return busyLabel.isBusy(); }
	
	/**
	 * Returns the collection of files that could not be imported.
	 * 
	 * @return See above.
	 */
	List<FileImportComponent> getMarkedFiles()
	{
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		File f;
		List<FileImportComponent> l;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
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
	List<FileImportComponent> getFilesToReimport()
	{
		List<FileImportComponent> list = new ArrayList<FileImportComponent>();
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		File f;
		List<FileImportComponent> l;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
			l = fc.getReImport();
			if (l != null && l.size() > 0)
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
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		Map<Long, DatasetData> datasets = new HashMap<Long, DatasetData>(); 
		Map<Long, DataObject> projects = new HashMap<Long, DataObject>(); 
		Map<Long, DataObject> screens = new HashMap<Long, DataObject>(); 
		DatasetData d;
		DataObject object;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
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
	 * Returns <code>true</code> if errors to send, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasFailuresToSend()
	{
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
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
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
			if (fc.hasFailuresToReimport()) 
				return true;
		}
		return false;
	}
	
	/** Indicates that the import has been cancel. */
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
		Entry entry;
		Iterator i = components.entrySet().iterator();
		FileImportComponent fc;
		while (i.hasNext()) {
			entry = (Entry) i.next();
			fc = (FileImportComponent) entry.getValue();
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
			Iterator i = components.entrySet().iterator();
			FileImportComponent fc;
			Entry entry;
			int failure = 0;
			int v;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				fc = (FileImportComponent) entry.getValue();
				v = fc.getImportStatus();
				if (v == FileImportComponent.PARTIAL)
					return IMPORT_PARTIAL;
				if (v == FileImportComponent.FAILURE) failure++;
			}
			if (failure == totalToImport) return IMPORT_FAIL;
			else if (failure != 0) return IMPORT_PARTIAL;
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

	/**
	 * Returns a copy of the importable object.
	 * 
	 * @return See above.
	 */
	ImportableObject getImportableObject() { return object.copy(); }
	
}
