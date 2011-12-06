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
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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

//Third-party libraries
import loci.formats.gui.ComboFileFilter;
import info.clearthought.layout.TableLayout;
import org.jdesktop.swingx.JXTaskPane;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.SelectionWizard;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DiskQuota;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.rnd.RenderingControl;
import org.openmicroscopy.shoola.util.ui.ClosableTabbedPaneComponent;
import org.openmicroscopy.shoola.util.ui.NumericalTextField;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.GenericFileChooser;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;

/** 
 * Dialog used to select the files to import.
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
public class ImportDialog 
	extends ClosableTabbedPaneComponent//JDialog
	implements ActionListener, PropertyChangeListener
{

	/** Bound property indicating to create the object. */
	public static final String	CREATE_OBJECT_PROPERTY = "createObject";
	
	/** Bound property indicating to load the tags. */
	public static final String	LOAD_TAGS_PROPERTY = "loadTags";
	
	/** Bound property indicating that the cancel button is pressed. */
	public static final String	CANCEL_SELECTION_PROPERTY = "cancelSelection";
	
	/** Bound property indicating that the cancel button is pressed. */
	public static final String	CANCEL_ALL_IMPORT_PROPERTY = "cancelAllImport";
	
	/** Bound property indicating to import the selected files. */
	public static final String	IMPORT_PROPERTY = "import";

	/** Bound property indicating to refresh the location. */
	public static final String	REFRESH_LOCATION_PROPERTY = "refreshLocation";

	/** The default text. */
	private static final String	PROJECT_TXT = "Project";
	
	/** The default text. */
	private static final String	SCREEN_TXT = "Screen";
	
	/** The default text. */
	private static final String	DATASET_TXT = "Dataset";
	
	/** Action id indicating to import the selected files. */
	private static final int	IMPORT = 0;
	
	/** Action id indicating to close the dialog. */
	private static final int	CANCEL = 1;
	
	/** Action id indicating to refresh the file view. */
	private static final int	REFRESH = 2;
	
	/** Action id indicating to reset the names. */
	private static final int	RESET = 3;
	
	/** Action id indicating to apply the partial names to all.*/
	private static final int	APPLY_TO_ALL = 4;
	
	/** Action id indicating to add tags to the file. */
	private static final int	TAG = 5;
	
	/** Action id indicating to create a new dataset. */
	private static final int	CREATE_DATASET = 6;
	
	/** Action id indicating to create a new project. */
	private static final int	CREATE_PROJECT = 7;
	
	/** Action id indicating to refresh the containers. */
	private static final int	REFRESH_LOCATION = 8;
	
	/** Action id indicating to select the Project/Dataset or Screen.*/
	private static final int	LOCATION = 9;
	
	/** Action id indicating to select the Project/Dataset or Screen.*/
	private static final int	CANCEL_ALL_IMPORT = 10;
	
	/** The title of the dialog. */
	private static final String TITLE = "Select Data to Import";
	
	/** The message to display in the header. */
	private static final String MESSAGE_LOCATION = "Select where to import " +
			"the data";

	/** Warning when de-selecting the name overriding option. */
	private static final List<String> WARNING;
	
	/** The length of a column. */
	private static final int		COLUMN_WIDTH = 200;
	
	/** String used to retrieve if the value of the load thumbnail flag.*/
	private static final String LOAD_THUMBNAIL = "/options/LoadThumbnail";
	
	/** String used to retrieve if the value of the folder as dataset flag.*/
	private static final String FOLDER_AS_DATASET = "/options/FolderAsDataset";
	
	/** Indicates the context of the import */
	private static final String LOCATION_PROJECT = "Location: Project/Dataset";
	
	/** Indicates the context of the import */
	private static final String LOCATION_SCREEN = "Location: Screen";
	
	static {
		WARNING = new ArrayList<String>();
		WARNING.add("NOTE: Some file formats do not include the file name " +
				"in their metadata, ");
		WARNING.add("and disabling this option may result in files being " +
				"imported without a ");
		WARNING.add("reference to their file name e.g. " +
				"'myfile.lsm [image001]'");
		WARNING.add("would show up as 'image001' with this optioned " +
				"turned off.");
	}
	
	/** The approval option the user chose. */
	private int					option;

	/** The table hosting the file to import. */
	private FileSelectionTable  table;
	
	/** The file chooser. */
	private GenericFileChooser	chooser;
	
	/** Button to close the dialog. */
	private JButton				cancelButton;
	
	/** Button to cancel all imports. */
	private JButton				cancelImportButton;
	
	/** Button to import the files. */
	private JButton				importButton;
	
	/** Button to import the files. */
	private JButton				refreshButton;
	
	/** Button to reload the containers where to import the files. */
	private JToggleButton		reloadContainerButton;
	
	/** 
	 * Button used to select the location either <code>Project</code> 
	 * or <code>Screen</code>.
	 */
	private JButton				locationButton;
	
	/** Button used to select the screen as the location.*/
	private JLabel				locationLabel;
	
	/** 
	 * Resets the name of all files to either the full path
	 * or the partial name if selected. 
	 */
	private JButton				resetButton;
	
	/** Apply the partial name to all files. */
	private JButton				applyToAllButton;
	
	/** Indicates to use a partial name. */
	private JRadioButton		partialName;
	
	/** Indicates to use a full name. */
	private JRadioButton		fullName;
	
	/** Button indicating to override the name if selected. */
	private JCheckBox			overrideName;
	
	/** Text field indicating how many folders to include. */
	private NumericalTextField	numberOfFolders;
	
	/** The collection of supported filters. */
	private FileFilter[]	filters;

	/** Button to bring up the tags wizard. */
	private JButton						tagButton;
	
	/** The fields hosting the pixels size. First
	 * is for the size along the X-axis, then Y-axis, finally Z-axis
	 */
	private List<NumericalTextField>	pixelsSize;
	
	/** Components hosting the tags. */
	private JPanel						tagsPane;
	
	/** Map hosting the tags. */
	private Map<JButton, TagAnnotationData> tagsMap;
	
	/** The action listener used to handle tag selection. */
	private ActionListener				listener;
	
	/** The selected container where to import the data. */
	private TreeImageDisplay		selectedContainer;
	
	/** The possible nodes. */
	private Collection<TreeImageDisplay> 		objects;
	
	/** The possible P/D nodes. */
	private Collection<TreeImageDisplay> 		pdNodes;
	
	/** The possible Screen nodes. */
	private Collection<TreeImageDisplay> 		screenNodes;
	
	
	/** The component displaying the table, options etc. */
	private JTabbedPane 				tabbedPane;

	/** The collection of datasets to use by default. */
	private List<DataNode>				datasets;
	
	/** Component used to select the default dataset. */
	private JComboBox					datasetsBox;
	
	/** Component used to select the default dataset. */
	private JComboBox					parentsBox;
	
	/** The component displaying where the data will be imported. */
	private JPanel						locationPane;
	
	/** The type associated to the import. */
	private int							type;
	
	/** Button to create a new dataset or screen. */
	private JButton						addButton;
	
	/** Button to create a new dataset or screen. */
	private JButton						addProjectButton;
	
	/** Sorts the objects from the display. */
	private ViewerSorter				sorter;
	
	/** The class of reference for the container. */
	private Class						reference;
	
	/** Indicates to show thumbnails in import tab. */
	private JCheckBox					showThumbnails;

	/** Indicates to turn the folder as dataset. */
	//private JCheckBox					fadBox;
	
	/** The listener linked to the parents box. */
	private ActionListener				parentsBoxListener;
	
	/** The listener linked to the dataset box. */
	//private ActionListener				datasetsBoxListener;
	
	/** The collection of <code>HCS</code> filters. */
	private List<FileFilter> 			hcsFilters;
	
	/** The collection of general filters. */
	private List<FileFilter> 			generalFilters;
	
	/** The combine filter. */
	private FileFilter					combinedFilter;
	
	/** The component displaying the available and used disk space. */
	private JPanel						diskSpacePane;

	/** Displays the amount of free and used space. */
	private QuotaCanvas 				canvas;
	
	/** The size of the import. */
	private JLabel						sizeImportLabel;
	
	/** 
	 * Used to create a dataset using the folder containing the selected images. 
	 */
	//private JCheckBox					folderAsDatasetBox;
	
	/** The owner related to the component. */
	private JFrame						owner;
	
	/** The map holding the new nodes to create if in th P/D view.*/
	private Map<DataNode, List<DataNode>>		newNodesPD;
	
	/** The new nodes to create in the screen view.*/
	private List<DataNode>				newNodesS;
	
	/** Flag indicating that the containers view needs to be refreshed.*/
	private boolean refreshLocation;
	
	/** Indicates to pop-up the location.*/
	private boolean popUpLocation;
	
	/** The selected container if screen view.*/
	private TreeImageDisplay selectedScreen;
	
	/** The selected container if project view.*/
	private TreeImageDisplay selectedProject;
	
	/** Indicates to reload the hierarchies when the import is completed.*/
	private boolean reload;
	
	/** The component displaying the component.*/
	private JComponent toolBar;
	
	/** The number of items before adding new elements to the tool bar.*/
	private int			tbItems;
	
	/** 
	 * Creates the dataset.
	 * 
	 * @param dataset The dataset to create.
	 */
	private void createDataset(DatasetData dataset)
	{
		if (dataset == null) return;
		DataNode node = (DataNode) parentsBox.getSelectedItem();
		DataNode nn = new DataNode(dataset, node);
		List<DataNode> nodes = new ArrayList<DataNode>();
		nodes.add(nn);
		DataNode n, dn = null;
		for (int i = 0; i < datasetsBox.getItemCount(); i++) {
			n = (DataNode) datasetsBox.getItemAt(i);
			if (!n.isDefaultNode()) nodes.add(n);
			else dn = n;
		}
		List l = sorter.sort(nodes);
		if (dn != null) l.add(dn);
		datasetsBox.removeAllItems();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			datasetsBox.addItem((DataNode) i.next());
		}
		datasetsBox.setSelectedItem(nn);
		/*
		if (dataset == null || dataset.getName().trim().length() == 0) return;
		if (newNodesPD == null) 
			newNodesPD = new HashMap<DataNode, List<DataNode>>();
		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode n;
		for (int i = 0; i < datasetsBox.getItemCount(); i++) {
			n = (DataNode) datasetsBox.getItemAt(i);
			if (!n.isDefaultNode()) 
				nodes.add(n);
		}
		DataNode node = (DataNode) parentsBox.getSelectedItem();
		n = new DataNode(dataset, node);
		nodes.add(n);
		List<DataNode> l = newNodesPD.get(node);
		if (l == null) l = new ArrayList<DataNode>();
		l.add(n);
		node.addNewNode(n);
		newNodesPD.put(node, l);
		datasetsBox.removeAllItems();
		l = sorter.sort(nodes);
		Iterator i = l.iterator();
		while (i.hasNext()) {
			datasetsBox.addItem((DataNode) i.next());
		}
		datasetsBox.setSelectedItem(n);
		repaint();
		*/
	}

	/**
	 * Creates a project or screen.
	 * 
	 * @param data The project or screen to create.
	 */
	private void createContainer(DataObject data)
	{
		if (data == null) return;
		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode n;
		DataNode dn = null;
		for (int i = 0; i < parentsBox.getItemCount(); i++) {
			n = (DataNode) parentsBox.getItemAt(i);
			if (!n.isDefaultProject()) nodes.add(n);
			else dn = n;
		}
		DataNode nn = new DataNode(data);
		if (data instanceof ProjectData) 
			nn.addNode(new DataNode(DataNode.createDefaultDataset(), nn));
		nodes.add(nn);
		List l = sorter.sort(nodes);
		if (dn != null) l.add(dn);
		parentsBox.removeActionListener(parentsBoxListener);
		parentsBox.removeAllItems();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			parentsBox.addItem((DataNode) i.next());
		}
		parentsBox.addActionListener(parentsBoxListener);
		parentsBox.setSelectedItem(nn);
		repaint();
		
		/*//code if not saved.
		if (project == null || project.getName().trim().length() == 0) return;
		if (newNodesPD == null) 
			newNodesPD = new HashMap<DataNode, List<DataNode>>();
		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode n;
		DataNode defaultNode = null;
		for (int i = 0; i < parentsBox.getItemCount(); i++) {
			n = (DataNode) parentsBox.getItemAt(i);
			if (!n.isDefaultNode()) 
				nodes.add(n);
			else defaultNode = n;
		}
		n = new DataNode(project);
		n.addNode(new DataNode(DataNode.createDefaultDataset(), n));
		nodes.add(n);
		newNodesPD.put(n, new ArrayList<DataNode>());
		List l = sorter.sort(nodes);
		if (defaultNode != null) l.add(defaultNode);
		parentsBox.removeActionListener(parentsBoxListener);
		parentsBox.removeAllItems();
		parentsBox.addActionListener(parentsBoxListener);
		Iterator i = l.iterator();
		while (i.hasNext()) {
			parentsBox.addItem((DataNode) i.next());
		}
		parentsBox.setSelectedItem(n);
		repaint();
		*/
	}
	
	/**
	 * Creates a screen.
	 * 
	 * @param screen The screen to create.
	 */
	private void createScreen(ScreenData screen)
	{
		if (screen == null || screen.getName().trim().length() == 0) return;
		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode n;
		DataNode defaultNode = null;
		for (int i = 0; i < parentsBox.getItemCount(); i++) {
			n = (DataNode) parentsBox.getItemAt(i);
			if (!n.isDefaultNode()) 
				nodes.add(n);
			else defaultNode = n;
		}
		n = new DataNode(screen);
		if (newNodesS == null) newNodesS = new ArrayList<DataNode>();
		newNodesS.add(n);
		nodes.add(n);
		List l = sorter.sort(nodes);
		if (defaultNode != null) l.add(defaultNode);
		parentsBox.removeAllItems();
		Iterator i = l.iterator();
		while (i.hasNext()) {
			parentsBox.addItem((DataNode) i.next());
		}
		parentsBox.setSelectedItem(n);
		repaint();
	}
	
	
	/** Adds the files to the selection. */
	private void addFiles()
	{
		File[] files = chooser.getSelectedFiles();
		if (files == null || files.length == 0) return;
		List<File> l = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			checkFile(files[i], l);
		}
		chooser.setSelectedFile(new File("."));
		table.addFiles(l, isParentFolderAsDataset());//fadBox.isSelected());
		importButton.setEnabled(table.hasFilesToImport());
	}

	/** Displays the location of the import.*/
	private void showLocationDialog()
	{
		//addFiles();
		if (!popUpLocation) {
			addFiles();
		} else {
			LocationDialog dialog = new LocationDialog(owner, locationPane);
			if (dialog.centerLocation() == LocationDialog.ADD_OPTION) {
				addFiles();
			}
		}
	}
	
	/** 
	 * Handles <code>Enter</code> key pressed. 
	 * 
	 * @param source The source of the mouse pressed.
	 */
	private void handleEnterKeyPressed(Object source)
	{
		if (source instanceof JList || source instanceof JTable) {
			JComponent c = (JComponent) source;
			if (c.isFocusOwner()) //addFiles();
				showLocationDialog();
		}
	}
	
	/**
	 * Handles the selection of tags.
	 * 
	 * @param tags The selected tags.
	 */
	private void handleTagsSelection(Collection tags)
	{
		Collection<TagAnnotationData> set = tagsMap.values();
		Map<String, TagAnnotationData> 
			newTags = new HashMap<String, TagAnnotationData>();
		TagAnnotationData tag;
		Iterator i = set.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() < 0)
				newTags.put(tag.getTagValue(), tag);
		}
		List<TagAnnotationData> toKeep = new ArrayList<TagAnnotationData>();
		i = tags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() < 0) {
				if (!newTags.containsKey(tag.getTagValue())) {
					toKeep.add(tag);
				}
			} else toKeep.add(tag);
		}
		toKeep.addAll(newTags.values());
		
		//layout the tags
		tagsMap.clear();
		tagsPane.removeAll();
		i = toKeep.iterator();
		IconManager icons = IconManager.getInstance();
		JPanel entry;
		JPanel p = initRow();
		int width = 0;
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			entry = buildTagEntry(tag, icons.getIcon(IconManager.MINUS_11));
			if (width+entry.getPreferredSize().width >= COLUMN_WIDTH) {
		    	tagsPane.add(p);
		    	p = initRow();
				width = 0;
		    } else {
		    	width += entry.getPreferredSize().width;
		    	width += 2;
		    }
			p.add(entry);
		}
		if (p.getComponentCount() > 0) tagsPane.add(p);
		tagsPane.validate();
		tagsPane.repaint();
	}
	
	/**
	 * Creates a row.
	 * 
	 * @return See above.
	 */
	private JPanel initRow()
	{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		return p;
	}
	
	/** 
	 * Builds and lays out a tag.
	 * 
	 * @param tag The tag to display.
	 * @param icon The icon used to remove the tag from the display.
	 * @return See above.
	 */
	private JPanel buildTagEntry(TagAnnotationData tag, Icon icon)
	{
		JButton b = new JButton(icon);
		UIUtilities.unifiedButtonLookAndFeel(b);
		//add listener
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
	 * @param type			The type of objects to handle.
	 * @param available 	The available objects.
	 * @param selected  	The selected objects.
	 * @param addCreation	Pass <code>true</code> to add a component
	 * 						allowing creation of object of the passed type,
	 * 						<code>false</code> otherwise.
	 */
	private void showSelectionWizard(Class type, Collection available, 
									Collection selected, boolean addCreation)
	{
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
		long userID = ImporterAgent.getUserDetails().getId();
		SelectionWizard wizard = new SelectionWizard(
				reg.getTaskBar().getFrame(), available, selected, type,
				addCreation, userID);
		wizard.setAcceptButtonText("Save");
		wizard.setTitle(title, text, icon);
		wizard.addPropertyChangeListener(this);
		UIUtilities.centerAndShow(wizard);
	}
	
	/** Sets the properties of the dialog. */
	private void setProperties()
	{
		/*
		setTitle(TITLE);
        setModal(true);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        */
	}

	/** Installs the listeners. */
	private void installListeners()
	{
        //addWindowListener(new WindowAdapter() {
    		
			/** 
			 * Cancels the selection.
			 * @see WindowAdapter#windowClosing(WindowEvent)
			 */
			//public void windowClosing(WindowEvent e) { cancelSelection(); }
		
		//});
	}
	
	/**
	 * Formats the {@link #projectLocationButton}.
	 * 
	 * @param t The type to handle.
	 */
	private void formatSwitchButton(int t)
	{
		IconManager icons = IconManager.getInstance();
		if (t == Importer.PROJECT_TYPE) {
			locationButton.setIcon(icons.getIcon(IconManager.PROJECT));
			locationLabel.setText(LOCATION_PROJECT);
		} else {
			locationButton.setIcon(icons.getIcon(IconManager.SCREEN));
			locationLabel.setText(LOCATION_SCREEN);
		}
	}
	
	/**
	 * Handles the switch of containers either <code>Project</code>
	 * or <code>Screen</code>.
	 * 
	 * @param src The source component.
	 */
	private void handleLocationSwitch()
	{
		int t = Importer.PROJECT_TYPE;
		Collection<TreeImageDisplay> nodes = null;
		TreeImageDisplay display = null;
		if (getType() == Importer.PROJECT_TYPE) {
			t = Importer.SCREEN_TYPE;
			nodes = screenNodes;
			display = selectedScreen;
		} else {
			nodes = pdNodes;
			display = selectedProject;
		}
			
		formatSwitchButton(t);
		if (nodes == null || nodes.size() == 0) //load the missing nodes
			firePropertyChange(REFRESH_LOCATION_PROPERTY, getType(), t);
		else {
			//Iterator<TreeImageDisplay> i = nodes.iterator();
			//if (i.hasNext()) display = i.next();
			reset(display, nodes, t);
		}
	}
	
	/** 
	 * Initializes the components composing the display. 
	 * 
	 * @param filters The filters to handle.
	 */
	private void initComponents(FileFilter[] filters)
	{
		/*
		List<String> tips = new ArrayList<String>();
		tips.add("If selected, when adding images to queue, ");
    	tips.add("the folder containing the images will be ");
    	tips.add("marked to be turned into dataset.");
    	folderAsDatasetBox = new JCheckBox();
    	folderAsDatasetBox.setToolTipText(UIUtilities.formatToolTipText(tips));
    	folderAsDatasetBox.setBackground(UIUtilities.BACKGROUND_COLOR);
    	folderAsDatasetBox.setText("New Dataset from folder's name");
    	folderAsDatasetBox.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				boolean b = !folderAsDatasetBox.isSelected();
				datasetsBox.setEnabled(b);
				addButton.setEnabled(b);
			}
		});
    	*/
    	canvas = new QuotaCanvas();
		sizeImportLabel = new JLabel();
		//sizeImportLabel.setText(UIUtilities.formatFileSize(0));
		diskSpacePane = new JPanel();
		diskSpacePane.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		//diskSpacePane.setBackground(UIUtilities.BACKGROUND);
		diskSpacePane.add(UIUtilities.setTextFont("Free Space "));
		diskSpacePane.add(canvas);
		
		showThumbnails = new JCheckBox("Show Thumbnails when imported");
		showThumbnails.setVisible(false);
		Boolean b = (Boolean) ImporterAgent.getRegistry().lookup(
    			LOAD_THUMBNAIL);
    	if (b != null) {
    		if (b.booleanValue()) {
    			showThumbnails.setVisible(true);
    			showThumbnails.setSelected(true);
    		}
    	}
    	b = (Boolean) ImporterAgent.getRegistry().lookup(
    			FOLDER_AS_DATASET);
    	
    	/*
    	fadBox = new JCheckBox("Folder As Dataset");
    	tips = new ArrayList<String>();
    	tips.add("If selected, folders added to the import queue ");
    	tips.add("will be marked to be turned into dataset.");
    	fadBox.setToolTipText(UIUtilities.formatToolTipText(tips));
    	//fadBox.setVisible(type != Importer.SCREEN_TYPE);
    	fadBox.setBackground(UIUtilities.BACKGROUND);
    	fadBox.setSelected(true);
    	fadBox.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				//
				int n = handleFilesSelection(chooser.getSelectedFiles());
				if (n == 0) {
					datasetsBox.setEnabled(true);
					folderAsDatasetBox.setEnabled(true);
				} 
			}
		});
		if (b != null) fadBox.setSelected(b.booleanValue());
		*/
    	
    	if (!isFastConnection()) //slow connection
    		showThumbnails.setSelected(false);
		reference = null;
		parentsBox = new JComboBox();
		parentsBoxListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				populateDatasetsBox();
			}
		};
		parentsBox.addActionListener(parentsBoxListener);
		datasetsBox = new JComboBox();
		/*
		datasetsBoxListener = new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				DataNode node = (DataNode) datasetsBox.getSelectedItem();
				folderAsDatasetBox.setSelected(node != null && 
						node.isDefaultNode());
			}
		};
		*/
		sorter = new ViewerSorter();
		datasets = new ArrayList<DataNode>();
		addProjectButton = new JButton("New...");
		//addProjectButton.setBackground(UIUtilities.BACKGROUND);
		addProjectButton.setToolTipText("Create a new Project.");
		if (type == Importer.SCREEN_TYPE) {
			addProjectButton.setToolTipText("Create a new Screen.");
		}
		
		addProjectButton.setActionCommand(""+CREATE_PROJECT);
		addProjectButton.addActionListener(this);
		
		addButton = new JButton("New...");
		//addButton.setBackground(UIUtilities.BACKGROUND);
		addButton.setToolTipText("Create a new Dataset.");
		addButton.setActionCommand(""+CREATE_DATASET);
		addButton.addActionListener(this);

		IconManager icons = IconManager.getInstance();
		reloadContainerButton = new JToggleButton(
				icons.getIcon(IconManager.REFRESH));
		reloadContainerButton.setBackground(UIUtilities.BACKGROUND);
		reloadContainerButton.setToolTipText("Reloads the container where to " +
				"import the data.");
		reloadContainerButton.setActionCommand(""+REFRESH_LOCATION);
		reloadContainerButton.addActionListener(this);
		UIUtilities.unifiedButtonLookAndFeel(reloadContainerButton);
		
		locationButton = new JButton();
		//locationButton.setBackground(UIUtilities.BACKGROUND);
		locationButton.setToolTipText("Select the location of the data.");
		locationButton.addActionListener(this);
		locationButton.setActionCommand(""+LOCATION);
		
		locationLabel = new JLabel();
		locationLabel.setFont(locationLabel.getFont().deriveFont(Font.BOLD));
		formatSwitchButton(getType());
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
		locationPane = new JPanel();
		locationPane.setLayout(new BoxLayout(locationPane, BoxLayout.Y_AXIS));
		//locationPane.setBackground(UIUtilities.BACKGROUND);
		
		tabbedPane = new JTabbedPane();
		numberOfFolders = new NumericalTextField();
		numberOfFolders.setMinimum(0);
		numberOfFolders.setText("0");
		numberOfFolders.setColumns(3);
		//numberOfFolders.setEnabled(false);
		numberOfFolders.addPropertyChangeListener(this);
		tagsMap = new LinkedHashMap<JButton, TagAnnotationData>();

		tagButton = new JButton(icons.getIcon(IconManager.PLUS_12));
		UIUtilities.unifiedButtonLookAndFeel(tagButton);
		tagButton.addActionListener(this);
		tagButton.setActionCommand(""+TAG);
		tagButton.setToolTipText("Add Tags.");
		tagsPane = new JPanel();
		tagsPane.setLayout(new BoxLayout(tagsPane, BoxLayout.Y_AXIS));

		overrideName = new JCheckBox("Override default File naming. " +
				"Instead use");
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
			 * @see KeyListener#keyPressed(KeyEvent)
			 */
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					handleEnterKeyPressed(e.getSource());
				}
			}
		};
		if (list != null) list.addKeyListener(ka);
		if (list == null) {
			JTable t = (JTable) 
				UIUtilities.findComponent(chooser, JTable.class);
			if (t != null) t.addKeyListener(ka);
		}

		try {
			File f = UIUtilities.getDefaultFolder();
			if (f != null) chooser.setCurrentDirectory(f);
		} catch (Exception e) {
			//Ignore: could not set the default container
		}
		
		chooser.addPropertyChangeListener(this);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setControlButtonsAreShown(false);
		chooser.setApproveButtonText("Import");
		chooser.setApproveButtonToolTipText("Import the selected files " +
				"or directories");
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
				} else {
					/*
					if (ImportableObject.isHCSFormat(filter.toString())) {
						hcsFilters.add(filter);
					} else {
						generalFilters.add(filter);
					}
					*/
				}
			}
			Iterator<FileFilter> j;
			if (type == Importer.SCREEN_TYPE) {
				j = hcsFilters.iterator();
				while (j.hasNext())
					chooser.addChoosableFileFilter(j.next());
				chooser.setFileFilter(hcsFilters.get(0));
			} else {
				chooser.addChoosableFileFilter(combinedFilter);
				j = generalFilters.iterator();
				while (j.hasNext())
					chooser.addChoosableFileFilter(j.next());
				chooser.setFileFilter(combinedFilter);
			}
			while (j.hasNext())
				chooser.addChoosableFileFilter(j.next());
			//chooser.setFileFilter(filters[0]);
		} else chooser.setAcceptAllFileFilterUsed(true);
		
		
		table = new FileSelectionTable(this);
		table.addPropertyChangeListener(this);
		cancelButton = new JButton("Close");
		cancelButton.setToolTipText("Close the dialog and do not import.");
		cancelButton.setActionCommand(""+CANCEL);
		cancelButton.addActionListener(this);

		cancelImportButton = new JButton("Cancel All");
		cancelImportButton.setToolTipText("Cancel all ongoing imports.");
		cancelImportButton.setActionCommand(""+CANCEL_ALL_IMPORT);
		cancelImportButton.addActionListener(this);
		
		importButton = new JButton("Import");
		importButton.setToolTipText("Import the selected files or" +
				" directories.");
		importButton.setActionCommand(""+IMPORT);
		importButton.addActionListener(this);
		importButton.setEnabled(false);
		refreshButton = new JButton("Refresh");
		refreshButton.setToolTipText("Reloads the files view.");
		refreshButton.setActionCommand(""+REFRESH);
		refreshButton.setBorderPainted(false);
		refreshButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.setToolTipText("Reset the name of all files to either " +
				"the full path or the partial name if selected.");
		resetButton.setActionCommand(""+RESET);
		resetButton.addActionListener(this);
		applyToAllButton = new JButton("Apply Partial Name");
		applyToAllButton.setToolTipText("Apply the partial name to " +
				"all files in the queue.");
		applyToAllButton.setActionCommand(""+APPLY_TO_ALL);
		applyToAllButton.addActionListener(this);
		applyToAllButton.setEnabled(false);

		//getRootPane().setDefaultButton(cancelButton);
		
		pixelsSize = new ArrayList<NumericalTextField>();
		NumericalTextField field;
		for (int i = 0; i < 3; i++) {
			field = new NumericalTextField();
			field.setNumberType(Double.class);
			field.setColumns(2);
			pixelsSize.add(field);
		}
		initializeLocationBoxes();
		
		List<Component> boxes = 
			UIUtilities.findComponents(chooser, JComboBox.class);
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
	private JPanel buildToolBarRight()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//bar.add(resetButton);
		//bar.add(Box.createHorizontalStrut(20));
		bar.add(cancelImportButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(cancelButton);
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
	private JPanel buildToolBarLeft()
	{
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.LEFT));
		//bar.add(refreshButton);
		//bar.add(Box.createHorizontalStrut(5));
		bar.add(showThumbnails);
		return bar;
	}
	
	/**
	 * Builds and lays out the components.
	 * 
	 * @return See above
	 */
	private JPanel buildPathComponent()
	{
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
	private JXTaskPane buildMetadataComponent()
	{
		JXTaskPane pane = new JXTaskPane();
		Font font = pane.getFont();
		pane.setFont(font.deriveFont(font.getStyle(), font.getSize()-2));
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
	private JPanel buildPixelSizeComponent()
	{
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
	private JComponent buildNamingComponent()
	{
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
	private JPanel buildAnnotationComponent()
	{
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
	 * @param container Container where to import the image.
	 * @return See above.
	 */
	private JPanel buildOptionsPane()
	{
		//Lays out the options
		JPanel options = new JPanel();
		double[][] size = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.PREFERRED, 
			TableLayout.PREFERRED}};
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
	private JPanel createRow()
	{
		return createRow(UIUtilities.BACKGROUND);
	}
	
	/**
	 * Creates a row.
	 * 
	 * @param background The background of color.
	 * @return See above.
	 */
	private JPanel createRow(Color background)
	{
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		if (background != null)
			row.setBackground(background);
		row.setBorder(null);
		return row;
	}
	
	/**
	 * Returns the collection of new datasets.
	 * 
	 * @return See above.
	 */
	private List<DataNode> getOrphanedNewDatasetNode()
	{
		if (newNodesPD == null) return null;
		Iterator<DataNode> i = newNodesPD.keySet().iterator();
		DataNode n;
		while (i.hasNext()) {
			n = i.next();
			if (n.isDefaultNode()) 
				return newNodesPD.get(n);
		}
		return null;
	}
	
	/**
	 * Retrieves the new nodes to add the project.
	 * 
	 * @param data The data object to handle.
	 * @param node The node hosting the data object.
	 */
	private void getNewDataset(DataObject data, DataNode node)
	{
		if (newNodesPD == null || data instanceof ScreenData) return;
		Iterator<DataNode> i = newNodesPD.keySet().iterator();
		DataNode n;
		DataObject ho;
		List<DataNode> l;
		Iterator<DataNode> k;
		while (i.hasNext()) {
			n = i.next();
			ho = n.getDataObject();
			if (ho.getClass().equals(data.getClass()) 
					&& data.getId() == ho.getId()) {
				l = newNodesPD.get(n);
				if (l != null) {
					k = l.iterator();
					while (k.hasNext()) {
						node.addNewNode(k.next());
					}
				}
			}
		}
	}
	
	/** Initializes the selection boxes. */
	private void initializeLocationBoxes()
	{
		parentsBox.removeActionListener(parentsBoxListener);
		parentsBox.removeAllItems();
		parentsBox.addActionListener(parentsBoxListener);
		datasetsBox.removeAllItems();
		List<DataNode> topList = new ArrayList<DataNode>();
		List<DataNode> datasetsList = new ArrayList<DataNode>();
		DataNode n;
		Object ho;
		TreeImageDisplay node;
		if (objects != null && objects.size() > 0) {
			Iterator<TreeImageDisplay> i = objects.iterator();
			while (i.hasNext()) {
				node = i.next();
				ho = node.getUserObject();
				if (ho instanceof ProjectData || ho instanceof ScreenData) {
					n = new DataNode((DataObject) ho);
					getNewDataset((DataObject) ho, n);
					n.setRefNode(node);
					topList.add(n); 
				} else if (ho instanceof DatasetData) {
					n = new DataNode((DataObject) ho);
					n.setRefNode(node);
					datasetsList.add(n);
				}
			}
		}
		//check if new top nodes
		DataObject data;
		Iterator<DataNode> j;
		if (type == Importer.PROJECT_TYPE) {
			if (newNodesPD != null) {
				j = newNodesPD.keySet().iterator();
				while (j.hasNext()) {
					n = j.next();
					data = n.getDataObject();
					if (data.getId() <= 0) {
						topList.add(n);
					}
				}
			}
		} else if (type == Importer.SCREEN_TYPE) {
			if (newNodesS != null) {
				j = newNodesS.iterator();
				while (j.hasNext()) {
					n = j.next();
					data = n.getDataObject();
					if (data.getId() <= 0)
						topList.add(n);
				}
			}
		}
		List sortedList = new ArrayList();
		if (topList.size() > 0) {
			sortedList = sorter.sort(topList);
		}
		int size;
		List finalList = new ArrayList();
		int index = 0;
		if (type == Importer.PROJECT_TYPE) {
			//sort the node
			List<DataNode> l = getOrphanedNewDatasetNode();
			if (datasetsList.size() > 0) { //orphaned datasets.
				datasetsList.add(new DataNode(DataNode.createDefaultDataset()));
				if (l != null)
					datasetsList.addAll(l);
				n = new DataNode(datasetsList);
			} else {
				List<DataNode> list = new ArrayList<DataNode>();
				list.add(new DataNode(DataNode.createDefaultDataset()));
				if (l != null && l.size() > 0)
					list.addAll(l);
				n = new DataNode(list);
			}
			//sortedList.add(n);
			finalList.add(n);
			finalList.addAll(sortedList);
			parentsBox.removeActionListener(parentsBoxListener);
			parentsBox.setModel(new DefaultComboBoxModel(finalList.toArray()));
			parentsBox.addActionListener(parentsBoxListener);
			//Determine the node to select.
			size = parentsBox.getItemCount();
			if (selectedContainer != null) {
				ho = selectedContainer.getUserObject();
				ProjectData p = null;
				if (ho instanceof ProjectData) {
					p = (ProjectData) ho;
				} else if (ho instanceof DatasetData) {
					node = selectedContainer.getParentDisplay();
					if (node != null && 
						node.getUserObject() instanceof ProjectData) {
						p = (ProjectData) node.getUserObject();
					}
				}
				if (p != null) {
					long id = p.getId();
					for (int i = 0; i < size; i++) {
						n = (DataNode) parentsBox.getItemAt(i);
						if (n.getDataObject().getId() == id) {
							index = i;
							break;
						}
					}
				}
			} 
			parentsBox.setSelectedIndex(index);
		} else if (type == Importer.SCREEN_TYPE) {
			finalList.add(new DataNode(DataNode.createDefaultScreen()));
			finalList.addAll(sortedList);
			parentsBox.removeActionListener(parentsBoxListener);
			parentsBox.setModel(new DefaultComboBoxModel(finalList.toArray()));
			parentsBox.addActionListener(parentsBoxListener);
			size = parentsBox.getItemCount();
			index = 0;
			if (selectedContainer != null) {
				ho = selectedContainer.getUserObject();
				if (ho instanceof ScreenData) {
					long id = ((ScreenData) ho).getId();
					for (int i = 0; i < size; i++) {
						n = (DataNode) parentsBox.getItemAt(i);
						if (n.getDataObject().getId() == id) {
							index = i;
							break;
						}
					}
					
				}
			}
			parentsBox.setSelectedIndex(index);
		}
	}
	
	/** Populates the datasets box depending on the selected project. */
	private void populateDatasetsBox()
	{
		if (type == Importer.SCREEN_TYPE) return;
		DataNode n = (DataNode) parentsBox.getSelectedItem();
		List<DataNode> list = n.getDatasetNodes();
		List<DataNode> nl = n.getNewNodes();
		if (nl != null) list.addAll(nl);
		List l = sorter.sort(list);
		//datasetsBox.removeActionListener(datasetsBoxListener);
		datasetsBox.removeAllItems();
		
		datasetsBox.setModel(new DefaultComboBoxModel(l.toArray()));
		if (selectedContainer != null) {
			Object o = selectedContainer.getUserObject();
			if (o instanceof DatasetData) {
				DatasetData d = (DatasetData) o;
				Iterator<DataNode> i = l.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (n.getDataObject().getId() == d.getId()) {
						datasetsBox.setSelectedItem(n);
						break;
					}
				}
			}
		} else { // no node selected
			if (l.size() > 1) {
				Iterator<DataNode> i = l.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (!n.isDefaultDataset()) {
						datasetsBox.setSelectedItem(n);
						break;
					}
				}
			}
			
		}
		//now check what is the selected node
		//n = (DataNode) datasetsBox.getSelectedItem();
		//folderAsDatasetBox.setSelected(n.isDefaultNode());
		//datasetsBox.addActionListener(datasetsBoxListener);
	}
	
	/**
	 * Builds and lays out the controls for the location.
	 * 
	 * @return See above.
	 */
	private JComponent buildLocationBar()
	{
		toolBar = new JPanel();
		//bar.setBackground(UIUtilities.BACKGROUND);
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		toolBar.add(reloadContainerButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(locationButton);
		toolBar.add(Box.createHorizontalStrut(5));
		toolBar.add(locationLabel);
		toolBar.add(Box.createHorizontalStrut(5));
		tbItems = toolBar.getComponentCount();
		return toolBar;
	}
	
	/**
	 * Returns the file queue and indicates where the files will be imported.
	 * 
	 * @return See above.
	 */
	private void buildLocationPane()
	{
		locationPane.removeAll();
		//locationPane.add(buildLocationBar());
		//locationPane.add(new JSeparator());
		JPanel row = createRow(null);
		String message = PROJECT_TXT;
		if (type == Importer.SCREEN_TYPE) message = SCREEN_TXT;
		row.add(UIUtilities.setTextFont(MESSAGE_LOCATION));
		locationPane.add(row);
		locationPane.add(Box.createVerticalStrut(2));
		locationPane.add(new JSeparator());
		row = createRow(null);
		row.add(UIUtilities.setTextFont(message));
		row.add(parentsBox);
		row.add(addProjectButton);
		locationPane.add(row);
		if (type == Importer.PROJECT_TYPE) {
			locationPane.add(Box.createVerticalStrut(8));
			row = createRow(null);
			row.add(UIUtilities.setTextFont(DATASET_TXT));
			row.add(datasetsBox);
			row.add(addButton);
			locationPane.add(row);
			locationPane.add(new JSeparator());
		}
	}

	/** 
	 * Lays out the quota.
	 * 
	 * @return See above.
	 */
	private JPanel buildQuotaPane()
	{
		JPanel row = new JPanel();
		row.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		row.add(UIUtilities.buildComponentPanelRight(diskSpacePane, 0, 0, true));
		row.add(UIUtilities.setTextFont(QuotaCanvas.IMPORT_SIZE_TEXT));
		row.add(UIUtilities.buildComponentPanel(sizeImportLabel, 0, 0, true));
		row.setBorder(null);
		return row;
	}
	
	private JPanel container;
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		setLayout(new BorderLayout(0, 0));
		add(buildLocationBar(), BorderLayout.NORTH);
		JPanel p = new JPanel();
		p.setBorder(null);
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildQuotaPane());
		p.add(table);
		tabbedPane.add("Files to import", p);
		tabbedPane.add("Options", buildOptionsPane());
		
		container = new JPanel();
		double[][] size = {{TableLayout.PREFERRED, 10, 5, TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.FILL}};
		container.setLayout(new TableLayout(size));
		container.add(table.buildControls(), "0, 1, LEFT, CENTER");
		
		buildLocationPane();
		if (!popUpLocation)
			container.add(locationPane, "3, 0");
		container.add(tabbedPane, "2, 1, 3, 1");
		JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chooser, 
				container);
		JPanel body = new JPanel();
		double[][] ss = {{TableLayout.FILL}, 
				{TableLayout.PREFERRED, TableLayout.FILL}};
		body.setLayout(new TableLayout(ss));
		body.setBackground(UIUtilities.BACKGROUND);

		body.add(pane, "0, 1");
		add(body, BorderLayout.CENTER);
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
		
		//Lays out the buttons.
		JPanel bar = new JPanel();
		bar.setLayout(new BoxLayout(bar, BoxLayout.X_AXIS));
		bar.add(buildToolBarLeft());
		bar.add(buildToolBarRight());
		controls.add(new JSeparator());
		controls.add(bar);
		
		//c.add(controls, BorderLayout.SOUTH);
		add(controls, BorderLayout.SOUTH);
		if (JDialog.isDefaultLookAndFeelDecorated()) {
			boolean supportsWindowDecorations = 
				UIManager.getLookAndFeel().getSupportsWindowDecorations();
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
	private boolean isFastConnection()
	{
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
	private int handleFilesSelection(File[] files)
	{
		int count = 0;
		if (files == null) return count;
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
    private void importFiles()
    {
    	option = IMPORT;
    	importButton.setEnabled(false);
    	//Set the current directory as the defaults
    	File dir = chooser.getCurrentDirectory();
    	if (dir != null) UIUtilities.setDefaultFolder(dir.toString());
    	List<ImportableFile> files = table.getFilesToImport();
    	// That's the hard part.
    	if (files.size() == 0) return;
    	ImportableObject object = new ImportableObject(files,
    			overrideName.isSelected());
    	Iterator<ImportableFile> i = files.iterator();
    	ImportableFile file;

    	if (!reload) {
    		while (i.hasNext()) {
    			file = i.next();
    			if (file.isFolderAsContainer() && 
    					!ImportableObject.isHCSFile(file.getFile())) {
    				//going to check if the dataset has been created.
    				reload = true;
    				break;
    			}
    		}
    	}
    	
    	/*
    	ProjectData project;
    	DataObject parent;
    	DatasetData dataset, folder;
    	OmeroDataService svc = ImporterAgent.getRegistry().getDataService();
    	Logger log = ImporterAgent.getRegistry().getLogger();
    	while (i.hasNext()) {
			file = i.next();
			if (file.isFolderAsContainer() && 
					!ImportableObject.isHCSFile(file.getFile())) {
				//going to check if the dataset has been created.
				parent = file.getParent();
				try {
					if (parent != null && parent instanceof ProjectData) {
						folder = (DatasetData) object.createFolderAsContainer(
								file);
						dataset = object.isDatasetCreated(parent.getId(),
								folder);
						
						if (dataset == null) {
							dataset = (DatasetData) 
								svc.createDataObject(folder, parent, null);
							//reload the project.
							object.registerDataset(parent.getId(), dataset);
							//toReload.add(parent);
							reload = true;
						}
						file.setLocation(parent, dataset);
						file.setFolderAsContainer(false);
					} else if (parent == null) {
						folder = (DatasetData) object.createFolderAsContainer(
								file);
						parent = object.hasObjectBeenCreated(folder);
						if (parent == null) {
							dataset = (DatasetData) 
								svc.createDataObject(folder, null, 
										null);
							object.addNewDataObject(dataset);
							reload = true;
						} else dataset = (DatasetData) parent;
						file.setLocation(null, dataset);
						file.setFolderAsContainer(false);
					}
				} catch (Exception e) {
					LogMessage msg = new LogMessage();
					msg.print("Cannot create container");
					msg.print(e);
					log.error(this, msg);
				}
			}
		}
		if (reload) {
    		Class klass = ProjectData.class;
    		if (type == Importer.SCREEN_TYPE)
    			klass = ScreenData.class;
    		try {
    			ExperimenterData exp = ImporterAgent.getUserDetails();
    			Set set = svc.loadContainerHierarchy(klass, null, false, 
        				exp.getId(), -1);
    			if (set != null) {
    				
    				Set nodes = TreeViewerTranslator.transformHierarchy(set, 
    						exp.getId(), -1);
    				DataNode node = (DataNode) parentsBox.getSelectedItem();
    				Iterator kk = nodes.iterator();
					TreeImageDisplay display;
					Object ho;
					DataObject o = null;
					String name = "", hoName = "";
    				if (node.isDefaultNode()) {
    					selectedContainer = null;
    					node = (DataNode) datasetsBox.getSelectedItem();
    					if (!node.isDefaultNode()) {
    						o = node.getDataObject();
    						name = node.toString().trim();
    					}
    				} else {
    					o = node.getDataObject();
    					name = node.toString().trim();
    				}
    				if (o != null) {
    					while (kk.hasNext()) {
    						display = (TreeImageDisplay) kk.next();
    						ho = display.getUserObject();
    						if (ho instanceof ProjectData) {
    							hoName = ((ProjectData) ho).getName();
    						} else if (ho instanceof ScreenData) {
    							hoName = ((ScreenData) ho).getName();
    						}
    						if (ho.getClass().equals(o.getClass()) && 
    								name.equals(hoName)) {
    							selectedContainer = display;
    							break;
    						}
    					}
    				}
    				
    				reset(selectedContainer, nodes, type);
    			}
			} catch (Exception e) {
				LogMessage msg = new LogMessage();
				msg.print("Cannot reload container");
				msg.print(e);
				log.error(this, msg);
			}
    	}
    	*/
    	
    	
    	
    	object.setScanningDepth(ImporterAgent.getScanningDepth());
    	Boolean b = (Boolean) ImporterAgent.getRegistry().lookup(
    			LOAD_THUMBNAIL);
    	if (b != null)
    		object.setLoadThumbnail(b.booleanValue());
    	//if slow connection 
    	if (!isFastConnection())
    		object.setLoadThumbnail(false);
    	if (showThumbnails.isVisible()) {
    		object.setLoadThumbnail(showThumbnails.isSelected());
    	}
    	//tags
    	if (tagsMap.size() > 0) object.setTags(tagsMap.values());
    	if (partialName.isSelected()) {
    		Integer number = (Integer) numberOfFolders.getValueAsNumber();
        	if (number != null && number >= 0) object.setDepthForName(number);
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
			} else size[index] = 1;
			index++;
		}
    	if (count > 0) object.setPixelsSize(size);
    	//Check if we need to display the refresh text
    	boolean refresh = false;
    	Iterator<ImportableFile> j = files.iterator();
    	while (j.hasNext()) {
    		if (j.next().isFolderAsContainer()) {
    			refresh = true;
    			break;
    		}
		}
    	if (newNodesPD != null && newNodesPD.size() > 0 ||
				newNodesS != null && newNodesS.size() > 0) {
			refresh = true;
		}
    	if (refresh) refreshLocation = true;
    	if (newNodesPD != null) newNodesPD.clear();
    	if (newNodesS != null) newNodesS.clear();
    	firePropertyChange(IMPORT_PROPERTY, null, object);
    	table.removeAllFiles();
    	tagsMap.clear();
		tagsPane.removeAll();
		tagsPane.repaint();
    	//sizeImportLabel.setText(UIUtilities.formatFileSize(0));
    	//setVisible(false);
    	//dispose();
    }

	/**
	 * Checks if the file can be added to the passed list. Returns the 
	 * <code>true</code> if the file is a directory, <code>false</code>
	 * otherwise.
	 * 
	 * @param f The file to handle.
	 */
	private boolean checkFile(File f, List<File> l)
	{
		if (f == null || f.isHidden()) return false;
		if (f.isFile()) {
			if (isFileImportable(f)) l.add(f);
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
	 * Returns <code>true</code> if the file can be imported, 
	 * <code>false</code> otherwise.
	 * 
	 * @param f The file to check.
	 * @return See above.
	 */
	private boolean isFileImportable(File f)
	{
		if (f == null || f.isHidden()) return false;
		return true;
	}

	/**
	 * Checks if the passed container is hosting the desired object.
	 * 
	 * @param container The container to handle.
	 * @return See above.
	 */
	private TreeImageDisplay checkContainer(TreeImageDisplay container)
	{
		if (container == null) return null;
		Object ho = container.getUserObject();
		if (ho instanceof DatasetData || ho instanceof ProjectData ||
			ho instanceof ScreenData)
			return container;
		return null;
	}
	
	/** 
     * Creates a new instance.
     * 
     * @param owner 	The owner of the dialog.
     * @param filters 	The list of filters.
     * @param containers The container where to import the files.
     * @param objects    The possible objects.
     * @param type 		One of the type constants.
     */
    public ImportDialog(JFrame owner, FileFilter[] filters, 
    		TreeImageDisplay selectedContainer, 
    		Collection<TreeImageDisplay> objects, int type)
    {
    	//super(owner);
    	super(0, TITLE, TITLE);
    	selectedContainer = checkContainer(selectedContainer);
    	this.owner = owner;
    	setClosable(false);
    	setCloseVisible(false);
    	this.objects = objects;
    	if (type == Importer.PROJECT_TYPE) {
    		pdNodes = objects;
    		selectedProject = selectedContainer;
    	} else {
    		screenNodes = objects;
    		selectedScreen = selectedContainer;
    	}
    	this.type = type;
    	this.selectedContainer = selectedContainer;
    	popUpLocation = selectedContainer == null;
    	setProperties();
    	initComponents(filters);
    	installListeners();
    	buildGUI();
    	//Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    	//setSize(7*(screenSize.width/10), 7*(screenSize.height/10));
    }

    /** 
     * Returns the type of the import.
     * 
     * @return See above.
     */
    public int getType() { return type; }
    
    /** Display the size of files to add. */
    void onSelectionChanged()
    {
		if (canvas != null) {
			long size = table.getSizeFilesInQueue();
			canvas.setSizeInQueue(size);
			String v = (int) Math.round(canvas.getPercentageToImport()*100)
			+"% of Remaining Space";
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
    	if (type == Importer.SCREEN_TYPE) return false;
    	DataNode node = (DataNode) datasetsBox.getSelectedItem();
    	return node.isDefaultDataset();
    	//return folderAsDatasetBox.isSelected();
    }
    /**
	 * Returns the name to display for a file.
	 * 
	 * @param fullPath The file's absolute path.
	 * @return See above.
	 */
	String getDisplayedFileName(String fullPath)
	{
		if (fullPath == null || !partialName.isSelected()) return fullPath;
		Integer number = (Integer) numberOfFolders.getValueAsNumber();
		return UIUtilities.getDisplayedFileName(fullPath, number);
	}
	
	/**
	 * Returns <code>true</code> if the folder can be used as a container,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean useFolderAsContainer()
	{
		return (type != Importer.SCREEN_TYPE);
	}

	/**
	 * Returns where to import the file when selected.
	 * 
	 * @return See above.
	 */
	DataNode getImportLocation()
	{
		if (type == Importer.SCREEN_TYPE) {
			if (parentsBox.getItemCount() > 0) 
				return (DataNode) parentsBox.getSelectedItem();
			return null;
		}
		if (datasetsBox.getItemCount() > 0) {
			return (DataNode) datasetsBox.getSelectedItem();
		}
		return null;
	}
	
	/**
	 * Returns where to import the file when selected.
	 * 
	 * @return See above.
	 */
	DataNode getParentImportLocation()
	{
		if (parentsBox.getItemCount() > 0) 
			return (DataNode) parentsBox.getSelectedItem();
		return null;
	}
	
	/**
	 * Returns <code>true</code> to indicate that the refresh containers
	 * view needs to be refreshed.
	 * 
	 * @return See above.
	 */
	public boolean isRefreshLocation() { return refreshLocation; }
	
	/**
     * Resets the text and remove all the files to import.
     * 
     * @param objects	The possible objects.
     * @param type		One of the constants used to identify the type of 
     * 					import.
     */
	public void reset(Collection<TreeImageDisplay> objects, int type)
	{
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
					if (nho.getClass().equals(ho.getClass()) &&
						nho instanceof DataObject) {
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
						if (cho.getClass().equals(ho.getClass()) &&
							cho instanceof DataObject) {
							if (((DataObject) cho).getId() == id) {
								selected = child;
								break;
							}
						}
					}
				}
			}
		}
		reset(selected, objects, type);
	}
	
    /**
     * Resets the text and remove all the files to import.
     * 
     * @param selectedContainer The container where to import the files.
     * @param objects    The possible objects.
     * @param type       One of the constants used to identify the type of 
     * 					 import.
     */
	public void reset(TreeImageDisplay selectedContainer, 
			Collection<TreeImageDisplay> objects, int type)
	{
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
		formatSwitchButton(type);
		if (oldType != this.type) { 
			//change filters.
			//reset name
			FileFilter[] filters = chooser.getChoosableFileFilters();
			for (int i = 0; i < filters.length; i++) {
				chooser.removeChoosableFileFilter(filters[i]);
			}
			Iterator<FileFilter> j;
			if (type == Importer.SCREEN_TYPE) {
				j = hcsFilters.iterator();
				while (j.hasNext()) {
					chooser.addChoosableFileFilter(j.next());
				}
				chooser.setFileFilter(hcsFilters.get(0));
			} else {
				chooser.addChoosableFileFilter(combinedFilter);
				j = generalFilters.iterator();
				while (j.hasNext()) {
					chooser.addChoosableFileFilter(j.next());
				}
				chooser.setFileFilter(combinedFilter);
			}
			//File[] files = chooser.getSelectedFiles();
			//table.reset(files != null && files.length > 0);
		}
		File[] files = chooser.getSelectedFiles();
		table.allowAddition(files != null && files.length > 0);
		handleTagsSelection(new ArrayList());
		tabbedPane.setSelectedIndex(0);
		FileFilter[] filters = chooser.getChoosableFileFilters();
		if (filters != null && filters.length > 0)
			chooser.setFileFilter(filters[0]);
		initializeLocationBoxes();
		buildLocationPane();
		boolean b = popUpLocation;
		popUpLocation = this.selectedContainer == null;
		if (b != popUpLocation) {
			if (b) container.add(locationPane, "3, 0");
			else container.remove(locationPane);
			container.repaint();
		}
		locationPane.repaint();
		tagsPane.removeAll();
		tagsMap.clear();
	}
	
    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int showDialog()
    {
	    UIUtilities.setLocationRelativeToAndShow(getParent(), this);
	    return option;
    }

    /**
     * Shows the chooser dialog. 
     * 
     * @return The option selected.
     */
    public int centerDialog()
    {
	    UIUtilities.centerAndShow(this);
	    return option;
    }
    
	/**
	 * Sets the collection of existing tags.
	 * 
	 * @param tags The collection of existing tags.
	 */
	public void setTags(Collection tags)
	{
		if (tags == null) return;
		Collection<TagAnnotationData> set = tagsMap.values();
		List<Long> ids = new ArrayList<Long>();
		List available = new ArrayList();
		List selected = new ArrayList();
		TagAnnotationData tag;
		Iterator i = set.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (tag.getId() > 0)
				ids.add(tag.getId());
		}
		i = tags.iterator();
		while (i.hasNext()) {
			tag = (TagAnnotationData) i.next();
			if (ids.contains(tag.getId())) 
				selected.add(tag);
			else available.add(tag);
		}
		//show the selection wizard
		showSelectionWizard(TagAnnotationData.class, available, selected, true);
	}
	
	/**
	 * Displays the used and available disk space.
	 * 
	 * @param quota The value to set.
	 */
	public void setDiskSpace(DiskQuota quota)
	{
		if (quota == null) return;
		long free = quota.getAvailableSpace();
		long used = quota.getUsedSpace();
		if (free <= 0 || used < 0) return;
		canvas.setPercentage(quota);
		canvas.setVisible(true);
	}
	
	/**
	 * Adds the component.
	 * 
	 * @param bar The component to add.
	 */
	public void addToolBar(JComponent bar)
	{
		if (bar == null) return;
		toolBar.add(bar);
		//invoke when master
		cancelButton.setVisible(false);
	}
	
	/**
	 * Refreshes the display when the user reconnect to server.
	 * 
	 * @param bar The component to add.
	 */
	public void onReconnected(JComponent bar)
	{
		int n = toolBar.getComponentCount();
		int diff = n-tbItems;
		if (diff > 0) {
			for (int i = 0; i < diff; i++) {
				toolBar.remove(tbItems+i);
			}
			toolBar.add(bar);
			toolBar.validate();
			toolBar.repaint();
		}
		table.removeAllFiles();
		locationPane.repaint();
		tagsPane.removeAll();
		tagsMap.clear();
	}
	
	/** 
	 * Notifies that the new object has been created.
	 * 
	 * @param d The newly created object.
	 * @param parent The parent of the object.
	 */
	public void onDataObjectSaved(DataObject d, DataObject parent)
	{
		if (d instanceof ProjectData || d instanceof ScreenData) {
			createContainer(d);
		//} else if (d instanceof ScreenData) {
		//	createScreen((ScreenData) d);
		} else if (d instanceof DatasetData) {
			createDataset((DatasetData) d);
		}
	}
	
	/**
	 * Returns <code>true</code> if need to reload the hierarchies,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean reloadHierarchies() { return reload; }

	/**
	 * Reacts to property fired by the table.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (FileSelectionTable.ADD_PROPERTY.equals(name)) {
			//addFiles();
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
		    	if (number != null && number >= 0) table.applyToAll();
			}
		} else if (SelectionWizard.SELECTED_ITEMS_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Set set = m.entrySet();
			Entry entry;
			Iterator i = set.iterator();
			Class type;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				type = (Class) entry.getKey();
				if (TagAnnotationData.class.getName().equals(type.getName()))
					handleTagsSelection((Collection) entry.getValue());
			}
		} else if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name)) {
			Object ho = evt.getNewValue();
			List<DataObject> l = new ArrayList<DataObject>();
			if (ho instanceof ProjectData || ho instanceof ScreenData) {
				l.add((DataObject) ho);
			} else if (ho instanceof DatasetData) {
				l.add((DataObject) ho);
				DataNode n = (DataNode) parentsBox.getSelectedItem();
				if (!n.isDefaultNode()) {
					l.add(n.getDataObject());
				}
			}
			if (l.size() > 0) 
				firePropertyChange(CREATE_OBJECT_PROPERTY, null, l);
			/*
			if (ho instanceof DatasetData)
				createDataset((DatasetData) ho);
			else if (ho instanceof ProjectData)
				createProject((ProjectData) ho);
			else if (ho instanceof ScreenData)
				createScreen((ScreenData) ho);
				*/
		}
	}

	/**
	 * Cancels or imports the files.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt)
	{
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
			case CREATE_DATASET:
				d = new EditorDialog(owner, new DatasetData(), false);
				d.addPropertyChangeListener(this);
				d.setModal(true);
				UIUtilities.centerAndShow(d);
				break;
			case CREATE_PROJECT:
				if (type == Importer.PROJECT_TYPE)
					d = new EditorDialog(owner, new ProjectData(), false);
				else d = new EditorDialog(owner, new ScreenData(), false);
				d.addPropertyChangeListener(this);
				d.setModal(true);
				UIUtilities.centerAndShow(d);
				break;
			case REFRESH_LOCATION:
				refreshLocation = false;
				chooser.rescanCurrentDirectory();
				chooser.repaint();
				firePropertyChange(REFRESH_LOCATION_PROPERTY, 
						-1, getType());
				break;
			case LOCATION:
				handleLocationSwitch();
				break;
			case CANCEL_ALL_IMPORT:
				firePropertyChange(CANCEL_ALL_IMPORT_PROPERTY,
						Boolean.valueOf(false), Boolean.valueOf(true));
		}
	}

}
