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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.GroupSelectionAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.PersonalManagementAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.SwitchUserAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
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
        JToolBar bar = new JToolBar();
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
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the po-pup menu.
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
        PopupMenu menu = new PopupMenu(controller,index);
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
    
}
