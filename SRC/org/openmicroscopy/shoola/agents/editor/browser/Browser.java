 /*
 * org.openmicroscopy.shoola.agents.editor.browser.Browser 
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
import java.util.Date;

import javax.swing.JComponent;
import javax.swing.tree.TreeModel;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.component.ObservableComponent;


/** 
 * The public interface for the browser component
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public interface Browser
	extends ObservableComponent
{
	
	/** Indicates to create a blank protocol. */
	public static final int 	PROTOCOL = 100;
	
	/** Indicates to create a blank experiment. */
	public static final int 	EXPERIMENT = 101;
	
	/** 
	 * Bound property indicating that the edit mode of the browser has
	 * changed.
	 */
	public static final String BROWSER_EDIT_PROPERTY = "browserEdit";

	/**
	 * A Flag to denote the <i>Editing</i> mode.
	 * Specifies that the protocol is editable (and Experimental values
	 * may or may not be editable). 
	 */
	public static final int EDIT_PROTOCOL = 1;
	
	/**
	 * A Flag to denote the <i>Editing</i> mode.
	 * Specifies that the experiment is editable (and Protocol 
	 * should not be editable). 
	 */
	public static final int EDIT_EXPERIMENT = 2;
	
	/**
	 * A Flag to denote that the tree is editable, and is currently in 
	 * the <i>Saved</i> state.
	 * This specifies that the UI should be for tree editing, not
	 * simply display.
	 */
	public static final int TREE_SAVED = 3;
	
	/**
	 * This state indicates that the tree has been edited.
	 * E.g. users will be asked if they want to save before quitting. 
	 */
	public static final int TREE_EDITED = 4;
	
	
	
	
    /**
     * Sets a new treeModel for the browser. 
     * 
     * @param model		The new treeModel.
     */
    public void setTreeModel(TreeModel model);
    
    /**
     * Gets the treeModel for the browser. 
     * 
     * @return TreeModel		see above.
     */
    public TreeModel getTreeModel();
    
    /** 
     * Returns the UI component, not including the tool-bar. 
     * Allows UI and tool-bar to be independently placed in the Editor UI. 
     * @see #getToolBar();
     * 
     * @return See above.
     */
    public JComponent getUI();
    
    /**
     * Returns the tool-bar for the browser. 
     * 
     * @return	See above. 
     */
    public JComponent getToolBar();
   
    /**
     * Sets the Edited state of the Browser.
     * 
     * @param editable		Set to true if the file has been edited. 
     */
    public void setEdited(boolean editable);
    
    /**
     * Sets the Editing mode of the Browser. Either
	 * {@link #EDIT_EXPERIMENT} or {@link #EDIT_PROTOCOL}
     */
	public void setEditingMode(int editingMode);
    
    /**
     * Sets the ID of the original file (on server) that is currently being
     * displayed. Allows the Browser to display this ID. 
     * 
     * @param id		see above.
     */
    public void setId(long id);
    
    /**
	 * Queries the current editing mode. Returns {@link #FILE_LOCKED}, 
	 * {@link #EDIT_EXPERIMENT} or {@link #EDIT_PROTOCOL}
	 * 
	 * @return see above.
	 */
	public int getEditingMode();
	
	/**
	 * Queries the current saved state. 
	 * Either {@link #TREE_EDITED} or {@link #TREE_SAVED}
	 * 
	 * @return One of the flags above
	 */
	public int getSavedState();
	
	/**
	 * Returns true if we are currently editing an Experiment, otherwise 
	 * (editing a Protocol) returns false;
	 * 
	 * @return		see above.
	 */
	public boolean isModelExperiment();
	
	/**
	 * Date the file was last saved. (or null if not). 
	 * @return	see above.
	 */
	public Date getLastSavedDate();
	
	/**
     * Allows the file to be locked to prevent editing. 
     * @param locked
     */
    public void setFileLocked(boolean locked);
    
    /**
     * Returns true if the file is locked. 
     * @return	see above. 
     */
    public boolean isFileLocked();
    
    /**
	 * Deletes the ExperimentInfo and any Step Notes. 
	 * Adds this edit to the undo/redo queue. 
	 */
	public void deleteExperimentInfo();

}
