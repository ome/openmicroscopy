/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */


package actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import ui.AbstractComponent;
import ui.Controller;
import ui.IModel;

/**
 * This is the superclass of all OMERO.editor Actions. 
 * It has a reference to an implementation of IModel, to which subclasses delegate most actions.
 * If the model is also an instance of the Observable <code>AbstractComponent</code>, 
 * then this Action class registers itself as a changeListener.
 * Action subclasses then override stateChanged() to disable themselves, change their text etc, 
 * depending on the state of model. 
 * 
 * @author will
 *
 */
public class ProtocolEditorAction 
	extends AbstractAction 
	implements ChangeListener{

	protected IModel model;
	//protected AbstractComponent publisher;
	
	// might use this later if this class knows about view
	protected JFrame frame = null;
	
	public ProtocolEditorAction(IModel model) {
		this.model = model;
		if (model instanceof AbstractComponent) {
			((AbstractComponent)model).addChangeListener(this);
		}
	}
	
	/**
	 * This method is used by many Actions that cause fields to be edited, to set their enabled state. 
	 * These Actions should be disabled if no file is open.
	 * They should also be disabled if the currently highlighted field or 
	 * any of its ancestors are locked.
	 *  
	 * @return		true if a file is open and the currently highlighted fields and their ancestors are unlocked
	 */
	public boolean fieldTemplatesEditable() {
		
		// if no files open, action is disabled.
		if (!filesOpen()) {
			return false;
		}
		
		// if files are open, check to see if highlighted fields are locked
		else {
			String lockLevel = model.getMaxHighlightedLockingLevel();
			if (lockLevel == null) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * This method is used by any Actions that edit field values, to set their enabled state. 
	 * Highlighted fields' values are editable as long as a file is open, and the 
	 * highlighted fields are not locked OR template-locked,
	 *  
	 * @return		true if a file is open and the currently highlighted fields are not fully 
	 */
	public boolean fieldValuesEditable() {
		
		// if no files open, action is disabled.
		if (!filesOpen()) {
			return false;
		}
		
		// if files are open, check to see if highlighted fields are locked
		else {
			String lockLevel = model.getMaxHighlightedLockingLevel();
			if ((lockLevel == null) || (lockLevel.equals(DataFieldConstants.LOCKED_TEMPLATE))){
				return true;
			} else {
				return false;
			}
		}
	}
	
	/**
	 * This method used to check whether any files are open. 
	 * @return	true if at least one file is open. 
	 */
	public boolean filesOpen() {
		String[] fileList = model.getOpenFileList();
		return (fileList.length > 0);
	}
	
	/**
	 * Reacts to changes in the model
	 * subclasses override this method
	 */
	public void stateChanged(ChangeEvent e) {}

	
    /** 
     * Subclasses should implement the method.
     * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
     */
	public void actionPerformed(ActionEvent e) {}

}
