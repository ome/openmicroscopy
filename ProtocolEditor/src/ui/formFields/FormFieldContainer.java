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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import tree.DataFieldNode;

/**
 * This vertical box contains the parent FormField at the top, then the children.
 * (Each child is in it's own <code>FormFieldContainer</code>, with any children below it)
 * @author will
 *
 */
public class FormFieldContainer extends JPanel {

	DataFieldNode parent;
	JToolBar childContainer;
	
	protected static int childLeftIndent = 40;

	
	public FormFieldContainer(DataFieldNode parent) {
		
		this.parent = parent;
		
		setLayout(new BorderLayout());
		//setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		// Handy for debugging layout
		// setBorder(BorderFactory.createLineBorder(Color.red));
		
		childContainer = new JToolBar(JToolBar.VERTICAL);
		//childContainer.setBorder(new EtchedBorder());
		childContainer.setFloatable(false);
		childContainer.setBorder(BorderFactory.createEmptyBorder(0, childLeftIndent, 0, 0));
		
		
		/*
		Dimension min = new Dimension(0,0);
		Dimension pref = new Dimension(0, 0);
		Dimension max = new Dimension(1000,1000);
		add(new Box.Filler(min, pref, max), BorderLayout.SOUTH);
		*/
		
		//System.out.println("FormFieldContainer Constructor FieldName = " + parent.getName());
		
		FormField formField = (FormField)parent.getFormField();
		addParent(formField);
		formField.setChildContainer(childContainer);
		
		add(childContainer, BorderLayout.CENTER);
		
		//add(Box.createVerticalGlue());
		
	}

	
	public JComponent getChildContainer() {
		return childContainer;
	}
	
	public void addParent(Component parent) {
		add(parent, BorderLayout.NORTH);
	}

	
	// override the add() method to add components to the child container
	public void addChild (Component child) {
		
			childContainer.add(child);
		
	}
	
	
	public Component getParentOfRootContainer() {
		if (isRootContainer()) {
			return getParent();
		} else if (getParent() instanceof FormFieldContainer){
			return ((FormFieldContainer)getParent()).getParentOfRootContainer();
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
			int y = getY() + getParent().getY() + ((FormFieldContainer)getParent().getParent()).getYPositionWithinRootContainer();
			return y;
		}
	}
	
	public void collapseAllFormFieldChildrn(boolean collapsed) {
		
		Component[] children = getComponents();
		for (int i=0; i< children.length; i++) {
			if (children[i] instanceof FormField) {
				((FormField)children[i]).setSubStepsCollapsed(collapsed);
			} 
			else if (children[i] instanceof FormFieldContainer) {
				((FormFieldContainer)children[i]).collapseAllFormFieldChildrn(collapsed);
			}
		}
	}
	
	public String toString() {
		if (parent != null) 
			return "FormFieldContainer " + parent.getName();
		else return "";
	}
}
