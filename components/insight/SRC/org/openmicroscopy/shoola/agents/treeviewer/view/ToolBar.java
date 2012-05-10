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
import java.awt.Font;
import java.awt.Point;
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
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

//Third-party libraries
import org.jdesktop.swingx.JXBusyLabel;

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RunScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchGroup;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.ExperimenterVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.agents.util.ui.ScriptSubMenu;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.ui.TaskBar;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
        
        b = new JButton(controller.getAction(TreeViewerControl.REFRESH_TREE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        //bar.add(b);
        
        TreeViewerAction a = controller.getAction(TreeViewerControl.MANAGER);
        b = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((ManagerAction) a);
        bar.add(b);
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
        	/*
        	b = new JButton(controller.getAction(
            		TreeViewerControl.IMPORT_NO_SELECTION));
            UIUtilities.unifiedButtonLookAndFeel(b);
            bar.add(b);
            */
            bar.add(new JSeparator(JSeparator.VERTICAL));
        }
        
        /*
        b = new JButton(controller.getAction(
        		TreeViewerControl.EDITOR_NO_SELECTION));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        */
        
        fullScreen = new JToggleButton(
        		controller.getAction(TreeViewerControl.FULLSCREEN));
        //UIUtilities.unifiedButtonLookAndFeel(button);
        fullScreen.setSelected(model.isFullScreen());
        //bar.add(fullScreen);
        if (TreeViewerAgent.isAdministrator()) {
        	b = new JButton(controller.getAction(
        			TreeViewerControl.UPLOAD_SCRIPT));
            UIUtilities.unifiedButtonLookAndFeel(b);
            bar.add(b);
        }
        a = controller.getAction(TreeViewerControl.AVAILABLE_SCRIPTS);
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
       
        a = controller.getAction(TreeViewerControl.SWITCH_USER);
        usersButton = new JButton(a);
        usersButton.addMouseListener((SwitchUserAction) a);
        UIUtilities.unifiedButtonLookAndFeel(usersButton);
        
        IconManager icons = IconManager.getInstance();
    	menuButton = new JButton(icons.getIcon(IconManager.OWNER_GROUP));
    	menuButton.setVisible(false);
    	usersButton.setVisible(false);
    	UIUtilities.unifiedButtonLookAndFeel(menuButton);
    	menuButton.addMouseListener(new MouseAdapter() {
    		
    		/**
    		 * Shows the menu with the various 
    		 */
    		public void mousePressed(MouseEvent me)
    		{
    			createSelectionOption(me);
    		}
		});
    	groupContext.addMouseListener(new MouseAdapter() {
    		
    		/**
    		 * Shows the menu with the various 
    		 */
    		public void mousePressed(MouseEvent me)
    		{
    			SwitchGroup action = (SwitchGroup)
    				controller.getAction(TreeViewerControl.SWITCH_GROUP);
    			action.setPoint(me.getPoint());
    			action.actionPerformed(new ActionEvent(me.getSource(), 0, ""));
    		}
		});
    	bar.add(menuButton);
    	bar.add(usersButton);
    	bar.add(Box.createHorizontalStrut(5));
    	bar.add(groupContext);
    	setPermissions();
        return bar;
    }
    
    /** 
     * Creates the selection menu.
     * 
     * @param me The event to handle.
     */
    private void createSelectionOption(MouseEvent me)
    {
		JPopupMenu selectionMenu = new JPopupMenu();
		
		JMenu menu = new JMenu(SwitchUserAction.NAME_TO);
		menu.setToolTipText(SwitchUserAction.DESCRIPTION);
		//Check the groups that already in the view.
		Browser browser = model.getBrowser(Browser.PROJECTS_EXPLORER);
		List<Long> ids = new ArrayList<Long>();
		ExperimenterVisitor v = new ExperimenterVisitor(browser, -1);
		browser.accept(v, ExperimenterVisitor.TREEIMAGE_SET_ONLY);
		List<TreeImageDisplay> nodes = v.getNodes();
		Iterator<TreeImageDisplay> j = nodes.iterator();
		TreeImageDisplay node;
		GroupData g;
		while (j.hasNext()) {
			node = j.next();
			g = (GroupData) node.getUserObject();
			if (g.getExperimenters().size() > 1)
				ids.add(g.getId());
		}
		ButtonGroup buttonGroup = new ButtonGroup();
		List<GroupSelectionAction> l = controller.getUserGroupAction(false);
    	Iterator<GroupSelectionAction> i = l.iterator();
    	long id = model.getSelectedGroupId();
    	GroupSelectionAction a;
    	JMenuItem item;
		while (i.hasNext()) {
			a = i.next();
			item = new JCheckBoxMenuItem(a);
			item.setEnabled(ids.contains(a.getGroupId()));
			item.setSelected(a.isSameGroup(id));
			initMenuItem(item);
			buttonGroup.add(item);
			menu.add(item);
		}
		
		selectionMenu.add(menu);
		TreeViewerAction action = 
			controller.getAction(TreeViewerControl.SWITCH_GROUP);
		action.putValue(Action.SMALL_ICON, null);
		item = new JMenuItem(action);
		item.setText(SwitchGroup.NAME);
		selectionMenu.add(item);
		selectionMenu.show((JComponent) me.getSource(), me.getX(), me.getY());
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
        //bars.add(createEditBar());
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
        //if (p == null) throw new IllegalArgumentException("No point.");
        //if (personalMenu == null) {
        	personalMenu = new JPopupMenu();
        	personalMenu.setBorder(
        			BorderFactory.createBevelBorder(BevelBorder.RAISED));
        	List<JMenuItem> l = createMenuItem(false);
        	Iterator<JMenuItem> i = l.iterator();
        	while (i.hasNext()) {
				personalMenu.add(i.next());
			}
        //}
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
	
	/** Sets the permissions level.*/
    void setPermissions()
    {
    	GroupData group = model.getSelectedGroup();
    	if (group == null || groupContext == null) {
    		menuButton.setVisible(false);
        	groupContext.setVisible(false);
    		return;
    	}
    	String desc = "";
		Icon icon = null;
		switch (group.getPermissions().getPermissionsLevel()) {
			case GroupData.PERMISSIONS_PRIVATE:
				desc = GroupData.PERMISSIONS_PRIVATE_TEXT;
				icon = PERMISSIONS_PRIVATE;
				break;
			case GroupData.PERMISSIONS_GROUP_READ:
				desc = GroupData.PERMISSIONS_GROUP_READ_TEXT;
				icon = PERMISSIONS_GROUP_READ;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_LINK:
				desc = GroupData.PERMISSIONS_GROUP_READ_LINK_TEXT;
				icon = PERMISSIONS_GROUP_READ_LINK;
				break;
			case GroupData.PERMISSIONS_GROUP_READ_WRITE:
				desc = GroupData.PERMISSIONS_GROUP_READ_WRITE_TEXT;
				icon = PERMISSIONS_GROUP_READ_WRITE;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_TEXT;
				icon = PERMISSIONS_PUBLIC_READ;
				break;
			case GroupData.PERMISSIONS_PUBLIC_READ_WRITE:
				desc = GroupData.PERMISSIONS_PUBLIC_READ_WRITE_TEXT;
				icon = PERMISSIONS_PUBLIC_READ;
		}
		if (icon != null) groupContext.setIcon(icon);
		groupContext.setText(group.getName());
		groupContext.setToolTipText(desc);

        Set set = TreeViewerAgent.getAvailableUserGroups();
        boolean b = set != null && set.size() > 1;
        menuButton.setVisible(b);
    	groupContext.setVisible(b);
    	usersButton.setVisible(!b);
		repaint();
    }

}
