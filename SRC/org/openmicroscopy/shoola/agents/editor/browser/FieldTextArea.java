 /*
 * org.openmicroscopy.shoola.agents.editor.browser.FieldTextArea 
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

import java.awt.Color;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.RunElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamEditorDialog;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/** 
 * This Text Area is represents a "Field" (or a node) of the data model tree,
 * when it is displayed in the "Text Document" view (rather than a JTree view).
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class FieldTextArea 
	extends JEditorPane 
	implements FocusListener,
	CaretListener,
	PropertyChangeListener
{
	
	/** The field that is represented by this UI component */
	private IField 					field;

	/** 
	 * The JTree that is coordinates selection of fields etc.
	 * When undoable edits are committed to the undo/redo queue, they include
	 * a reference to this JTree, so that they can select the correct node
	 * when undo is performed. 
	 */
	private JTree	 				navTree;
	
	/** 
	 * The treeNode that this Text Area represents. 
	 * When undoable edits are committed to the undo/redo queue, they include
	 * a reference to this node, so that they can select the correct node
	 * when undo is performed. 
	 */
	private DefaultMutableTreeNode 	treeNode;
	
	/**
	 * Controller for editing actions etc. 
	 */
	private BrowserControl 			controller;
	
	/**
	 * A dialog for displaying a pop-up edit of a parameter.
	 */
	private JDialog					paramEditDialog;
	
	/** A border to illustrate when this field is selected */
	private Border 					selectedBorder;
	
	/** A border to illustrate when this field is unselected */
	private Border 					unselectedBorder;
	
	/**
	 * Initialises UI components. 
	 */
	private void initialise()
	{
		Border emptyBorder = new EmptyBorder(7,7,7,7);
		Border lb = BorderFactory.createLineBorder(UIUtilities.LIGHT_GREY);
		selectedBorder = BorderFactory.createCompoundBorder(lb, emptyBorder);
		lb = BorderFactory.createLineBorder(Color.white);
		unselectedBorder = BorderFactory.createCompoundBorder(lb, emptyBorder);
	}
	
	/**
	 * Builds the UI
	 */
	private void buildUI()
	{
		setBorder(unselectedBorder);
		setEditable(true);
		setBackground(null);
		addFocusListener(this);
		
		//refreshField();
		
		String text = field.toHtmlString();
		setText(text);
		addCaretListener(this);
	}
	
	private void showParamDialog(int index, Point point) 
	{
		if ((paramEditDialog != null) && (paramEditDialog.isVisible())) {
			paramEditDialog.setVisible(false);
			paramEditDialog.dispose();
		}
			
		IFieldContent content = field.getContentAt(index);
		if (content instanceof IParam) {
			IParam param = (IParam)content;
			paramEditDialog = new ParamEditorDialog(param, point, this);
			paramEditDialog.setVisible(true);
		}
	}
	
	/**
	 * Returns true if the {@link #treeNode} is selected in the {@link #navTree}.
	 * 
	 * @return		see above.
	 */
	private boolean isFieldSelected() 
	{
		if (treeNode == null) return false;
		if (navTree == null) return false;
		
		TreePath path = new TreePath(treeNode.getPath());
		return navTree.isPathSelected(path);
	}

	/**
	 * Creates an instance of this class. 
	 * 
	 * @param field			The field used to build this UI
	 * @param tree			For undo/redo selection see {@link #navTree}
	 * @param treeNode		For undo/redo selection see {@link #treeNode}
	 * @param controller	For managing edits
	 */
	FieldTextArea(IField field, JTree tree, DefaultMutableTreeNode treeNode,
			BrowserControl controller) 
	{
		super("text/html", "");
		
		this.field = field;
		this.navTree = tree;
		this.treeNode = treeNode;
		this.controller = controller;
		
		initialise();
		buildUI();
	}
	
	/**
	 * Refreshes the text displayed, according to the {@link #field}.
	 * This is called by the parent UI when a tree-nodes-changed event 
	 * or a tree-selection-event is received. 
	 * 
	 * If the field is selected, the text is updated from the field. 
	 * Don't update unselected fields, otherwise all fields are updated 
	 * and the scroll-pane displaying them will scroll to the last field. 
	 * If only the selected field is updated, this field will also be 
	 * scrolled to be visible.
	 * 
	 * Also updates the border to correspond to the selection state and
	 * if the field is not selected, the {@link #paramEditDialog} is disposed. 
	 */
	public void refreshField()
	{
		boolean selected = isFieldSelected();
		
		if (selected) {
			String text = field.toHtmlString();
			setText(text);
		}
		
		setBorder(selected ? selectedBorder : unselectedBorder);
		
		if ((! selected) && (paramEditDialog != null)) {
			paramEditDialog.dispose();
		}
	}
	
	/**
	 * Implemented as specified by the {@link FocusListener} interface.
	 * Sets a selected border, and attempts to select the corresponding
	 * path in the navigation {@link JTree}
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		setBorder(selectedBorder);
		
		if (treeNode == null) return;
		TreePath path = new TreePath(treeNode.getPath());
		if (navTree != null)
		navTree.setSelectionPath(path);
	}

	/**
	 * Implemented as specified by the {@link FocusListener} interface.
	 * Sets an unselected border.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		setBorder(unselectedBorder);
	}
	
	/**
	 * Implemented as specified by the {@link CaretListener} interface.
	 * If the caret is placed in a "a" tag, {@link #showParamDialog(int, Point)}
	 * is called, to edit the parameter. 
	 * 
	 * @see CaretListener#caretUpdate(CaretEvent)
	 */
	public void caretUpdate(CaretEvent e) 
	{
		if (! isVisible()) return;
		
		int dot = e.getDot();
		// relative location of mouse (within textPane), and location of textPane
		
		Point mouseLoc = getMousePosition(); 
		Point paneLoc = getLocationOnScreen();
		// give the exact location of the mouse
		if (mouseLoc != null)
			paneLoc.translate((int)mouseLoc.getX(), (int)mouseLoc.getY());
        
    	StyledDocument styledDoc = (StyledDocument)getDocument();
    	Element el = styledDoc.getCharacterElement(dot);
    	
    	if (el instanceof RunElement) {
    		RunElement rE = (RunElement)el;
    		Object a = rE.getAttribute(Tag.A);
    		if (a != null) {
    			if (a instanceof SimpleAttributeSet) {
    				SimpleAttributeSet sas = (SimpleAttributeSet)a;
    				String href = sas.getAttribute(HTML.Attribute.HREF).toString();
    				
    				showParamDialog(Integer.parseInt(href), paneLoc);
    			}
			}
    	}
        
	}
	
	/**
	 * Implemented as specified by the {@link PropertyChangeListener} interface.
	 * Listens for changes to the dialog for editing parameters.
	 * Delegates editing to the {@link #controller}. 
	 * 
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		
		String propName = evt.getPropertyName();
		
		if (ITreeEditComp.VALUE_CHANGED_PROPERTY.equals(propName)) {
			
			if (evt.getSource() instanceof ITreeEditComp) {
				
				/* Need controller to pass on the edit  */
				if (controller == null) return;
				
				ITreeEditComp src = (ITreeEditComp)evt.getSource();
				IAttributes param = src.getParameter();
				String attrName = src.getAttributeName();
				String displayName = src.getEditDisplayName();
				
				// System.out.println("FieldPanel propChanged: "+ attrName + 
				//	" " + evt.getNewValue());
				
				String newValue;
				Object newVal = evt.getNewValue();
				if ((newVal instanceof String) || (newVal == null)){
					newValue = (newVal == null ? null : newVal.toString());
				 	 controller.editAttribute(param, attrName, newValue, 
				 			displayName, navTree, treeNode);
				}
				
				else if (newVal instanceof HashMap) {
					HashMap newVals = (HashMap)newVal;
					controller.editAttributes(param, displayName, newVals, 
							navTree, treeNode);
				}
				
			}
		}
	}

}
