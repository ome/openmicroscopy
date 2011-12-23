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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.RunScriptAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.util.ui.ScriptMenuItem;
import org.openmicroscopy.shoola.agents.util.ui.ScriptSubMenu;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import pojos.ExperimenterData;

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
        b = new JButton(controller.getAction(TreeViewerControl.VIEW));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.REFRESH_TREE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        //bar.add(b);
        
        bar.add(new JSeparator(JSeparator.VERTICAL));
       
        TreeViewerAction a = controller.getAction(TreeViewerControl.MANAGER);
        b = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((ManagerAction) a);
        bar.add(b);
        b = new JButton(controller.getAction(
        		TreeViewerControl.EDITOR_NO_SELECTION));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(
        		TreeViewerControl.IMPORT_NO_SELECTION));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        bar.add(new JSeparator(JSeparator.VERTICAL));
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
        a = controller.getAction(TreeViewerControl.SWITCH_USER);
        b = new JButton(a);
        b.addMouseListener((SwitchUserAction) a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        Set set = TreeViewerAgent.getAvailableUserGroups();
        if (set != null && set.size() > 0) {
        	a = controller.getAction(TreeViewerControl.PERSONAL);
            b = new JButton(a);
            BorderFactory.createCompoundBorder(new EmptyBorder(2, 2, 2, 2), 
            		BorderFactory.createLineBorder(Color.GRAY));
            b.addMouseListener((PersonalManagementAction) a);
            bar.add(b);
        }
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
        	List<GroupSelectionAction> l = controller.getUserGroupAction();
        	Iterator<GroupSelectionAction> i = l.iterator();
        	GroupSelectionAction a;
        	JCheckBoxMenuItem item;
        	ButtonGroup buttonGroup = new ButtonGroup();
        	ExperimenterData exp = TreeViewerAgent.getUserDetails();
        	long id = exp.getDefaultGroup().getId();
        	while (i.hasNext()) {
				a = i.next();
				item = new JCheckBoxMenuItem(a);
				item.setEnabled(true);
				item.setSelected(a.isSameGroup(id));
				initMenuItem(item);
				buttonGroup.add(item);
				personalMenu.add(item);
			}
        //}
        personalMenu.show(c, p.x, p.y);
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
	
}
