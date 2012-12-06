/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog 
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;

//Java imports
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import loci.formats.gui.ComboFileFilter;

import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.filter.file.HCSFilter;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.GroupData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/**
 * Dialog used to select the files to import.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
  * @author Scott Littlewood &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:sylittlewood@dundee.ac.uk"
 *         >sylittlewood@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class ImportDialog extends ClosableTabbedPaneComponent
		implements ActionListener, PropertyChangeListener {
	
	/** Bound property indicating to create the object. */
	public static final String CREATE_OBJECT_PROPERTY = "createObject";

	/** Bound property indicating to load the tags. */
	public static final String LOAD_TAGS_PROPERTY = "loadTags";

	/** Bound property indicating that the cancel button is pressed. */
	public static final String CANCEL_SELECTION_PROPERTY = "cancelSelection";

	/** Bound property indicating that the cancel button is pressed. */
	public static final String CANCEL_ALL_IMPORT_PROPERTY = "cancelAllImport";

	/** Bound property indicating to import the selected files. */
	public static final String IMPORT_PROPERTY = "import";

	/** Bound property indicating to refresh the location. */
	public static final String REFRESH_LOCATION_PROPERTY = "refreshLocation";


	/** Action id indicating to import the selected files. */
	private static final int IMPORT = 0;

	/** Action id indicating to close the dialog. */
	private static final int CANCEL = 1;

	/** Action id indicating to refresh the file view. */
	private static final int REFRESH = 2;

	/** Action id indicating to reset the names. */
	private static final int RESET = 3;

	/** Action id indicating to apply the partial names to all. */
	private static final int APPLY_TO_ALL = 4;

	/** Action id indicating to add tags to the file. */
	private static final int TAG = 5;

	/** Action id indicating to refresh the containers. */
	private static final int REFRESH_LOCATION = 8;

	/** Action id indicating to select the Project/Dataset or Screen. */
	private static final int LOCATION = 9;

	/** Action id indicating to select the Project/Dataset or Screen. */
	private static final int CANCEL_ALL_IMPORT = 10;

	/** The title of the dialog. */
	private static final String TITLE = "Select Data to Import";

	/** Warning when de-selecting the name overriding option. */
	private static final List<String> WARNING;

	/** The length of a column. */
	private static final int COLUMN_WIDTH = 200;

	/** String used to retrieve if the value of the load thumbnail flag. */
	private static final String LOAD_THUMBNAIL = "/options/LoadThumbnail";

	/** String used to retrieve if the value of the folder as dataset flag. */
	private static final String FOLDER_AS_DATASET = "/options/FolderAsDataset";

	/** Indicates the context of the import */
	private static final String LOCATION_PROJECT = "Project/Dataset";

	/** Indicates the context of the import */
	private static final String LOCATION_SCREEN = "Screen";

	static {
		WARNING = new ArrayList<String>();
		WARNING.add("NOTE: Some file formats do not include the file name "
				+ "in their metadata, ");
		WARNING.add("and disabling this option may result in files being "
				+ "imported without a ");
		WARNING.add("reference to their file name e.g. "
				+ "'myfile.lsm [image001]'");
		WARNING.add("would show up as 'image001' with this optioned "
				+ "turned off.");
	}

	/** The approval option the user chose. */
	private int option;

	/** The table hosting the file to import. */
	private FileSelectionTable table;

	/** The file chooser. */
	private GenericFileChooser chooser;

	/** Button to close the dialog. */
	private JButton closeButton;

	/** Button to cancel all imports. */
	private JButton cancelImportButton;

	/** Button to import the files. */
	private JButton importButton;

	/** Button to import the files. */
	private JButton refreshButton;

	/** Button to reload the containers where to import the files. */
	private JToggleButton reloadContainerButton;

	/**
	 * Resets the name of all files to either the full path or the partial name
	 * if selected.
	 */
	private JButton resetButton;

	/** Apply the partial name to all files. */
	private JButton applyToAllButton;

	/** Indicates to use a partial name. */
	private JRadioButton partialName;

	/** Indicates to use a full name. */
	private JRadioButton fullName;

	/** Button indicating to override the name if selected. */
	private JCheckBox overrideName;

	/** Text field indicating how many folders to include. */
	private NumericalTextField numberOfFolders;

	/** The collection of supported filters. */
	private FileFilter[] filters;

	/** Button to bring up the tags wizard. */
	private JButton tagButton;

	/**
	 * The fields hosting the pixels size. First is for the size along the
	 * X-axis, then Y-axis, finally Z-axis
	 */
	private List<NumericalTextField> pixelsSize;

	/** Components hosting the tags. */
	private JPanel tagsPane;

	/** Map hosting the tags. */
	private Map<JButton, TagAnnotationData> tagsMap;

	/** The action listener used to handle tag selection. */
	private ActionListener listener;

	/** The selected container where to import the data. */
	private TreeImageDisplay selectedContainer;

	/** The possible nodes. */
	private Collection<TreeImageDisplay> objects;

	/** The possible P/D nodes. */
	private Collection<TreeImageDisplay> pdNodes;

	/** The possible Screen nodes. */
	private Collection<TreeImageDisplay> screenNodes;

	/** The component displaying the table, options etc. */
	private JTabbedPane tabbedPane;

	/** The collection of datasets to use by default. */
	private List<DataNode> datasets;

	/** The type associated to the import. */
	private int type;

	/** Indicates to show thumbnails in import tab. */
	private JCheckBox showThumbnails;

	/** The collection of <code>HCS</code> filters. */
	private List<FileFilter> hcsFilters;

	/** The collection of general filters. */
	private List<FileFilter> generalFilters;

	/** The combined filter. */
	private FileFilter combinedFilter;

	/** The combined filter for HCS. */
	private FileFilter combinedHCSFilter;

	/** The component displaying the available and used disk space. */
	private JPanel diskSpacePane;

	/** Displays the amount of free and used space. */
	private QuotaCanvas canvas;

	/** The size of the import. */
	private JLabel sizeImportLabel;

	/** The owner related to the component. */
	private JFrame owner;

	/** Flag indicating that the containers view needs to be refreshed. */
	private boolean refreshLocation;

	/** The selected container if screen view. */
	private TreeImageDisplay selectedScreen;

	/** The selected container if project view. */
	private TreeImageDisplay selectedProject;

	/** Indicates to reload the hierarchies when the import is completed. */
	private boolean reload;

	/** The component displaying the component. */
	private JComponent toolBar;

	/** The number of items before adding new elements to the tool bar. */
	private int tbItems;

	/** The selected group. */
	private GroupData group;

	/** The pane hosting the location information. */
	private JXTaskPane pane;

	/** The import settings that are returned from the ImportLocation dialogue */
	private ImportLocationSettings importSettings;
	
	/**
	 * 
	 */
	private JPanel container;

	/**
	 * The dialog to allow for the user to select the import location.
	 */
	private LocationDialog locationDialog;
	
	/** Adds the files to the selection. */
	private void addFiles(ImportLocationSettings importSettings) {
		File[] files = chooser.getSelectedFiles();
		
		if (files == null || files.length == 0)
			return;
		
		List<File> fileList = new ArrayList<File>();
		
		for (int i = 0; i < files.length; i++) {
			checkFile(files[i], fileList);
		}
		
		chooser.setSelectedFile(new File("."));
		
		table.addFiles(fileList, importSettings.isParentFolderAsDataset(), importSettings.getImportGroup());
		importButton.setEnabled(table.hasFilesToImport());
	}

	/** Displays the location of the import.*/
	private void showLocationDialog()
	{
		if (locationDialog.centerLocation() == LocationDialog.CMD_ADD) {
			importSettings = locationDialog.getImportSettings();
			addFiles(importSettings);
		}
	}

	/**
	 * Handles <code>Enter</code> key pressed.
	 * 
	 * @param source
	 *            The source of the mouse pressed.
	 */
	private void handleEnterKeyPressed(Object source) {
		if (source instanceof JList || source instanceof JTable) {
			JComponent c = (JComponent) source;
			if (c.isFocusOwner()) // addFiles();
				showLocationDialog();
		}
	}

	/**
	 * Handles the selection of tags.
	 * 
	 * @param tags
	 *            The selected tags.
	 */
	private void handleTagsSelection(Collection<TagAnnotationData> tags)
	{
		Collection<TagAnnotationData> set = tagsMap.values();
		Map<String, TagAnnotationData> newTags = new HashMap<String, TagAnnotationData>();
		TagAnnotationData tag;
		Iterator<TagAnnotationData> i = set.iterator();
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getId() < 0)
				newTags.put(tag.getTagValue(), tag);
		}
		List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
		i = tags.iterator();
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getId() < 0) {
				if (!newTags.containsKey(tag.getTagValue())) {
					toKeep.add(tag);
				}
			} else
				toKeep.add(tag);
		}
		toKeep.addAll(newTags.values());

		// layout the tags
		tagsMap.clear();
		tagsPane.removeAll();
		i = toKeep.iterator();
		IconManager icons = IconManager.getInstance();
		JPanel entry;
		JPanel p = initRow();
		int width = 0;
		while (i.hasNext()) {
			tag = i.next();
			entry = buildTagEntry(tag, icons.getIcon(IconManager.MINUS_11));
			if (width + entry.getPreferredSize().width >= COLUMN_WIDTH) {
				tagsPane.add(p);
				p = initRow();
				width = 0;
			} else {
				width += entry.getPreferredSize().width;
				width += 2;
			}
			p.add(entry);
		}
		if (p.getComponentCount() > 0)
			tagsPane.add(p);
		tagsPane.validate();
		tagsPane.repaint();
	}

	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel initRow() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		return p;
	}

	/**
	 * Builds and lays out a tag.
	 * 
	 * @param tag
	 *            The tag to display.
	 * @param icon
	 *            The icon used to remove the tag from the display.
	 * @return See above.
	 */
	private JPanel buildTagEntry(TagAnnotationData tag, Icon icon) {
		JButton b = new JButton(icon);
		UIUtilities.unifiedButtonLookAndFeel(b);
		// add listener
		b.addActionListener(listener);
		tagsMap.put(b, tag);
		JPanel p = new JPanel();
		JLabel l = new JLabel();
		l.setText(tag.getTagValue());
		p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		p.add(l);
		p.add(b);
		return p;
	}

	/**
	 * Shows the selection wizard.
	 * 
	 * @param type
	 *            The type of objects to handle.
	 * @param available
	 *            The available objects.
	 * @param selected
	 *            The selected objects.
	 * @param addCreation
	 *            Pass <code>true</code> to add a component allowing creation of
	 *            object of the passed type, <code>false</code> otherwise.
	 */
	private void showSelectionWizard(Class<TagAnnotationData> type,
			Collection<Object> available, Collection<Object> selected,
			boolean addCreation) {
		IconManager icons = IconManager.getInstance();
		Registry reg = ImporterAgent.getRegistry();
		String title = "";
		String text = "";
		Icon icon = null;
		if (TagAnnotationData.class.equals(type)) {
			title = "Tags Selection";
			text = "Select the Tags to add or remove, \nor Create new Tags";
			icon = icons.getIcon(IconManager.TAGS_48);
		}
		SelectionWizard wizard = new SelectionWizard(reg.getTaskBar()
				.getFrame(), available, selected, type, addCreation,
				ImporterAgent.getUserDetails());
		wizard.setAcceptButtonText("Save");
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}

	/**
	 * Initializes the components composing the display.
	 * 
	 * @param filters
	 *            The filters to handle.
	 */
	private void initComponents(FileFilter[] filters) {
		pane = new JXTaskPane();
		pane.setTitle("Import Location");
		pane.setCollapsed(true);
		canvas = new QuotaCanvas();
		sizeImportLabel = new JLabel();
		diskSpacePane = new JPanel();
		diskSpacePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		diskSpacePane.add(UIUtilities.setTextFont("Free Space "));
		diskSpacePane.add(canvas);

		showThumbnails = new JCheckBox("Show Thumbnails when imported");
		showThumbnails.setVisible(false);
		Boolean b = (Boolean) ImporterAgent.getRegistry()
				.lookup(LOAD_THUMBNAIL);
		if (b != null) {
			if (b.booleanValue()) {
				showThumbnails.setVisible(true);
				showThumbnails.setSelected(true);
			}
		}
		b = (Boolean) ImporterAgent.getRegistry().lookup(FOLDER_AS_DATASET);
		if (!isFastConnection()) // slow connection
			showThumbnails.setSelected(false);
		
		datasets = new ArrayList<DataNode>();
		
		IconManager icons = IconManager.getInstance();
		reloadContainerButton = new JToggleButton(
				icons.getIcon(IconManager.REFRESH));
		reloadContainerButton.setBackground(UIUtilities.BACKGROUND);
		reloadContainerButton.setToolTipText("Reloads the container where to "
				+ "import the data.");
		reloadContainerButton.setActionCommand("" + REFRESH_LOCATION);
		reloadContainerButton.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(reloadContainerButton);

		locationDialog = new LocationDialog(owner, selectedContainer, type, objects, (Collection<GroupData>) ImporterAgent.getAvailableUserGroups(), ImporterAgent.getUserDetails().getGroupId());
		locationDialog.addPropertyChangeListener(this);
		
		listener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object src = e.getSource();
				if (src instanceof JButton) {
					TagAnnotationData tag = tagsMap.get(src);
					if (tag != null) {
						tagsMap.remove(src);
						handleTagsSelection(tagsMap.values());
					}
				}
			}
		};

		tabbedPane = new JTabbedPane();
		numberOfFolders = new NumericalTextField();
		numberOfFolders.setMinimum(0);
		numberOfFolders.setText("0");
		numberOfFolders.setColumns(3);
		numberOfFolders.addPropertyChangeListener(this);
		tagsMap = new LinkedHashMap<JButton, TagAnnotationData>();

		tagButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(tagButton);
		tagButton.addActionListener(this);
		tagButton.setActionCommand("" + TAG);
		tagButton.setToolTipText("Add Tags.");
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));

		overrideName = new JCheckBox("Override default File naming. "
				+ "Instead use");
		overrideName.setToolTipText(UIUtilities.formatToolTipText(WARNING));
		overrideName.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		fullName = new JRadioButton("Full Path+File's name");
		group.add(fullName);
		partialName = new JRadioButton();
		partialName.setText("Partial Path+File's name with");
		partialName.setSelected(true);
		group.add(partialName);

		chooser = new GenericFileChooser();
		JList list = (JList) UIUtilities.findComponent(chooser, JList.class);
		KeyAdapter ka = new KeyAdapter() {

			/**
			 * Adds the files to the import queue.
			 * 
			 * @see KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleEnterKeyPressed(e.getSource());
				}
			}
		};
		if (list != null)
			list.addKeyListener(ka);
		if (list == null) {
			JTable t = (JTable) UIUtilities
					.findComponent(chooser, JTable.class);
			if (t != null)
				t.addKeyListener(ka);
		}

		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null)
				chooser.setCurrentDirectory(f);
		} catch (Exception e) {
			// Ignore: could not set the default container
		}

		chooser.addPropertyChangeListener(this);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setControlButtonsAreShown(false);
		chooser.setApproveButtonText("Import");
		chooser.setApproveButtonToolTipText("Import the selected files "
				+ "or directories");
		hcsFilters = new ArrayList<FileFilter>();
		generalFilters = new ArrayList<FileFilter>();
		if (filters != null) {
			chooser.setAcceptAllFileFilterUsed(false);
			FileFilter filter;
			for (int i = 0; i < filters.length; i++) {
				filter = filters[i];
				if (filter instanceof ComboFileFilter) {
					combinedFilter = filter;
					ComboFileFilter cff = (ComboFileFilter) filter;
					FileFilter[] extensionFilters = cff.getFilters();
					for (int j = 0; j < extensionFilters.length; j++) {
						FileFilter ff = extensionFilters[j];
						if (ImportableObject.isHCSFormat(ff.toString())) {
							hcsFilters.add(ff);
						} else {
							generalFilters.add(ff);
						}
					}
					break;
				}
			}
			Set<String> set = ImportableObject.HCS_FILES_EXTENSION;
			combinedHCSFilter = new HCSFilter(
					set.toArray(new String[set.size()]));
			Iterator<FileFilter> j;
			if (type == Importer.SCREEN_TYPE) {
				chooser.addChoosableFileFilter(combinedHCSFilter);
				j = hcsFilters.iterator();
				while (j.hasNext())
					chooser.addChoosableFileFilter(j.next());
				chooser.setFileFilter(combinedHCSFilter);
			} else {
				chooser.addChoosableFileFilter(combinedFilter);
				j = generalFilters.iterator();
				while (j.hasNext())
					chooser.addChoosableFileFilter(j.next());
				chooser.setFileFilter(combinedFilter);
			}
			while (j.hasNext())
				chooser.addChoosableFileFilter(j.next());
		} else
			chooser.setAcceptAllFileFilterUsed(true);

		table = new FileSelectionTable(this);
		table.addPropertyChangeListener(this);
		closeButton = new JButton("Close");
		closeButton.setToolTipText("Close the dialog and do not import.");
		closeButton.setActionCommand("" + CANCEL);
		closeButton.addActionListener(this);

		cancelImportButton = new JButton("Cancel All");
		cancelImportButton.setToolTipText("Cancel all ongoing imports.");
		cancelImportButton.setActionCommand("" + CANCEL_ALL_IMPORT);
		cancelImportButton.addActionListener(this);

		importButton = new JButton("Import");
		importButton.setToolTipText("Import the selected files or"
				+ " directories.");
		importButton.setActionCommand("" + IMPORT);
		importButton.addActionListener(this);
		importButton.setEnabled(false);
		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Reloads the files view.");
		refreshButton.setActionCommand("" + REFRESH);
		refreshButton.setBorderPainted(false);
		refreshButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset the name of all files to either "
				+ "the full path or the partial name if selected.");
		resetButton.setActionCommand("" + RESET);
		resetButton.addActionListener(this);
		applyToAllButton = new JButton("Apply Partial Name");
		applyToAllButton.setToolTipText("Apply the partial name to "
				+ "all files in the queue.");
		applyToAllButton.setActionCommand("" + APPLY_TO_ALL);
		applyToAllButton.addActionListener(this);
		applyToAllButton.setEnabled(false);

		pixelsSize = new ArrayList<NumericalTextField>();
		NumericalTextField field;
		for (int i = 0; i < 3; i++) {
			field = new NumericalTextField();
			field.setNumberType(Double.class);
			field.setColumns(2);
			pixelsSize.add(field);
		}


		List<Component> boxes = UIUtilities.findComponents(chooser,
				JComboBox.class);
		if (boxes != null) {
			JComboBox box;
			JComboBox filerBox = null;
			Iterator<Component> i = boxes.iterator();
			while (i.hasNext()) {
				box = (JComboBox) i.next();
				Object o = box.getItemAt(0);
				if (o instanceof FileFilter) {
					filerBox = box;
					break;
				}
			}
			if (filerBox != null) {
				filerBox.addKeyListener(new KeyAdapter() {

					public void keyPressed(KeyEvent e) {
						String value = KeyEvent.getKeyText(e.getKeyCode());
						JComboBox box = (JComboBox) e.getSource();
						int n = box.getItemCount();
						FileFilter filter;
						FileFilter selectedFilter = null;
						String d;
						for (int j = 0; j < n; j++) {
							filter = (FileFilter) box.getItemAt(j);
							d = filter.getDescription();
							if (d.startsWith(value)) {
								selectedFilter = filter;
								break;
							}
						}
						if (selectedFilter != null)
							box.setSelectedItem(selectedFilter);
					}
				});
			}
		}
	}

	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarRight() {
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bar.add(cancelImportButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(closeButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(importButton);
		bar.add(Box.createHorizontalStrut(10));
		return bar;
	}

	/**
	 * Builds and lays out the tool bar.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolBarLeft() {
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		bar.add(showThumbnails);
		return bar;
	}

	/**
	 * Builds and lays out the components.
	 * 
	 * @return See above
	 */
	private JPanel buildPathComponent() {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(numberOfFolders);
		JLabel l = new JLabel();
		l.setText("Directories before File");
		p.add(l);
		return p;
	}

	/**
	 * Builds and lays out the component displaying the options for the
	 * metadata.
	 * 
	 * @return See above.
	 */
	private JXTaskPane buildMetadataComponent() {
		JXTaskPane pane = new JXTaskPane();
		Font font = pane.getFont();
		pane.setFont(font.deriveFont(font.getStyle(), font.getSize() - 2));
		pane.setCollapsed(true);
		pane.setTitle("Metadata Defaults");
		pane.add(buildPixelSizeComponent());
		return pane;
	}

	/**
	 * Builds and lays out the pixels size options.
	 * 
	 * @return See above.
	 */
	private JPanel buildPixelSizeComponent() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder("Pixels Size Defaults"));
		JLabel l = new JLabel();
		l.setText("Used if no values included in the file:");
		p.add(UIUtilities.buildComponentPanel(l));
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT));
		l = new JLabel();
		l.setText("X: ");
		row.add(l);
		row.add(pixelsSize.get(0));
		l = new JLabel();
		l.setText("Y: ");
		row.add(l);
		row.add(pixelsSize.get(1));
		l = new JLabel();
		l.setText("Z: ");
		row.add(l);
		row.add(pixelsSize.get(2));
		p.add(row);
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Builds and lays out the components displaying the naming options.
	 * 
	 * @return See above.
	 */
	private JComponent buildNamingComponent() {
		JPanel content = new JPanel();
		content.setBorder(BorderFactory.createTitledBorder("File Naming"));

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(fullName);
		panel.add(partialName);
		JPanel pp = new JPanel();
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
		pp.add(UIUtilities.buildComponentPanel(panel));
		pp.add(buildPathComponent());
		GridBagConstraints c = new GridBagConstraints();
		content.setLayout(new GridBagLayout());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.0;
		c.gridwidth = 2;
		c.gridx = 0;
		c.gridy = 0;
		content.add(overrideName, c);
		c.gridwidth = 1;
		c.gridy++;
		content.add(Box.createHorizontalStrut(15), c);
		c.gridx++;
		content.add(pp, c);

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(content);
		p.add(buildAnnotationComponent());
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Builds the component hosting the controls to add annotations.
	 * 
	 * @return See above.
	 */
	private JPanel buildAnnotationComponent() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		JLabel l = new JLabel();
		l.setText("Add tag to images");
		JPanel tagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		tagPanel.add(l);
		tagPanel.add(tagButton);
		l = new JLabel();
		l.setText(": ");
		tagPanel.add(l);
		tagPanel.add(tagsPane);

		p.add(tagPanel);
		return UIUtilities.buildComponentPanel(p);
	}

	/**
	 * Builds and lays out the import options available.
	 * 
	 * @param container
	 *            Container where to import the image.
	 * @return See above.
	 */
	private JPanel buildOptionsPane() {
		// Lays out the options
		JPanel options = new JPanel();
		double[][] size = {
				{ TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.PREFERRED,
						TableLayout.PREFERRED } };
		options.setLayout(new TableLayout(size));
		options.add(buildNamingComponent(), "0, 1");
		options.add(buildMetadataComponent(), "0, 2");
		return options;
	}

	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel createRow() {
		return createRow(UIUtilities.BACKGROUND);
	}

	/**
	 * Creates a row.
	 * 
	 * @param background
	 *            The background of color.
	 * @return See above.
	 */
	private JPanel createRow(Color background) {
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		if (background != null)
			row.setBackground(background);
		row.setBorder(null);
		return row;
	}

	/**
	 * Lays out the quota.
	 * 
	 * @return See above.
	 */
	private JPanel buildQuotaPane() {
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		row.add(UIUtilities.buildComponentPanelRight(diskSpacePane, 0, 0, true));
		row.add(UIUtilities.setTextFont(QuotaCanvas.IMPORT_SIZE_TEXT));
		row.add(UIUtilities.buildComponentPanel(sizeImportLabel, 0, 0, true));
		row.setBorder(null);
		return row;
	}

	/** Builds and lays out the UI. */
	private void buildGUI() {
		setLayout(new BorderLayout(0, 0));
		JPanel p = new JPanel();
		p.setBorder(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildQuotaPane());
		p.add(table);
		tabbedPane.add("Files to import", p);
		tabbedPane.add("Options", buildOptionsPane());

		JPanel tablePanel = new JPanel();
		double[][] size = {
				{ TableLayout.PREFERRED, 10, 5, TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL }
			};
		tablePanel.setLayout(new TableLayout(size));
		tablePanel.add(table.buildControls(), "0, 1, LEFT, CENTER");
		tablePanel.add(tabbedPane, "2, 1, 3, 1");
		
		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				chooser, tablePanel);
		
		JPanel mainPanel = new JPanel();
		double[][] ss = { { TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL } };
		mainPanel.setLayout(new TableLayout(ss));
		mainPanel.setBackground(UIUtilities.BACKGROUND);
		mainPanel.add(pane, "0, 1");
		
		this.add(mainPanel, BorderLayout.CENTER);
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));

		// Lays out the buttons.
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(buildToolBarLeft());
		bar.add(buildToolBarRight());
		controls.add(new JSeparator());
		controls.add(bar);

		// c.add(controls, BorderLayout.SOUTH);
		add(controls, BorderLayout.SOUTH);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = UIManager.getLookAndFeel()
					.getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				getRootPane().setWindowDecorationStyle(
						JRootPane.FILE_CHOOSER_DIALOG);
		}
		
		locationDialog.buildGUI();
	}

	/**
	 * Helper method returning <code>true</code> if the connection is fast,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	private boolean isFastConnection() {
		int value = (Integer) ImporterAgent.getRegistry().lookup(
				LookupNames.CONNECTION_SPEED);
		return value == RenderingControl.UNCOMPRESSED;
	}

	/**
	 * Handles the selection of files. Returns the files that can be imported.
	 * 
	 * @param The
	 *            selected files.
	 * @return See above.
	 */
	private int handleFilesSelection(File[] files) {
		int count = 0;
		if (files == null)
			return count;
		File f;
		int directory = 0;
		for (int i = 0; i < files.length; i++) {
			f = files[i];
			if (!f.isHidden()) {
				count++;
				if (f.isDirectory()) {
					directory++;
				}
			}
		}
		return count;
	}

	/** Imports the selected files. */
	private void importFiles() {
		option = IMPORT;
		importButton.setEnabled(false);
		// Set the current directory as the defaults
		File dir = chooser.getCurrentDirectory();
		if (dir != null)
			UIUtilities.setDefaultFolder(dir.toString());
		List<ImportableFile> files = table.getFilesToImport();
		// That's the hard part.
		if (files.size() == 0)
			return;
		ImportableObject object = new ImportableObject(files,
				overrideName.isSelected());
		Iterator<ImportableFile> i = files.iterator();
		ImportableFile file;

		if (!reload) {
			while (i.hasNext()) {
				file = i.next();
				if (file.isFolderAsContainer()
						&& !ImportableObject.isHCSFile(file.getFile())) {
					// going to check if the dataset has been created.
					reload = true;
					break;
				}
			}
		}

		object.setScanningDepth(ImporterAgent.getScanningDepth());
		Boolean b = (Boolean) ImporterAgent.getRegistry()
				.lookup(LOAD_THUMBNAIL);
		if (b != null)
			object.setLoadThumbnail(b.booleanValue());
		// if slow connection
		if (!isFastConnection())
			object.setLoadThumbnail(false);
		if (showThumbnails.isVisible()) {
			object.setLoadThumbnail(showThumbnails.isSelected());
		}
		// tags
		if (tagsMap.size() > 0) {
			Iterator<TagAnnotationData> j = tagsMap.values().iterator();
			List<TagAnnotationData> l = new ArrayList<TagAnnotationData>();
			while (j.hasNext()) {
				l.add(j.next());
			}
			object.setTags(l);
		}
			
		if (partialName.isSelected()) {
			Integer number = (Integer) numberOfFolders.getValueAsNumber();
			if (number != null && number >= 0)
				object.setDepthForName(number);
		}
		NumericalTextField nf;
		Iterator<NumericalTextField> ij = pixelsSize.iterator();
		Number n;
		double[] size = new double[3];
		int index = 0;
		int count = 0;
		while (ij.hasNext()) {
			nf = ij.next();
			n = nf.getValueAsNumber();
			if (n != null) {
				count++;
				size[index] = n.doubleValue();
			} else
				size[index] = 1;
			index++;
		}
		if (count > 0)
			object.setPixelsSize(size);
		// Check if we need to display the refresh text
		boolean refresh = false;
		Iterator<ImportableFile> j = files.iterator();
		while (j.hasNext()) {
			if (j.next().isFolderAsContainer()) {
				refresh = true;
				break;
			}
		}
		/*
		if (newNodesPD != null && newNodesPD.size() > 0 || newNodesS != null
				&& newNodesS.size() > 0) {
			refresh = true;
		}
		if (refresh)
			refreshLocation = true;
		if (newNodesPD != null)
			newNodesPD.clear();
		if (newNodesS != null)
			newNodesS.clear();
			*/
		firePropertyChange(IMPORT_PROPERTY, null, object);
		table.removeAllFiles();
		tagsMap.clear();
		tagsPane.removeAll();
		tagsPane.repaint();
	}

	/**
	 * Checks if the file can be added to the passed list. Returns the
	 * <code>true</code> if the file is a directory, <code>false</code>
	 * otherwise.
	 * 
	 * @param f
	 *            The file to handle.
	 */
	private boolean checkFile(File f, List<File> l) {
		if (f == null || f.isHidden())
			return false;
		if (f.isFile()) {
			if (isFileImportable(f))
				l.add(f);
		} else if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null && list.length > 0) {
				l.add(f);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the file can be imported, <code>false</code>
	 * otherwise.
	 * 
	 * @param f
	 *            The file to check.
	 * @return See above.
	 */
	private boolean isFileImportable(File f) {
		return !(f == null || f.isHidden());
	}

	/**
	 * Checks if the passed container is hosting the desired object.
	 * 
	 * @param container
	 *            The container to handle.
	 * @return See above.
	 */
	private TreeImageDisplay checkContainer(TreeImageDisplay container) {
		if (container == null)
			return null;
		Object ho = container.getUserObject();
		if (ho instanceof DatasetData || ho instanceof ProjectData
				|| ho instanceof ScreenData)
			return container;
		return null;
	}

	/**
	 * Creates a new instance.
	 * 
	 * @param owner
	 *            The owner of the dialog.
	 * @param filters
	 *            The list of filters.
	 * @param containers
	 *            The container where to import the files.
	 * @param objects
	 *            The possible objects.
	 * @param type
	 *            One of the type constants.
	 */
	public ImportDialog(JFrame owner, FileFilter[] filters,
			TreeImageDisplay selectedContainer,
			Collection<TreeImageDisplay> objects, int type, Collection<GroupData> groups) {
		// super(owner);
		super(0, TITLE, TITLE);
		
		this.owner = owner;
		this.objects = objects;
		this.type = type;
		this.selectedContainer = selectedContainer;
		
		selectedContainer = checkContainer(selectedContainer);
		
		if (type == Importer.PROJECT_TYPE) {
			pdNodes = objects;
			selectedProject = selectedContainer;
		} else {
			screenNodes = objects;
			selectedScreen = selectedContainer;
		}
		
		setClosable(false);
		setCloseVisible(false);
		initComponents(filters);
		buildGUI();
	}

	/**
	 * Returns the type of the import.
	 * 
	 * @return See above.
	 */
	public int getType() {
		return type;
	}

	/**
	 * Returns <code>true</code> if only one group for the user,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleGroup() {
		Collection l = ImporterAgent.getAvailableUserGroups();
		return (l.size() <= 1);
	}

	/** Display the size of files to add. */
	void onSelectionChanged() {
		if (canvas != null) {
			long size = table.getSizeFilesInQueue();
			canvas.setSizeInQueue(size);
			String v = (int) Math.round(canvas.getPercentageToImport() * 100)
					+ "% of Remaining Space";
			sizeImportLabel.setText(UIUtilities.formatFileSize(size));
			sizeImportLabel.setToolTipText(v);
		}
	}

    /**
     * Returns <code>true</code> if the folder containing an image has to be
     * used as a dataset, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    boolean isParentFolderAsDataset()
    {
    	return importSettings.isParentFolderAsDataset();
    }
    /**
	 * Returns the name to display for a file.
	 * 
	 * @param fullPath
	 *            The file's absolute path.
	 * @return See above.
	 */
	String getDisplayedFileName(String fullPath) {
		if (fullPath == null || !partialName.isSelected())
			return fullPath;
		Integer number = (Integer) numberOfFolders.getValueAsNumber();
		return UIUtilities.getDisplayedFileName(fullPath, number);
	}

	/**
	 * Returns <code>true</code> if the folder can be used as a container,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean useFolderAsContainer() {
		return (type != Importer.SCREEN_TYPE);
	}

	/**
	 * Returns where to import the file when selected.
	 * 
	 * @return See above.
	 */
	DataNode getImportLocation() {
		// based on the selected tab?
		
		return importSettings.getImportLocation();
	}

	/**
	 * Returns where to import the file when selected.
	 * 
	 * @return See above.
	 */
	DataNode getParentImportLocation() {
		return importSettings.getParentImportLocation();
	}

	/**
	 * Returns <code>true</code> to indicate that the refresh containers view
	 * needs to be refreshed.
	 * 
	 * @return See above.
	 */
	public boolean isRefreshLocation() {
		return refreshLocation;
	}

	/**
	 * Resets the text and remove all the files to import.
	 * 
	 * @param objects
	 *            The possible objects.
	 * @param type
	 *            One of the constants used to identify the type of import.
	 * @param changeGroup
	 *            Flag indicating that the group has been modified if
	 *            <code>true</code>, <code>false</code> otherwise.
	 */
	public void reset(Collection<TreeImageDisplay> objects, int type,
			long currentGroupId) {
		TreeImageDisplay selected = null;
		if (this.selectedContainer != null) {
			if (objects != null) {
				Iterator<TreeImageDisplay> i = objects.iterator();
				Object ho = this.selectedContainer.getUserObject();
				TreeImageDisplay node, child;
				Object nho, cho;
				long id = -1;
				if (ho instanceof DataObject)
					id = ((DataObject) ho).getId();
				List l;
				Iterator j;
				while (i.hasNext()) {
					node = i.next();
					nho = node.getUserObject();
					if (nho.getClass().equals(ho.getClass())
							&& nho instanceof DataObject) {
						if (((DataObject) nho).getId() == id) {
							selected = node;
							break;
						}
					}
					l = node.getChildrenDisplay();
					j = l.iterator();
					while (j.hasNext()) {
						child = (TreeImageDisplay) j.next();
						cho = child.getUserObject();
						if (cho.getClass().equals(ho.getClass())
								&& cho instanceof DataObject) {
							if (((DataObject) cho).getId() == id) {
								selected = child;
								break;
							}
						}
					}
				}
			}
		}
		reset(selected, objects, type, false, currentGroupId);
	}

	/**
	 * Resets the text and remove all the files to import.
	 * 
	 * @param selectedContainer
	 *            The container where to import the files.
	 * @param objects
	 *            The possible objects.
	 * @param type
	 *            One of the constants used to identify the type of import.
	 * @param remove
	 *            Pass <code>true</code> o
	 * @param changeGroup
	 *            Flag indicating that the group has been modified if
	 *            <code>true</code>, <code>false</code> otherwise.
	 */
	public void reset(TreeImageDisplay selectedContainer,
			Collection<TreeImageDisplay> objects, int type, boolean remove,
			long currentGroupId) {
		
		canvas.setVisible(true);
		this.selectedContainer = checkContainer(selectedContainer);
		this.objects = objects;
		
		int oldType = this.type;
		
		this.type = type;
		if (type == Importer.PROJECT_TYPE) {
			pdNodes = objects;
			selectedProject = selectedContainer;
		} else {
			screenNodes = objects;
			selectedScreen = selectedContainer;
		}
		
		if (oldType != this.type) {
			// change filters.
			// reset name
			FileFilter[] filters = chooser.getChoosableFileFilters();
			for (int i = 0; i < filters.length; i++) {
				chooser.removeChoosableFileFilter(filters[i]);
			}
			Iterator<FileFilter> j;
			if (type == Importer.SCREEN_TYPE) {
				j = hcsFilters.iterator();
				chooser.addChoosableFileFilter(combinedHCSFilter);
				while (j.hasNext()) {
					chooser.addChoosableFileFilter(j.next());
				}
				chooser.setFileFilter(combinedHCSFilter);
			} else {
				chooser.addChoosableFileFilter(combinedFilter);
				j = generalFilters.iterator();
				while (j.hasNext()) {
					chooser.addChoosableFileFilter(j.next());
				}
				chooser.setFileFilter(combinedFilter);
			}
		}
		
		File[] files = chooser.getSelectedFiles();
		table.allowAddition(files != null && files.length > 0);
		handleTagsSelection(new ArrayList<TagAnnotationData>());
		tabbedPane.setSelectedIndex(0);
		
		FileFilter[] filters = chooser.getChoosableFileFilters();
		
		if (filters != null && filters.length > 0)
			chooser.setFileFilter(filters[0]);
		
		locationDialog.reset(selectedContainer, type, objects, currentGroupId);

		tagsPane.removeAll();
		tagsMap.clear();
	}

	/**
	 * Shows the chooser dialog.
	 * 
	 * @return The option selected.
	 */
	public int showDialog() {
		UIUtilities.setLocationRelativeToAndShow(getParent(), this);
		return option;
	}

	/**
	 * Shows the chooser dialog.
	 * 
	 * @return The option selected.
	 */
	public int centerDialog() {
		UIUtilities.centerAndShow(this);
		return option;
	}

	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags
	 *            The collection of existing tags.
	 */
	public void setTags(Collection<TagAnnotationData> tags) {
		if (tags == null)
			return;
		Collection<TagAnnotationData> set = tagsMap.values();
		List<Long> ids = new ArrayList<Long>();

		List<Object> available = new ArrayList<Object>();
		List<Object> selected = new ArrayList<Object>();

		TagAnnotationData tag;
		Iterator<TagAnnotationData> i = set.iterator();
		while (i.hasNext()) {
			tag = i.next();
			if (tag.getId() > 0)
				ids.add(tag.getId());
		}
		i = tags.iterator();
		while (i.hasNext()) {
			tag = i.next();
			if (ids.contains(tag.getId()))
				selected.add(tag);
			else
				available.add(tag);
		}
		// show the selection wizard
		showSelectionWizard(TagAnnotationData.class, available, selected, true);
	}

	/**
	 * Displays the used and available disk space.
	 * 
	 * @param quota
	 *            The value to set.
	 */
	public void setDiskSpace(DiskQuota quota) {
		if (quota == null)
			return;
		long free = quota.getAvailableSpace();
		long used = quota.getUsedSpace();
		if (free <= 0 || used < 0)
			return;
		canvas.setPercentage(quota);
		canvas.setVisible(true);
	}

	/**
	 * Refreshes the display when the user reconnect to server.
	 * 
	 * @param bar
	 *            The component to add.
	 */
	public void onReconnected(Collection<GroupData> availableGroups, long currentGroupId) {
		/*int n = toolBar.getComponentCount();
		int diff = n - tbItems;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				toolBar.remove(tbItems + i);
			}
			toolBar.add(bar);
			toolBar.validate();
			toolBar.repaint();
		}
		*/
		table.removeAllFiles();
		locationDialog.onReconnected(availableGroups, currentGroupId);
		tagsPane.removeAll();
		tagsMap.clear();
	}

	/**
	 * Notifies that the new object has been created.
	 * 
	 * @param d
	 *            The newly created object.
	 * @param parent
	 *            The parent of the object.
	 */
	public void onDataObjectSaved(DataObject d, DataObject parent) {
		if (d instanceof ProjectData)
		{
			locationDialog.createProject(d);
		} else if (d instanceof ScreenData) {
			locationDialog.createScreen(d);
		} else if (d instanceof DatasetData) {
			locationDialog.createDataset((DatasetData) d);
		}
	}

	/**
	 * Returns <code>true</code> if need to reload the hierarchies,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean reloadHierarchies() {
		return reload;
	}

	/**
	 * Sets the selected group.
	 * 
	 * @param group
	 *            The group to set.
	 */
	public void setSelectedGroup(GroupData group) {
		this.group = group;
	}

	/**
	 * Reacts to property fired by the table.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		
		if (FileSelectionTable.ADD_PROPERTY.equals(name)) {
			showLocationDialog();
		} else if (FileSelectionTable.REMOVE_PROPERTY.equals(name)) {
			int n = handleFilesSelection(chooser.getSelectedFiles());
			table.allowAddition(n > 0);
			importButton.setEnabled(table.hasFilesToImport());
		} else if (JFileChooser.SELECTED_FILES_CHANGED_PROPERTY.equals(name)) {
			int n = handleFilesSelection(chooser.getSelectedFiles());
			table.allowAddition(n > 0);
		} else if (NumericalTextField.TEXT_UPDATED_PROPERTY.equals(name)) {
			if (partialName.isSelected()) {
				Integer number = (Integer) numberOfFolders.getValueAsNumber();
				if (number != null && number >= 0)
					table.applyToAll();
			}
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1)
				return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				if (TagAnnotationData.class.getName().equals(type.getName()))
					handleTagsSelection((Collection<TagAnnotationData>) entry
							.getValue());
			}
		} else if (LocationDialog.GROUP_CHANGED_PROPERTY.equals(name)
				|| ImportDialog.REFRESH_LOCATION_PROPERTY.equals(name)
				|| ImportDialog.CREATE_OBJECT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		}
		
	}

	/**
	 * Cancels or imports the files.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		int index = Integer.parseInt(evt.getActionCommand());
		EditorDialog d;
		switch (index) {
		case IMPORT:
			importFiles();
			break;
		case CANCEL:
			firePropertyChange(CANCEL_SELECTION_PROPERTY,
					Boolean.valueOf(false), Boolean.valueOf(true));
			break;
		case REFRESH:
			chooser.rescanCurrentDirectory();
			chooser.repaint();
			break;
		case RESET:
			partialName.setSelected(false);
			table.resetFilesName();
			break;
		case APPLY_TO_ALL:
			table.applyToAll();
			break;
		case TAG:
			firePropertyChange(LOAD_TAGS_PROPERTY, Boolean.valueOf(false),
					Boolean.valueOf(true));
			break;
		case REFRESH_LOCATION:
			refreshLocation = false;
			chooser.rescanCurrentDirectory();
			chooser.repaint();
			firePropertyChange(REFRESH_LOCATION_PROPERTY, -1, getType());
			break;
		case CANCEL_ALL_IMPORT:
			firePropertyChange(CANCEL_ALL_IMPORT_PROPERTY,
					Boolean.valueOf(false), Boolean.valueOf(true));
		}
	}

}
