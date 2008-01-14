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

package ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;

import tree.DataFieldNode;

import java.awt.BorderLayout;
import java.util.ArrayList;

// this panel displays the hierarchical tree, made from (FormField) JPanels
// uses recursive buildFormTree() method, indenting children each time

public class FormDisplay extends JPanel {
	
	private XMLView parentXMLView;
	
	int childLeftIndent = 40;
	
	Box verticalFormBox;
	
	DataFieldNode rootNode;
	
	FormDisplay(XMLView parent) {
		
		parentXMLView = parent;
		rootNode = parentXMLView.getRootNode();
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		verticalFormBox = Box.createVerticalBox();
		this.add(verticalFormBox, BorderLayout.NORTH);
				
		// get the formField JPanel from the dataField
		if (rootNode != null) {
			
			JPanel newFormField = parentXMLView.getRootNode().getFormField();
			verticalFormBox.add(newFormField);
			
			// pass the node and the Box that already contains it to buildFormTree()
			// this will get the nodes children and add them to the Box (within a new Box)
			buildFormTree(parentXMLView.getRootNode(), verticalFormBox);
		}
	}
	
	FormDisplay(DataFieldNode rootNode) {
		
		this.rootNode = rootNode;
		
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		verticalFormBox = Box.createVerticalBox();
		this.add(verticalFormBox, BorderLayout.NORTH);
				
		// get the formField JPanel from the dataField
		if (rootNode != null) {
			JPanel newFormField = rootNode.getFormField();
			verticalFormBox.add(newFormField);
			
			// pass the node and the Box that already contains it to buildFormTree()
			// this will get the nodes children and add them to the Box (within a new Box)
			buildFormTree(rootNode, verticalFormBox);
		}
	}
	
//	 this will get the node's children and add them to the Box (within a new Box)
	public void buildFormTree(DataFieldNode dfNode, Box verticalBox) {
		
		ArrayList<DataFieldNode> children = dfNode.getChildren();
		
		Box childBox = Box.createVerticalBox();
		childBox.setBorder(BorderFactory.createEmptyBorder(0, childLeftIndent, 0, 0));
		// the node gets a ref to the Box (used for collapsing. Box becomes hidden)
		dfNode.setChildBox(childBox);
		//	set visibility of the childBox wrt collapsed boolean of dataField
		//	 & sets collapse button visible if dataFieldNode has children
		dfNode.getDataField().refreshTitleCollapsed();
		
		System.out.println("FormDisplay: buildFormTree() " + dfNode.getDataField().getName());
	
		// for each child, get their JPanel, add it to the childBox
		for (DataFieldNode child: children){
			JPanel newFormField = child.getFormField();
			childBox.add(newFormField);
			// recursively build the tree below each child
			buildFormTree(child, childBox);
		}
		// add the new childBox to it's parent
		verticalBox.add(childBox);
	}
	
	public void refreshForm() {
		// update reference to the root
		rootNode = parentXMLView.getRootNode();
		
		refreshUI();
		
	}
	
	public void refreshForm(DataFieldNode rootNode) {
		
		this.rootNode = rootNode;
		
		refreshUI();
	}
	
	
	public void refreshUI() {
		
		if (rootNode == null) return;
		
		verticalFormBox.setVisible(false);	// otherwise if the new form is smaller, old one still visible
		
		this.remove(verticalFormBox);
		
		verticalFormBox = Box.createVerticalBox();
		
		JPanel newFormField = rootNode.getDataField().getFormField();
		verticalFormBox.add(newFormField);
		
		buildFormTree(rootNode, verticalFormBox);
		
		this.add(verticalFormBox, BorderLayout.NORTH);
		this.getParent().getParent().validate();
		this.invalidate();
		this.repaint();		
	}
	
}

