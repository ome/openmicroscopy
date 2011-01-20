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

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.ScrollablePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * The UI for the Browser (the View of the Browser MVC).
 * Displays a Navigation tree, {@link #navTree} on the left, 
 * a tabbed pane containing alternative views of the tree-model in the center,
 * and a {@link FieldEditorDisplay} on the right, for editing fields.
 * Does not include tool-bar, which can be retrieved with {@link #getToolBar()}  
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
	
	/** The Controller. */
    private BrowserControl  		controller;
    
    /** The model. */
    private BrowserModel    		model;
    
    private Browser					browser;
    
	/** The title of the tree tabbed pane. */
	private static final String 		TREE_VIEW = "Tree View";
	
	/** The title of the text tabbed pane. */
	private static final String 		TEXT_VIEW = "Text View";
	
	private int 						DIVIDER_WIDTH = 6;

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
     * disabled (i.e. if the {@link Browser} is in the 
     * {@link Browser#FILE_LOCKED} state.
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

    /** A panel to display experimental info in the text tab-pane */
    private ExperimentInfoPanel		expInfoText;
    
    /** A panel to display experimental info in the tree tab-pane */
    private ExperimentInfoPanel		expInfoTree;
    
    /**
     * An alternative way of viewing the treeModel, resembling a text document.
     * Contains text-components corresponding to each node of the tree, but
     * not organised hierarchically. 
     */
    private TextAreasView 			textView;
    
    /** Reference to the metadata UI. */
    private MetadataUI 				metadataUI;

    /**
     * Initialises the JTrees for this UI.
     */
    private void createTrees() 
    {
    	navTree = new NavTree();
    	treeDisplay = new EditableTree(controller, navTree);
    	
    	// passed a reference to Browser to listen for changes
    	textView = new TextAreasView(navTree, controller, browser);
    	
    	metadataUI = new MetadataUI(this, model.getTreeModel(), controller);
    	
    	
		ToolTipManager.sharedInstance().registerComponent(treeDisplay);
		ToolTipManager.sharedInstance().registerComponent(navTree);
    }
    
    /**
     * Builds the UI for this Panel
     */
    private void buildUI() 
    {
    	setLayout(new BorderLayout(0, 0));
    	setBackground(UIUtilities.BACKGROUND_COLOR);

    	//Box previewContainer = Box.createVerticalBox();
    	//previewContainer.add(metadataUI);
        JPanel previewPanel = new ScrollablePanel();
        previewPanel.setLayout(new BorderLayout());
        previewPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        previewPanel.add(metadataUI, BorderLayout.NORTH);
        previewPanel.add(navTree, BorderLayout.CENTER);
         
    	JSplitPane leftSplitPane = new JSplitPane();
    	leftSplitPane.setOneTouchExpandable(true);
    	leftSplitPane.setDividerLocation(220);
    	leftSplitPane.setDividerSize(DIVIDER_WIDTH);
    	leftSplitPane.setResizeWeight(0.3);
    	leftSplitPane.setBorder(null);
    	leftSplitPane.setBackground(UIUtilities.BACKGROUND_COLOR);
    	JScrollPane previewScroll = new JScrollPane(previewPanel);
    	previewScroll.setOpaque(true);
    	previewScroll.getViewport().setBackground(UIUtilities.BACKGROUND_COLOR);
    	previewScroll.setBackground(UIUtilities.BACKGROUND_COLOR);
    	leftSplitPane.setLeftComponent(previewScroll);
        
        rightSplitPane = new JSplitPane();
        rightSplitPane.setOneTouchExpandable(true);
        rightSplitPane.setBorder(null);
        //rightSplitPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        rightSplitPane.setResizeWeight(0.75);
        rightSplitPane.setDividerSize(DIVIDER_WIDTH);
        
        // The central component (tab pane)...
        // TODO: Need to split this out into it's own class, that has a
        // single setTreeModel() method, and only instantiates the views 
        // as needed. 
        tabbedPane = new JTabbedPane();
        
        JPanel textTabPane = new JPanel(new BorderLayout());
        textTabPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        JScrollPane textScroller = new JScrollPane(textView);
        textScroller.getViewport().setBackground(UIUtilities.BACKGROUND_COLOR);
        textTabPane.add(textScroller, BorderLayout.CENTER);
        expInfoText = new ExperimentInfoPanel(navTree, controller);
        textTabPane.add(expInfoText, BorderLayout.NORTH);
        
        JPanel treeTabPane = new JPanel(new BorderLayout());
        treeTabPane.setBackground(UIUtilities.BACKGROUND_COLOR);
        JScrollPane treeScroller = new JScrollPane(treeDisplay);
        treeScroller.setBackground(UIUtilities.BACKGROUND_COLOR);
        treeTabPane.add(treeScroller, BorderLayout.CENTER);
        expInfoTree = new ExperimentInfoPanel(navTree, controller);
        treeTabPane.add(expInfoTree, BorderLayout.NORTH);
        
        tabbedPane.addTab(TEXT_VIEW, textTabPane);
        tabbedPane.addTab(TREE_VIEW, treeTabPane);
        // listen for changes to tab-view, to update view-mode in controller
        tabbedPane.addChangeListener(this);
        // goes in the left part of the right splitPane
        rightSplitPane.setLeftComponent(tabbedPane);
        
        editorPanel = new FieldEditorDisplay(navTree, controller);
        editorPanel.setBackground(UIUtilities.BACKGROUND_COLOR);
        rightSplitPane.setRightComponent(editorPanel);
        
        leftSplitPane.setRightComponent(rightSplitPane);
        
        add(leftSplitPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets the visibility of the {@link #editorPanel} and the properties of
     * the {@link rightSplitPane}, in which it is displayed. 
     * 
     * @param visible		True if the editorPanel should be visible.
     */
    private void showFieldEditor(boolean visible) 
    {
    	if (editorPanel != null) {
    		editorPanel.setVisible(visible);
    	}
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
    BrowserUI(Browser browser)
    {
    	if (browser == null) throw new NullPointerException("No browser.");
    	this.browser = browser;
    	browser.addChangeListener(this);	// listen for change in locked
    }
    
    /**
     * Returns a tool-bar for the Browser. 
     * 
     * @return		see above. 
     */
    JComponent getToolBar()
    {
    	return new ToolBar(controller, navTree);
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
    void displayTree()
    {
    	TreeModel tm = model.getTreeModel();
    	
    	navTree.setModel(tm);
    	metadataUI.setTreeModel(tm);
    	
    	// set the Tree Model on both main tab windows...
    	textView.setTreeModel(tm);
    	treeDisplay.setModel(tm);
    	//... and on the Experimental info panels 
    	expInfoText.setTreeModel(tm);
    	expInfoTree.setTreeModel(tm);
    	
    	if (tm != null)
    	tm.addTreeModelListener(editorPanel);
    	
    	// select the root
    	TreePath rootPath = navTree.getPathForRow(0);
    	if (rootPath != null)
    		navTree.setSelectionPath(rootPath);
    	
    	// make sure that the controller is in sync with tabbed pane view.
    	updateViewingMode();
    }
    
    /**
     * The state has changed.
     * Update the editable status of the main tree display. 
     */
    void onStateChanged() {
    	//int state = model.getState();
    	
    	// May want to re-implement this code in future, to toggle editable state.
    	// if the state is editable (not Display mode), enable and show...
    	//boolean editable = (state != Browser.TREE_DISPLAY);
    	//treeDisplay.setEditable(editable);	
    	// showFieldEditor(true);
    	
    	// update editorPanel, to show correct editing view.
    	editorPanel.refreshEditorDisplay();
    	// should also update the tabbed pane to the correct view, 
    	// but, can assume any change in view-mode will have come from the
    	// tabbed-pane, so it will already be in correct view.
    	editorPanel.setId(model.getId());
    }

    /**
     * Implemented as specified by the {@link ChangeListener} interface.
     * Responds to changes in the tabbed pane, and locked/editing mode.
     * 
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e) {
    	if (e.getSource() == null)	return;
    	
    	if (e.getSource().equals(tabbedPane))
    		updateViewingMode();
    	
    	else if (e.getSource().equals(browser)) {
    		// edit tree if file not locked and we are editing experiment
    		boolean editable = ((! browser.isFileLocked()) 
    				&& (browser.getEditingMode() == Browser.EDIT_EXPERIMENT));
    		treeDisplay.setEditable(editable);
    		expInfoTree.refreshEditingMode();
    		expInfoText.refreshEditingMode();
    	}
	}
    
}
