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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.fsimporter.IconManager;
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
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class LocationDialog extends JDialog implements ActionListener,
		PropertyChangeListener {

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

	/** The message to display in the header. */
	private static final String MESSAGE_LOCATION = "Select where to import the data";

	/** Action id indicating to create a new project. */
	private static final int CMD_CREATE_PROJECT = 1;

	/** Action id indicating to create a new dataset. */
	private static final int CMD_CREATE_DATASET = 2;

	/** Action id indicating to create a new screen. */
	private static final int CMD_CREATE_SCREEN = 3;

	/** The default text for a project. */
	private static final String LABEL_PROJECT = "Project";

	/** The default text for a dataset. */
	private static final String LABEL_DATASET = "Dataset";

	/** The default text for a screen. */
	private static final String LABEL_SCREEN = "Screen";

	/** The message to display in the header. */
	private static final String LABEL_GROUP = "Group";

	/** User has selected to add the files. */
	public final static int CMD_ADD = 1;

	/** User has selected to cancel. */
	public final static int CMD_CLOSE = 0;

	/** The title of the dialog. */
	private static String TITLE = "Location selection";

	/** Component indicating to add to the queue. */
	private JButton addButton;

	/** Component indicating to cancel the addition. */
	private JButton cancelButton;

	/** Option chosen by the user. */
	private int option;

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

	/** The map holding the new nodes to create if in th P/D view. */
	private Map<DataNode, List<DataNode>> newNodesPD;

	/** The new nodes to create in the screen view. */
	private List<DataNode> newNodesS;

	/** Sorts the objects from the display. */
	private ViewerSorter sorter;

	private TreeImageDisplay selectedContainer;

	private ImportLocationSettings importSettings;

	private int importDataType;

	private JFrame owner;

	private Collection<GroupData> groups;

	private long currentGroupId;

	private GroupData currentGroup;

	/**
	 * Creates a new instance.
	 * 
	 * @param parent
	 *            The parent of the dialog.
	 * @param objects
	 * @param groupsBox
	 * @param location
	 *            The component displaying the option.
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
		this.currentGroupId = currentGroupId;

		setModal(true);
		setTitle(TITLE);
		initComponents();
		buildGUI();
		pack();

		int minHeight = this.getHeight();
		int minWidth = this.getWidth();
		Dimension minimumSize = new Dimension(minWidth, minHeight);

		setMinimumSize(minimumSize);
	}

	/** Initialises the components. */
	private void initComponents() {
		sorter = new ViewerSorter();

		// main components
		groupsBox = new JComboBox();

		screensBox = new JComboBox();

		projectsBox = new JComboBox();
		projectsBoxListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				populateDatasetsBox();
			}
		};
		projectsBox.addActionListener(projectsBoxListener);

		datasetsBox = new JComboBox();

		populateGroupBox(groups, currentGroupId);
		
		initializeLocationBoxes();

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

		// lower buttons
		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Close and do not add the files to the "
				+ "queue.");

		ActionListener buttonListener = new ActionListener() {

			public void actionPerformed(ActionEvent ae) {
				// TODO Auto-generated method stub 21 Nov 2012 15:05:13 scott
				int commandId = Integer.parseInt(ae.getActionCommand());

				switch (commandId) {
				case CMD_ADD:
					GroupData selectedGroup = (GroupData) ((JComboBoxImageObject) groupsBox
							.getSelectedItem()).getData();
					importSettings = new FakeImportSettings(importDataType,
							selectedGroup);
					close();
					break;
				case CMD_CLOSE:
					close();
				}

			}

		};

		cancelButton.addActionListener(buttonListener);
		cancelButton.setActionCommand("" + CMD_CLOSE);

		addButton = new JButton("Add to the Queue");
		addButton.setToolTipText("Add the files to the queue.");
		addButton.addActionListener(buttonListener);
		addButton.setActionCommand("" + CMD_ADD);

		getRootPane().setDefaultButton(addButton);
	}

	/**
	 * Builds and lays out the UI.
	 * 
	 * @return See above.
	 */
	private JPanel buildToolbar() {
		JPanel bar = new JPanel();
		bar.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		bar.add(addButton);
		bar.add(Box.createHorizontalStrut(5));
		bar.add(cancelButton);
		return bar;
	}

	private JPanel layoutMainPanel() {
		JPanel locationPane = new JPanel();
		locationPane.setLayout(new BorderLayout());

		JTabbedPane tabPane = new JTabbedPane();

		IconManager icons = IconManager.getInstance();

		Icon projectIcon = icons.getIcon(IconManager.PROJECT);
		JPanel projectPanel = createProjectPanel();

		Icon screenIcon = icons.getIcon(IconManager.SCREEN);
		JPanel screenPanel = createScreenPanel();

		tabPane.addTab("Projects", projectIcon, projectPanel,
				"Import settings for Projects");

		tabPane.addTab("Screens", screenIcon, screenPanel,
				"Import settings for Screens");

		ChangeListener changeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent evt) {
				JTabbedPane tabbedPane = (JTabbedPane) evt.getSource();
				int selectedTabIndex = tabbedPane.getSelectedIndex();
				reset(selectedContainer, selectedTabIndex, objects);
			}
		};
		
		tabPane.addChangeListener(changeListener);
		
		JPanel groupPane = new JPanel();
		groupPane.add(UIUtilities.setTextFont(LABEL_GROUP), BorderLayout.WEST);
		groupPane.add(groupsBox, BorderLayout.CENTER);

		locationPane.add(groupPane, BorderLayout.NORTH);
		locationPane.add(tabPane, BorderLayout.CENTER);

		return locationPane;
	}

	private JPanel createProjectPanel() {
		JPanel projectPanel = new JPanel();
		projectPanel.setLayout(new BoxLayout(projectPanel, BoxLayout.Y_AXIS));

		JPanel projectRow = new JPanel();
		projectRow.add(UIUtilities.setTextFont(LABEL_PROJECT),
				BorderLayout.WEST);
		projectRow.add(projectsBox, BorderLayout.CENTER);
		projectRow.add(addProjectButton, BorderLayout.EAST);

		JPanel datasetRow = new JPanel();
		datasetRow.add(UIUtilities.setTextFont(LABEL_DATASET),
				BorderLayout.WEST);
		datasetRow.add(datasetsBox, BorderLayout.CENTER);
		datasetRow.add(addDatasetButton, BorderLayout.EAST);

		projectPanel.add(projectRow);
		projectPanel.add(Box.createVerticalStrut(8));
		projectPanel.add(datasetRow);

		projectPanel.add(new JSeparator());

		return projectPanel;
	}

	private JPanel createScreenPanel() {
		JPanel screenPanel = new JPanel();
		screenPanel.setLayout(new BoxLayout(screenPanel, BoxLayout.Y_AXIS));

		JPanel screenRow = new JPanel();
		screenRow.add(UIUtilities.setTextFont(LABEL_SCREEN), BorderLayout.WEST);
		screenRow.add(screensBox, BorderLayout.CENTER);
		screenRow.add(addScreenButton, BorderLayout.EAST);

		screenPanel.add(screenRow);
		screenPanel.add(new JSeparator());

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
			long selectedGroupId) {
		JComboBoxImageObject[] comboBoxObjects = new JComboBoxImageObject[availableGroups
				.size()];

		int selectedIndex = 0;
		int index = 0;

		for (GroupData group : availableGroups) {
			
			if (group.getId() == selectedGroupId)
			{
				currentGroup = group;
				selectedIndex = index;
			}
			
			comboBoxObjects[index] = new JComboBoxImageObject(group,
					getGroupIcon(group));
			
			groupsBox.addItem(comboBoxObjects[index]);
			
			index++;
		}

		groupsBox.setSelectedIndex(selectedIndex);
		JComboBoxImageRenderer rnd = new JComboBoxImageRenderer();
		groupsBox.setRenderer(rnd);
		rnd.setPreferredSize(new Dimension(200, 130));
		
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
		Container c = getContentPane();
		c.add(layoutMainPanel(), BorderLayout.CENTER);
		c.add(buildToolbar(), BorderLayout.SOUTH);
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
		return option;
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
		return option;
	}

	/**
	 * Sets the option.
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		
		
		Object sourceObject = ae.getSource();
		
		if(sourceObject == groupsBox)
		{
			GroupData selectedGroup = (GroupData) ((JComboBoxImageObject) groupsBox
					.getSelectedItem()).getData();
			
			firePropertyChange(GROUP_CHANGED_PROPERTY, currentGroup, selectedGroup );
		}
		else
		{
			int commandId = Integer.parseInt(ae.getActionCommand());
			
			DataObject emptyObject = null;
			switch (commandId) {
			case CMD_CREATE_PROJECT:
				emptyObject = new ProjectData();
				break;
			case CMD_CREATE_DATASET:
				emptyObject = new DatasetData();
				break;
			case CMD_CREATE_SCREEN:
				emptyObject = new ScreenData();
				break;
			}

			EditorDialog d = new EditorDialog(owner, emptyObject, false);
			d.addPropertyChangeListener(this);
			d.setModal(true);
			UIUtilities.centerAndShow(d);
		}
	}

	public ImportLocationSettings getImportSettings() {
		// TODO Auto-generated method stub 21 Nov 2012 12:43:08 scott
		return null;
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

			List<String> tooltipLines = UIUtilities.wrapStyleWord(projectName,
					50);

			tooltips.add(UIUtilities.formatToolTipText(tooltipLines));
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
		DataNode n;
		DataNode dn = null;

		for (int i = 0; i < projectsBox.getItemCount(); i++) {
			n = (DataNode) projectsBox.getItemAt(i);
			if (!n.isDefaultProject())
				nodes.add(n);
			else
				dn = n;
		}

		DataNode nn = new DataNode(data);
		nn.addNode(new DataNode(DataNode.createDefaultDataset(), nn));
		nodes.add(nn);

		List<DataNode> l = sorter.sort(nodes);
		if (dn != null)
			l.add(dn);

		projectsBox.removeActionListener(projectsBoxListener);
		projectsBox.removeAllItems();

		for (DataNode dataNode : l) {
			projectsBox.addItem(dataNode);
		}

		projectsBox.addActionListener(projectsBoxListener);
		projectsBox.setSelectedItem(nn);

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

	/** Initialises the selection boxes. */
	private void initializeLocationBoxes() {
		projectsBox.removeActionListener(projectsBoxListener);
		projectsBox.removeAllItems();
		datasetsBox.removeAllItems();
		screensBox.removeAllItems();

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
		if (importDataType == Importer.PROJECT_TYPE) {
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
		} else if (importDataType == Importer.SCREEN_TYPE) {
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
		List<DataNode> sortedList = new ArrayList<DataNode>();
		if (topList.size() > 0) {
			sortedList = sorter.sort(topList);
		}

		if(importDataType == Importer.PROJECT_TYPE)
			loadProjects(hostObject, datasetsList, sortedList);
		
		if(importDataType == Importer.SCREEN_TYPE)
			loadScreens(hostObject, sortedList);
		
		projectsBox.addActionListener(projectsBoxListener);
	}

	private void loadScreens(Object hostObject, List<DataNode> sortedList) {
		List<DataNode> finalList = new ArrayList<DataNode>();
		finalList.add(new DataNode(DataNode.createDefaultScreen()));
		finalList.addAll(sortedList);
		
		if (hostObject instanceof ScreenData) {
			populateAndAddTooltipsToComboBox(finalList, screensBox);
	
			int size = screensBox.getItemCount();
			int index = 0;
			if (selectedContainer != null) {
			
				long id = ((ScreenData) hostObject).getId();
				for (int i = 0; i < size; i++) {
					DataNode n = (DataNode) screensBox.getItemAt(i);
					if (n.getDataObject().getId() == id) {
						index = i;
						break;
					}
				}

			}
			screensBox.setSelectedIndex(index);
		}
		
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

	private void loadProjects(Object hostObject, List<DataNode> datasetsList,
			List<DataNode> sortedList) {
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
		int size = projectsBox.getItemCount();
		if (selectedContainer != null) {
			hostObject = selectedContainer.getUserObject();
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
				for (int i = 0; i < size; i++) {
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

	public void reset(TreeImageDisplay container, int type,
			Collection<TreeImageDisplay> objects) {

		this.selectedContainer = container;
		this.importDataType = type;
		this.objects = objects;
		
		initializeLocationBoxes();
	}

	public void onReconnected(Collection<GroupData> availableGroups,
			long currentGroupId) {

	}
}
