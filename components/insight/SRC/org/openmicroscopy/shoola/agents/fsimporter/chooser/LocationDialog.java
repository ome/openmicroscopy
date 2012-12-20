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

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
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

/**
 * Provides the user with the options to select the location to import data.
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
class LocationDialog extends JDialog implements ActionListener,
		PropertyChangeListener, ChangeListener, ItemListener {
	
	// table design presets

	/** Default GAP value for UI components */
	private static final int UI_GAP = 5;
	
	/** Template for a table layout with a GAP on all 4 sides */
	private static final double[][] TABLE_GAP = new double[][]
			{
				{UI_GAP, TableLayout.FILL, UI_GAP},
				{UI_GAP, TableLayout.FILL, UI_GAP}
			};
	
	/** Helper setting for TableLayout.PREFERRED*/
	private static final double[] TABLE_PREF = {TableLayout.PREFERRED};
	
	/** Table template for 2 settings of Preferred width / height */
	private static final double[] TABLE_PREF_PREF = 
		{TableLayout.PREFERRED, TableLayout.PREFERRED};

	/** Table template for 3 settings of Preferred width / height */
	private static final double[] TABLE_PREF_PREF_PREF = 
		{TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED};
	

	/** Table template for expandable middle column / row */
	private static final double[] TABLE_PREF_FILL_PREF = 
		{TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED};

	// other constants	
	/** Minimum width of the dialog in pixels */
	private static final int MIN_WIDTH = 640;

	// String constants
	
	/** The title of the dialog. */
	private static String TEXT_TITLE =
			"Import Location - Select where to import your data.";
	
	/** Text for projects */
	private static final String TEXT_PROJECTS = "Projects";

	/** Text for screens */
	private static final String TEXT_SCREENS = "Screens";

	/** Text for a project. */
	private static final String TEXT_PROJECT = "Project";

	/** Text for a dataset. */
	private static final String TEXT_DATASET = "Dataset";

	/** Text for a screen. */
	private static final String TEXT_SCREEN = "Screen";

	/** Text for a group. */
	private static final String TEXT_GROUP = "Group";

	/** Tooltip text for New Screen button */
	private static final String TOOLTIP_NEW_SCREEN = "Create a new Screen.";
	
	/** Tooltip text for New Dataset button */
	private static final String TOOLTIP_NEW_DATASET = "Create a new Dataset.";
	
	/** Tooltip text for New Project button */
	private static final String TOOLTIP_NEW_PROJECT = "Create a new Project.";

	/** Tooltip for Reload button */
	private static final String TEXT_REFRESH = "Refresh";

	/** Tooltip for Reload button */
	private static final String TOOLTIP_REFRESH =
			"Reload the Groups, Projects, Datasets and/or Screens.";
	
	/** Text for New buttons */
	private static final String TEXT_NEW = "New...";

	/** Text for Close button */
	private static final String TEXT_CLOSE = "Close";
	
	/** Tooltip text for Close button */
	private static final String TOOLTIP_CLOSE_DIALOGUE = 
			"Close the dialogue and do not add the files to the queue.";

	/** Text for Add button */
	private static final String TEXT_QUEUE_ITEMS = "Add to the Queue";
	
	/** Tooltip text for Add button */
	private static final String TOOLTIP_QUEUE_ITEMS = 
			"Add the files to the queue.";
	
	// Icon constants
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

	// Command constants
	/** Action id indicating to create a new project. */
	private static final int CMD_CREATE_PROJECT = 1;

	/** Action id indicating to create a new dataset. */
	private static final int CMD_CREATE_DATASET = 2;

	/** Action id indicating to create a new screen. */
	private static final int CMD_CREATE_SCREEN = 3;

	/** User has chosen to force a refresh of the containers */
	private static final int CMD_REFRESH_DISPLAY = 4;

	/** User has selected to add the files. */
	final static int CMD_ADD = 5;

	/** User has selected to cancel. */
	final static int CMD_CLOSE = 6;

	/** Action button command Id chosen by the user. */
	private int userSelectedActionCommandId;

	// UI Components
	/** Component indicating to add to the queue. */
	private JButton addButton;

	/** Component indicating to close the dialog. */
	private JButton closeButton;

	/** component used to select the import group. */
	private JComboBox groupsBox;

	/** Component used to select the default project. */
	private JComboBox projectsBox;

	/** Component used to select the default dataset. */
	private JComboBox datasetsBox;

	/** Component used to select the default screen. */
	private JComboBox screensBox;

	/** Button to create a new project. */
	private JButton newProjectButton;

	/** Button to create a new dataset. */
	private JButton newDatasetButton;

	/** Button to create a new screen. */
	private JButton newScreenButton;

	/** Button used to refresh the groups/projects/datasets & screens */
	private JButton refreshButton;

	/** Tab pane used to hose the Project/Screen selection UI component. */
	private JTabbedPane tabbedPane;

	/** A reference to the parent object that created this dialog. */
	private JFrame owner;
	
	// Operational variables & constants
	/** Constant value for no data type */
	private static final int NO_DATA_TYPE = -1;
	
	/** The map holding the new nodes to create if in the P/D view. */
	private Map<DataNode, List<DataNode>> newNodesPD;

	/** The new nodes to create in the screen view. */
	private List<DataNode> newNodesS;

	/** Sorts the objects from the display. */
	private ViewerSorter sorter;

	/** A reference to the selected target for import data. */
	private TreeImageDisplay selectedContainer;

	/** The id of the import data type (Screen/Project) */
	private int importDataType = NO_DATA_TYPE;

	/** Internal list of available groups. */
	private Collection<GroupData> groups;

	/** The currently selected group in the groups combo box. */
	private GroupData currentGroup;

	/** The current possible import location nodes. */
	private Collection<TreeImageDisplay> objects;
	
	/** List of the available projects in the current group */
	private Collection<TreeImageDisplay> currentProjects;
	
	/** List of the available screens in the current group */
	private Collection<TreeImageDisplay> currentScreens;

	/** The Project panel */
	private JPanel projectPanel;

	/** The Screen panel */
	private JPanel screenPanel;
	
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
    LocationDialog(JFrame parent, TreeImageDisplay selectedContainer,
			int importDataType, Collection<TreeImageDisplay> objects,
			Collection<GroupData> groups, long currentGroupId) {
		super(parent);

		this.owner = parent;
		this.selectedContainer = selectedContainer;
		this.importDataType = importDataType;
		this.objects = objects;
		this.groups = groups;
		this.currentGroup = findGroupWithId(groups, currentGroupId);
		
		setModal(true);
		setTitle(TEXT_TITLE);
		
		initComponents();
		buildGUI();
		
		switchToDataType(importDataType);
	}

	/**
	 * Swaps the data that is currently active.
	 * @param importDataType The id of the data type (Screen/Project)
	 */
	private void switchToDataType(int importDataType) {
		switch(importDataType)
		{
			case Importer.PROJECT_TYPE:
				currentProjects = objects;
				this.tabbedPane.setSelectedComponent(projectPanel);
				break;
			case Importer.SCREEN_TYPE:
				currentScreens = objects;
				this.tabbedPane.setSelectedComponent(screenPanel);
		}
	}

	/**
	 * Scans the groups provided and returns the Group with matching id.
	 * <null> if not found
	 * @param groups The available groups.
	 * @param currentGroupIdThe if of the current group.
	 * @return Finds the group in the list with the id provided, 
	 * 		<null> if not found.
	 */
	private GroupData findGroupWithId(Collection<GroupData> groups,
			long currentGroupId) {
		
		for (GroupData group : groups) {
			if(group.getId() == currentGroupId)
				return group;
		}
		
		return null;
	}

	/**
	 * Initialises the UI components of the dialog.
	 */
	private void initComponents() {
		sorter = new ViewerSorter();

		// main components
		groupsBox = new JComboBox();
		groupsBox.addItemListener(this);
		
		refreshButton = new JButton(TEXT_REFRESH);
		refreshButton.setBackground(UIUtilities.BACKGROUND);
		refreshButton.setToolTipText(TOOLTIP_REFRESH);
		refreshButton.setActionCommand("" + CMD_REFRESH_DISPLAY);
		refreshButton.addActionListener(this);
		
		screensBox = new JComboBox();

		projectsBox = new JComboBox();
		projectsBox.addItemListener(this);

		datasetsBox = new JComboBox();

		populateGroupBox(groups, currentGroup);
		
		populateLocationComboBoxes();

		// main location panel buttons
		newProjectButton = new JButton(TEXT_NEW);
		newProjectButton.setToolTipText(TOOLTIP_NEW_PROJECT);
		newProjectButton.setActionCommand("" + CMD_CREATE_PROJECT);
		newProjectButton.addActionListener(this);

		newDatasetButton = new JButton(TEXT_NEW);
		newDatasetButton.setToolTipText(TOOLTIP_NEW_DATASET);
		newDatasetButton.setActionCommand("" + CMD_CREATE_DATASET);
		newDatasetButton.addActionListener(this);

		newScreenButton = new JButton(TEXT_NEW);
		newScreenButton.setToolTipText(TOOLTIP_NEW_SCREEN);
		newScreenButton.setActionCommand("" + CMD_CREATE_SCREEN);
		newScreenButton.addActionListener(this);
		
		// main action buttons
		addButton = new JButton(TEXT_QUEUE_ITEMS);
		addButton.setToolTipText(TOOLTIP_QUEUE_ITEMS);
		addButton.addActionListener(this);
		addButton.setActionCommand("" + CMD_ADD);
		
		closeButton = new JButton(TEXT_CLOSE);
		closeButton.setToolTipText(TOOLTIP_CLOSE_DIALOGUE);
		closeButton.addActionListener(this);
		closeButton.setActionCommand("" + CMD_CLOSE);
		
		getRootPane().setDefaultButton(addButton);
	}

	/**
	 * Builds a JPanel holding the main action buttons
	 * @return The JPanel holding the lower main action buttons.
	 */
	private JPanel buildLowerButtonPanel() {
		TableLayout buttonLayout = 
				createTableLayout(TABLE_PREF_FILL_PREF, TABLE_PREF);
		JPanel buttonPanel = new JPanel(buttonLayout);
		buttonPanel.add(closeButton, "0, 0, l, c");
		buttonPanel.add(refreshButton, "1, 0, l, c");
		buttonPanel.add(addButton, "2, 0, r, c");
		JPanel buttonWrapper = wrapInPaddedPanel(buttonPanel, UI_GAP, 0, 0, 0);
		Border border = 
				BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK); 
		buttonWrapper.setBorder(border);
		return buttonWrapper;
	}

	/**
	 * Builds a TabbedPane holding the Screen / Project import sections
	 * @return The tab panel for Screen / Project selection
	 */
	private JPanel buildDataTypeTabbedPane() {
		IconManager icons = IconManager.getInstance();
		
		Icon projectIcon = icons.getIcon(IconManager.PROJECT);
		projectPanel = buildProjectSelectionPanel();

		Icon screenIcon = icons.getIcon(IconManager.SCREEN);
		screenPanel = buildScreenSelectionPanel();

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab(TEXT_PROJECTS, projectIcon, projectPanel,
				"Import settings for Projects");

		tabbedPane.addTab(TEXT_SCREENS, screenIcon, screenPanel,
				"Import settings for Screens");
		
		tabbedPane.addChangeListener(this);
		
		return wrapInGapPanel(tabbedPane);
	}
	
	/**
	 * Builds a JPanel holding the group selection section.
	 * @return JPanel holding the group selection UI elements
	 */
	private JPanel buildGroupSelectionPanel() {
		TableLayout groupLayout = 
				createTableLayout(TABLE_PREF_PREF, TABLE_PREF);
		JPanel groupPanel = new JPanel(groupLayout);
        
		if(groups.size() > 1)
		{
	        groupPanel.add(UIUtilities.setTextFont(TEXT_GROUP), "0, 0, r, c");
	        groupPanel.add(groupsBox,"1, 0");
		}
       
		return groupPanel;
	}
	
	/**
	 * Builds a JPanel holding the project selection section.
	 * @return JPanel holding the project selection UI elements
	 */
	private JPanel buildProjectSelectionPanel() {
		TableLayout projectLayout = 
				createTableLayout(TABLE_PREF_FILL_PREF, TABLE_PREF_PREF_PREF);
		
		JPanel projectPanel = new JPanel(projectLayout);
        
        projectPanel.add(UIUtilities.setTextFont(TEXT_PROJECT), "0, 0, r, c");
        projectPanel.add(projectsBox,"1, 0");
        projectPanel.add(newProjectButton, "2, 0, c, c");
        
        projectPanel.add(UIUtilities.setTextFont(TEXT_DATASET), "0, 1, r, c");
        projectPanel.add(datasetsBox,"1, 1");
        projectPanel.add(newDatasetButton, "2, 1, c, c");
       
		return wrapInGapPanel(projectPanel);
	}

	/**
	 * Builds a JPanel holding the screen selection section.
	 * @return JPanel holding the screen selection UI elements
	 */
	private JPanel buildScreenSelectionPanel() {
		TableLayout screenLayout = 
				createTableLayout(TABLE_PREF_FILL_PREF, TABLE_PREF);

		JPanel screenPanel = new JPanel(screenLayout);
        
        screenPanel.add(UIUtilities.setTextFont(TEXT_SCREEN), "0, 0, r, c");
        screenPanel.add(screensBox,"1, 0");
        screenPanel.add(newScreenButton, "2, 0, c, c");
       
		return wrapInGapPanel(screenPanel);
	}

	/**
	 * Builds the toolbar when the importer is the entry point.
	 * @param availableGroups
	 * @return See above.
	 */
	private void populateGroupBox(Collection<GroupData> availableGroups,
			GroupData selectedGroup) {
		groupsBox.removeItemListener(this);
		groupsBox.removeAllItems();
		
		JComboBoxImageObject selectedGroupItem = null;
		
		for (GroupData group : availableGroups) {
			JComboBoxImageObject comboBoxItem = new JComboBoxImageObject(group,
					getGroupIcon(group));
			groupsBox.addItem(comboBoxItem);		
			if (group.getId() == selectedGroup.getId())
			{
				selectedGroupItem = comboBoxItem;
			}
		}
		
		if (selectedGroup != null)
			groupsBox.setSelectedItem(selectedGroupItem);

		JComboBoxImageRenderer renderer = new JComboBoxImageRenderer();
		renderer.setPreferredSize(new Dimension(200, 130));
		groupsBox.setRenderer(renderer);
		
		groupsBox.addItemListener(this);
	}

	/**
	 * Returns the icon associated to the group.
	 * @param group  The group to handle.
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
	 */
	private void buildGUI() {
		BorderLayout layout = new BorderLayout();
		layout.setHgap(UI_GAP);
		layout.setVgap(UI_GAP);
		
		JPanel mainPanel = new JPanel(layout);
		mainPanel.add(buildGroupSelectionPanel(),BorderLayout.NORTH);
		mainPanel.add(buildDataTypeTabbedPane(), BorderLayout.CENTER);
		mainPanel.add(buildLowerButtonPanel(), BorderLayout.SOUTH);
		
		TableLayout containerLayout = createTableLayout(TABLE_GAP);
		Container contentPane = this.getContentPane();
		contentPane.setLayout(containerLayout);
		contentPane.add(mainPanel, "1, 1");
		
		// resize the window to minimum size
		this.pack();
		int minHeight = this.getHeight();
		
		Dimension minSize = new Dimension(MIN_WIDTH, minHeight);
		this.setMinimumSize(minSize);
		this.setPreferredSize(minSize);
		this.setSize(minSize);
	}

	/**
	 * Creates a TableLayout based on the design passed in
	 * with a HGap and VGap set to UI_GAP
	 * @param design the column / row template
	 * @return A table layout with the design & spacing applied
	 */
	private TableLayout createTableLayout(double[][] design) {
		TableLayout tableLayout =  new TableLayout(design);
		tableLayout.setHGap(UI_GAP);
		tableLayout.setVGap(UI_GAP);
		return tableLayout;
	}

	/**
	 * Creates a TableLayout based on the design passed in
	 * with a HGap and VGap set to UI_GAP
	 * @param columns the column template to use for the table design
	 * @param rows the row template to use for the table design
	 * @return A table layout with the design & spacing applied
	 */
	private TableLayout createTableLayout(double[] columns, double[] rows) {
		double[][] design = new double[][]{columns, rows};
		return createTableLayout(design);
	}
	
	/**
	 * Wraps the container in a JPanel with a bordered TableLayout
	 * with a border of UI_GAP
	 * @param container the container to wrap
	 * @return The JPanel wrapping the container
	 */
	private <T extends Container> JPanel wrapInGapPanel(T container) {
		TableLayout paddedLayout = createTableLayout(TABLE_GAP);
		JPanel gapPanel = new JPanel(paddedLayout);
		gapPanel.add(container, "1, 1");
		return gapPanel;
	}
	
	/**
	 * Wraps the container in a JPanel with a bordered TableLayout
	 * with the border width specified
	 * @param container
	 * @param top The amount of padding on the top of the container
	 * @param left The amount of padding on the left of the container
	 * @param bottom The amount of padding on the bottom of the container
	 * @param right The amount of padding on the right of the container
	 * @return The JPanel wrapping the container
	 */
	private <T extends Container> JPanel wrapInPaddedPanel(T container, 
			int top, int left, int bottom, int right) {
		double [][] spacedLayout = new double[][]
				{
					{left, TableLayout.FILL, right},
					{top, TableLayout.FILL, bottom}
				};
		
		TableLayout paddedLayout = createTableLayout(spacedLayout);
		JPanel gapPanel = new JPanel(paddedLayout);
		gapPanel.add(container, "1, 1");
		return gapPanel;
	}

	/**
	 * Closes the dialog.
	 */
	private void close() {
		setVisible(false);
		dispose();
	}

	/**
	 * Shows the message box and returns the option selected by the user.
	 * @return The option selected by the user.
	 */
	int centerLocation() {
		UIUtilities.centerAndShow(this);
		return userSelectedActionCommandId;
	}

	/**
	 * Shows the message box and returns the option selected by the user.
	 * @param location The location of the top-left corner of the dialog.
	 * @return The option selected by the user.
	 */
	int showLocation(Point location) {
		setLocation(location);
		setVisible(true);
		return userSelectedActionCommandId;
	}

	/**
	 * Sets the option.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		String actionCommand = ae.getActionCommand();
		
		try {
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
					firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY,
							NO_DATA_TYPE, importDataType);
			}

			if(newDataObject != null) {
				EditorDialog editor = new EditorDialog(owner,
						newDataObject, false);
				editor.addPropertyChangeListener(this);
				editor.setModal(true);
				UIUtilities.centerAndShow(editor);
			}
		} catch (NumberFormatException nfe) {
		}
	}

	/**
	 * Switch to display the projects/datasets/screens from the selected group
	 */
	private void switchToSelectedGroup() {
		GroupData selectedNewGroup = getSelectedGroup();
		
		if(selectedNewGroup.getId() != currentGroup.getId()) {
			objects = null;
			currentProjects = null;
			currentScreens = null;
			firePropertyChange(ImportDialog.PROPERTY_GROUP_CHANGED,
					currentGroup, selectedNewGroup);
		}
	}

	/**
	 * The import settings chosen but the user.
	 * @return The import settings selected by the user.
	 */
	protected ImportLocationSettings getImportSettings() {
		
		ImportLocationSettings importSettings = 
				new NullImportSettings(currentGroup);
		
		switch(importDataType)
		{
			case Importer.PROJECT_TYPE:
				DataNode project = (DataNode) projectsBox.getSelectedItem();
				DataNode dataset = (DataNode) datasetsBox.getSelectedItem();
				importSettings = new ProjectImportLocationSettings(currentGroup, 
						project, dataset);
				break;
			case Importer.SCREEN_TYPE:
				DataNode screen = (DataNode) screensBox.getSelectedItem();
				importSettings = new ScreenImportLocationSettings(currentGroup,
						screen);
				break;
		}
		
		return importSettings;
	}

	/**
	 * Populates the JComboBox with the items provided adding the top item, 
	 * selecting the specified item and adding hover tooltips.
	 * @param comboBox The JComboBox to populate
	 * @param nodes The items to populate the box with
	 * @param topItem The item to add at the top of the JComboBox
	 * @param selected The item to select in the JComboBox
	 * @param listener The action listener for this JComboBox
	 */
	private <T extends DataNode> void populateWithItemsAndTooltips(
			JComboBox comboBox, List<T> nodes, T topItem, T selected) {
		populateWithItemsAndTooltips(comboBox, nodes, topItem, selected, null);
	}
	
	/**
	 * Populates the JComboBox with the items provided adding the top item, 
	 * selecting the specified item and adding hover tooltips.
	 * @param comboBox The JComboBox to populate
	 * @param nodes The items to populate the box with
	 * @param topItem The item to add at the top of the JComboBox
	 * @param selected The item to select in the JComboBox
	 * @param listener The action listener for this JComboBox
	 */
	private <T extends DataNode> void populateWithItemsAndTooltips(
			JComboBox comboBox, List<T> nodes, T topItem, T selected, 
			ItemListener listener) {
		
		if(listener != null)
			comboBox.removeItemListener(listener);
		
		if(comboBox == null || nodes == null)
			return;
		
		ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer();
		comboBox.setRenderer(renderer);
		
		if (topItem != null)
			nodes.add(0, topItem);

		List<String> tooltips = new ArrayList<String>(nodes.size());
		
		comboBox.removeAllItems();
		
		for (T node : nodes) {
			comboBox.addItem(node);
			
			String nodeName = node.getFullName();
			List<String> wrapped = UIUtilities.wrapStyleWord(nodeName, 50);
			tooltips.add(UIUtilities.formatToolTipText(wrapped));
		}

		if(listener != null)
			comboBox.addItemListener(listener);
		
		if (selected != null && nodes.contains(selected))
			comboBox.setSelectedItem(selected);

		renderer.setTooltips(tooltips);
	}
	
	/**
	 * Creates a project.
	 * @param newProject The project to create.
	 */
	protected void createProject(DataObject newProject) {
		if (newProject == null)
			return;
		
		DataNode defaultNode = null;
		DataNode newProjectNode = new DataNode(newProject);
		DataNode newDatasetNode = new 
				DataNode(DataNode.createDefaultDataset(), newProjectNode);
		newProjectNode.addNode(newDatasetNode);
		
		List<DataNode> projects = new ArrayList<DataNode>();
		projects.add(newProjectNode);

		for (int i = 0; i < projectsBox.getItemCount(); i++) {
			DataNode currentNode = (DataNode) projectsBox.getItemAt(i);
			if (currentNode.isDefaultProject())
				defaultNode = currentNode;
			else
				projects.add(currentNode);
		}
		
		populateWithItemsAndTooltips(projectsBox, sorter.sort(projects), 
				defaultNode, newProjectNode, this);

		repaint();
	}

	/**
	 * Creates the dataset.
	 * @param dataset The dataset to create.
	 */
	protected void createDataset(DatasetData dataset) {
		if (dataset == null)
			return;
		
		DataNode defaultNode = null;
		DataNode selectedProject = (DataNode) projectsBox.getSelectedItem();
		DataNode newDatasetNode = new DataNode(dataset, selectedProject);
		List<DataNode> datasets = new ArrayList<DataNode>();
		datasets.add(newDatasetNode);
		
		for (int i = 0; i < datasetsBox.getItemCount(); i++) {
			DataNode currentNode = (DataNode) datasetsBox.getItemAt(i);
			if (currentNode.isDefaultNode())
				defaultNode = currentNode;
			else
				datasets.add(currentNode);
		}
		
		populateWithItemsAndTooltips(datasetsBox, sorter.sort(datasets), 
				defaultNode, newDatasetNode);
		
		repaint();
	}

	/**
	 * Creates a screen.
	 * @param newScreenObject The screen to create.
	 */
	protected void createScreen(DataObject newScreenObject) {
		if (newScreenObject == null)
			return;

		DataNode defaultNode = null;
		DataNode newScreenNode = new DataNode(newScreenObject);
		List<DataNode> screens = new ArrayList<DataNode>();
		screens.add(newScreenNode);
		
		for (int i = 0; i < screensBox.getItemCount(); i++) {
			DataNode currentNode = (DataNode) screensBox.getItemAt(i);
			if (currentNode.isDefaultScreen())
				defaultNode = currentNode;
			else
				screens.add(currentNode);
		}
		
		populateWithItemsAndTooltips(screensBox, sorter.sort(screens), 
				defaultNode, newScreenNode);
		repaint();
	}

	/**
	 * Populates the datasets box depending on the selected project.
	 */
	private void populateDatasetsBox() {
		DataNode selectedProject = (DataNode) projectsBox.getSelectedItem();
		List<DataNode> datasets = selectedProject.getDatasetNodes();
		List<DataNode> newDatasets = selectedProject.getNewNodes();
		
		DataNode selectedNode = null;
		
		if (newDatasets != null)
			datasets.addAll(newDatasets);
		
		List<DataNode> sortedDatasets = sorter.sort(datasets);

		if (selectedContainer != null) {
			Object o = selectedContainer.getUserObject();
			if (o instanceof DatasetData) {
				DatasetData dataset = (DatasetData) o;
				Iterator<DataNode> i = sortedDatasets.iterator();
				while (i.hasNext() && selectedNode == null) {
					DataNode currentNode = i.next();
					if (currentNode.getDataObject().getId() == dataset.getId()) {
						selectedNode = currentNode;
					}
				}
			}
		}

		populateWithItemsAndTooltips(datasetsBox, sortedDatasets, 
				null, selectedNode);
	}

	/**
	 * Handles property fired by the Editor Dialog.
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
			
			GroupData selectedGroup = getSelectedGroup();

			if (child != null) {
				firePropertyChange(ImportDialog.CREATE_OBJECT_PROPERTY, null,
						new ObjectToCreate(selectedGroup, child, parent));
			}
		}
	}

	/**
	 * Returns the currently selected group in the Group selection box.
	 * @return The currently selected group in the Group ComboBox
	 */
	private GroupData getSelectedGroup() {
		JComboBoxImageObject selectedEntry =
				(JComboBoxImageObject) groupsBox.getSelectedItem();
		return (GroupData) selectedEntry.getData();
	}

	/**
	 * Populates the selection boxes with the currently selected data.
	 */
	private void populateLocationComboBoxes() {
		List<DataNode> parentList = new ArrayList<DataNode>();
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
					parentList.add(n);
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
				projectsBox.removeItemListener(this);
				
				projectsBox.removeAllItems();
				datasetsBox.removeAllItems();
				
				if (newNodesPD != null) {
					j = newNodesPD.keySet().iterator();
					while (j.hasNext()) {
						n = j.next();
						data = n.getDataObject();
						if (data.getId() <= 0) {
							parentList.add(n);
						}
					}
				}
				
				loadProjects(datasetsList, sorter.sort(parentList));
				
				projectsBox.addItemListener(this);
				break;
			case Importer.SCREEN_TYPE:
				screensBox.removeAllItems();
				
				if (newNodesS != null) {
					j = newNodesS.iterator();
					while (j.hasNext()) {
						n = j.next();
						data = n.getDataObject();
						if (data.getId() <= 0)
							parentList.add(n);
					}
				}
				
				loadScreens(sorter.sort(parentList));
		}
	}

	/**
	 * Populates the screens box with the screen selection options
	 * @param sortedList The list of screens to use.
	 */
	private void loadScreens(List<DataNode> sortedList) {
		List<DataNode> finalList = new ArrayList<DataNode>();
		finalList.add(new DataNode(DataNode.createDefaultScreen()));
		finalList.addAll(sortedList);

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

		populateWithItemsAndTooltips(screensBox, finalList, null, selectedNode);
	}

	/**
	 * Returns the collection of new datasets.
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
	 * Populates the projects and datasets boxes with the current options
	 * @param datasets Datasets used to populate the Datasets JComboBox
	 * @param projects Projects used to populate the Projects JComboBox
	 */
	private void loadProjects(List<DataNode> datasets, 
			List<DataNode> projects) {
		
		List<DataNode> finalList = new ArrayList<DataNode>();
		DataNode n;
		List<DataNode> l = getOrphanedNewDatasetNode();

		if (datasets.size() > 0) { // orphaned datasets.
			datasets.add(new DataNode(DataNode.createDefaultDataset()));
			if (l != null)
				datasets.addAll(l);
			n = new DataNode(datasets);
		} else {
			List<DataNode> list = new ArrayList<DataNode>();
			list.add(new DataNode(DataNode.createDefaultDataset()));
			if (l != null && l.size() > 0)
				list.addAll(l);
			n = new DataNode(list);
		}
		finalList.add(n);
		finalList.addAll(projects);

		TreeImageDisplay node;
		DataNode selectedNode = null;
				
		// Determine the node to select.
		if (selectedContainer != null) {
			Object hostObject = selectedContainer.getUserObject();
			ProjectData selectedProject = null;
			if (hostObject instanceof ProjectData) {
				selectedProject = (ProjectData) hostObject;
			} else if (hostObject instanceof DatasetData) {
				node = selectedContainer.getParentDisplay();
				if (node != null &&
						node.getUserObject() instanceof ProjectData) {
					selectedProject = (ProjectData) node.getUserObject();
				}
			}
			
			if (selectedProject != null) {
				Iterator<DataNode> i = finalList.iterator();
				while (i.hasNext() && selectedNode == null) {
					DataNode currentNode = i.next();
					long dataNodeId = currentNode.getDataObject().getId();
					if (dataNodeId == selectedProject.getId()) {
						selectedNode = currentNode;
					}
				}
			}
		}


		populateWithItemsAndTooltips(projectsBox, finalList, null, 
				selectedNode);
		
		populateDatasetsBox();
	}

	/**
	 * Retrieves the new nodes to add the project.
	 * @param data The data object to handle.
	 * @param node The node hosting the data object.
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
	 * @param container The container that is selected
	 * @param type The data type identifier (Project / SCreen)
	 * @param objects The objects to use.
	 * @param currentGroupId The currently active user group.
	 */
	void reset(TreeImageDisplay container, int type,
			Collection<TreeImageDisplay> objects , long currentGroupId) {

		this.selectedContainer = container;
		this.importDataType = type;
		this.objects = objects;
		
		switchToDataType(type);
		
		onReconnected(groups, currentGroupId);
	}

	/**
	 * Re-populates and resets the groups, screens, projects & dataset options.
	 * @param availableGroups The list of available groups to this user.
	 * @param currentGroupId The currently active user group's ID.
	 */
	void onReconnected(Collection<GroupData> availableGroups,
			long currentGroupId) {
		this.groups = availableGroups;
		this.currentGroup = findGroupWithId(availableGroups, currentGroupId);
		
		populateGroupBox(groups, currentGroup);
		populateLocationComboBoxes();
	}
	
	/**
	 * Listener for the swapping of Screen / Project tabs
	 * @see ChangeListener
	 */
	public void stateChanged(ChangeEvent evt) {
		Object source = evt.getSource();
		if(source == tabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) evt.getSource();
			
			int dataType = tabbedPane.getSelectedIndex();

			switch(dataType)
			{
				case Importer.PROJECT_TYPE:
					objects = currentProjects;
					break;
				case Importer.SCREEN_TYPE:
					objects = currentScreens;
					break;
			}
			
			if (objects == null) {
				firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY,
						importDataType, dataType);
			} else {
				reset(selectedContainer, dataType, objects, 
						currentGroup.getId());
			}
		}
	}

	/**
	 * Listener for Group and Project JComboBox selection events
	 * @see ItemChangeListener
	 */
	public void itemStateChanged(ItemEvent ie) {
		Object source = ie.getSource();
		
		if(ie.getStateChange() == ItemEvent.SELECTED)
		{
			if(source == groupsBox) {
				switchToSelectedGroup();
			} else if (source == projectsBox) {
				populateDatasetsBox();
			}
		}
	}

	/**
	 * Sets the currently selected group
	 * @param group The group to set as selected
	 */
	void setSelectedGroup(GroupData group) {
		this.currentGroup = group;
		groupsBox.setSelectedItem(group);
	}
}
