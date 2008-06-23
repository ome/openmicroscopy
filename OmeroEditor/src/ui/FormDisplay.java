/*
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
 *	author Will Moore will@lifesci.dundee.ac.uk
 */

package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.DataFieldConstants;
import tree.DataFieldNode;
import ui.formFields.FormField;
import ui.formFields.FormFieldContainer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.ArrayList;

// this panel displays the hierarchical tree, made from (FormField) JPanels
// uses recursive buildFormTree() method, indenting children each time

public class FormDisplay 
	extends JPanel 
	implements ChangeListener {
	
	private IModel model;
	
	FormFieldContainer verticalFormBox;
	
	DataFieldNode rootNode;
	
	FormDisplay(IModel model) {
		
		this.model = model;
		rootNode = model.getRootNode();
		
		if (model instanceof AbstractComponent) {
			((AbstractComponent)model).addChangeListener(this);
		}
		
		initialise();
		
		initTreeBuild();
	}
	
	
	
	FormDisplay(DataFieldNode rootNode) {
		
		this.rootNode = rootNode;
		
		initialise();
				
		initTreeBuild();
	}
	
	public void initialise() {
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setBackground(Color.white);
	}
	
	public void initTreeBuild() {
		// get the formField JPanel from the dataField
		if (rootNode != null) {
			
			verticalFormBox = new FormFieldContainer(rootNode, model);
			this.add(verticalFormBox, BorderLayout.NORTH);
			
			
			FormField formField = (FormField)rootNode.getFormField();
			formField.setModel(model);
			formField.refreshRootField(true);	// displays the correct buttons etc. 
		
			// pass the node and the Box that already contains it to buildFormTree()
			// this will get the nodes children and add them to the Box (within a new Box)
			buildFormTree(rootNode, verticalFormBox, model);
		}
	}
	
//	 this will get the node's children and add them to the Box (within a new Box)
	// the Panel of dfNode has already been added at the top of verticalBox
	public static void buildFormTree(DataFieldNode dfNode, FormFieldContainer verticalBox, IModel model) {
		
		ArrayList<DataFieldNode> children = dfNode.getChildren();
		
		//System.out.println("FormDisplay: buildFormTree() " + dfNode.getDataField().getName());
		
		boolean subStepsCollapsed = ((FormField)dfNode.getFormField()).subStepsCollapsed();
		
		if (!subStepsCollapsed) {
			// add the children to the childBox - this will recursively build tree for each
			showChildren(children, verticalBox.getChildContainer(), model);
		}
		
		//		set visibility of the childBox wrt collapsed boolean of dataField
		//	 & sets collapse button visible if dataFieldNode has children
		((FormField)dfNode.getFormField()).refreshTitleCollapsed();
		
	}
	
	public static void showChildren(ArrayList<DataFieldNode> children, Container childBox, IModel model) {
		
		//System.out.println("	showChildren()");
		
		// for each child, get their JPanel, add it to the childBox
		for (DataFieldNode child: children){
			
			FormFieldContainer childContainer = new FormFieldContainer(child, model);
			
			childBox.add(childContainer);
			// recursively build the tree below each child
			buildFormTree(child, childContainer, model);
		}
		//childBox.add(Box.createVerticalGlue());
		/*
		 * Trying to get a nice horizontal-children layout.
		 * But this doesn't help! 
		JPanel filler = new JPanel();
		filler.setBorder(BorderFactory.createLineBorder(Color.red));	// border for bug-fixing
		childBox.add(filler);
		*/
	}
	
	public void refreshTree() {
		// update reference to the root
		rootNode = model.getRootNode();
		
		refreshUI();
		
	}
	
	public void refreshForm(DataFieldNode rootNode) {
		
		this.rootNode = rootNode;
		
		refreshUI();
	}
	
	
	public void refreshUI() {
		
		if (rootNode == null) return;
		
		if (verticalFormBox != null) {
			verticalFormBox.setVisible(false);	// otherwise if the new form is smaller, old one still visible
		
			this.remove(verticalFormBox);
		}
		
		initTreeBuild();
		
		this.getParent().getParent().validate();
		this.invalidate();
		this.repaint();		
	}

	public void stateChanged(ChangeEvent e) {
		if (model.treeNeedsRefreshing()) {
			refreshTree();
		}
	}
	
}

