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
import java.awt.Point;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ClassifierAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
import org.openmicroscopy.shoola.agents.util.finder.QuickFinder;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
    
    /** Reference to the finder. */
    private QuickFinder			finder;
    
    /**
     * Helper method to create the tool bar hosting the management items.
     * 
     * @return See above.
     */
    private JToolBar createManagementBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        JButton b = new JButton(controller.getAction(TreeViewerControl.VIEW));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.REFRESH_TREE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        //UIUtilities.unifiedButtonLookAndFeel(b);
        //bar.add(new JToggleButton(
        //		controller.getAction(TreeViewerControl.ROLL_OVER)));
        bar.add(new JSeparator(JSeparator.VERTICAL));
        TreeViewerAction a = controller.getAction(TreeViewerControl.MANAGER);
        b = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((ManagerAction) a);
        bar.add(b);
        b = new JButton(controller.getAction(
                TreeViewerControl.CREATE_TOP_CONTAINER));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.CREATE_OBJECT));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.SWITCH_USER));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        bar.add(new JSeparator(JSeparator.VERTICAL));
        return bar;
    }
    
    /**
     * Helper method to create the tool bar hosting the edit items.
     * 
     * @return See above.
     */
    private JToolBar createEditBar()
    {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        JButton b = new JButton(controller.getAction(
                                    TreeViewerControl.ANNOTATE));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        TreeViewerAction a = controller.getAction(TreeViewerControl.CLASSIFIER);
        b = new JButton(a);
        b.addMouseListener((ClassifierAction) a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.PROPERTIES));
        UIUtilities.unifiedButtonLookAndFeel(b);
        b = new JButton(controller.getAction(TreeViewerControl.SEARCH));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        //bar.add(b);
        //bar.add(new JSeparator(JSeparator.VERTICAL));
        return bar;
    }
    
    /** 
     * Builds the quick search component.
     * 
     * @return See above.
     */
    private JComponent createQuickSearch()
    {
    	finder = new QuickFinder(TreeViewerAgent.getRegistry());
    	return finder;
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        JPanel bars = new JPanel(), outerPanel = new JPanel();
        bars.setBorder(null);
        bars.setLayout(new BoxLayout(bars, BoxLayout.X_AXIS));
        bars.add(createManagementBar());
        bars.add(createEditBar());
        outerPanel.setBorder(null);
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.X_AXIS));
        outerPanel.add(bars);
        outerPanel.add(Box.createRigidArea(HBOX));
        outerPanel.add(Box.createHorizontalGlue());  
        
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
       
        add(UIUtilities.buildComponentPanel(outerPanel));
        add(UIUtilities.buildComponentPanelRight(createQuickSearch()));
    }

    /**
     * Creates a new instance.
     * 
     * @param controller    Reference to the control. 
     *                      Mustn't be <code>null</code>.
     */
    ToolBar(TreeViewerControl   controller)
    {
        if (controller == null) 
            throw new NullPointerException("No controller.");
        this.controller = controller;
        buildGUI();
    }
    
    /** Cancels any ongoing search. */
    void discard()
    {
    	if (finder != null) finder.cancel();
    }
    
    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showManagementMenu(Component c, Point p)
    {
        if (c == null) throw new IllegalArgumentException("No component.");
        if (p == null) throw new IllegalArgumentException("No point.");
        ManagePopupMenu managePopupMenu = new ManagePopupMenu(controller);
        managePopupMenu.show(c, p.x, p.y);
    }
    
    /**
     * Brings up the <code>ClassifyPopupMenu</code> on top of the specified 
     * component at the specified location.
     * 
     * @param c The component that requested the popup menu.
     * @param p The point at which to display the menu, relative to the
     *            <code>component</code>'s coordinates.
     */
    void showClassifyMenu(Component c, Point p)
    {
        if (c == null) throw new IllegalArgumentException("No component.");
        if (p == null) throw new IllegalArgumentException("No point.");
        ClassifyPopupMenu classifyPopupMenu = new ClassifyPopupMenu(controller);
        classifyPopupMenu.show(c, p.x, p.y);
    }
    
}
