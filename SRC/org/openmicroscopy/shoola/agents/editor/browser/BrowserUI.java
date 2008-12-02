 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserUI 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.editor.browser;

//Java imports

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies

/** 
 * The UI for the Browser (the View of the Browser MVC).
 * Displays a Navigation tree, {@link #navTree} on the left, 
 * a tabbed pane containing alternative views of the tree-model in the center,
 * and a {@link FieldEditorDisplay} on the right, for editing fields.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class BrowserUI 
	extends JPanel
	implements ChangeListener 
{
	
	public static final String 		TREE_VIEW = "Tree View";
	
	public static final String 		TEXT_VIEW = "Text View";

	/** The tree hosting the display. */
    private JTree           		treeDisplay;
    
    /**
     * An outline view of the Tree, displayed on the left, used
     * to navigate
     */
    private JTree					navTree;
    
    /**
     * This UI component displays the {@link FieldEditorPanel}.
     * The {@link FieldEditorDisplay} listens to selection changes to the
     * {@link #navTree} and creates a new {@link FieldEditorPanel} for
     * the selected field. 
     * The {@link #editorPanel} is not visible if Editing of the tree is
     * disabled (ie. if the {@link Browser} is in the 
     * {@link Browser#TREE_DISPLAY} state.
     */
    private FieldEditorDisplay 		editorPanel;
    
    /**
     * The split pane on the right of the UI, which holds the 
     * {@link #treeDisplay} display in the left, and the
     *  {@link #editorPanel} in the right.
     */
    private JSplitPane 				rightSplitPane;
    
    /** 
     * Tab pane to hold the different views of the Protocol.
     * Either text-view or tree-view.
     */
    private JTabbedPane 			tabbedPane;
    
    /** The Controller. */
    private BrowserControl  		controller;
    
    /** The model. */
    private BrowserModel    		model;
    
    /**
     * An alternative way of viewing the treeModel, resembling a text document.
     * Contains text-components corresponding to each node of the tree, but
     * not organised hierarchically. 
     */
    private TextAreasView 			textView;
    
    
    /**
     * Initialises the JTrees for this UI.
     */
    private void createTrees() 
    {
    	navTree = new NavTree();
    	treeDisplay = new EditableTree(controller, navTree);
    	
    	textView = new TextAreasView(navTree, controller);
    	
    	int state = model.getState();
    	if (state == Browser.TREE_EDIT)
    		treeDisplay.setEditable(true);
    	
		ToolTipManager.sharedInstance().registerComponent(treeDisplay);
		ToolTipManager.sharedInstance().registerComponent(navTree);
    }
    
    /**
     * Builds the UI for this Panel
     */
    private void buildUI() 
    {
    	setLayout(new BorderLayout(0, 0));

    	JSplitPane leftSplitPane = new JSplitPane();
    	leftSplitPane.setOneTouchExpandable(true);
    	leftSplitPane.setDividerLocation(220);
    	leftSplitPane.setResizeWeight(0.3);
    	leftSplitPane.setBorder(null);
        leftSplitPane.setLeftComponent(new JScrollPane(navTree));
        
        rightSplitPane = new JSplitPane();
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setBorder(null);
        rightSplitPane.setResizeWeight(0.75);
        
        // The central component (tab pane)...
        // TODO: Need to split this out into it's own class, that has a
        // single setTreeModel() method, and only instantiates the views 
        // as needed. 
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(TEXT_VIEW, new JScrollPane(textView));
        tabbedPane.addTab(TREE_VIEW, new JScrollPane(treeDisplay));
        // listen for changes to tab-view, to update view-mode in controller
        tabbedPane.addChangeListener(this);
        // goes in the left part of the right splitPane
        rightSplitPane.setLeftComponent(tabbedPane);
        
        editorPanel = new FieldEditorDisplay(navTree, controller);
        rightSplitPane.setRightComponent(editorPanel);
        
        leftSplitPane.setRightComponent(rightSplitPane);
        
        add(leftSplitPane, BorderLayout.CENTER);
        
        add(new ToolBar(controller, navTree), BorderLayout.NORTH);
    }
    
    /**
     * Sets the visibility of the {@link #editorPanel} and the properties of
     * the {@link rightSplitPane}, in which it is displayed. 
     * 
     * @param visible		True if the editorPanel should be visible.
     */
    private void showFieldEditor(boolean visible) 
    {
    	editorPanel.setVisible(visible);
    	rightSplitPane.setDividerSize(visible ? 9 : 0);
    	rightSplitPane.setDividerLocation(visible ? 0.7 : 1.0);
    }
    
    /**
     * Sets the viewing mode in the controller, according to the currently 
     * displayed tab in the tabbed pane. 
     * Should be called when the tabbed pane changes, and also when the view
     * is first used to display a file (called by {@link #displayTree()}
     */
    private void updateViewingMode() 
    {
    	String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
    	
		if (TEXT_VIEW.equals(title)) {
			controller.setViewingMode(BrowserControl.TEXT_VIEW);
		} else {
			controller.setViewingMode(BrowserControl.TREE_VIEW);
		}
    }
    
    
    /**
     * Creates a new instance.
     * The {@link #initialize(BrowserControl, BrowserModel) initialize} method
     * should be called straight after to link this View to the Controller.
     */
    BrowserUI()
    {
    }
    
    /**
     * Links this View to its Controller and its Model.
     * 
     * @param controller    The Controller.
     * @param model         The Model.
     * @param exp			The experimenter the tree view is for.
     */
    void initialize(BrowserControl controller, BrowserModel model)
    {
    	if (controller == null)
    		throw new IllegalArgumentException("Controller cannot be null");
    	if (model == null)
    		throw new IllegalArgumentException("Model cannot be null");
        this.controller = controller;
        this.model = model;
        createTrees();
        buildUI();
        
        onStateChanged(); 	// update editing state etc.
    }
    
    /**
     * Sets the model of the JTrees with the treeModel from the BrowserModel. 
     */
    void displayTree() {
    	TreeModel tm = model.getTreeModel();
    	
    	navTree.setModel(tm);
    	
    	//TODO  need to merge these componenets into a single UI class.
    	textView.setTreeModel(tm);
    	treeDisplay.setModel(tm);
    	
    	if (tm != null)
    	tm.addTreeModelListener(editorPanel);
    	
    	// make sure that the controller is in sync with tabbed pane view.
    	updateViewingMode();
    }
    
    /**
     * The state has changed.
     * Update the editable status of the main tree display. 
     */
    void onStateChanged() {
    	int state = model.getState();
    	treeDisplay.setEditable(state == Browser.TREE_EDIT);	
    	showFieldEditor(state == Browser.TREE_EDIT);
    	
    	// should also update the tabbed pane to the correct view, 
    	// but, can assume any change in view-mode will have come from the
    	// tabbed-pane, so it will already be in correct view. 
    	
    	// update editorPanel, to show correct editing view.
    	editorPanel.refreshEditorDisplay();
    	
    }

    /**
     * Implemented as specified by the {@link ChangeListener} interface.
     * Responds to changes in the tabbed pane.
     * Calls {@link #updateViewingMode()}
     * 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
    	updateViewingMode();
	}
}
