 /*
 * treeModel.TreeEditorComponent 
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
package treeModel;

import java.io.File;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import treeIO.TreeModelFactory;
import treeModel.view.TreeEditorUI;

//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
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
public class TreeEditorComponent
	implements ITreeEditor {
	
	private TreeEditorModel model;
	
	private TreeEditorUI view;
	
	private TreeEditorControl controller;
	
	
	public TreeEditorComponent(TreeEditorModel model) {
		
		this.model = model;
		
		view = new TreeEditorUI();
		
		controller = new TreeEditorControl();
	}
	
	public void initialise() {
		controller.initialise(this, view);
		
		view.initialise(model, controller);
	}


	/**
	 * Returns the complete UI, including toolbar, editable tree-view and 
	 * field editor panel. 
	 */
	public JComponent getUI() {
		
		return view.getUI();
	}
	
	/**
	 * Returns a view of the Tree, in a scrollPane.
	 * This is a non-editable view of the data in a tree. 
	 * 
	 * @return		Custom JTree in a scrollpane. 
	 */
	public JComponent getTreeView() {
		return view.getTreeUI(TreeEditorUI.NON_EDITABLE_TREE);
	}

	/**
	 * NOT TESTED YET!
	 */
	/*
	 * TODO	test this etc. 
	 */
	public void openFile(File xmlFile) {
		
		TreeModel treeModel = TreeModelFactory.getTree(xmlFile);
		
		model.setRoot((TreeNode)treeModel.getRoot());
	}

	
	public Action getAction(int actionIndex) {
		
		return controller.getAction(actionIndex);
	}

}
