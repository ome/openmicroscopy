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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.fsimporter.util.ObjectToCreate;
import org.openmicroscopy.shoola.agents.fsimporter.view.Importer;
import org.openmicroscopy.shoola.agents.util.ComboBoxToolTipRenderer;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.DataNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.EditorDialog;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageObject;
import org.openmicroscopy.shoola.agents.util.ui.JComboBoxImageRenderer;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.util.ui.Selectable;
import org.openmicroscopy.shoola.util.ui.SelectableComboBoxModel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
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

	/** Text for Screens tab */
	private static final String TOOLTIP_SCREENS_TAB = 
			"Import settings for Screens";

	/** Text for Projects tab */
	private static final String TOOLTIP_PROJECTS_TAB = 
			"Import settings for Projects";
	
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
	/** The Project panel tab. */
	private JPanel projectPanel;

	/** The Screen panel tab. */
	private JPanel screenPanel;
	
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
	
	/** Sorts the objects from the display. */
	private ViewerSorter sorter;

	/** A reference to the selected target for import data. */
	private TreeImageDisplay container;

	/** The id of the import data type (Screen/Project) */
	private int dataType = NO_DATA_TYPE;

	/** Internal list of available groups. */
	private Collection<GroupData> groups;

	/** The currently selected group in the groups combo box. */
	//private GroupData currentGroup;

	/** The current possible import location nodes. */
	private Collection<TreeImageDisplay> objects;
	
	/** The list of currently available Projects */
	private List<DataNode> projects = new ArrayList<DataNode>();

	/** The list of currently available Screens */
	private List<DataNode> screens = new ArrayList<DataNode>();

	/** The projects -> dataset map of currently available Datasets */
	private Map<DataNode, List<DataNode>> datasets = 
			new Hashtable<DataNode, List<DataNode>>();

	/** The currently selected Project */
	private DataNode currentProject;

	/** The currently selected Dataset */
	private DataNode currentDataset;

	/** The currently selected Screen */
	private DataNode currentScreen;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent  The parent of the dialog.
	 * @param selectedContainer The container selected by the user.
	 * @param objects The screens / projects to be shown.
	 * @param groups  The available groups to the user.
	 * @param currentGroupId The id of the current user group.
	 */
    LocationDialog(JFrame parent, TreeImageDisplay selectedContainer,
			int importDataType, Collection<TreeImageDisplay> objects,
			Collection<GroupData> groups, long currentGroupId) {
		super(parent);

		this.owner = parent;
		this.container = selectedContainer;
		this.dataType = importDataType;
		this.objects = objects;
		this.groups = groups;
		setModal(true);
		setTitle(TEXT_TITLE);
		
		initUIComponents();
		layoutUI();
		
		populateUIWithDisplayData(findWithId(groups, currentGroupId));
	}

    /** 
     * Populates the various components.
     * 
     * @param selectedGroup The selected group.
     */
	private void populateUIWithDisplayData(GroupData selectedGroup)
	{
		convertToDisplayData(objects);
		populateGroupBox(groups, selectedGroup);
		populateLocationComboBoxes();
		displayViewFor(dataType);
	}
    /**
     * Returns the loaded experimenter corresponding to the specified user.
     * if the user is not loaded. Returns <code>null</code> if no user 
     * can be found.
     * 
     * @param owner The experimenter to handle.
     * @return see above.
     */
    private ExperimenterData getExperimenter(ExperimenterData owner)
	{
    	if (owner == null) return null;
    	if (owner.isLoaded()) return owner;
		List l = (List) ImporterAgent.getRegistry().lookup(
				LookupNames.USERS_DETAILS);
		if (l == null) return null;
		Iterator i = l.iterator();
		ExperimenterData exp;
		long id = owner.getId();
		while (i.hasNext()) {
			exp = (ExperimenterData) i.next();
			if (exp.getId() == id) return exp;
		}
		return null;
	}

	/**
	 * Swaps the data view that is currently active.
	 * @param dataType The id of the data type (Screen/Project)
	 */
	private void displayViewFor(int dataType) {
		switch(dataType)
		{
			case Importer.PROJECT_TYPE:
				this.tabbedPane.setSelectedComponent(projectPanel);
				break;
			case Importer.SCREEN_TYPE:
				this.tabbedPane.setSelectedComponent(screenPanel);
		}
	}

	/**
	 * Scans the DataObjects provided and returns the Object with matching id.
	 * <null> if not found
	 * @param dataObjects The list of DataObjects.
	 * @param id The id of the object to find.
	 * @return The DataObject in the list with matching id, <null> if not found.
	 */
	private <T extends DataObject> T findWithId(Collection<T> dataObjects,
			long id) {
		
		for (T dataObject : dataObjects) {
			if(dataObject.getId() == id)
				return dataObject;
		}
		
		return null;
	}
	
	/**
	 * Scans the DataNodes provided and returns the Object with matching id.
	 * <null> if not found
	 * @param dataObjects The list of DataObjects.
	 * @param id The id of the object to find.
	 * @return The DataObject in the list with matching id, <null> if not found.
	 */
	private DataNode findDataNodeById(Collection<DataNode> nodes, long id) {
		if (nodes == null || nodes.size() == 0)
			return null;
		
		for (DataNode node : nodes) {
			if(getIdOf(node) == id)
				return node;
		}
		
		return null ;
	}

	/**
	 * Initialises the UI components of the dialog.
	 */
	private void initUIComponents() {
		sorter = new ViewerSorter();

		// main components
		groupsBox = new JComboBox();
		groupsBox.addItemListener(this);
		
		refreshButton = new JButton(TEXT_REFRESH);
		refreshButton.setBackground(UIUtilities.BACKGROUND);
		refreshButton.setToolTipText(TOOLTIP_REFRESH);
		refreshButton.setActionCommand("" + CMD_REFRESH_DISPLAY);
		refreshButton.addActionListener(this);
		

		projectsBox = new JComboBox();
		projectsBox.addItemListener(this);
		
		datasetsBox = new JComboBox();
		datasetsBox.addItemListener(this);
		
		screensBox = new JComboBox();
		screensBox.addItemListener(this);
		
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
				TOOLTIP_PROJECTS_TAB);

		tabbedPane.addTab(TEXT_SCREENS, screenIcon, screenPanel,
				TOOLTIP_SCREENS_TAB);
		
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
			if (selectedGroup != null && selectedGroup.getId() == group.getId())
			{
				selectedGroupItem = comboBoxItem;
			}
		}
		
		if (selectedGroupItem != null)
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
	private void layoutUI() {
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
					storeCurrentSelections();
					
					firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY,
							NO_DATA_TYPE, dataType);
			}

			if (newDataObject != null) {
				
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
		setInputsEnabled(false);
		firePropertyChange(ImportDialog.PROPERTY_GROUP_CHANGED,
				null, selectedNewGroup);
	}

	/**
	 * The import settings chosen but the user.
	 * @return The import settings selected by the user.
	 */
	protected ImportLocationSettings getImportSettings() {
		
		GroupData group = getSelectedGroup();
		switch(dataType)
		{
			case Importer.PROJECT_TYPE:
				DataNode project = getSelectedItem(projectsBox);
				DataNode dataset = getSelectedItem(datasetsBox);
				return new ProjectImportLocationSettings(group,
						project, dataset);
			case Importer.SCREEN_TYPE:
				DataNode screen = getSelectedItem(screensBox);
				return new ScreenImportLocationSettings(group, screen);
		}
		
		return new NullImportSettings(group);
	}


	/**
	 * Populates the JComboBox with the items provided adding hover tooltips.
	 * @param comboBox The JComboBox to populate
	 * @param nodes The items to populate the box with
	 * @param topItem The item to add at the top of the JComboBox
	 */
	private void displayItemsWithTooltips(JComboBox comboBox,
			List<DataNode> listItems) {
		displayItems(comboBox, listItems, null, null);
	}
	
	/**
	 * Populates the JComboBox with the items provided adding hover tooltips 
	 * and selecting the specified item.
	 * @param comboBox The JComboBox to populate
	 * @param nodes The items to populate the box with
	 * @param topItem The item to add at the top of the JComboBox
	 * @param selected The item to select in the JComboBox
	 */
	private void displayItemsWithTooltips(JComboBox comboBox, 
			List<DataNode> listItems, DataNode selected) {
		displayItems(comboBox, listItems, selected, null);
	}
	
	/**
	 * Populates the JComboBox with the items provided adding hover tooltips, 
	 * selecting the specified item and attaching the listener.
	 * @param comboBox The JComboBox to populate
	 * @param nodes The items to populate the box with
	 * @param topItem The item to add at the top of the JComboBox
	 * @param select The item to select in the JComboBox
	 * @param itemListener An item listener to add for the JComboBox
	 */
	private void displayItems(JComboBox comboBox,
			List<DataNode> items, DataNode select, 
			ItemListener itemListener) {

		if (comboBox == null || items == null) return;
		//Only add the item the user can actually select
		if (itemListener != null)
			comboBox.removeItemListener(itemListener);
		comboBox.removeAllItems();

		List<String> tooltips = new ArrayList<String>(items.size());
		List<String> lines;
		ExperimenterData exp;
		ExperimenterData loggedInUser = ImporterAgent.getUserDetails();
		DefaultComboBoxModel model = new SelectableComboBoxModel();
		Selectable<DataNode> selected = null;
		
		for (DataNode node : items) {
			exp = getExperimenter(node.getOwner());
			lines = new ArrayList<String>();
			if (exp != null) {
				lines.add("<b>Owner: </b>"+EditorUtil.formatExperimenter(exp));
			}
			lines.addAll(UIUtilities.wrapStyleWord(node.getFullName()));
			tooltips.add(UIUtilities.formatToolTipText(lines));
			
			boolean selectable = true;
			if (!node.isDefaultNode()) {
				selectable = node.getDataObject().canLink();
			}
			
			Selectable<DataNode> comboBoxItem = 
					new Selectable<DataNode>(node, selectable);
			if(select != null && node.getDataObject().getId() == select.getDataObject().getId())
				selected = comboBoxItem;
			
			model.addElement(comboBoxItem);
		}

		//To be modified
		ComboBoxToolTipRenderer renderer = new ComboBoxToolTipRenderer(
				loggedInUser.getId());
		renderer.setTooltips(tooltips);
		comboBox.setModel(model);
		comboBox.setRenderer(renderer);
		
		if (selected != null)
			comboBox.setSelectedItem(selected);

		if(itemListener != null)
			comboBox.addItemListener(itemListener);
	}
	
	/**
	 * Creates a project.
	 * @param newProject The project to create.
	 */
	protected void createProject(DataObject newProject) {
		if (newProject == null)
			return;

		DataNode newProjectNode = new DataNode(newProject);
		DataNode newDefaultDatasetNode = new 
				DataNode(DataNode.createDefaultDataset(), newProjectNode);
		newProjectNode.addNode(newDefaultDatasetNode);
		
		List<DataNode> newDatasets = new ArrayList<DataNode>();
		newDatasets.add(newDefaultDatasetNode);
		
		projects.add(newProjectNode);
		projects = sort(projects); 
		datasets.put(newProjectNode, newDatasets);
		
		displayItems(projectsBox, projects, newProjectNode, this);
		displayItemsWithTooltips(datasetsBox, newDatasets);
		
		repaint();
	}

	/**
	 * Creates the dataset.
	 * @param dataset The dataset to create.
	 */
	protected void createDataset(DatasetData dataset) {
		if (dataset == null)
			return;

		DataNode selectedProject =  getSelectedItem(projectsBox);
		DataNode newDatasetNode = new DataNode(dataset, selectedProject);

		List<DataNode> projectDatasets = datasets.get(selectedProject);
		
		if(projectDatasets == null)
		{
			projectDatasets = new ArrayList<DataNode>();
			DataNode newDefaultDatasetNode = new 
					DataNode(DataNode.createDefaultDataset(), selectedProject);
			selectedProject.addNode(newDefaultDatasetNode);
			
			projectDatasets.add(newDefaultDatasetNode);
		}
		
		projectDatasets.add(newDatasetNode);
		projectDatasets = sort(projectDatasets);
		
		datasets.put(selectedProject, projectDatasets);

		displayItemsWithTooltips(datasetsBox, projectDatasets, newDatasetNode);
		
		repaint();
	}

	/**
	 * Creates a screen.
	 * @param newScreenObject The screen to create.
	 */
	protected void createScreen(DataObject newScreenObject) {
		if (newScreenObject == null)
			return;

		DataNode newScreenNode = new DataNode(newScreenObject);
		
		screens.add(newScreenNode);
		screens = sort(screens);
		
		displayItemsWithTooltips(screensBox, screens, newScreenNode);
		
		repaint();
	}

	/**
	 * Populates the datasets box depending on the selected project.
	 */
	private void populateDatasetsBox() {
		DataNode project = getSelectedItem(projectsBox);

		displayItemsWithTooltips(datasetsBox, datasets.get(project));
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
				DataNode n = getSelectedItem(projectsBox);
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
	 * @return see above.
	 */
	private GroupData getSelectedGroup() {
		JComboBoxImageObject selectedEntry =
				(JComboBoxImageObject) groupsBox.getSelectedItem();
		return (GroupData) selectedEntry.getData();
	}


	/**
	 * Converts the treeNodes in to DataNdoes used to hold display data for the 
	 * location combo boxes.
	 * @param treeNodes The nodes to convert for import target options.
	 */
	private void convertToDisplayData(Collection<TreeImageDisplay> treeNodes) {
		projects.clear();
		datasets.clear();
		screens.clear();
	
		DataNode defaultProject = new DataNode(DataNode.createDefaultProject());
		List<DataNode> orphanDatasets = new ArrayList<DataNode>();
		
		List<DataNode> lp = new ArrayList<DataNode>();
		List<DataNode> ls = new ArrayList<DataNode>();
		if (treeNodes != null)
		{
			for (TreeImageDisplay treeNode : treeNodes) {
				Object userObject = treeNode.getUserObject();
				
				if(userObject instanceof ProjectData)
				{
					DataNode project = new DataNode((ProjectData) userObject);
					lp.add(project);

					List<DataNode> projectDatasets = new ArrayList<DataNode>();

					List children = treeNode.getChildrenDisplay();
					TreeImageDisplay n;
					for (Object child : children) {
						n = (TreeImageDisplay) child;
						projectDatasets.add(new DataNode(
								(DatasetData) n.getUserObject()));
					}
					List<DataNode> list = new ArrayList<DataNode>();
					list.add(new DataNode(DataNode.createDefaultDataset()));
					list.addAll(sort(projectDatasets));
					datasets.put(project, list);
				}
				
				if(userObject instanceof ScreenData)
				{
					DataNode screen = new DataNode((ScreenData) userObject);
					ls.add(screen);
				}
				
				if(userObject instanceof DatasetData)
				{
					DataNode dataset = new DataNode((DatasetData) userObject);
					orphanDatasets.add(dataset);
				}	
			}
		}
		List<DataNode> l = new ArrayList<DataNode>();
		l.add(new DataNode(DataNode.createDefaultDataset()));
		l.addAll(sort(orphanDatasets));
		datasets.put(defaultProject, l);
		
		projects.add(defaultProject);
		projects.addAll(sort(lp));
		
		screens.add(new DataNode(DataNode.createDefaultScreen()));
		screens.addAll(sort(ls));
	}
	
	/**
	 * Helper method to sort objects and casts as a List<T>.
	 * @param list The list to sort
	 * @return The sorted list
	 */
	private <T> List<T> sort(List<T> list)
	{
		return (List<T>) sorter.sort(list);
	}
	
	/**
	 * Populates the selection boxes with the currently selected data.
	 */
	private void populateLocationComboBoxes() {
		DataNode selectedProject = null;
		DataNode selectedDataset = null;
		DataNode selectedScreen = null;
		
		/* work out what to select, 
		 * NOTE: this defaults back to the selected container 
		 * on tab switch / refresh
		 */
		if (container != null)
		{
			Object hostObject = container.getUserObject();
			if(hostObject instanceof ProjectData)
			{
				selectedProject = findDataNode(projects, 
					hostObject, ProjectData.class);
			}
			
			if(hostObject instanceof DatasetData)
			{
				Object parentNode = getParentUserObject(container);
				
				selectedProject = findDataNode(projects, 
						parentNode, ProjectData.class);
				
				DatasetData datasetData = (DatasetData) hostObject;
				long datasetId = datasetData.getId();
				selectedDataset = findDataNodeById(
						datasets.get(selectedProject), datasetId);
			}
			
			if(hostObject instanceof ScreenData)
			{
				selectedScreen = findDataNode(screens, 
					hostObject, ScreenData.class);
			}
		}
		else
		{
			selectedProject = findDataNode(projects, currentProject);
			selectedDataset = findDataNode(datasets.get(selectedProject), 
					currentDataset);
			selectedScreen = findDataNode(screens, currentScreen);
		}
		
		switch (dataType)
		{
			case Importer.PROJECT_TYPE:
				
				
				displayItems(projectsBox, projects, selectedProject, this);
				displayItemsWithTooltips(datasetsBox, 
						datasets.get(selectedProject), selectedDataset);
				break;
				
			case  Importer.SCREEN_TYPE:
				if (container != null)
				{
					Object hostObject = container.getUserObject();
					selectedScreen = findDataNode(screens, 
							hostObject, ScreenData.class);
				}
				
				displayItemsWithTooltips(screensBox, 
						screens, selectedScreen);
		}
	}

	/**
	 * Searches the list of nodes returning the entry with the same Id as 
	 * the find parameter, returns <null> if the list is empty, or the first 
	 * item in the list if the find parameter is not found or is null.
	 * @param nodes The list of nodes to scan.
	 * @param find The node to match Id against.
	 * @return The item, <null> if no list, first list item if find is <null>.
	 */
	private DataNode findDataNode(List<DataNode> nodes, DataNode find) {
		if(nodes == null || nodes.size() == 0)
			return null;
		
		if(find == null)
			return nodes.get(0);
		
		for (DataNode node : nodes) {
			if(getIdOf(node) == getIdOf(find))
				return node;
		}
		
		return nodes.get(0);
	}

	/**
	 * Helper method to return the UserObject of the parent node.
	 * @return see above.
	 */
	private Object getParentUserObject(TreeImageDisplay node) {
		if (node.getParentDisplay() == null) return null;
		return node.getParentDisplay().getUserObject();
	}
	
	/**
	 * Searches a list of DataNodes for an entry with a matching Id and type to 
	 * that of the DataObject provided
	 * @param list The list of DataNodes to search through.
	 * @param find The object to 
	 * @param klass The Class<T> description of the type to match against.
	 * @return
	 */
	private <T extends DataObject> DataNode findDataNode(
			List<DataNode> list, Object find, Class<T> klass) {
		DataNode selectedItem = null;
		if(find != null && klass.isInstance(find))
		{
			T dataObject = klass.cast(find);
			long nodeId = dataObject.getId();
			selectedItem = findDataNodeById(list, nodeId);
		}
		if(selectedItem == null)
			selectedItem = list.get(0);
		
		return selectedItem;
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

		this.dataType = type;
		this.objects = objects;
		this.container = container;
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
		populateUIWithDisplayData(findWithId(availableGroups, currentGroupId));
		setInputsEnabled(true);
	}
	
	/**
	 * Listener for the swapping of Screen / Project tabs
	 * @see ChangeListener
	 */
	public void stateChanged(ChangeEvent evt) {
		Object source = evt.getSource();
		if(source == tabbedPane) {
			JTabbedPane tabbedPane = (JTabbedPane) evt.getSource();
			
			JPanel activePanel = (JPanel) tabbedPane.getSelectedComponent();
			// default to projects
			int newDataType = Importer.PROJECT_TYPE;
			
			if(activePanel == screenPanel)
				newDataType = Importer.SCREEN_TYPE;
			
			storeCurrentSelections();
			
			firePropertyChange(ImportDialog.REFRESH_LOCATION_PROPERTY,
					dataType, newDataType);
		}
	}

	/**
	 * Stores the currently selected combobox items for restoration later.
	 */
	private void storeCurrentSelections() {
		currentProject = getSelectedItem(projectsBox);
		currentDataset = getSelectedItem(datasetsBox);
		currentScreen = getSelectedItem(screensBox);
	}

	/**
	 * Returns the selected item in the combo box as a DataNode.
	 * @param comboBox see above.
	 * @return see above.
	 */
	private DataNode getSelectedItem(JComboBox comboBox) {
		Object compareItem = comboBox.getSelectedItem();
		if(compareItem instanceof Selectable<?>)
		{
			Selectable<?> selectable = (Selectable<?>) compareItem;
			Object innerItem = selectable.getObject();
			if(innerItem instanceof DataNode)
				return (DataNode) innerItem;
		}
		return null;
	}
	
	/**
	 * Returns the Id of the DataNode.
	 * @param node The node to use.
	 * @return The id of the node.
	 */
	private long getIdOf(DataNode node)
	{
		return node.getDataObject().getId();
	}

	/**
	 * Listener for Group and Project JComboBox selection events
	 * @see ItemChangeListener
	 */
	public void itemStateChanged(ItemEvent ie) {
		Object source = ie.getSource();
		
		if (ie.getStateChange() == ItemEvent.SELECTED)
		{
			if (source == groupsBox) {
				storeCurrentSelections();
				
				switchToSelectedGroup();
			} else if (source == projectsBox) {
				DataNode node = getSelectedItem(projectsBox);
				datasetsBox.setEnabled(true);
				newDatasetButton.setEnabled(true);
				if (node.isDefaultProject()) {
					newDatasetButton.setEnabled(true);
				} else if (!node.getDataObject().canLink()) {
					projectsBox.setSelectedIndex(0);
					return;
				}
				populateDatasetsBox();
			} else if (source == datasetsBox) {
				DataNode node = getSelectedItem(datasetsBox);
				if (!node.isDefaultNode()) {
					if (!node.getDataObject().canLink())
						datasetsBox.setSelectedIndex(0);
				}
			} else if (source == screensBox) {
				DataNode node =  getSelectedItem(screensBox);
				if (!node.isDefaultNode()) {
					if (!node.getDataObject().canLink())
						screensBox.setSelectedIndex(0);
				}
			}
		}
	}

	/**
	 * Sets the currently selected group
	 * @param group The group to set as selected
	 */
	void setSelectedGroup(GroupData group) {
		groupsBox.setSelectedItem(group);
	}
	
	/**
	 * Enables or disables the user input controls
	 * @param isEnabled Whether to enable or disable the controls
	 */
	private void setInputsEnabled(boolean isEnabled)
	{
		projectsBox.setEnabled(isEnabled);
		datasetsBox.setEnabled(isEnabled);
		screensBox.setEnabled(isEnabled);

		newProjectButton.setEnabled(isEnabled);
		newDatasetButton.setEnabled(isEnabled);
		newScreenButton.setEnabled(isEnabled);
		addButton.setEnabled(isEnabled);
	}
}
