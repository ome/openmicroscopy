/*
 * org.openmicroscopy.shoola.agents.fsimporter.chooser.LocationDialog 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageRenderer;
import org.openmicroscopy.shoola.util.ui.ComboBoxToolTipRenderer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.GroupData;
import pojos.ProjectData;
import pojos.ScreenData;

//Third-party libraries

//Application-internal dependencies

/**
 * Displays the possible location of the imports.
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
public class LocationDialog extends JDialog implements ActionListener,
		PropertyChangeListener, ChangeListener {

	/** Bound property indicating to change the import group. */
	public static final String GROUP_CHANGED_PROPERTY = "groupChanged";
			
	/** Reference to the <code>Group Private</code> icon. */
	private static final Icon GROUP_PRIVATE_ICON;

	/** Reference to the <code>Group RWR---</code> icon. */
	private static final Icon GROUP_READ_ONLY_ICON;

	/** Reference to the <code>Group RWRA--</code> icon. */
	private static final Icon GROUP_READ_LINK_ICON;

	/** Reference to the <code>Group RWRW--</code> icon. */
	private static final Icon GROUP_READ_WRITE_ICON;

	/** Reference to the <code>Group</code> icon. */
	private static final Icon GROUP_PUBLIC_READ_ICON;

	/** Reference to the <code>Group</code> icon. */
	private static final Icon GROUP_PUBLIC_READ_WRITE_ICON;

	static {
		IconManager icons = IconManager.getInstance();
		GROUP_PRIVATE_ICON = icons.getIcon(IconManager.PRIVATE_GROUP);
		GROUP_READ_ONLY_ICON = icons.getIcon(IconManager.READ_GROUP);
		GROUP_READ_LINK_ICON = icons.getIcon(IconManager.READ_LINK_GROUP);
		GROUP_READ_WRITE_ICON = icons.getIcon(IconManager.READ_WRITE_GROUP);
		GROUP_PUBLIC_READ_ICON = icons.getIcon(IconManager.PUBLIC_GROUP);
		GROUP_PUBLIC_READ_WRITE_ICON = icons.getIcon(IconManager.PUBLIC_GROUP);
	}

	/** The possible nodes. */
	private Collection<TreeImageDisplay> objects;

	/** Action id indicating to create a new project. */
	private static final int CMD_CREATE_PROJECT = 1;

	/** Action id indicating to create a new dataset. */
	private static final int CMD_CREATE_DATASET = 2;

	/** Action id indicating to create a new screen. */
	private static final int CMD_CREATE_SCREEN = 3;

	/** User has chosen to force a refresh of the containers */
	private static final int CMD_REFRESH_DISPLAY = 4;

	/** User has selected to add the files. */
	public final static int CMD_ADD = 5;

	/** User has selected to cancel. */
	public final static int CMD_CLOSE = 6;
	
	/** The default text for a project. */
	private static final String LABEL_PROJECT = "Project";

	/** The default text for a dataset. */
	private static final String LABEL_DATASET = "Dataset";

	/** The default text for a screen. */
	private static final String LABEL_SCREEN = "Screen";

	/** The message to display in the header. */
	private static final String LABEL_GROUP = "Group";


	/** Constant value definging the value of an unknown/unselected group*/
	private static final long UNKNOWN_GROUP_ID = -1;


	/** The title of the dialog. */
	private static String TITLE = "Import Location - Select where to import your data.";

	/** Component indicating to add to the queue. */
	private JButton addButton;

	/** Component indicating to cancel the addition. */
	private JButton cancelButton;

	/** Action button command Id chosen by the user. */
	private int userSelectedActionCommandId;

	/** component used to select the import group. */
	private JComboBox groupsBox;

	/** Component used to select the default project. */
	private JComboBox projectsBox;

	/** Component used to select the default dataset. */
	private JComboBox datasetsBox;

	/** Component used to select the default screen. */
	private JComboBox screensBox;

	/** Button to create a new project. */
	private JButton addProjectButton;

	/** Button to create a new dataset. */
	private JButton addDatasetButton;

	/** Button to create a new screen. */
	private JButton addScreenButton;

	/** The listener linked to the parents box. */
	private ActionListener projectsBoxListener;

	/** The map holding the new nodes to create if in the P/D view. */
	private Map<DataNode, List<DataNode>> newNodesPD;

	/** The new nodes to create in the screen view. */
	private List<DataNode> newNodesS;

	/** Sorts the objects from the display. */
	private ViewerSorter sorter;

	/** A reference to the selected target for import data. */
	private TreeImageDisplay selectedContainer;

	/** The id of the import data type (Screen/Project) */
	private int importDataType;

	/** A reference to the parent object that created this dialog. */
	private JFrame owner;

	/** Internal list of available groups. */
	private Collection<GroupData> groups;

	/** The currently selected group in the groups combo box. */
	private GroupData currentGroup;

	private Collection<TreeImageDisplay> currentProjects;
	private Collection<TreeImageDisplay> currentScreens;

	private JToggleButton refreshButton;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            The parent of the dialog.
	 * @param selectedContainer
	 * 			  The container selected by the user Project/Dataset/Screen
	 * @param objects
	 *            The screens / projects to be shown
	 * @param groups
     *            The available groups to the user
	 * @param currentGroupId
	 *            The id of the current user group
	 */
	public LocationDialog(JFrame parent, TreeImageDisplay selectedContainer,
			int importDataType, Collection<TreeImageDisplay> objects,
			Collection<GroupData> groups, long currentGroupId) {
		super(parent);

		this.owner = parent;
		this.selectedContainer = selectedContainer;
		this.importDataType = importDataType;
		this.objects = objects;
		this.groups = groups;
		this.currentGroup = fingGroupWithId(groups, currentGroupId);
		
		switch(importDataType)
		{
			case Importer.PROJECT_TYPE:
				currentProjects = objects;
				break;
			case Importer.SCREEN_TYPE:
				currentScreens = objects;
		}
		
		setModal(true);
		setTitle(TITLE);
		
		initComponents();
		buildGUI();
	}

	/**
	 * @param The available groups.
	 * @param The if of the current group.
	 * @return Finds the group in the list with the id provided, 
	 * 		<null> if not found.
	 */
	private GroupData fingGroupWithId(Collection<GroupData> groups,
			long currentGroupId) {
		
		for (GroupData group : groups) {
			if(group.getId() == currentGroupId)
				return group;
		}
		
		return null;
	}

	/** Initialises the components. */
	private void initComponents() {
		sorter = new ViewerSorter();

		// main components
		groupsBox = new JComboBox();
		groupsBox.addActionListener(this);
		
		
		IconManager icons = IconManager.getInstance();
		refreshButton = new JToggleButton(icons.getIcon(IconManager.REFRESH));
		refreshButton.setBackground(UIUtilities.BACKGROUND);
		refreshButton.setToolTipText("Refresh the displays.");
		refreshButton.setActionCommand("" + CMD_REFRESH_DISPLAY);
		refreshButton.addActionListener(this);
		
		UIUtilities.unifiedButtonLookAndFeel(refreshButton);
		
		screensBox = new JComboBox();

		projectsBox = new JComboBox();
		projectsBoxListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				populateDatasetsBox();
			}
		};
		projectsBox.addActionListener(projectsBoxListener);

		datasetsBox = new JComboBox();

		populateGroupBox(groups, currentGroup);
		
		populateLocationComboBoxes();

		// main panel buttons
		
		addProjectButton = new JButton("New...");
		addProjectButton.setToolTipText("Create a new Project.");
		addProjectButton.setActionCommand("" + CMD_CREATE_PROJECT);
		addProjectButton.addActionListener(this);

		addDatasetButton = new JButton("New...");
		addDatasetButton.setToolTipText("Create a new Dataset.");
		addDatasetButton.setActionCommand("" + CMD_CREATE_DATASET);
		addDatasetButton.addActionListener(this);

		addScreenButton = new JButton("New...");
		addScreenButton.setToolTipText("Create a new Screen.");
		addScreenButton.setActionCommand("" + CMD_CREATE_SCREEN);
		addScreenButton.addActionListener(this);
		
		// main action buttons
		
		addButton = new JButton("Add to the Queue");
		addButton.setToolTipText("Add the files to the queue.");
		addButton.addActionListener(this);
		addButton.setActionCommand("" + CMD_ADD);
		
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close and do not add the files to the queue.");
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("" + CMD_CLOSE);


		getRootPane().setDefaultButton(addButton);
	}

	/**
	 * @return The JPanel holding the lower main action buttons.
	 */
	private JPanel buildButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(addButton, BorderLayout.EAST);
		buttonPanel.add(cancelButton, BorderLayout.WEST);
		return buttonPanel;
	}

	private JTabbedPane buildDataTypeTabbedPane() {
		JTabbedPane tabPane = new JTabbedPane();

		IconManager icons = IconManager.getInstance();

		Icon projectIcon = icons.getIcon(IconManager.PROJECT);
		JPanel projectPanel = buildProjectSelectionPanel();

		Icon screenIcon = icons.getIcon(IconManager.SCREEN);
		JPanel screenPanel = buildScreenSelectionPanel();

		tabPane.addTab("Projects", projectIcon, projectPanel,
				"Import settings for Projects");

		tabPane.addTab("Screens", screenIcon, screenPanel,
				"Import settings for Screens");
		
		tabPane.addChangeListener(this);
		return tabPane;
	}
	
	/**
	 * @return JPanel holding the group selection UI elements
	 */
	private JPanel buildGroupSelectionPanel() {
		double size[][] =
            {{TableLayout.PREFERRED, TableLayout.FILL,TableLayout.PREFERRED},
            {TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.PREFERRED}};

		TableLayout tableLayout = new TableLayout(size);
		tableLayout.setHGap(5);
		tableLayout.setVGap(5);
		
		JPanel groupPanel = new JPanel(tableLayout);
        
        groupPanel.add(UIUtilities.setTextFont(LABEL_GROUP), "0, 0, r, c");
        groupPanel.add(groupsBox,"1, 0");
        groupPanel.add(refreshButton, "2, 0, c, c");
       
		return groupPanel;
	}
	
	/**
	 * @return JPanel holding the project selection UI elements
	 */
	private JPanel buildProjectSelectionPanel() {
		double size[][] =
            {{TableLayout.PREFERRED, TableLayout.FILL,TableLayout.PREFERRED},
            {TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.PREFERRED}};

		TableLayout tableLayout = new TableLayout(size);
		tableLayout.setHGap(5);
		tableLayout.setVGap(5);
		
		JPanel projectPanel = new JPanel(tableLayout);
        
        projectPanel.add(UIUtilities.setTextFont(LABEL_PROJECT), "0, 0, r, c");
        projectPanel.add(projectsBox,"1, 0");
        projectPanel.add(addProjectButton, "2, 0, c, c");
        
        projectPanel.add(UIUtilities.setTextFont(LABEL_DATASET), "0, 1, r, c");
        projectPanel.add(datasetsBox,"1, 1");
        projectPanel.add(addDatasetButton, "2, 1, c, c");
       
		return projectPanel;
	}

	/**
	 * @return JPanel holding the screen selection UI elements
	 */
	private JPanel buildScreenSelectionPanel() {
		double size[][] =
            {{TableLayout.PREFERRED, TableLayout.FILL,TableLayout.PREFERRED},
            {TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.PREFERRED}};

		TableLayout tableLayout = new TableLayout(size);
		tableLayout.setHGap(5);
		tableLayout.setVGap(5);
		
		JPanel screenPanel = new JPanel(tableLayout);
        
        screenPanel.add(UIUtilities.setTextFont(LABEL_SCREEN), "0, 0, r, c");
        screenPanel.add(screensBox,"1, 0");
        screenPanel.add(addScreenButton, "2, 0, c, c");
       
		return screenPanel;
	}

	/**
	 * Builds the toolbar when the importer is the entry point.
	 * 
	 * @param availableGroups
	 * 
	 * @return See above.
	 */
	private void populateGroupBox(Collection<GroupData> availableGroups,
			GroupData selectedGroup) {
		groupsBox.removeActionListener(this);
		groupsBox.removeAllItems();
		JComboBoxImageObject selected = null;
		
		for (GroupData group : availableGroups) {
			JComboBoxImageObject comboBoxItem = new JComboBoxImageObject(group,
					getGroupIcon(group));
			groupsBox.addItem(comboBoxItem);		
			if (group.getId() == selectedGroup.getId())
			{
				selected = comboBoxItem;
			}
		}
		
		if (selected != null)
			groupsBox.setSelectedItem(selected);

		JComboBoxImageRenderer renderer = new JComboBoxImageRenderer();
		groupsBox.setRenderer(renderer);
		renderer.setPreferredSize(new Dimension(200, 130));
		
		groupsBox.addActionListener(this);
	}

	/**
	 * Returns the icon associated to the group.
	 * 
	 * @param group
	 *            The group to handle.
	 * @return See above.
	 */
	private Icon getGroupIcon(GroupData group) {
		switch (group.getPermissions().getPermissionsLevel()) {
		case GroupData.PERMISSIONS_PRIVATE:
			return GROUP_PRIVATE_ICON;
		case GroupData.PERMISSIONS_GROUP_READ:
			return GROUP_READ_ONLY_ICON;
		case GroupData.PERMISSIONS_GROUP_READ_LINK:
			return GROUP_READ_LINK_ICON;
		case GroupData.PERMISSIONS_GROUP_READ_WRITE:
			return GROUP_READ_WRITE_ICON;
		case GroupData.PERMISSIONS_PUBLIC_READ:
			return GROUP_PUBLIC_READ_ICON;
		case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
			return GROUP_PUBLIC_READ_WRITE_ICON;
		}
		return null;
	}

	/**
	 * Builds and lays out the UI.
	 * 
	 * @param location
	 *            The component displaying the option.
	 */
	private void buildGUI() {
		Container contentPane = getContentPane();
		contentPane.add(buildGroupSelectionPanel(), BorderLayout.NORTH);
		contentPane.add(buildDataTypeTabbedPane(), BorderLayout.CENTER);
		contentPane.add(buildButtonPanel(), BorderLayout.SOUTH);
		
		pack();
	}

	/** Closes the dialog. */
	private void close() {
		setVisible(false);
		dispose();
	}

	/**
	 * Shows the message box and returns the option selected by the user.
	 * 
	 * @return The option selected by the user.
	 */
	int centerLocation() {
		UIUtilities.centerAndShow(this);
		return userSelectedActionCommandId;
	}

	/**
	 * Shows the message box and returns the option selected by the user.
	 * 
	 * @param location
	 *            The location of the top-left corner of the dialog.
	 * @return The option selected by the user.
	 */
	int showLocation(Point location) {
		setLocation(location);
		setVisible(true);
		return userSelectedActionCommandId;
	}

	/**
	 * Sets the option.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		
		Object eventSource = ae.getSource();
		String actionCommand = ae.getActionCommand();
		
		if(	actionCommand.equals("comboBoxChanged") && eventSource == groupsBox)
		{
			switchToSelectedGroup();
		}
		else
		{
			int commandId = Integer.parseInt(actionCommand);
			
			DataObject newDataObject = null;
			
			switch (commandId) {
				case CMD_CREATE_PROJECT:
					newDataObject = new ProjectData();
					break;
				case CMD_CREATE_DATASET:
					newDataObject = new DatasetData();
					break;
				case CMD_CREATE_SCREEN:
					newDataObject = new ScreenData();
					break;
				case CMD_ADD:
				case CMD_CLOSE:
					userSelectedActionCommandId = commandId;
					close();
					break;
				case CMD_REFRESH_DISPLAY:
					objects = null;
					currentProjects = null;
					currentScreens = null;
					firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY, -1, importDataType);
			}

			if(newDataObject != null) {
				EditorDialog editor = new EditorDialog(owner, newDataObject, false);
				editor.addPropertyChangeListener(this);
				editor.setModal(true);
				UIUtilities.centerAndShow(editor);
			}
		}
	}

	/**
	 * Switch to display the projects/datasets/screens from the selected group
	 */
	private void switchToSelectedGroup() {
		JComboBoxImageObject comboBoxItem = (JComboBoxImageObject) groupsBox.getSelectedItem();
		GroupData selectedNewGroup = (GroupData) comboBoxItem.getData();
		
		objects = null;
		currentProjects = null;
		currentScreens = null;
		
		if(selectedNewGroup.getId() != currentGroup.getId())
			firePropertyChange(GROUP_CHANGED_PROPERTY, currentGroup, selectedNewGroup);
	}

	/**
	 * @return The import settings selected by the user.
	 */
	public ImportLocationSettings getImportSettings() {
		
		ImportLocationSettings importSettings = new NullImportSettings(currentGroup);
		
		switch(importDataType)
		{
			case Importer.PROJECT_TYPE:
				DataNode selectedProject = (DataNode) projectsBox.getSelectedItem();
				DataNode selectedDataset = (DataNode) datasetsBox.getSelectedItem();
				importSettings = new ProjectImportLocationSettings(currentGroup, 
						selectedProject, selectedDataset);
				break;
			case Importer.SCREEN_TYPE:
				DataNode selectedScreen = (DataNode) screensBox.getSelectedItem();
				importSettings = new ScreenImportLocationSettings(currentGroup,
						selectedScreen);
				break;
		}
		
		return importSettings;
	}

	/**
	 * Takes the dataNdoes and populates the combo box with the values as well
	 * as adding a tooltip for each item
	 * 
	 * @param dataNodes
	 *            the nodes used to be displayed in the combo box
	 * @param comboBox
	 *            the JComboBox that hosts the options
	 */
	private void populateAndAddTooltipsToComboBox(List<DataNode> dataNodes,
			JComboBox comboBox) {
		List<String> tooltips = new ArrayList<String>(dataNodes.size());

		ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();

		comboBox.setRenderer(renderer);

		for (DataNode projectNode : dataNodes) {
			comboBox.addItem(projectNode);

			String projectName = projectNode.getFullName();
			List<String> wrapped = UIUtilities.wrapStyleWord(projectName, 50);

			tooltips.add(UIUtilities.formatToolTipText(wrapped));
		}

		renderer.setTooltips(tooltips);
	}

	/**
	 * Creates a project.
	 * 
	 * @param data
	 *            The project to create.
	 */
	public void createProject(DataObject data) {
		if (data == null)
			return;

		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode node;
		DataNode defaultNode = null;

		for (int i = 0; i < projectsBox.getItemCount(); i++) {
			node = (DataNode) projectsBox.getItemAt(i);
			if (!node.isDefaultProject())
				nodes.add(node);
			else
				defaultNode = node;
		}

		DataNode newNode = new DataNode(data);
		newNode.addNode(new DataNode(DataNode.createDefaultDataset(), newNode));
		nodes.add(newNode);

		List<DataNode> sortedList = sorter.sort(nodes);
		if (defaultNode != null)
			sortedList.add(defaultNode);

		projectsBox.removeActionListener(projectsBoxListener);
		projectsBox.removeAllItems();

		for (DataNode dataNode : sortedList) {
			projectsBox.addItem(dataNode);
		}

		projectsBox.addActionListener(projectsBoxListener);
		projectsBox.setSelectedItem(newNode);

		repaint();
	}

	/**
	 * Creates the dataset.
	 * 
	 * @param dataset
	 *            The dataset to create.
	 */
	public void createDataset(DatasetData dataset) {
		if (dataset == null)
			return;
		DataNode node = (DataNode) projectsBox.getSelectedItem();
		DataNode nn = new DataNode(dataset, node);
		List<DataNode> nodes = new ArrayList<DataNode>();
		nodes.add(nn);
		DataNode n, dn = null;
		for (int i = 0; i < datasetsBox.getItemCount(); i++) {
			n = (DataNode) datasetsBox.getItemAt(i);
			if (!n.isDefaultNode())
				nodes.add(n);
			else
				dn = n;
		}
		List<DataNode> l = sorter.sort(nodes);
		if (dn != null)
			l.add(dn);
		datasetsBox.removeAllItems();

		for (DataNode dataNode : l) {
			datasetsBox.addItem(dataNode);
		}

		datasetsBox.setSelectedItem(nn);
	}

	/**
	 * Creates a screen.
	 * 
	 * @param data
	 *            The screen to create.
	 */
	public void createScreen(DataObject data) {
		if (data == null)
			return;

		List<DataNode> nodes = new ArrayList<DataNode>();
		DataNode n;
		DataNode dn = null;
		
		for (int i = 0; i < screensBox.getItemCount(); i++) {
			n = (DataNode) screensBox.getItemAt(i);
			if (!n.isDefaultScreen())
				nodes.add(n);
			else
				dn = n;
		}

		DataNode nn = new DataNode(data);
		nodes.add(nn);

		List<DataNode> l = sorter.sort(nodes);
		if (dn != null)
			l.add(dn);

		screensBox.removeAllItems();

		for (DataNode dataNode : l) {
			screensBox.addItem(dataNode);
		}

		screensBox.setSelectedItem(nn);

		repaint();
	}

	/** Populates the datasets box depending on the selected project. */
	private void populateDatasetsBox() {
		DataNode n = (DataNode) projectsBox.getSelectedItem();
		List<DataNode> list = n.getDatasetNodes();
		List<DataNode> nl = n.getNewNodes();
		if (nl != null)
			list.addAll(nl);
		List<DataNode> sortedDatasets = sorter.sort(list);
		datasetsBox.removeAllItems();

		populateAndAddTooltipsToComboBox(sortedDatasets, datasetsBox);

		if (selectedContainer != null) {
			Object o = selectedContainer.getUserObject();
			if (o instanceof DatasetData) {
				DatasetData d = (DatasetData) o;
				Iterator<DataNode> i = sortedDatasets.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (n.getDataObject().getId() == d.getId()) {
						datasetsBox.setSelectedItem(n);
						break;
					}
				}
			}
		} else { // no node selected
			if (sortedDatasets.size() > 1) {
				Iterator<DataNode> i = sortedDatasets.iterator();
				while (i.hasNext()) {
					n = i.next();
					if (n.isDefaultDataset()) {
						datasetsBox.setSelectedItem(n);
						break;
					}
				}
			}
		}
	}

	/**
	 * Reacts to property fired by the table.
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();

		if (EditorDialog.CREATE_NO_PARENT_PROPERTY.equals(name)) {
			Object ho = evt.getNewValue();
			DataObject child = null, parent = null;
			if (ho instanceof ProjectData || ho instanceof ScreenData) {
				child = (DataObject) ho;
			} else if (ho instanceof DatasetData) {
				child = (DataObject) ho;
				DataNode n = (DataNode) projectsBox.getSelectedItem();
				if (!n.isDefaultNode()) {
					parent = n.getDataObject();
				}
			}
			JComboBoxImageObject selectedGroup = (JComboBoxImageObject) groupsBox
					.getSelectedItem();
			GroupData g = (GroupData) selectedGroup.getData();

			if (child != null) {
				firePropertyChange(ImportDialog.CREATE_OBJECT_PROPERTY, null,
						new ObjectToCreate(g, child, parent));
			}
		}
	}

	/** Populates the selection boxes with the currently selected data. */
	private void populateLocationComboBoxes() {
		List<DataNode> topList = new ArrayList<DataNode>();
		List<DataNode> datasetsList = new ArrayList<DataNode>();
		DataNode n;
		Object hostObject = null;
		TreeImageDisplay node;
		
		if (objects != null && objects.size() > 0) {
			Iterator<TreeImageDisplay> i = objects.iterator();
			while (i.hasNext()) {
				node = i.next();
				hostObject = node.getUserObject();
				if (hostObject instanceof ProjectData
						|| hostObject instanceof ScreenData) {
					n = new DataNode((DataObject) hostObject);
					getNewDataset((DataObject) hostObject, n);
					n.setRefNode(node);
					topList.add(n);
				} else if (hostObject instanceof DatasetData) {
					n = new DataNode((DataObject) hostObject);
					n.setRefNode(node);
					datasetsList.add(n);
				}
			}
		}
		// check if new top nodes
		DataObject data;
		Iterator<DataNode> j;

		switch (importDataType) {
			case Importer.PROJECT_TYPE:
				projectsBox.removeActionListener(projectsBoxListener);
				
				projectsBox.removeAllItems();
				datasetsBox.removeAllItems();
				
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
				
				loadProjects(datasetsList, sorter.sort(topList));
				
				projectsBox.addActionListener(projectsBoxListener);
				break;
			case Importer.SCREEN_TYPE:
				screensBox.removeAllItems();
				
				if (newNodesS != null) {
					j = newNodesS.iterator();
					while (j.hasNext()) {
						n = j.next();
						data = n.getDataObject();
						if (data.getId() <= 0)
							topList.add(n);
					}
				}
				
				loadScreens(sorter.sort(topList));
		}
	}

	/**
	 * Populates the screens box with the screen selection options
	 * @param hostObject
	 * @param sortedList
	 */
	private void loadScreens(List<DataNode> sortedList) {
		List<DataNode> finalList = new ArrayList<DataNode>();
		finalList.add(new DataNode(DataNode.createDefaultScreen()));
		finalList.addAll(sortedList);

		populateAndAddTooltipsToComboBox(finalList, screensBox);

		DataNode selectedNode = null;

		if (selectedContainer != null) {
			Object hostObject = selectedContainer.getUserObject();
			if(hostObject instanceof ScreenData)
			{
				ScreenData screenData = (ScreenData) hostObject;
				for (DataNode dataNode : finalList) {
					if(dataNode.getDataObject().getId() == screenData.getId())
						selectedNode = dataNode;
				}
			}
		}
		
		if(selectedNode != null)
			screensBox.setSelectedItem(selectedNode);
	}

	/**
	 * Returns the collection of new datasets.
	 * 
	 * @return See above.
	 */
	private List<DataNode> getOrphanedNewDatasetNode() {
		if (newNodesPD == null)
			return null;
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
	 * Populates the projects & datasets boxes with the current options
	 * @param hostObject
	 * @param datasetsList
	 * @param sortedList
	 */
	private void loadProjects(List<DataNode> datasetsList, List<DataNode> sortedList) {
		List<DataNode> finalList = new ArrayList<DataNode>();
		DataNode n;
		List<DataNode> l = getOrphanedNewDatasetNode();

		if (datasetsList.size() > 0) { // orphaned datasets.
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
		finalList.add(n);
		finalList.addAll(sortedList);

		populateAndAddTooltipsToComboBox(finalList, projectsBox);

		int index = 0;
		TreeImageDisplay node;

		// Determine the node to select.
		if (selectedContainer != null) {
			Object hostObject = selectedContainer.getUserObject();
			ProjectData p = null;
			if (hostObject instanceof ProjectData) {
				p = (ProjectData) hostObject;
			} else if (hostObject instanceof DatasetData) {
				node = selectedContainer.getParentDisplay();
				if (node != null && node.getUserObject() instanceof ProjectData) {
					p = (ProjectData) node.getUserObject();
				}
			}
			if (p != null) {
				long id = p.getId();
				for (int i = 0; i < projectsBox.getItemCount(); i++) {
					n = (DataNode) projectsBox.getItemAt(i);
					if (n.getDataObject().getId() == id) {
						index = i;
						break;
					}
				}
			}
		}
		projectsBox.setSelectedIndex(index);

		populateDatasetsBox();
	}

	/**
	 * Retrieves the new nodes to add the project.
	 * 
	 * @param data
	 *            The data object to handle.
	 * @param node
	 *            The node hosting the data object.
	 */
	private void getNewDataset(DataObject data, DataNode node) {
		if (newNodesPD == null || data instanceof ScreenData)
			return;
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

	/**
	 * Resets the display to the selection and group specified
	 * @param container
	 * @param type
	 * @param objects
	 * @param currentGroupId
	 */
	public void reset(TreeImageDisplay container, int type,
			Collection<TreeImageDisplay> objects , long currentGroupId) {

		this.selectedContainer = container;
		this.importDataType = type;
		this.objects = objects;
		
		switch(importDataType)
		{
			case Importer.PROJECT_TYPE:
				currentProjects = objects;
				break;
			case Importer.SCREEN_TYPE:
				currentScreens = objects;
				break;
		}
		
		onReconnected(groups, currentGroupId);
	}

	/**
	 * Re-populates and resets the groups, screens, projects & dataset selection options.
	 * @param availableGroups
	 * @param currentGroupId
	 */
	public void onReconnected(Collection<GroupData> availableGroups,
			long currentGroupId) {
		
		// TODO: Work around for currentGroup being passed as -1
		if(currentGroupId == UNKNOWN_GROUP_ID) {
			GroupData defaultUserGroup = ImporterAgent.getUserDetails().getDefaultGroup();
			firePropertyChange(GROUP_CHANGED_PROPERTY, null, defaultUserGroup);
			return;
		}
		
		this.groups = availableGroups;
		this.currentGroup = fingGroupWithId(availableGroups, currentGroupId);
		
		populateGroupBox(groups, currentGroup);
		populateLocationComboBoxes();
	}
	
	public void stateChanged(ChangeEvent evt) {
		JTabbedPane tabbedPane = (JTabbedPane) evt.getSource();
		int newDataType = tabbedPane.getSelectedIndex();
		
		switch(newDataType)
		{
			case Importer.PROJECT_TYPE:
				objects = currentProjects;
				break;
			case Importer.SCREEN_TYPE:
				objects = currentScreens;
				break;
		}
		
		if (objects == null) {
			firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY, importDataType, newDataType);
		} else {
			reset(this.selectedContainer, newDataType, objects, this.currentGroup.getId());
		}
	}
}
