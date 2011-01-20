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
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXBusyLabel;
import org.openmicroscopy.shoola.agents.treeviewer.actions.ManagerAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.NewObjectAction;
import org.openmicroscopy.shoola.agents.treeviewer.actions.TreeViewerAction;
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
    
    /** Reference to the model. */
    private TreeViewerModel	   model;
    
    /** Reference to the view. */
    private TreeViewerWin	   view;
    
    /** The component indicating the status of the import. */
    private JXBusyLabel			importLabel;
    
    /** The import bar. */
    private JToolBar			importBar;
    
    /** Initializes the components. */
    private void initComponents()
    {
    	importLabel = new JXBusyLabel();//new JXBusyLabel(SIZE);
    	importLabel.setEnabled(true);
    	importLabel.setVisible(false);
    	importLabel.setHorizontalTextPosition(JXBusyLabel.LEFT);
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
        a = controller.getAction(TreeViewerControl.NEW_OBJECT);
        b = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((NewObjectAction) a);
        //bar.add(b);
        a = controller.getAction(TreeViewerControl.NEW_TAG_OBJECT);
        b = new JButton(a);
        UIUtilities.unifiedButtonLookAndFeel(b);
        b.addMouseListener((NewObjectAction) a);
        //bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.SWITCH_USER));
        UIUtilities.unifiedButtonLookAndFeel(b);
        if (model.isMultiUser())  bar.add(b);
        b = new JButton(controller.getAction(
        		TreeViewerControl.EDITOR_NO_SELECTION));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
        b = new JButton(controller.getAction(TreeViewerControl.IMPORT));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
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
    
    /**
     * Returns the tool bar indicating the status of the import.
     * 
     * @return See above.
     */
    private JToolBar createImportBar()
    {
    	JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setRollover(true);
        bar.setBorder(null);
        bar.add(importLabel);
        JButton b = new JButton(controller.getAction(
        		TreeViewerControl.IMPORTER));
        UIUtilities.unifiedButtonLookAndFeel(b);
        bar.add(b);
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
        if (!view.getLayoutType().equals(TreeViewerWin.JXTASKPANE_TYPE)) {
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
     * Sets the status of the import.
     * 
     * @param perc  The text do display
     * @param show  Pass <code>true</code> to show the wheel, <code>false</code>
     * 				to hide it.
     */
    void setImportStatus(String perc, boolean show)
    {
    	if (importBar == null) {
    		importBar = createImportBar();
    		add(UIUtilities.buildComponentPanelRight(importBar));
    	}
    	importLabel.setBusy(true);
    	importLabel.setText(perc);
    	importLabel.setVisible(show);
    	
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
            throw new NullPointerException("No controller.");
        if (model == null) 
            throw new NullPointerException("No model.");
        if (view == null) 
            throw new NullPointerException("No view.");
        this.model = model;
        this.controller = controller;
        this.view = view;
        initComponents();
        buildGUI();
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
    	if (p == null) return;
        if (c == null) throw new IllegalArgumentException("No component.");
        //if (p == null) throw new IllegalArgumentException("No point.");
        ManagePopupMenu managePopupMenu = new ManagePopupMenu(controller);
        managePopupMenu.show(c, p.x, p.y);
    }
    
    /**
     * Brings up the <code>ManagePopupMenu</code>on top of the specified
     * component at the specified location.
     * 
     * @param c 	The component that requested the popup menu.
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
    
}
