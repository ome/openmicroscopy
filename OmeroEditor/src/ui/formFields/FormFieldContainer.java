package ui.formFields;

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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;


import tree.DataFieldNode;
import treeModel.fields.FieldPanel;
import ui.IModel;

/**
 * This container lays out a <code>FormField</code> panel and it's child panels.
 * Each child panel will be within another instance of this <code>FormFieldContainer</code> class,
 * in order to display any children of that panel etc. 
 * 
 * Instead of using getParent() and getComponents() to traverse the hierarchy,
 * getParentContainer() is implemented in order to return the parent <code>FormFieldContainer</code>
 * and getChildContainers() returns an array of components that are the child <code>FormFieldContainer</code>s.
 * This means that the layout of the children and parent within the panel can be changed
 * without affecting the tree traversal, as long as getParentContainer()  and
 * getChildContainers() are implemented correctly. 
 * 
 * Note, traversal of the UI tree via this class is intended for UI operations, such as 
 * collapsing or expanding every node, or finding the position of a field for scrolling to view. 
 * 
 * @author will
 *
 */
public class FormFieldContainer extends JPanel {

	DataFieldNode dataFieldNode;
	FieldPanel formField;
	
	JToolBar childContainer;
	
	
	protected static int childLeftIndent = 40;

	
	public FormFieldContainer(DataFieldNode dataFieldNode, IModel model) {
		
		this.dataFieldNode = dataFieldNode;
		
		this.setBackground(null);
		setLayout(new BorderLayout());
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Handy for debugging layout
		// setBorder(BorderFactory.createLineBorder(Color.red));
		
		
		formField = (FieldPanel)dataFieldNode.getFormField();
		formField.setModel(model);
		add(formField, BorderLayout.NORTH);
		
		
		childContainer = new JToolBar(JToolBar.VERTICAL);
		//childContainer.setBorder(new EtchedBorder());
		childContainer.setBackground(null);
		childContainer.setFloatable(false);
		childContainer.setBorder(BorderFactory.createEmptyBorder(0, childLeftIndent, 0, 0));
	
		// formField manages visibility of it's children, and lazy loading, via a reference to childContainer.
		formField.setChildContainer(childContainer);
		
		JPanel nicerLayoutPanel = new JPanel(new BorderLayout());
		nicerLayoutPanel.setBackground(null);
		nicerLayoutPanel.add(childContainer, BorderLayout.NORTH);
		
		add(nicerLayoutPanel, BorderLayout.CENTER);
		
		//add(Box.createVerticalGlue());
		
	}

	
	public JComponent getChildContainer() {
		return childContainer;
	}

	
	// override the add() method to add components to the child container
	public void addChild (Component child) {
		
			childContainer.add(child);
		
	}
	
	/**
	 *  This method needs to be implemented wrt the number of containers within each "level"
	 *  of the UI hierarchy. 
	 */
	// getParent() = toolBar -> getParent() = layoutPanel -> getParent() = FormFieldContainer!
	public FormFieldContainer getParentContainer() {
		if (getParent().getParent().getParent() instanceof FormFieldContainer)
			return (FormFieldContainer)getParent().getParent().getParent();
		else return null;
	}
	
	/**
	 * This method needs to return an array of child containers
	 * that are instances of this <code>FormFieldContainer</code> class. 
	 * @return
	 */
	public Component[] getChildContainers() {
		return childContainer.getComponents();
	}
	
	
	public Component getParentOfRootContainer() {
		if (isRootContainer()) {
			return getParent();
		} else if (getParentContainer() != null){
			return getParentContainer().getParentOfRootContainer();
		} else
			return null;
	}
	
	public boolean isRootContainer() {
		//System.out.println("FormFieldContainer  isRootContainer() parent = " + getParent());
		return (!(getParent() instanceof JToolBar));
	}
	
	public int getYPositionWithinRootContainer() {
		if (isRootContainer()) {
			return getY();
		} else {
			/* The position of this component within a childBox, within parent of the same class is..
			 * the getY() (within childBox) + childBox.getY() (within FormFieldContainer) +
			 * the recursive getY() of the FormFieldContainer...
			 */
			int y = getY() + getParent().getParent().getY() + (getParentContainer()).getYPositionWithinRootContainer();
			return y;
		}
	}
	
	/**
	 * Expands or collapses the whole tree, starting at this root.
	 * Calls setSubStepsCollapsed() on the <code>formField</code> of this level,
	 * then delegates to all the child <code>FormFieldContainers</code>.
	 * 
	 * @param collapsed		True if the children are collapsed (hidden);
	 */
	public void collapseAllFormFieldChildren(boolean collapsed) {
		
		formField.setSubStepsCollapsed(collapsed);
		
		Component[] children = getChildContainers();
		for (int i=0; i< children.length; i++) {
			if (children[i] instanceof FormFieldContainer) {
				((FormFieldContainer)children[i]).collapseAllFormFieldChildren(collapsed);
			}
		}
	}
	
	public String toString() {
		if (dataFieldNode != null) 
			return "FormFieldContainer " + dataFieldNode.getName();
		else return "";
	}
}
