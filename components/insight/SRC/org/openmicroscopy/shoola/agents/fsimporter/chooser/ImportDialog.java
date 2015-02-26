/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.ImportDialog 
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
package org.openmicroscopy.shoola.agents.fsimporter.chooser;

//Java imports
import ij.WindowManager;
import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
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
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import loci.formats.gui.ComboFileFilter;

import org.apache.commons.collections.CollectionUtils;
import org.jdesktop.swingx.JXTaskPane;
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.actions.ImporterAction;
import org.openmicroscopy.shoola.agents.fsimporter.view.ImportLocationDetails;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
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

	// public constants
	/** Bound property indicating to change the import group. */
	public static final String PROPERTY_GROUP_CHANGED = "groupChanged";
	
	/** Bound property indicating to create the object. */
	public static final String CREATE_OBJECT_PROPERTY = "createObject";

	/** Bound property indicating to load the tags. */
	public static final String LOAD_TAGS_PROPERTY = "loadTags";

	/** Bound property indicating that the cancel button is pressed. */
	public static final String CANCEL_SELECTION_PROPERTY = "cancelSelection";

	/** Bound property indicating to import the selected files. */
	public static final String IMPORT_PROPERTY = "import";

	/** Bound property indicating to refresh the location. */
	public static final String REFRESH_LOCATION_PROPERTY = "refreshLocation";

	// Command Ids
	/** Action id indicating to import the selected files. */
	private static final int CMD_IMPORT = 1;

	/** Action id indicating to close the dialog. */
	private static final int CMD_CLOSE = 2;

	/** Action id indicating to reset the names. */
	private static final int CMD_RESET = 3;

	/** Action id indicating to apply the partial names to all. */
	private static final int CMD_APPLY_TO_ALL = 4;

	/** Action id indicating to add tags to the file. */
	private static final int CMD_TAG = 5;

	/** Action id indicating to refresh the file chooser. */
	private static final int CMD_REFRESH_FILES = 6;

	// String constants

	/** Naming Option display text */
	private static final String TEXT_NAMING_PARTIAL_PATH =
			"Partial Path+File's name with";

	/** Naming Option display text */
	private static final String TEXT_NAMING_FULL_PATH = "Full Path+File's name";

	/** The title of the dialog. */
	private static final String TITLE = "Select Data to Import";

	/** Text for the Import button */
	private static final String TEXT_IMPORT = "Import";

	/** Tooltip text for the Import button */
	private static final String TOOLTIP_IMPORT =
			"Import the selected files or directories";
	
	/** Text for metadata pane */
	private static final String TEXT_METADATA_DEFAULTS = "Metadata Defaults";

	/** Text for naming panel */
	private static final String TEXT_DIRECTORIES_BEFORE_FILE =
			"Directories before File";

	/** Quota text */
	private static final String TEXT_FREE_SPACE = "Free Space ";

	/** Save button text */
	private static final String TEXT_SAVE = "Save";

	/** Tag selection instructions */
	private static final String TEXT_TAGS_DETAILS =
			"Select the Tags to add or remove, \nor Create new Tags";

	/** Tag selection heading */
	private static final String TEXT_TAGS_SELECTION = "Tags Selection";

	/** Text for add tags button */
	private static final String TOOLTIP_ADD_TAGS = "Add Tags.";

	/** File naming checkbox text */
	private static final String TEXT_OVERRIDE_FILE_NAMING =
			"Override default File naming. Instead use";

	/** Close button text */
	private static final String TEXT_CLOSE = "Close";

	/** Tooltip for Close button */
	private static final String TOOLTIP_CLOSE =
			"Close the dialog and do not import.";

	/** Show Thumbnails label */
	private static final String TEXT_SHOW_THUMBNAILS =
			"Show Thumbnails when imported";

	private static final String TOOLTIP_REMAINING_FORMAT =
			"%s%% of Remaining Space";

	/** String used to retrieve if the value of the load thumbnail flag. */
	private static final String LOAD_THUMBNAIL = "/options/LoadThumbnail";

	/** Tooltip for refresh files button */	
	private static final String TOOLTIP_REFRESH_FILES =
			"Refresh the file chooser list.";
	
	/** Text for refresh files button */	
	private static final String TEXT_REFRESH_FILES =
			"Refresh";
	
	/** Title for the warning dialog if the file limit is exceeded */
	private static final String TITLE_FILE_LIMIT_EXCEEDED = "File limit exceeded";
	
	/** Wildcard for the file limit used in the warning message */
	private static final String FILE_LIMIT_WILDCARD = "@@FILE_LIMIT@@";
	
	/** Warning if the file limit is exceeded */
	private static final String TEXT_FILE_LIMIT_EXCEEDED = "The import is limited to "+FILE_LIMIT_WILDCARD+" files at once.\n\nFor importing a large number of files you may\nconsider using the command line tools.";
	
	/** Warning when de-selecting the name overriding option. */
	private static final List<String> WARNING;
	
	static {
		WARNING = new ArrayList<String>();
		WARNING.add("NOTE: Some file formats do not include the file name " +
				"in their metadata, ");
		WARNING.add("and disabling this option may result in files being " +
				"imported without a ");
		WARNING.add("reference to their file name e.g. myfile.lsm [image001]'");
		WARNING.add("would show up as 'image001' with this optioned turned off.");
	}

	// functional constants & variables

	/** The length of a column. */
	private static final int COLUMN_WIDTH = 200;
	
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

	/** Button to refresh the file chooser. */
	private JButton refreshFilesButton;

	/** Indicates to use a partial name. */
	private JRadioButton partialName;

	/** Indicates to use a full name. */
	private JRadioButton fullName;

	/** Button indicating to override the name if selected. */
	private JCheckBox overrideName;

	/** Text field indicating how many folders to include. */
	private NumericalTextField numberOfFolders;

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
	private ActionListener tagSelectionListener;

	/** The selected container where to import the data. */
	private TreeImageDisplay selectedContainer;

	/** The possible nodes. */
	private Collection<TreeImageDisplay> objects;

	/** The component displaying the table, options etc. */
	private JTabbedPane tabbedPane;

	/** The type associated to the import. */
	private int type;

	/** Indicates to show thumbnails in import tab. */
	private JCheckBox showThumbnails;
	
	/** The collection of general filters. */
	private List<FileFilter> bioFormatsFileFilters;

	/** The combined filter. */
	private FileFilter bioFormatsFileFiltersCombined;

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

	/** Indicates to reload the hierarchies when the import is completed. */
	private boolean reload;
	
	/**
	 * The dialog to allow for the user to select the import location.
	 */
	private LocationDialog locationDialog;

	/** Stores the currently active file filter */
	private FileFilter currentFilter;

	/** Reference to the model.*/
	private Importer model;

	/**
	 * Adds the files to the selection.
	 * 
	 * @param importSettings The import settings.
	 */
	private void addFiles(ImportLocationSettings importSettings)
	{
		File[] files = chooser.getSelectedFiles();
		
		if (files == null || files.length == 0)
			return;
		
		List<FileObject> fileList = new ArrayList<FileObject>();
		
		for (int i = 0; i < files.length; i++) {
			checkFile(files[i], fileList);
		}
		
		chooser.setSelectedFile(new File("."));
		
		table.addFiles(fileList, importSettings);
		importButton.setEnabled(table.hasFilesToImport());
	}

	/** Displays the location of the import.*/
	private void showLocationDialog()
	{
		if (checkFileCount() && (locationDialog.centerLocation() == LocationDialog.CMD_ADD)) {
			addFiles(locationDialog.getImportSettings());
		}
	}
	
        /**
         * Check if the user wants to import more files than the current limit
         * 
         * @return <code>true</code> if the file limit is exceeded,
         *         <code>false</code> otherwise
         */
        private boolean checkFileCount() {
            int maxFiles = (Integer) ImporterAgent.getRegistry().lookup(
                    "/options/ImportFileLimit");
    
            File[] files = chooser.getSelectedFiles();
            int nFiles = 0;
            for (File file : files) {
                nFiles += countFiles(file);
            }
    
            nFiles += table.getFilesToImport().size();
    
            if (nFiles > maxFiles) {
                String msg = TEXT_FILE_LIMIT_EXCEEDED.replaceAll(
                        FILE_LIMIT_WILDCARD, "" + maxFiles);
                ImporterAgent.getRegistry().getUserNotifier()
                        .notifyError(TITLE_FILE_LIMIT_EXCEEDED, msg);
                return false;
            }
    
            return true;
        }
	
        /**
         * Counts the files within the given directory (and sub directories)
         * 
         * @param file
         *            The directory or file
         * @return The number of files within the directory (and sub directories) or
         *         <code>1</code> if the provided argument is a file instead of a
         *         directory
         */
        private int countFiles(File file) {
    
            if (file.isDirectory()) {
                int count = 0;
                for (File child : file.listFiles()) {
                    count += countFiles(child);
                }
                return count;
            }
            
            return 1;
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
			if (c.isFocusOwner())
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
		Map<String, TagAnnotationData> newTags =
				new HashMap<String, TagAnnotationData>();
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
			entry = buildTagEntryPanel(tag, icons.getIcon(IconManager.MINUS_11));
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
	private JPanel buildTagEntryPanel(TagAnnotationData tag, Icon icon) {
		JButton tagButton = new JButton(icon);
		UIUtilities.unifiedButtonLookAndFeel(tagButton);
		tagButton.addActionListener(tagSelectionListener);
		tagsMap.put(tagButton, tag);
		JPanel tagPanel = new JPanel();
		JLabel tagLabel = new JLabel(tag.getTagValue());
		tagPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		tagPanel.add(tagLabel);
		tagPanel.add(tagButton);
		return tagPanel;
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
			title = TEXT_TAGS_SELECTION;
			text = TEXT_TAGS_DETAILS;
			icon = icons.getIcon(IconManager.TAGS_48);
		}
		SelectionWizard wizard = new SelectionWizard(reg.getTaskBar()
				.getFrame(), available, selected, type, addCreation,
				ImporterAgent.getUserDetails());
		wizard.setAcceptButtonText(TEXT_SAVE);
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}

	/**
	 * Initializes the components composing the display.
	 * 
	 * @param filters The filters to handle.
	 * @param importerAction The cancel-all-imports action.
	 */
	private void initComponents(FileFilter[] filters,
	        ImporterAction importerAction) {
		canvas = new QuotaCanvas();
		sizeImportLabel = new JLabel();
		diskSpacePane = new JPanel();
		diskSpacePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		diskSpacePane.add(UIUtilities.setTextFont(TEXT_FREE_SPACE));
		diskSpacePane.add(canvas);

		showThumbnails = new JCheckBox(TEXT_SHOW_THUMBNAILS);
		showThumbnails.setVisible(false);
		
		Registry registry = ImporterAgent.getRegistry();
		
		Boolean loadThumbnails = (Boolean) registry.lookup(LOAD_THUMBNAIL);
		
		if (loadThumbnails != null) {
			if (loadThumbnails.booleanValue()) {
				showThumbnails.setVisible(true);
				showThumbnails.setSelected(loadThumbnails.booleanValue());
			}
		}

		if (!isFastConnection()) // slow connection
			showThumbnails.setSelected(false);
		long groupId = -1;
		if (model.getSelectedGroup() != null)
		    groupId = model.getSelectedGroup().getGroupId();
		if (groupId < 0) groupId = ImporterAgent.getUserDetails().getGroupId();
		
		locationDialog = new LocationDialog(owner, selectedContainer, type,
				objects, model, groupId);
		locationDialog.addPropertyChangeListener(this);
		
		tagSelectionListener = new ActionListener() {

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

		IconManager icons = IconManager.getInstance();
		
		refreshFilesButton = new JButton(TEXT_REFRESH_FILES);		  	
	    refreshFilesButton.setBackground(UIUtilities.BACKGROUND);
	    refreshFilesButton.setToolTipText(TOOLTIP_REFRESH_FILES);
	    refreshFilesButton.setActionCommand("" + CMD_REFRESH_FILES);
	    refreshFilesButton.addActionListener(this);
				
		tagButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(tagButton);
		tagButton.addActionListener(this);
		tagButton.setActionCommand("" + CMD_TAG);
		tagButton.setToolTipText(TOOLTIP_ADD_TAGS);
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));

		overrideName = new JCheckBox(TEXT_OVERRIDE_FILE_NAMING);
		overrideName.setToolTipText(UIUtilities.formatToolTipText(WARNING));
		overrideName.setSelected(true);
		ButtonGroup group = new ButtonGroup();
		fullName = new JRadioButton(TEXT_NAMING_FULL_PATH);
		group.add(fullName);
		partialName = new JRadioButton();
		partialName.setText(TEXT_NAMING_PARTIAL_PATH);
		partialName.setSelected(true);
		group.add(partialName);

		table = new FileSelectionTable(this);
		table.addPropertyChangeListener(this);
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
		chooser.setApproveButtonText(TEXT_IMPORT);
		chooser.setApproveButtonToolTipText(TOOLTIP_IMPORT);
		
		bioFormatsFileFilters = new ArrayList<FileFilter>();
		
		if (filters != null) {
			chooser.setAcceptAllFileFilterUsed(false);
			
			for (FileFilter fileFilter : filters) {
				if (fileFilter instanceof ComboFileFilter) {
					bioFormatsFileFiltersCombined = fileFilter;
					
					ComboFileFilter comboFilter = (ComboFileFilter) fileFilter;
					FileFilter[] extensionFilters = comboFilter.getFilters();
					
					for (FileFilter combinedFilter : extensionFilters) {
						bioFormatsFileFilters.add(combinedFilter);
					}
					break;
				}
			}
			
			chooser.addChoosableFileFilter(bioFormatsFileFiltersCombined);
			
			for (FileFilter fileFilter : bioFormatsFileFilters) {
				chooser.addChoosableFileFilter(fileFilter);
			}
				
			chooser.setFileFilter(bioFormatsFileFiltersCombined);
		} else {
			chooser.setAcceptAllFileFilterUsed(true);
		}
		

		closeButton = new JButton(TEXT_CLOSE);
		closeButton.setToolTipText(TOOLTIP_CLOSE);
		closeButton.setActionCommand("" + CMD_CLOSE);
		closeButton.addActionListener(this);

		cancelImportButton = new JButton(importerAction);
		importerAction.setEnabled(false);

		importButton = new JButton(TEXT_IMPORT);
		importButton.setToolTipText(TOOLTIP_IMPORT);
		importButton.setActionCommand("" + CMD_IMPORT);
		importButton.addActionListener(this);
		importButton.setEnabled(false);

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
			JComboBox filterBox = null;
			Iterator<Component> i = boxes.iterator();
			while (i.hasNext()) {
				box = (JComboBox) i.next();
				Object o = box.getItemAt(0);
				if (o instanceof FileFilter) {
					filterBox = box;
					break;
				}
			}
			if (filterBox != null) {
				filterBox.addKeyListener(new KeyAdapter() {

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
	 * @return See above.
	 */
	private JPanel buildToolBarRight() {
		JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bar.add(cancelImportButton);
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
	    JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    bar.add(closeButton);
	    int plugin = ImporterAgent.runAsPlugin();
	    if (!(plugin == LookupNames.IMAGE_J_IMPORT ||
	            plugin == LookupNames.IMAGE_J)) {
	        bar.add(Box.createHorizontalStrut(5));
	        bar.add(refreshFilesButton);
	    }

	    return bar;
	}

	/**
	 * Builds and lays out the components.
	 * 
	 * @return See above
	 */
	private JPanel buildPathComponent() {
		JLabel directoriesLabel = new JLabel(TEXT_DIRECTORIES_BEFORE_FILE);
		
		JPanel pathPanel = new JPanel();
		pathPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		pathPanel.add(numberOfFolders);
		pathPanel.add(directoriesLabel);
		
		return pathPanel;
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
		pane.setTitle(TEXT_METADATA_DEFAULTS);
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

		double[][] tablePanelDesign = {
				{ TableLayout.PREFERRED, 10, 5, TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL }
			};
		JPanel tablePanel = new JPanel(new TableLayout(tablePanelDesign));
		tablePanel.add(table.buildControls(), "0, 1, LEFT, CENTER");
		tablePanel.add(tabbedPane, "2, 1, 3, 1");
		int plugin = ImporterAgent.runAsPlugin();
		JSplitPane pane;
		if (plugin == LookupNames.IMAGE_J_IMPORT ||
		   plugin == LookupNames.IMAGE_J) {
		    JPanel panel = new JPanel();
		    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		    JLabel label = UIUtilities.setTextFont(
                    "Select where to import the image(s).");
		    panel.add(UIUtilities.buildComponentPanel(label));
		    panel.add(locationDialog.getContentPane());
		    pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
		            panel, tablePanel);
		} else {
		    pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
	                chooser, tablePanel);
		}
		
		JPanel mainPanel = new JPanel();
		double[][] mainPanelDesign = { { TableLayout.FILL },
				{ TableLayout.PREFERRED, TableLayout.FILL } };
		mainPanel.setLayout(new TableLayout(mainPanelDesign));
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
		
		add(controls, BorderLayout.SOUTH);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = UIManager.getLookAndFeel()
					.getSupportsWindowDecorations();
			if (supportsWindowDecorations)
				getRootPane().setWindowDecorationStyle(
						JRootPane.FILE_CHOOSER_DIALOG);
		}
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
	 * @param The selected files.
	 * @return See above.
	 */
	private int handleFilesSelection(File[] files) {
		int count = 0;
		if (files == null)
			return count;
		File f;
		for (int i = 0; i < files.length; i++) {
			f = files[i];
			if (!f.isHidden()) {
				count++;
			}
		}
		return count;
	}

	/** Imports the selected files. */
	public void importFiles() {
		option = CMD_IMPORT;
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
		Boolean loadThumbnails = (Boolean) ImporterAgent.getRegistry()
				.lookup(LOAD_THUMBNAIL);
		if (loadThumbnails != null)
			object.setLoadThumbnail(loadThumbnails.booleanValue());
		
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
	private boolean checkFile(File f, List<FileObject> l)
	{
		if (f == null || f.isHidden())
			return false;
		if (f.isFile()) {
			if (isFileImportable(f))
				l.add(new FileObject(f));
		} else if (f.isDirectory()) {
			File[] list = f.listFiles();
			if (list != null && list.length > 0) {
				l.add(new FileObject(f));
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
	 * @param importerAction
	 *            The cancel-all-imports action.
	 */
	public ImportDialog(JFrame owner, FileFilter[] filters,
			TreeImageDisplay selectedContainer,
			Collection<TreeImageDisplay> objects, int type,
			ImporterAction importerAction, Importer model)
	{
		super(0, TITLE, TITLE);
		this.owner = owner;
		this.objects = objects;
		this.type = type;
		this.model = model;
		this.selectedContainer = checkContainer(selectedContainer);
		setClosable(false);
		setCloseVisible(false);
		initComponents(filters, importerAction);
		buildGUI();
	}

	/**
	 * Returns the type of the import.
	 * 
	 * @return See above.
	 */
	public int getType() { return type; }

	/**
	 * Returns <code>true</code> if only one group for the user,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean isSingleGroup()
	{
		return ImporterAgent.getAvailableUserGroups().size() <= 1;
	}

	/**
	 * Returns <code>true</code> if the user can import the data for other
	 * users, <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean canImportAs() { return model.canImportAs(); }
	
	/** Display the size of files to add. */
	void onSelectionChanged() {
		if (canvas == null) return;
		long size = table.getSizeFilesInQueue();
		canvas.setSizeInQueue(size);
		int remaining = (int) Math.round(canvas.getPercentageToImport() * 100);
		String tooltip = String.format(TOOLTIP_REMAINING_FORMAT, remaining);
		sizeImportLabel.setText(UIUtilities.formatFileSize(size));
		sizeImportLabel.setToolTipText(tooltip);
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
	 * @param objects The possible objects.
	 * @param type One of the constants used to identify the type of import.
	 * @param currentGroupId The id of the group.
	 * @param userID The if of the user.
	 */
	public void reset(Collection<TreeImageDisplay> objects, int type,
			long currentGroupId, long userID) {
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
				List<TreeImageDisplay> l;
				Iterator<TreeImageDisplay> j;
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
		reset(selected, objects, type, currentGroupId, userID);
	}

	/**
	 * Resets the text and remove all the files to import.
	 * 
	 * @param selectedContainer The container where to import the files.
	 * @param objects The possible objects.
	 * @param type One of the constants used to identify the type of import.
	 * @param currentGroupId The id of the group.
	 * @param userID The id of the user.
	 */
	public void reset(TreeImageDisplay selectedContainer,
			Collection<TreeImageDisplay> objects, int type,
			long currentGroupId, long userID)
	{
		canvas.setVisible(true);
		this.selectedContainer = checkContainer(selectedContainer);
		this.objects = objects;
		this.type = type;
		
		File[] files = chooser.getSelectedFiles();
		table.allowAddition(files != null && files.length > 0);
		handleTagsSelection(new ArrayList<TagAnnotationData>());
		tabbedPane.setSelectedIndex(0);
		
		FileFilter[] filters = chooser.getChoosableFileFilters();
		
		if (filters != null && filters.length > 0)
		{
			if(currentFilter == null)
				currentFilter = filters[0];
			
			chooser.setFileFilter(currentFilter);
		}

		locationDialog.reset(this.selectedContainer, this.type, this.objects,
				currentGroupId, userID);

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
	 * Notifies that the new object has been created.
	 * 
	 * @param d The newly created object.
	 * @param parent The parent of the object.
	 */
	public void onDataObjectSaved(DataObject d, DataObject parent) {
		if (d instanceof ProjectData) locationDialog.createProject(d);
		else if (d instanceof ScreenData) locationDialog.createScreen(d);
		else if (d instanceof DatasetData) 
			locationDialog.createDataset((DatasetData) d);
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
		locationDialog.setSelectedGroup(group);
	}

	   /**
     * Adds the images from imageJ to the queue.
     *
     * @param list The files to import
     */
    public void addImageJFiles(List<FileObject> list)
    {
        int plugin = ImporterAgent.runAsPlugin();
        
        if (!(plugin == LookupNames.IMAGE_J_IMPORT ||
                plugin == LookupNames.IMAGE_J)) return;
        if (CollectionUtils.isEmpty(list)) {
            boolean active = locationDialog.isActiveWindow();
            list = new ArrayList<FileObject>();
            FileObject f, ff;
            if (active) {
                f = new FileObject(WindowManager.getCurrentImage());
                //check if there are associated files
                int[] values = WindowManager.getIDList();
                String path = f.getAbsolutePath();
                if (path != null) {
                    for (int i = 0; i < values.length; i++) {
                        ff = new FileObject(WindowManager.getImage(values[i]));
                        if (path.equals(ff.getAbsolutePath())) {
                            f.addAssociatedFile(ff);
                        }
                    }
                }
                list.add(f);
            } else {
                int[] values = WindowManager.getIDList();
                for (int i = 0; i < values.length; i++) {
                    list.add(new FileObject(WindowManager.getImage(values[i])));
                }
            }
        }
        
        ImportLocationSettings settings = locationDialog.getImportSettings();
        table.addFiles(list, settings);
        importButton.setEnabled(table.hasFilesToImport());
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
		} else if (ImportDialog.PROPERTY_GROUP_CHANGED.equals(name)
				|| ImportDialog.REFRESH_LOCATION_PROPERTY.equals(name)
				|| ImportDialog.CREATE_OBJECT_PROPERTY.equals(name)) {
			firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
		} else if (LocationDialog.ADD_TO_QUEUE_PROPERTY.equals(name)) {
		    addImageJFiles(null);
		}
	}

	/**
	 * Cancels or imports the files.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		int commandId = Integer.parseInt(evt.getActionCommand());

		switch (commandId) {
			case CMD_IMPORT:
				importFiles();
				break;
			case CMD_CLOSE:
				firePropertyChange(CANCEL_SELECTION_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
				break;
			case CMD_RESET:
				partialName.setSelected(false);
				table.resetFilesName();
				break;
			case CMD_APPLY_TO_ALL:
				table.applyToAll();
				break;
			case CMD_TAG:
				firePropertyChange(LOAD_TAGS_PROPERTY, Boolean.valueOf(false),
						Boolean.valueOf(true));
				break;
			case CMD_REFRESH_FILES:
				refreshLocation = false;
				currentFilter = chooser.getFileFilter();
				chooser.rescanCurrentDirectory();
				chooser.repaint();
				ImportLocationDetails details = new ImportLocationDetails(getType());
				firePropertyChange(REFRESH_LOCATION_PROPERTY, null, details);
				break;
		}
	}

}
