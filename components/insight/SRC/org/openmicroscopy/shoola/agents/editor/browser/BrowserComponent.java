 /*
 * org.openmicroscopy.shoola.agents.editor.browser.BrowserComponent 
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

import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

/**  
 * Implements the {@link Browser} interface to provide the functionality
 * required of the editor browser component.
 * This class is the component hub and embeds the component's MVC triad.
 * It delegates actual functionality to the
 * MVC sub-components.
 *
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BrowserComponent 
	extends AbstractComponent
	implements Browser
{
	
	/** The Model sub-component. */
    private BrowserModel    	model;
    
    /** The View sub-component. */
    private BrowserUI       	view;
    
    /** The Controller sub-component. */
    private BrowserControl  	controller;
    
    /**
     * Creates an instance. 
     * Also initialises the controller and the view. 
     * 
     * @param model				The model of the MVC
     * @param viewingMode		A string to define the view/ edit mode of the UI
     */
    BrowserComponent(BrowserModel model) 
    {
    	this.model = model;
    	controller = new BrowserControl(this);
    	// Browser ref allows UI components to listen for changes to browser
        view = new BrowserUI(this);		
    }
    
    /** 
	 * Links up the MVC triad. Called by BrowserFactory.
	 */
	void initialize()
	{
	    model.initialize(this);
	    controller.initialize(view);
	    view.initialize(controller, model);
	}

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setTreeModel(TreeModel treeModel) 
     */
	public void setTreeModel(TreeModel treeModel) 
    {
    	model.setTreeModel(treeModel);
    	view.displayTree();
    }
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getTreeModel() 
     */
	public TreeModel getTreeModel() 
    {
    	return model.getTreeModel();
    }

	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getUI()
     */
	public JComponent getUI() { return view; }
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#getToolBar()
     */
	public JComponent getToolBar() { return view.getToolBar(); }
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setEdited(boolean)
     */
	public void setEdited(boolean edited) 
	{
		model.setEdited(edited);
		// fireStateChange();
		firePropertyChange(BROWSER_EDIT_PROPERTY, -1, model.getSavedState());
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setEditingMode(int)
     */
	public void setEditingMode(int editingMode) 
	{
		model.setEditingMode(editingMode);
		fireStateChange();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
     * Allows the file to be locked to prevent editing. 
     * @param locked
     */
    public void setFileLocked(boolean locked) 
    {
    	model.setFileLocked(locked);
    	fireStateChange();	// update Actions etc. 
    }
    
    /**
     * Implemented as specified by the {@link Browser} interface.
     * Returns true if the file is locked. 
     * @return	see above. 
     */
    public boolean isFileLocked()	{ return model.isFileLocked(); }
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#isModelExperiment()
     */
	public boolean isModelExperiment()
	{
		return model.isModelExperiment();
	}
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * @return	Date the file was last saved. (or null if not). 
	 */
	public Date getLastSavedDate()
	{
		return model.getLastSavedDate();
	}
	
	/**
     * Implemented as specified by the {@link Browser} interface.
     * @see Browser#setId(long)
     */
	public void setId(long id)
	{
		model.setId(id);
		// this is not really a state change. But the Browser doesn't really
		// have states, and this will simply update the UI
		fireStateChange();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getEditingMode()
	 */
	public int getEditingMode() { return model.getEditingMode(); }
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Browser#getEditingMode()
	 */
	public int getSavedState() { return model.getSavedState(); }
	
	/**
	 * Implemented as specified by the {@link Browser} interface.
	 * Deletes the ExperimentInfo and any Step Notes. 
	 * Adds this edit to the undo/redo queue. 
	 * @see Browser#deleteExperimentInfo()
	 */
	public void deleteExperimentInfo() 
	{
		controller.deleteExperimentInfo(model.getTreeModel());
	}
	
}
