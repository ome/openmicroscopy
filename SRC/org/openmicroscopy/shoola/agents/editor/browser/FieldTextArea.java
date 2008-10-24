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
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.text.html.HTMLDocument.RunElement;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamEditorDialog;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TextContent;
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
	PropertyChangeListener,
	DocumentListener
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
	 * This reference allows the dialog to be hidden when this field 
	 * is de-selected. 
	 */
	private JDialog					paramEditDialog;
	
	/** A border to illustrate when this field is selected */
	private Border 					selectedBorder;
	
	/** A border to illustrate when this field is unselected */
	private Border 					unselectedBorder;
	
	/**
	 * Set to true by document listener, set to false when saved to model.
	 */
	private boolean 				hasDataToSave;
	
	/**
	 * The HTML tag name to use for displaying the Field Name.
	 */
	public static final Tag			NAME_TAG = Tag.SPAN;
	
	/**
	 * The HTML tag name to use for displaying the Field Parameters
	 */
	public static final Tag			PARAM_TAG = Tag.A;
	
	/**
	 * The HTML tag name to use for displaying the Field Text contents
	 */
	public static final Tag			TEXT_TAG = Tag.SPAN;
	
	/**
	 * The HTML tag id to use for displaying the Field Name
	 */
	public static final String			NAME_ID = "FieldName";
	
	
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
		
		Document d = getDocument();
		AbstractDocument doc;
		if (d instanceof AbstractDocument) {
            doc = (AbstractDocument)d;
            if (doc instanceof StyledDocument)
            doc.setDocumentFilter(new TextAreaFilter((StyledDocument)doc, Tag.A));
        }
	}
	
	/**
	 * Builds and configures the UI
	 */
	private void buildUI()
	{
		setBorder(unselectedBorder);
		setEditable(true);
		setBackground(null);
		addFocusListener(this);
		addMouseListener(new ParamMouseListener());
		
		refreshText();
		
		getDocument().addDocumentListener(this);
	}
	
	/**
	 * Shows the pop-up dialog for editing a parameter. 
	 * The dialog adds this class as a {@link PropertyChangeListener} to 
	 * the UI component for editing the UI, so that when the user edits,
	 * the {@link ITreeEditComp#VALUE_CHANGED_PROPERTY} property
	 * the data is saved to the model, via controller for undo/ redo etc.
	 * 
	 * @param index		The index of the Parameter in the {@link #field}
	 * @param point		The screen location to show the dialog
	 */
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
	 * Sets the {@link #hasDataToSave} flag to <code>true</code>
	 */
	private void dataEdited() {
	    hasDataToSave = true;
	}

	/**
	 * Method for generating the html representation of the <code>field</code>.
	 * 
	 * @param field
	 * @return
	 */
	private static String getHtml(IField field)
	{
		String html = "";
		
		// html for the field name
		String name = field.getAttribute(Field.FIELD_NAME);
		if (name != null && name.length() > 0)
			html = "<"+ NAME_TAG  +" " + HTML.Attribute.ID + "='"+ NAME_ID +"'>" 
			+ name + "</"+ NAME_TAG +"><br>";
		
		String contentText;
		IFieldContent content;
		
		// html for the field contents. 
		for (int i=0; i<field.getContentCount(); i++) {
			content = field.getContentAt(i);
			if (content instanceof IParam) {
				// id attribute allows parameters to be linked to model
				// eg for editing parameters. 
				contentText = "<"+ PARAM_TAG + " href='#' " + HTML.Attribute.ID 
				+ "='" + i + "'>" + content.toString() + "</"+ PARAM_TAG + ">";
				
			} else {
				// don't need this ID attribute, but might be useful in future?
				contentText = "<"+ TEXT_TAG +" " + HTML.Attribute.ID + 
				"='" + i + "'>" + content.toString() 
								+ " </"+ TEXT_TAG + ">";
			}
			// add a space between each component. 
			html = html + contentText + " ";
		}
		return html;
	}
	
	/**
     * Saves the current textual content back to the model, if the 
     * text has been edited.
     */
    private void saveContent() 
    {
    	if (hasDataToSave) {
			
    		// convert the current text of this editor into a list of
    		// content, in the same format as the data model...
			List<IFieldContent> newContent = getNewContent();
			
			// replace the old content of the field with new content
			controller.editFieldContent(field, newContent, navTree, treeNode);
			
			// reset this flag
			hasDataToSave = false;
		}
    }
    
    /**
     * Gets the {@link TextToken} that defines the Field Name in the 
     * html editor.
     * 
     * @return		The field name, as a text token.
     */
    private TextToken getNameTag() {

    	List<TextToken> tags = getElementsByTag(NAME_TAG);
    	
    	for (TextToken token : tags) {
    		if (NAME_ID.equals(token.getId())) {
    			return token;
    		}
    	}
    	return null;
    }
    
    /**
     * Converts the current text of the editor into a list of
     * {@link IFieldContent}, as in the model. 
     * 
     * @return
     */
    private List<IFieldContent> getNewContent() 
    {
    	List<IFieldContent> contentList = new ArrayList<IFieldContent>();
    	
    	int lastChar = 0;
    	
    	// ignore the name token, but need to know when it ends (content begins)
    	TextToken nameTag = getNameTag();
    	if (nameTag != null) {
    		lastChar = nameTag.getEnd();
    	}
    	
    	// Get the parameter tokens, iterate through them...
    	List<TextToken> tags = getElementsByTag(PARAM_TAG);
    	Document d = getDocument();
    	String description;
    	int id;
    	
    	try {
    		for (TextToken param : tags) {
    			// is there any text between start of this tag and end of last?
				description = d.getText(lastChar, param.getStart()-lastChar).trim();
				if (description.length() > 0) {
					// if so, add a new text content object to the list
					contentList.add(new TextContent(description));
				}
				try {
					// now process the parameter. This should exist in the
					// field already, since text editing can only edit text 
					// content, or DELETE parameters.
					id = Integer.parseInt(param.getId());
					// Simply copy a reference into the new list
					contentList.add(field.getContentAt(id));
				} catch (NumberFormatException ex) {
					// ignore elements that do not have a valid (integer) id
				}
				
				lastChar = param.getEnd();	// update the end of last element
			}
    		// any text left over?
    		description = d.getText(lastChar, d.getLength()-lastChar).trim();
    		if (description.length() > 0) {
    			// if so, add another text content to the list
				contentList.add(new TextContent(description));
			}
    		
    	} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	
    	return contentList;
    }
    
    /**
     * A {@link MouseListener} added to the editor component. 
     * Used to launch the parameter edit dialog if the user clicks on a 
     * parameter element. 
     * Also, saves any changes to the model.
     * 
     * @author will
     *
     */
    private class ParamMouseListener extends MouseAdapter
    {
    	public void mouseClicked(MouseEvent e) 
    	{
    		Point mouseLoc = e.getPoint();
    		int c = FieldTextArea.this.viewToModel(mouseLoc);
    		
    		// if any changes have been made to the document, save changes.
    		saveContent();
    		
    		// if click is on a parameter, show edit dialog
    		if (isOffsetWithinTag(c, PARAM_TAG)) {
    			String id = getElementId(c);
    			if (id != null) { 
    				Point paneLoc = null;
    				try { 
    					paneLoc = getLocationOnScreen();
    					// give the exact location of the mouse
    					if (mouseLoc != null)
    						paneLoc.translate((int)mouseLoc.getX(), (int)mouseLoc.getY());
    				} catch (IllegalComponentStateException ex){
    					ex.printStackTrace();
    				}
    				showParamDialog(Integer.parseInt(id), paneLoc);
    			}
    		}
    	}
    }
    
    
    
    /**
     * Gets the "id" attribute of the {@link Tag.A} or {@link Tag.SPAN} 
     * element that contains the offset, or null if the offset does not 
     * fall within either of these tags. 
     * 
     * @param offset	The offset character position
     * @return String 	The id attribute of the element.
     */
    private String getElementId(int offset) 
    {
    	Document d = getDocument();
    	if ( !(d instanceof StyledDocument)) return null;
    	
    	StyledDocument styledDoc = (StyledDocument)d;
    	Element el = styledDoc.getCharacterElement(offset);
    	
    	if (el instanceof RunElement) {
    		
    		RunElement rE = (RunElement)el;
    		Object tag = rE.getAttribute(Tag.A);
    		if (tag != null) {
    			if (tag instanceof SimpleAttributeSet) {
    				SimpleAttributeSet sas = (SimpleAttributeSet)tag;
    				Object id = sas.getAttribute(HTML.Attribute.ID);
    				if (id != null) return id.toString();
    			}
			}
    		
    		tag = rE.getAttribute(Tag.SPAN);
			if (tag != null) {
				if (tag instanceof SimpleAttributeSet) {
    				SimpleAttributeSet sas = (SimpleAttributeSet)tag;
    				Object id = sas.getAttribute(HTML.Attribute.ID);
    				System.out.println("FieldTextArea getElementId SPAN: " + id);
    				if (id != null) return id.toString();
    			}
    		}
			
    	}
    	return null;
    }
    
    /**
     * Returns true if the specified <code>offset</code> is within the type
     * of tag specified by <code>tag</code>
     * 
     * @param offset	The character position
     * @param tag		The type of tag. eg Tag.A
     * 
     * @return	boolean 	see above. 
     */
    private boolean isOffsetWithinTag(int offset, Tag tag)
	{
    	if (offset == 0) return false;
    	
    	Document d = getDocument();
		Element el = ((StyledDocument)d).getCharacterElement(offset);
    	
    	if (el instanceof RunElement) {
    		RunElement rE = (RunElement)el;
    		Object ob = rE.getAttribute(tag);
    		if (ob != null) {
    			return true;
			}
    	}
    	return false;
	}

  
    /**
     * Creates a list of {@link TextToken} objects that correspond to the html
     * elements of type <code>tag</code> in the current document. 
     * {@link TextToken} defines the start, stop, text and id (if exists) of
     * each element. 
     * 
     * @param tag	The type of tag to get. e.g. {@link Tag.A}
     * @return	see above. 
     */
    private List<TextToken> getElementsByTag(Tag tag) 
    {
    	Document d = getDocument();
    	if (! (d instanceof StyledDocument)) return null;
    	
		HTMLDocument styledDoc = (HTMLDocument)d;
		
		List<TextToken> tokens = new ArrayList<TextToken>();
		
		Iterator i = styledDoc.getIterator(tag);
		AttributeSet atSet;
		int start;
		int end;
		String text;
		String id;
		while(i.isValid()) {
			atSet = i.getAttributes();
			Object idAttribute = atSet.getAttribute(HTML.Attribute.ID);
			start = i.getStartOffset();
			end = i.getEndOffset();
			try {
				text = styledDoc.getText(start, end-start);
				id = (idAttribute == null ? null : idAttribute.toString());
			
				tokens.add(new TextToken(start, end, text, id));
			
			} catch (BadLocationException e) {
				// ignore
			}
			i.next();
		}
		return tokens;
    }

	/**
	 * Creates an instance of this class. 
	 * Calls {@link #initialise()}, then {@link #buildUI()}.
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
	 * Refreshes the text of this UI, based on the {@link #field}.
	 * Removes and replaces the {@link CaretListener} so that it is not fired. 
	 * This method is called by the parent UI when the field has been 
	 * edited.
	 */
	public void refreshText() {
		
		removeCaretListener(this);
		
		setText(getHtml(field));
		
		addCaretListener(this);
		hasDataToSave = false;
	}

	/**
	 * Updates the border to correspond to the selection state and
	 * if the field is not selected, the {@link #paramEditDialog} is disposed. 
	 * 
	 * This is called by the parent UI when it receives tree-selection-changed
	 * events. 
	 */
	public void refreshSelection()
	{
		boolean selected = isFieldSelected();
		
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
		
		if (treeNode == null) return;
		TreePath path = new TreePath(treeNode.getPath());
		if (navTree != null)
		navTree.setSelectionPath(path);
	}

	/**
	 * Implemented as specified by the {@link FocusListener} interface.
	 * if any changes have been made to the document, save changes.
	 * 
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) {
		saveContent();
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

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Calls {@link #dataEdited(DocumentEvent)}
	 * 
	 * @see DocumentListener#insertUpdate(DocumentEvent)
	 */
	public void insertUpdate(DocumentEvent e) {
        dataEdited();
    }
	
	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Calls {@link #dataEdited(DocumentEvent)}
	 * 
	 * @see DocumentListener#removeUpdate(DocumentEvent)
	 */
    public void removeUpdate(DocumentEvent e) {
        dataEdited();
    }
    
    /**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Calls {@link #dataEdited(DocumentEvent)}
	 * 
	 * @see DocumentListener#changeUpdate(DocumentEvent)
	 */
    public void changedUpdate(DocumentEvent e) {
        dataEdited();
    }
    
    
}
