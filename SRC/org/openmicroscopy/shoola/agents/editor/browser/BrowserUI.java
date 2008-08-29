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
import javax.swing.JTree;
import javax.swing.ToolTipManager;

//Third-party libraries

//Application-internal dependencies

/** 
 * The UI for the Browser (the View of the Browser MVC).
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
{

	/** The tree hosting the display. */
    private JTree           		treeDisplay;
    
    /**
     * An outline view of the Tree, displayed on the left, used
     * to navigate
     */
    private JTree					treeOutline;
    
    /** The Controller. */
    private BrowserControl  		controller;
    
    /** The model. */
    private BrowserModel    		model;
    
    /** The component hosting the tree. */
    private JScrollPane             scrollPane;
    
    /**
     * Initialises the JTrees for this UI.
     */
    private void createTrees() 
    {
    	treeDisplay = new EditableTree(controller);
    	
    	int state = model.getState();
    	if (state == Browser.TREE_EDIT)
    		treeDisplay.setEditable(true);
    	
		treeOutline = new NavTree(treeDisplay);
		ToolTipManager.sharedInstance().registerComponent(treeDisplay);
		ToolTipManager.sharedInstance().registerComponent(treeOutline);
    }
    
    /**
     * Builds the UI for this Panel
     */
    private void buildUI() 
    {
    	setLayout(new BorderLayout(0, 0));

    	JSplitPane splitPane = new JSplitPane();
    	splitPane.setOneTouchExpandable(true);
    	splitPane.setDividerLocation(250);
    	splitPane.setBorder(null);
    	
        splitPane.setLeftComponent(new JScrollPane(treeOutline));
        
        scrollPane = new JScrollPane(treeDisplay);
        splitPane.setRightComponent(scrollPane);
        
        add(splitPane, BorderLayout.CENTER);
        
        add(new ToolBar(controller), BorderLayout.NORTH);
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
    }
    
    /**
     * Sets the model of the JTrees with the treeModel from the BrowserModel. 
     */
    void displayTree() {
    	treeDisplay.setModel(model.getTreeModel());
    	treeOutline.setModel(model.getTreeModel());
    }
    
    /**
     * The state has changed.
     * Update the editable status of the main tree display. 
     */
    void onStateChanged() {
    	int state = model.getState();
    	treeDisplay.setEditable(state == Browser.TREE_EDIT);	
    }
}
