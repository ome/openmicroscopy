 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserModel 
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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.openmicroscopy.shoola.agents.editor.model.CPEimport;
import org.openmicroscopy.shoola.agents.editor.model.ExperimentInfo;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;

//Third-party libraries

//Application-internal dependencies

/** 
 *	The Browser model. 
 * The data is stored in a treeModel.  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class BrowserModel
{
    
	/** Is the file locked? */
	private boolean				fileLocked;
	
	/** Either {@link Browser#TREE_EDITED} or {@link Browser#TREE_SAVED} */
	private int					savedState;
	
	/**
	 * If the file is saved to the server, this ID will be the ID of the
	 * original file on the server (not the file annotation).
	 * The Editor manages file saving etc and this ID is purely for display
	 * purposes in the Browser. 
	 */
	private long 				id;
	
    /** Reference to the component that embeds this model. */
    protected Browser           component;

    /** The model is delegated to a treeModel. */
    private TreeModel 			treeModel;
    
    /** The type of browser. */
    private int 				type;
    
    
    /**
     * Creates an instance. 
     * 
     * @param state	The editing mode of the browser. 
     * 				Either {@link Browser#EDIT_EXPERIMENT} or 
     * 				{@link Browser#EDIT_PROTOCOL}
     * @param type 
     */
    BrowserModel(int state, int type) 
    {
    	setEditingMode(state);
    	this.savedState = Browser.TREE_SAVED;
    	this.type = type;
    }
    
    /**
     * Called by the <code>Browser</code> after creation to allow this
     * object to store a back reference to the embedding component.
     * 
     * @param component The embedding component.
     */
    void initialize(Browser component) { this.component = component; }
    
    /**
     * Returns a reference to the treeModel
     * 
     * @return	A reference to the treeModel.
     */
    TreeModel getTreeModel() { return treeModel; }
    
    /**
     * Sets the treeModel.
     * 
     * @param model		the new TreeModel.
     */
    void setTreeModel(TreeModel model) 
    {
    	this.treeModel = model;
    	// if the model is an experiment, file is locked by default. 
    	if (isModelExperiment()) {
    		setEditingMode(Browser.EDIT_EXPERIMENT);
    		// fileLocked = true;
    	}
    }
    
    /**
	 * Returns the current state. Either {@link Browser#EDIT_EXPERIMENT} or 
     * {@link Browser#EDIT_PROTOCOL}. 
     * If we are not currently editing an Experiment, this will return 
     * {@link Browser#EDIT_PROTOCOL}. Otherwise, this method delegates to 
     * the {@link ExperimentInfo}.
	 * 
	 * @return see above
	 */
	int getEditingMode() { 
		
		if (isModelExperiment()) {
			if ("true".equals(ExperimentInfo.getExpInfo(treeModel).
					getAttribute(ExperimentInfo.EDIT_PROTOCOL))) 
					return Browser.EDIT_PROTOCOL;
			else return Browser.EDIT_EXPERIMENT;
		}
			
		return Browser.EDIT_PROTOCOL;
	}
	
	/**
	 * Returns the current saved state.
	 * 
	 * @return One of the flags defined by the {@link Browser} interface.  
	 */
	int getSavedState() { return savedState; }  
	
	/**
     * Sets the Edited state of the Browser.
     * 
     * @param edited Pass to <code>true</code> to indicate that the file is 
     * 				 edited, <code>false</code> otherwise. 
     */
    void setEdited(boolean edited)
    {
    	
    	if (edited)  savedState = Browser.TREE_EDITED;
    	else savedState = Browser.TREE_SAVED;
    	
    	// notify listeners of changes to the model 
    	// when file saved (eg Exp-Info last-saved date)
    	if (!edited) {
    		DefaultTreeModel d = ((DefaultTreeModel) treeModel);
    		d.nodeChanged((TreeNode) d.getRoot());
    	}
    }
    
    /**
     * Allows the file to be locked to prevent editing. 
     * @param locked
     */
    void setFileLocked(boolean locked) 
    {
    	fileLocked = locked;
    }
    
    /**
     * Returns true if the file is locked. 
     * NB This functionality is not currently used. 
     * 
     * @return	see above. 
     */
    boolean isFileLocked()	{ return fileLocked; }
    
    /**
     * Sets the editing mode. Either
     * {@link Browser#EDIT_EXPERIMENT} or {@link Browser#EDIT_PROTOCOL}
     * @param mode	see above
     */
    void setEditingMode(int mode) 
    {
    	if (isModelExperiment()) {
    		if (mode == Browser.EDIT_PROTOCOL) {
    			ExperimentInfo.getExpInfo(treeModel).setAttribute
    				(ExperimentInfo.EDIT_PROTOCOL, "true");
    		} else {
    			ExperimentInfo.getExpInfo(treeModel).setAttribute
					(ExperimentInfo.EDIT_PROTOCOL, "false");
    		}
    	}
    }
    
    /**
     * Determines whether a TreeModel of a protocol
	 * contains an Experimental Info object at the root node, thereby 
	 * defining it as an Experiment. 
	 * 
     * @return	see above. 
     */
    boolean isModelExperiment() 
    {
    	return ExperimentInfo.isModelExperiment(treeModel);
    }
    
    /**
     * Gets the Date that the file was last saved (archive date). 
     * @return		see above. 
     */
    Date getLastSavedDate() 
    {
    	DefaultMutableTreeNode root = (DefaultMutableTreeNode)
    														treeModel.getRoot();
    	IAttributes rootField = ((IAttributes)root.getUserObject());
    	String archiveUTC = rootField.getAttribute(CPEimport.ARCHIVE_DATE);
    	try {
    		long millis = new Long(archiveUTC);
    		Date d = new Date(millis);
    		return d;
    	} catch (NumberFormatException nfe) {
    		return null;
    	}
    }
    
    /**
     * Sets the ID so that it can be displayed in the browser.
     * 
     * @param id The value to set.
     */
    void setId(long id) { this.id = id; }
    
    /**
     * Gets the ID for display. Not used for file saving etc (handled by Editor)
     * 
     * @return		see above. 
     */
    long getId() {	return id; }
}
