/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.ToolBar
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.view;

//Java imports
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RunScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.util.GroupItem;
import org.openmicroscopy.shoola.agents.treeviewer.util.UserMenuItem;
import org.openmicroscopy.shoola.agents.util.ViewerSorter;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.agents.util.ui.ScriptSubMenu;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;
import pojos.GroupData;

/** 
 * The tool bar of {@link TreeViewer}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ToolBar
    extends JPanel
{
    
    /** Size of the horizontal box. */
    private static final Dimension HBOX = new Dimension(100, 16);

    /** The icon for private group.*/
    private static final Icon PERMISSIONS_PRIVATE;
    
    /** The icon for private group.*/
    private static final Icon PERMISSIONS_GROUP_READ;
    
    /** The icon for private group.*/
    private static final Icon PERMISSIONS_GROUP_READ_LINK;
    
    /** The icon for private group.*/
    private static final Icon PERMISSIONS_PUBLIC_READ;
    
    /** The icon for private group.*/
    private static final Icon PERMISSIONS_GROUP_READ_WRITE;
    
    /** The icon for private group.*/
    private static final Icon PERMISSIONS_PUBLIC_READ_WRITE;
    
    //Initializes the icons.
    static {
    	IconManager im = IconManager.getInstance();
    	PERMISSIONS_PRIVATE = im.getIcon(IconManager.PRIVATE_GROUP);
    	PERMISSIONS_GROUP_READ = im.getIcon(IconManager.READ_GROUP);
    	PERMISSIONS_GROUP_READ_LINK = im.getIcon(IconManager.READ_LINK_GROUP);
    	PERMISSIONS_GROUP_READ_WRITE = im.getIcon(IconManager.READ_WRITE_GROUP);
    	PERMISSIONS_PUBLIC_READ = im.getIcon(IconManager.PUBLIC_GROUP);
    	PERMISSIONS_PUBLIC_READ_WRITE = im.getIcon(IconManager.PUBLIC_GROUP);
    }
    
    /** Reference to the control. */
    private TreeViewerControl   controller;
    
    /** Reference to the model. */
    private TreeViewerModel	   model;
    
    /** Reference to the view. */
    private TreeViewerWin	   view;
    
    /** The menu displaying the groups the user is a member of. */
    private JPopupMenu			personalMenu;

    /** Button to open the full in a separate window. */
    private JToggleButton		fullScreen;
    
    /** The menu displaying the available scripts.*/
    private JPopupMenu			scriptsMenu;
    
    /** The button showing the available scripts.*/
    private JButton				scriptButton;
    
    /** Indicates the loading progress. */
	private JXBusyLabel		busyLabel;
	
	/** The index of the {@link #scriptButton}.*/
	private int index;
	
	/** The management bar.*/
	private JToolBar bar;
	
	/** The label displaying the group context.*/
	private JLabel groupContext;
	
	/** The button to display the menu.*/
	private JButton menuButton;
	
	/** The button to display the users.*/
	private JButton usersButton;

	/** Used to sort the list of users.*/
	private ViewerSorter sorter;
	
	/** Button indicating to add the user to the display.*/
	private JButton addToDisplay;
	
	/** The menu displaying the users option.*/
	private JPopupMenu usersMenu;
	
	/** The menu displaying the users option.*/
	private JPopupMenu groupsMenu;
	
	/** The selected group.*/
	private GroupItem selectedItem;
	
	/** Handles the group and users selection.*/
	private void handleUsersSelection()
	{
		if (selectedItem == null) return;
		//handle users and group selection.
		controller.setSelection(selectedItem.getGroup(),
				selectedItem.getSeletectedUsers(),
				!selectedItem.isGroupSelected());

		usersMenu.setVisible(false);
		groupsMenu.setVisible(false);
	}
	
	/**
	 * Formats the header.
	 * 
	 * @param text The text to display
	 * @return See above.
	 */
	private JPanel formatHeader(String text)
	{
		JPanel title = new JPanel();
		title.setLayout(new FlowLayout(FlowLayout.LEFT));
		title.add(UIUtilities.setTextFont(text));
		title.setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
		return title;
	}
	/**
	 * Creates the menu hosting the users belonging to the specified group.
	 * 
	 * @param groupItem The item hosting the group.
	 * @param groupNumber The number of groups.
	 * @return See above.
	 */
	private JComponent createGroupMenu(GroupItem groupItem, int groupNumber)
	{
		GroupData group = groupItem.getGroup();
		long id = model.getUserDetails().getId();
		//Determine the user already added to the display
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		TreeImageDisplay refNode = null;
		List<TreeImageDisplay> nodes;
		ExperimenterVisitor visitor;
		//Find the user already added to the selected group.
		visitor = new ExperimenterVisitor(browser, group.getId());
		browser.accept(visitor);
		nodes = visitor.getNodes();
		if (nodes.size() == 1) {
			refNode = nodes.get(0);
		}	
		visitor = new ExperimenterVisitor(browser, -1, -1);
		if (refNode != null) refNode.accept(visitor);
		else browser.accept(visitor);
		nodes = visitor.getNodes();
		List<Long> users = new ArrayList<Long>();
		Iterator<TreeImageDisplay> j = nodes.iterator();
		TreeImageDisplay n;
		while (j.hasNext()) {
			n = j.next();
			if (n.getUserObject() instanceof ExperimenterData) {
				users.add(((ExperimenterData) n.getUserObject()).getId());
			}
		}
		if (!users.contains(id)) users.add(id);
		//now add the users
		List<UserMenuItem> items = new ArrayList<UserMenuItem>();
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		List l = sorter.sort(group.getLeaders());
		Iterator i;
		ExperimenterData exp;
		
		UserMenuItem item;
		JPanel list;
		JCheckBox groupBox = new JCheckBox();
		Font font = groupBox.getFont();
		Font newFont = font.deriveFont(Font.BOLD);
		groupBox.setFont(newFont);
		groupBox.setText("Show Group");
		groupBox.setHorizontalTextPosition(SwingConstants.LEFT);
		if (groupNumber > 1)
			p.add(UIUtilities.buildComponentPanel(groupBox));
		groupItem.setGroupBox(groupBox);
		ActionListener al = new ActionListener() {
			
			/**
			 * Selects the group is not already selected.
			 * @see ActionListner#actionPerformed(ActionEvent)
			 */
			public void actionPerformed(ActionEvent e) {
				if (selectedItem == null) return;
				selectedItem.setGroupSelection(true);
				
			}
		};
		if (l != null && l.size() > 0) {
			p.add(formatHeader("Group owners"));
			i = l.iterator();
			list = new JPanel();
			list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
			while (i.hasNext()) {
				exp = (ExperimenterData) i.next();
				item = new UserMenuItem(exp, exp.getId() != id);
				item.setSelected(users.contains(exp.getId()));
				item.addActionListener(al);
				items.add(item);
				list.add(item);
			}
			p.add(UIUtilities.buildComponentPanel(list));
		}
		l = sorter.sort(group.getMembersOnly());
		if (l != null && l.size() > 0) {
			p.add(formatHeader("Members"));
			i = l.iterator();
			list = new JPanel();
			list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
			while (i.hasNext()) {
				exp = (ExperimenterData) i.next();
				item = new UserMenuItem(exp, exp.getId() != id);
				item.setSelected(users.contains(exp.getId()));
				item.addActionListener(al);
				items.add(item);
				list.add(item);
			}
			p.add(UIUtilities.buildComponentPanel(list));
		}
		
		int level = group.getPermissions().getPermissionsLevel();
		if (level == GroupData.PERMISSIONS_PRIVATE) {
			boolean owner = false;
			if (model.isAdministrator()) owner = true;
			else {
				ExperimenterData currentUser = model.getExperimenter();
				Set leaders = group.getLeaders();
				if (leaders != null) {
					Iterator k = leaders.iterator();
					while (k.hasNext()) {
						exp = (ExperimenterData) k.next();
						if (exp.getId() == currentUser.getId()) {
							owner = true;
							break;
						}
					}
				}
			}
			if (!owner) {
				Iterator<UserMenuItem> k = items.iterator();
				while (k.hasNext()) {
					k.next().setEnabled(false);
				}
			}
		}
		
		
		JScrollPane pane = new JScrollPane(p);
		groupItem.setUsersMenu(pane);
		groupItem.setUsersItem(items);
		return pane;
	}
	
	/**
	 * Creates the menu displaying the groups and users.
	 * 
	 * @param source The invoker.
	 * @param p The location of the mouse clicked.
	 */
	private void createGroupMenu(Component source, Point p)
	{
		if (!source.isEnabled()) return;
		Collection groups = model.getGroups();
		if (groups == null || groups.size() == 0) return;
		List sortedGroups = sorter.sort(groups);
		if (groupsMenu == null) {
			groupsMenu = new JPopupMenu();
			groupsMenu.addPopupMenuListener(new PopupMenuListener() {
				
				public void popupMenuWillBecomeVisible(PopupMenuEvent evt) {}
				
				/**
				 * Hides the menu
				 */
				public void popupMenuWillBecomeInvisible(PopupMenuEvent evt) {
					if (usersMenu != null) usersMenu.setVisible(false);
				}

				/**
				 * Hides the menu
				 */
				public void popupMenuCanceled(PopupMenuEvent evt) {
					if (usersMenu != null) usersMenu.setVisible(false);
				}
			});
		}
		groupsMenu.removeAll();
		
		GroupData group;
		if (addToDisplay == null) {
			addToDisplay = new JButton("Update");
			addToDisplay.addActionListener(new ActionListener() {
				
				public void actionPerformed(ActionEvent arg0) {
					handleUsersSelection();
				}
			});
			
		}
		if (usersMenu == null) {
			usersMenu = new JPopupMenu();
		}
		MouseAdapter l = new MouseAdapter() {
			
			/**
			 * Displays the users belonging to the selected group.
			 * @see MouseListener#mouseEntered(MouseEvent)
			 */
			public void mouseEntered(MouseEvent e) {
				GroupItem c = (GroupItem) e.getSource();
				selectedItem = c;
				usersMenu.setVisible(false);
				usersMenu.removeAll();
				usersMenu = new JPopupMenu();
				//usersMenu.setPopupSize(0, 0);
				
				Rectangle r = c.getBounds();
				usersMenu.add(c.getUsersMenu());
				usersMenu.add(UIUtilities.buildComponentPanel(addToDisplay));

				Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
				 
				//Set the size
				Dimension d = usersMenu.getPreferredSize();
				Point p1 = c.getLocation();
				SwingUtilities.convertPointToScreen(p1, c);
				int h = p1.y+d.height;
				int diff = h-size.height;
				if (diff > 0)  {
					usersMenu.setPopupSize(d.width+20, diff+30);
				}
				//Set the location
				usersMenu.show(e.getComponent(), r.width, 0);
			}
		};
		//Determine the group already displayed.
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		List<TreeImageDisplay> nodes;
		ExperimenterVisitor visitor;
		//Find the user already added to the selected group.
		visitor = new ExperimenterVisitor(browser, -1);
		browser.accept(visitor);
		nodes = visitor.getNodes();
		Iterator<TreeImageDisplay> k = nodes.iterator();
		List<Long> groupIds = new ArrayList<Long>();
		long id;
		while (k.hasNext()) {
			id = k.next().getUserObjectId();
			if (id >= 0) groupIds.add(id);
		}
		
		//Create the group menu.
		Iterator i = sortedGroups.iterator();
		GroupItem item;
		int size = sortedGroups.size();
		while (i.hasNext()) {
			group = (GroupData) i.next();
			item = new GroupItem(group, getGroupIcon(group));
			createGroupMenu(item, size);
			if (groupIds.contains(group.getId()) || size == 1)
				item.setGroupSelection(true);
			item.addMouseListener(l);
			groupsMenu.add(item);
		}
		groupsMenu.show(source, p.x, p.y);
	}
	
    /**
     * Sets the defaults of the specified menu item.
     * 
     * @param item The menu item.
     */
    private void initMenuItem(JMenuItem item)
    {
        item.setBorder(null);
        item.setFont((Font) 
                TreeViewerAgent.getRegistry().lookup(
                        "/resources/fonts/Labels"));
    }
    
    /**
     * Helper method to create the tool bar hosting the management items.
     * 
     * @return See above.
     */
    private JComponent createManagementBar()
    {
        bar = new JToolBar();
    	bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        JToggleButton button = new JToggleButton(
        		controller.getAction(TreeViewerControl.INSPECTOR));
        //UIUtilities.unifiedButtonLookAndFeel(button);
        button.setSelected(true);
        bar.add(button);
        
        button = new JToggleButton(
        		controller.getAction(TreeViewerControl.METADATA));
        //UIUtilities.unifiedButtonLookAndFeel(button);
        button.setSelected(true);
        bar.add(button);
        
        JButton b = new JButton(controller.getAction(TreeViewerControl.BROWSE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        switch (TreeViewerAgent.runAsPlugin()) {
			case TreeViewer.IMAGE_J:
				b = UIUtilities.formatButtonFromAction(
						controller.getAction(TreeViewerControl.VIEW));
				UIUtilities.unifiedButtonLookAndFeel(b);
				b.addMouseListener(new MouseAdapter() {
					
					/**
					 * Displays the menu when the user releases the mouse.
					 * @see MouseListener#mouseReleased(MouseEvent)
					 */
					public void mouseReleased(MouseEvent e)
					{
						controller.showMenu(TreeViewer.VIEW_MENU, 
								(JComponent) e.getSource(), e.getPoint());
					}
				});
				bar.add(b);
				break;
			default:
				b = new JButton(controller.getAction(TreeViewerControl.VIEW));
		        UIUtilities.unifiedButtonLookAndFeel(b);
		        bar.add(b);
		}
        

        bar.add(new JSeparator(JSeparator.VERTICAL));
        //Now register the agent if any
        TaskBar tb = TreeViewerAgent.getRegistry().getTaskBar();
        List<JComponent> l = tb.getToolBarEntries(TaskBar.AGENTS);
        if (l != null) {
        	Iterator<JComponent> i = l.iterator();
        	JComponent comp;
        	while (i.hasNext()) {
				comp = i.next();
				UIUtilities.unifiedButtonLookAndFeel(comp);
		        bar.add(comp);
			}
            bar.add(new JSeparator(JSeparator.VERTICAL));
        }
        fullScreen = new JToggleButton(
        		controller.getAction(TreeViewerControl.FULLSCREEN));
        fullScreen.setSelected(model.isFullScreen());
        //bar.add(fullScreen);
        if (TreeViewerAgent.isAdministrator()) {
        	b = new JButton(controller.getAction(
        			TreeViewerControl.UPLOAD_SCRIPT));
            UIUtilities.unifiedButtonLookAndFeel(b);
            bar.add(b);
        }
        TreeViewerAction a = controller.getAction(
        		TreeViewerControl.AVAILABLE_SCRIPTS);
        b = new JButton(a);
        Icon icon  = b.getIcon();
        Dimension d = new Dimension(UIUtilities.DEFAULT_ICON_WIDTH,
				UIUtilities.DEFAULT_ICON_HEIGHT);
        if (icon != null) 
        	d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
    	busyLabel = new JXBusyLabel(d);
    	busyLabel.setVisible(true);
        b.addMouseListener((RunScriptAction) a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        scriptButton = b;
        bar.add(b);
        index = bar.getComponentCount()-1;
        
        bar.add(new JSeparator(JSeparator.VERTICAL));
        
        groupContext = new JLabel();
        groupContext.setVisible(false);
       
        MouseAdapter adapter = new MouseAdapter() {
    		
    		/**
    		 * Shows the menu with the various 
    		 */
    		public void mousePressed(MouseEvent me)
    		{
    			//createSelectionOption(me);
    			createGroupMenu((Component) me.getSource(), me.getPoint());
    		}
		};
		
        a = controller.getAction(TreeViewerControl.SWITCH_USER);
        usersButton = new JButton(a);
        usersButton.addMouseListener(adapter);
        //usersButton.addMouseListener((SwitchUserAction) a);
        UIUtilities.unifiedButtonLookAndFeel(usersButton);
        
        IconManager icons = IconManager.getInstance();
    	menuButton = new JButton(icons.getIcon(IconManager.OWNER_GROUP));
    	menuButton.setVisible(false);
    	usersButton.setVisible(false);
    	UIUtilities.unifiedButtonLookAndFeel(menuButton);
    	
    	menuButton.addMouseListener(adapter);
    	groupContext.addMouseListener(adapter);
    	bar.add(usersButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(groupContext);
    	setPermissions();
        return bar;
    }
    
    /**
     * Helper method to create the tool bar hosting the edit items.
     * 
     * @return See above.
     */
    private JToolBar createSearchBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        bar.add(new JToggleButton(
        		controller.getAction(TreeViewerControl.SEARCH)));
        return bar;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel bars = new JPanel(), outerPanel = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createManagementBar());
        if (!TreeViewerWin.JXTASKPANE_TYPE.equals(view.getLayoutType())) {
        	bars.add(createSearchBar());
        }
        	
        outerPanel.setBorder(null);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(bars);
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createHorizontalGlue());  
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(UIUtilities.buildComponentPanel(outerPanel));
    }
    
    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the control. 
     *                      Mustn't be <code>null</code>.
     * @param model    		Reference to the model. 
     *                      Mustn't be <code>null</code>.
     * @param view    		Reference to the view. 
     *                      Mustn't be <code>null</code>.
     */
    ToolBar(TreeViewerControl controller, TreeViewerModel model, 
    		TreeViewerWin view)
    {
        if (controller == null) 
            throw new NullPointerException("No Controller.");
        if (model == null) 
            throw new NullPointerException("No Model.");
        if (view == null) 
            throw new NullPointerException("No View.");
        this.model = model;
        this.controller = controller;
        this.view = view;
        sorter = new ViewerSorter();
        sorter.setCaseSensitive(true);
        buildGUI();
    }
    
    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-up menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showManagementMenu(Component c, Point p)
    {
    	if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        ManagePopupMenu managePopupMenu = new ManagePopupMenu(controller);
        managePopupMenu.show(c, p.x, p.y);
    }
    
    /**
     * Brings up the <code>Personal Menu</code> on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-up menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showPersonalMenu(Component c, Point p)
    {
    	if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        personalMenu = new JPopupMenu();
        personalMenu.setBorder(
        		BorderFactory.createBevelBorder(BevelBorder.RAISED));
        List<JMenuItem> l = createMenuItem(false);
        Iterator<JMenuItem> i = l.iterator();
        while (i.hasNext()) {
        	personalMenu.add(i.next());
        }
        personalMenu.show(c, p.x, p.y);
    }
    
    /**
     * Creates the items for the menu.
     * 
     * @param add Pass <code>true</code> to build items for the <code>Add</code>
     * menu, <code>false</code> otherwise.
     * @return See above
     */
    private List<JMenuItem> createMenuItem(boolean add)
    {
    	List<JMenuItem> items = new ArrayList<JMenuItem>();
    	List<GroupSelectionAction> l = controller.getUserGroupAction(add);
    	Iterator<GroupSelectionAction> i = l.iterator();
    	GroupSelectionAction a;
    	JMenuItem item;
    	if (add) {
    		//Check the groups that already in the view.
    		Browser browser = model.getSelectedBrowser();
    		List<Long> ids = new ArrayList<Long>();
    		if (browser != null) {
    			ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
    			browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
    			List<TreeImageDisplay> nodes = v.getNodes();
    			Iterator<TreeImageDisplay> j = nodes.iterator();
    			TreeImageDisplay node;
    			while (j.hasNext()) {
					node = j.next();
					ids.add(((GroupData) node.getUserObject()).getId());
				}
    		}
    		
    		while (i.hasNext()) {
    			a = i.next();
    			item = new JMenuItem(a);
    			if (ids.size() > 0) {
    				item.setEnabled(!ids.contains(a.getGroupId()));
    			} else item.setEnabled(true);
    			initMenuItem(item);
    			items.add(item);
    		}
    	} else {
    		ButtonGroup buttonGroup = new ButtonGroup();
        	long id = model.getSelectedGroupId();
    		while (i.hasNext()) {
    			a = i.next();
    			item = new JCheckBoxMenuItem(a);
    			item.setEnabled(true);
    			item.setSelected(a.isSameGroup(id));
    			initMenuItem(item);
    			buttonGroup.add(item);
    			items.add(item);
    		}
    	}
    	
    	return items;
    }
    
    /**
     * Brings up the <code>Available Scripts</code> on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the pop-pup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showAvailableScriptsMenu(Component c, Point p)
    {
    	if (p == null) return;
        if (c == null) {
        	c = scriptButton;
        	//loading the data.
        }
        IconManager icons = IconManager.getInstance();
        Collection<ScriptObject> scripts = model.getAvailableScripts();
        if (scripts == null || scripts.size() == 0) return;
        if (scriptsMenu == null) {
        	scriptsMenu = new JPopupMenu();
        	JMenuItem refresh = new JMenuItem(icons.getIcon(
    				IconManager.REFRESH));
        	refresh.setText("Reload Scripts");
        	refresh.setToolTipText("Reloads the existing scripts.");
        	refresh.addMouseListener(new MouseAdapter() {
    			
    			/**
    			 * Launches the dialog when the user releases the mouse.
    			 * MouseAdapter#mouseReleased(MouseEvent)
    			 */
    			public void mouseReleased(MouseEvent e)
    			{
    				model.setAvailableScripts(null);
    				scriptsMenu = null;
    				controller.reloadAvailableScripts(e.getPoint());
    			}
        	});
        	scriptsMenu.add(refresh);
        	scriptsMenu.add(new JSeparator());
        	Iterator<ScriptObject> i = scripts.iterator();
        	ScriptObject so;
        	Map<String, ScriptSubMenu>
        		menus = new HashMap<String, ScriptSubMenu>();
        	String path;
        	ScriptSubMenu subMenu;
        	List<ScriptSubMenu> others = new ArrayList<ScriptSubMenu>();
        	List<String> formattedName = new ArrayList<String>();
        	
        	Icon icon = icons.getIcon(IconManager.ANALYSIS);
        	Icon largeIcon = icons.getIcon(IconManager.ANALYSIS_48);
        	ActionListener listener = new ActionListener() {
				
        		/** 
        		 * Listens to the selection of a script.
        		 * @see ActionListener#actionPerformed(ActionEvent)
        		 */
				public void actionPerformed(ActionEvent e) {
					ScriptMenuItem item = (ScriptMenuItem) e.getSource();
					controller.handleScriptSelection(item.getScript());
				}
			};
        	while (i.hasNext()) {
        		so = i.next();
        		if (so.getIcon() == null) {
        			so.setIcon(icon);
                	so.setIconLarge(largeIcon);
        		}
        		path = so.getPath();
        		subMenu = menus.get(path);
        		if (subMenu == null) {
        			subMenu = new ScriptSubMenu(path, formattedName);
        			menus.put(path, subMenu);
        			if (so.isOfficialScript()) scriptsMenu.add(subMenu);
        			else others.add(subMenu);
        		}
        		//if (!ScriptMenuItem.isScriptWithUI(so.getScriptLabel()))
        		subMenu.addScript(so).addActionListener(listener);
        	}
        	if (others.size() > 0) {
        		scriptsMenu.add(new JSeparator());
        		JMenu uploadedMenu = new JMenu("User Scripts");
        		scriptsMenu.add(uploadedMenu);
        		Iterator<ScriptSubMenu> j = others.iterator();
            	while (j.hasNext()) 
            		uploadedMenu.add(j.next());
        	}
        }
        scriptsMenu.show(c, p.x, p.y);
    }
    
    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c 	The component that requested the pop-up menu.
     * @param p 	The point at which to display the menu, relative to the
     *            	<code>component</code>'s coordinates.
     * @param index The index of the menu.
     */
    void showCreateMenu(Component c, Point p, int index)
    {
        if (c == null) return;
        if (p == null) return;
        PopupMenu menu = new PopupMenu(controller, index);
        menu.show(c, p.x, p.y);
    }
    
    /**
     * Sets the selected flag of the {@link #fullScreen} component.
     * 
     * @param selected The value to set.
     */
    void setFullScreenSelected(boolean selected)
    { 
    	fullScreen.setSelected(selected);
    }
    
	/** 
	 * Invokes when loadings scripts.
	 * 
	 * @param loading Passes <code>true</code> if there is an on-going loading.
	 *                <code>false</code> otherwise.
	 */
	void setScriptsLoadingStatus(boolean loading)
	{
		bar.remove(index);
		busyLabel.setBusy(loading);
		if (loading) bar.add(busyLabel, index);
		else bar.add(scriptButton, index);
		validate();
		repaint();
	}
	
	private Icon getGroupIcon(GroupData group)
	{
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				return PERMISSIONS_PRIVATE;
			case GroupData.PERMISSIONS_GROUP_READ:
				return PERMISSIONS_GROUP_READ;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				return PERMISSIONS_GROUP_READ_LINK;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				return PERMISSIONS_GROUP_READ_WRITE;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				return PERMISSIONS_PUBLIC_READ;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				return PERMISSIONS_PUBLIC_READ_WRITE;
		}
		return null;
	}
	
	/** Sets the permissions level.*/
    void setPermissions()
    {
    	Browser browser = model.getSelectedBrowser();
    	if (browser != null &&
    			browser.getBrowserType() == Browser.ADMIN_EXPLORER)
    		return;
    	GroupData group = model.getSelectedGroup();
    	if (group == null || groupContext == null) {
    		menuButton.setVisible(false);
        	groupContext.setVisible(false);
    		return;
    	}
    	String desc = "";
		Icon icon = getGroupIcon(group);
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
		}
		if (icon != null) groupContext.setIcon(icon);
		groupContext.setText(group.getName());
		groupContext.setToolTipText(desc);

        Collection set = TreeViewerAgent.getAvailableUserGroups();
        boolean b = set != null && set.size() > 1;
        menuButton.setVisible(b);
    	groupContext.setVisible(b);
    	usersButton.setVisible(!b);
		repaint();
    }

}
