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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ITreeEditComp;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.ParamEditorDialog;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.TextBoxEditor;
import org.openmicroscopy.shoola.agents.editor.browser.paramUIs.editTemplate.FieldContentEditor;
import org.openmicroscopy.shoola.agents.editor.model.Field;
import org.openmicroscopy.shoola.agents.editor.model.IAttributes;
import org.openmicroscopy.shoola.agents.editor.model.IField;
import org.openmicroscopy.shoola.agents.editor.model.IFieldContent;
import org.openmicroscopy.shoola.agents.editor.model.TextBoxStep;
import org.openmicroscopy.shoola.agents.editor.model.TextContent;
import org.openmicroscopy.shoola.agents.editor.model.TreeModelMethods;
import org.openmicroscopy.shoola.agents.editor.model.params.AbstractParam;
import org.openmicroscopy.shoola.agents.editor.model.params.FieldParamsFactory;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.model.params.TextParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.UIUtilities;

/** 
 * This Text Area is represents a Field/Step (or a node) of the data model tree,
 * when it is displayed in the "Text Document" view (rather than a JTree view).
 * The text is displayed using a {@link HtmlContentEditor}, but the editing
 * is managed by this class. 
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
	extends JPanel
	implements FocusListener,
	PropertyChangeListener,
	ActionListener
{
	/**  The text area that displays and allows users to edit the text */
	protected HtmlContentEditor		htmlEditor;
	
	private static String 			addParamToolTip = 
									"Add a parameter to this step";
	
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
	protected BrowserControl 		controller;
	
	/**
	 * A dialog for displaying a pop-up edit of a parameter.
	 * This reference allows the dialog to be hidden when this field 
	 * is de-selected. 
	 */
	private JDialog					paramEditDialog;
	

	/** A border to illustrate when this field is selected */
	protected Border 				selectedBorder;
	
	/** A border to illustrate when this field is unselected */
	private Border 					unselectedBorder;
	
	/** Button to add a parameter into this field. */
	private JButton 				addParamButton;

	/** Icon for the add-parameter button */
	private Icon 					addParamIcon;
	
	/** Icon for the add-parameter button, to "hide" button without changing
	 * it's size */
	private Icon 					blankIcon;
	
	/**
	 * This is only used if the Step that this UI displays is a 'Comment' step.
	 * This text area is displayed below the main description text editor. 
	 */
	private JTextArea				commentTextBox;
	
	/**
	 * The HTML tag id to use for displaying the Field Name.
	 * This needs to be all lower case, since the styleSheet text in the
	 * HTML header automatically is converted to lower case, but the 
	 * id names in the HTML body is not, so they won't match if one includes
	 * capitals!
	 */
	public static final String			NAME_ID = "fieldname";

	/**
	 * The HTML tag name to use for displaying the Field Text contents
	 */
	public static final Tag			TEXT_TAG = Tag.SPAN;

	/**
	 * The HTML tag name to use for displaying the Field Parameters
	 */
	public static final Tag			PARAM_TAG = Tag.A;

	/**
	 * The HTML tag name to use for displaying the Field Name.
	 */
	public static final Tag			NAME_TAG = Tag.SPAN;
	
	/**
	 * A useful bit of HTML for adding space between tags etc. 
	 */
	public static final String 		TEXT_SPACER = "<"+ TEXT_TAG +
													"> </"+ TEXT_TAG + ">";
	
	/**
	 * Initialises UI components. 
	 */
	private void initialise()
	{
		htmlEditor = new HtmlContentEditor();
		htmlEditor.addFocusListener(this);
		htmlEditor.addMouseListener(new ParamMouseListener());
		
		int indent = treeNode.getLevel() * 15;	// indent according to hierarchy
		Border emptyBorder = new EmptyBorder(7,indent,7,7);
		Border lb = BorderFactory.createLineBorder(UIUtilities.LIGHT_GREY);
		selectedBorder = BorderFactory.createCompoundBorder(lb, emptyBorder);
		lb = BorderFactory.createLineBorder(Color.white);
		unselectedBorder = BorderFactory.createCompoundBorder(lb, emptyBorder);
		
		addParamIcon = IconManager.getInstance().getIcon(IconManager.ADD_NUMBER);
		blankIcon = IconManager.getInstance().getIcon(IconManager.SPACER);
		
		Document d = htmlEditor.getDocument();
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
		setBackground(null);
		setLayout(new BorderLayout());
		refreshText();
		
		add(htmlEditor, BorderLayout.CENTER);
		
		addParamButton = new CustomButton(addParamIcon);
		addParamButton.setToolTipText(addParamToolTip);
		addParamButton.addActionListener(this);
		
		if (field instanceof TextBoxStep) {
			IParam textBoxParam = ((TextBoxStep)field).getTextBoxParam();
			TextBoxEditor tbe = new TextBoxEditor(textBoxParam);
			commentTextBox = tbe.getTextBox();
			tbe.addPropertyChangeListener(ITreeEditComp.VALUE_CHANGED_PROPERTY, this);
			add(tbe, BorderLayout.SOUTH);
		}

		JPanel buttonContainer = new JPanel(new BorderLayout());
		buttonContainer.setBackground(null);
		buttonContainer.add(addParamButton, BorderLayout.NORTH);
		add(buttonContainer, BorderLayout.EAST);
		
		setSelected(false);
	}

	
	/**
     * Converts the current text of the editor into a list of
     * {@link IFieldContent}, as in the model. 
     * 
     * @return
     */
    private List<IFieldContent> getNewContent() 
    {
    	return getNewContent(null);
    }
	
	/**
     * Converts the current text of the editor into a list of
     * {@link IFieldContent}, including a new parameter object, which will be
     * added wherever a parameter tag has id="new"
     * 
     * <code>newParam</code> can be null if not adding a new parameter. 
     * 
     * @return		a list of {@link IFieldContent} to represent the content
     */
    private List<IFieldContent> getNewContent(IFieldContent newParam) 
    {
    	List<IFieldContent> contentList = new ArrayList<IFieldContent>();
    	
    	int lastChar = 0;
    	Document d = htmlEditor.getDocument();
    	
    	// ignore the name token, but need to know when it ends (content begins)
    	TextToken nameTag = getNameTag();
    	if (nameTag != null) {
    		lastChar = nameTag.getEnd();	
    		// need to skip new-line character
    		try {
				String nextChar = d.getText(lastChar, 1);
				if (nextChar.trim().length() == 0) 
					lastChar++;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	// Get the parameter tokens, iterate through them...
    	List<TextToken> tags = htmlEditor.getElementsByTag
    											(FieldTextArea.PARAM_TAG);
    	
    	String description;
    	int id;
    	
    	try {
    		for (TextToken param : tags) {
    			// is there any text between start of this tag and end of last?
    			description = d.getText(lastChar, param.getStart()-lastChar);
				
				if (description.trim().length() > 0) {
					// if so, add a new text content object to the list
					// without trimming. 
					contentList.add(new TextContent(description));
				}
				String tagId = param.getId();
				// if the text references a "new" parameter, use newParam
				if (("new".equals(tagId)) && (newParam != null)) {
					contentList.add(newParam);
				}
				else {
				try {
					// now process the parameter. This should exist in the
					// field already, since text editing can only edit text 
					// content, or DELETE parameters.
					id = Integer.parseInt(param.getId());
					// Simply copy a reference into the new list
					if (id < field.getContentCount())
						contentList.add(field.getContentAt(id));
				} catch (NumberFormatException ex) {
					// ignore elements that do not have a valid (integer) id
				}
				}
				
				lastChar = param.getEnd();	// update the end of last element
			}
    		// any text left over?
    		description = d.getText(lastChar, d.getLength()-lastChar+1);
    		if (description.trim().length() > 0) {
    			// if so, add another text content to the list
				contentList.add(new TextContent(description));
			}
    		
    	} catch (BadLocationException e) {
			e.printStackTrace();
		}
    	
    	return contentList;
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
	protected void showParamDialog(int index, Point point) 
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
	 * Method for generating the html representation of the <code>field</code>.
	 * 
	 * @param field
	 * @return
	 */
	private String getHtml()
	{
		String html = "";
		
		// html for the field name
		String name = getFieldName();
		if (name != null) {
			html = "<"+ FieldTextArea.NAME_TAG  +" " + HTML.Attribute.ID +
				"='"+ FieldTextArea.NAME_ID +"'>" 
				+ name.trim() + "</"+ FieldTextArea.NAME_TAG +"><br>";
		}
		
		String contentText;
		String contentString;
		IFieldContent content;
		
		// if no content, put a place-holder so users can enter text
		if (field.getContentCount() == 0) {
			contentText = "<"+ FieldTextArea.TEXT_TAG +" " +
			HTML.Attribute.ID + 
			"='0'> </"+ FieldTextArea.TEXT_TAG + ">";
			return html + contentText;
		}
		
		// flag used to insert space between parameter objects, so user can
		// start typing and insert text between parameters. 
		boolean includeSpacer = false;	
		
		// html for the field contents. 
		for (int i=0; i<field.getContentCount(); i++) {
			content = field.getContentAt(i);
			if (content instanceof IParam) {
				contentString = content.toString();
				if (contentString.length() == 0)
					contentString = "param";
				
				contentString = "[" + contentString + "]";
				// id attribute allows parameters to be linked to model
				// eg for editing parameters. 
				contentText = (includeSpacer ? TEXT_SPACER : "") + // space before param
						"<"+ FieldTextArea.PARAM_TAG + " href='#' " +
				 		HTML.Attribute.ID + "='" + i + "'>" + 
				 		contentString + 
				 		"</"+ FieldTextArea.PARAM_TAG + ">";
				includeSpacer = true;
			} else {
				// don't need this ID attribute, but might be useful in future?
				contentText = "<"+ FieldTextArea.TEXT_TAG +" " +
				HTML.Attribute.ID + "='" + i + "'>" + 
				content.toString() 
				+ "</"+ FieldTextArea.TEXT_TAG + ">";
				if (content.toString().length() > 0)
					includeSpacer = false;		// don't need a space after text
			}
			// add each component. 
			html = html + contentText;
		}
		return html;
	}
	
	/**
     * Saves the current textual content back to the model, if the 
     * text has been edited.
     */
    protected void saveContent() 
    {
    	if (htmlEditor.hasDataToSave()) {
    		String fieldName = null;
    		// get the new name...
    		TextToken nameTag = getNameTag();
    		if (nameTag != null) {
    			fieldName = getEditedName(nameTag.getText());
    		}
    		
    		// convert the current text of this editor into a list of
    		// content, in the same format as the data model...
			List<IFieldContent> newContent = getNewContent();
			
			// replace the old content of the field with new content
			saveContent(field, fieldName, newContent, navTree, treeNode);
			
			// reset this flag
			htmlEditor.dataSaved();
		}
    }
    
   /**
    * Saves the content via the controller, which handles adding to undo/redo.
    * This method may be overridden by subclasses to change the saving 
    * behavior of this class.
    * 
    * @param fld		The field to add a new parameter to.
	* @param fieldName		The new name of the field
	* @param content 		The new content, as a list.
	* @param tree			The JTree to refresh with undo/redo
	* @param node		The node to highlight / refresh with undo/redo. 
    */
    protected void saveContent(IField fld, String fieldName,
			List<IFieldContent> content, JTree tree, TreeNode node) {
    	
    	// replace the old content of the field with new content
		controller.editFieldContent(fld, fieldName, content, tree, node);
    }
    
    /**
     * Gets the {@link TextToken} that defines the Field Name in the 
     * html editor.
     * 
     * @return		The field name, as a text token.
     */
    private TextToken getNameTag() {

    	List<TextToken> tags = htmlEditor.getElementsByTag
    											(FieldTextArea.NAME_TAG);
    	
    	for (TextToken token : tags) {
    		if (FieldTextArea.NAME_ID.equals(token.getId())) {
    			return token;
    		}
    	}
    	return null;
    }
    
    /**
	 * Inserts a new parameter, of the type defined by <code>paramType</code>
	 * into the text content at the current selection or caret position. 
	 * The currently highlighted text will become the value of the parameter. 
	 */
	private void insertParam(String paramType) 
	{
		// get the currently selected text and selection positions
		String selectedText = htmlEditor.getSelectedText();
		int start = htmlEditor.getSelectionStart();
		int end = htmlEditor.getSelectionEnd();
		
		Document d = htmlEditor.getDocument();
		
		int nameEnd = 0;
		// ignore the name token, but need to know when it ends (content begins)
    	TextToken nameTag = getNameTag();
    	if (nameTag != null) {
    		nameEnd = nameTag.getEnd();	
    		// need to skip new-line character
    		try {
				String nextChar = d.getText(nameEnd, 1);
				if (nextChar.trim().length() == 0) 
					nameEnd++;
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	// if selection is within the name tag, move to end of name before adding
    	// parameter. (can't have parameters in name). 
    	if (start < nameEnd) {
    		start = nameEnd;
    	}
    	if (end < nameEnd) {
    		end = nameEnd;
    	}
		
		// edit the document text, inserting tags around the currently-selected
		// text (or inserting the tag into the caret position, if no text
		// is selected). 
		MutableAttributeSet aAttributes = new SimpleAttributeSet();
		aAttributes.addAttribute(HTML.Attribute.ID, "new");
		
		MutableAttributeSet tagAttributes = new SimpleAttributeSet();
		tagAttributes.addAttribute(FieldTextArea.PARAM_TAG, aAttributes);
		
		if ((selectedText == null) || (selectedText.length() == 0)) {
			// need to insert something!
			selectedText = AbstractParam.DEFAULT_PARAM_NAME;	
		}
		
		try {
			// replace the selected text with the parameter 
			d.remove(start, end - start);
			
			// if textEditor doesn't have focus, char = 0. 
			if (start == 0) start++;	// to avoid adding into header! 
			
			d.insertString(start, selectedText, tagAttributes);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
		
		// create a new parameter
		IParam param = FieldParamsFactory.getFieldParam(paramType);
		param.setAttribute(TextParam.PARAM_NAME, selectedText);
		
		// and use it to build a new content list. This method links the 
		// newly inserted tag (id=new) with the parameter argument
		List<IFieldContent> newContent = getNewContent(param);
		
		// get the new name, just in case it changed...
		String fieldName = getEditedName(nameTag.getText());
		
		// replace the old content of the field with new content
		controller.editFieldContent(field, fieldName, 
				newContent, navTree, treeNode);
		
		// reset this flag
		htmlEditor.dataSaved();
	}
	
	/**
	 * Handy method for getting the edited name of the field/step.
	 * If the current name is the same as the default text (eg Step 1) then
	 * this method will return null, since the name has not been edited and
	 * should not be saved. 
	 * 
	 * @param currentText
	 * @return
	 */
	private String getEditedName(String currentText)
	{
		if (currentText == null)	return null;
		if (currentText.equals(TreeModelMethods.getNodeName(treeNode)))
				return null;
		
		return currentText.trim();
	}

	/**
     * Changes the appearance of this Field to indicate that the corresponding
     * node is selected in the JTree.
     * Called by {@link #refreshSelection()}
     * 
     * @param selected		If true, border is painted etc. 
     */
    private void setSelected(boolean selected)
	{
		setBorder(selected ? selectedBorder : unselectedBorder);
		// don't want to hide the button, or the text will expand to fill! 
		addParamButton.setIcon(selected ? addParamIcon : blankIcon);
		
		// don't allow addition of parameters to root. 
		if (selected) {
			if(treeNode.isRoot()) {
				selected = false;
			}
		}
		addParamButton.setEnabled(selected);
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
    		int c = htmlEditor.viewToModel(mouseLoc);
    		
    		// if any changes have been made to the document, save changes.
    		saveContent();
    		
    		// if click is on a parameter...
    		if (htmlEditor.isOffsetWithinTag(c, FieldTextArea.PARAM_TAG)) {
    			// disable add-param button
    			addParamButton.setEnabled(false);
    			
    			// show edit dialog
    			String id = htmlEditor.getElementId(c);
    			if ((id != null) && (isShowing())) { 
    				Point paneLoc = null;
    				try { 
    					paneLoc = getLocationOnScreen();
    					// give the exact location of the mouse
    					if (mouseLoc != null)
    						paneLoc.translate((int)mouseLoc.getX(), 
    											(int)mouseLoc.getY());
    				} catch (IllegalComponentStateException ex){
    					ex.printStackTrace();
    				}
    				showParamDialog(Integer.parseInt(id), paneLoc);
    			}
    		}
    		else {
    			// enable add-param button, unless root node
    			addParamButton.setEnabled(! treeNode.isRoot());
    		}
    	}
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
	public FieldTextArea(IField field, JTree tree, DefaultMutableTreeNode treeNode,
			BrowserControl controller) 
	{
		
		this.field = field;
		this.navTree = tree;
		this.treeNode = treeNode;
		this.controller = controller;
		
		initialise();
		buildUI();
	}
	
	/**
	 * Gets the field name for display. 
	 * This method may be overridden by subclasses if they do not want to
	 * display the field name, E.g. {@link FieldContentEditor}.
	 * 
	 * @return		The name of the field. 
	 */
	protected String getFieldName()
	{
		String name = field.getAttribute(Field.FIELD_NAME);
		if ((name == null) || (name.length() == 0)) {
			name = TreeModelMethods.getNodeName(treeNode);
		} 
		return name;
	}
	
	/**
	 * Refreshes the text of this UI, based on the {@link #field}.
	 * Sets the HTML to include CSS style header. 
	 * This method is called by the parent UI when the field has been 
	 * edited.
	 */
	public void refreshText() {
		
		String content = getHtml();

		String html = "<html><head> <style type='text/css'> \n" +
			FieldTextArea.NAME_TAG + "#" + FieldTextArea.NAME_ID +
			" {font-weight: 500; font-family: arial} \n" +
			"</style> </head> " +
			"<body> " + content + " </body> </html>";
		
		htmlEditor.setText(html);
		
		htmlEditor.dataSaved();
		
		// if this is a 'Comment Step' it will have a text box that needs update
		if (field instanceof TextBoxStep) {
			IParam textBoxParam = ((TextBoxStep)field).getTextBoxParam();
			String newText = textBoxParam.getAttribute(TextParam.PARAM_VALUE);
			if (commentTextBox != null)
				commentTextBox.setText(newText);
		}
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
		
		setSelected(selected);
		
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
	 * Implemented as specified by the {@link ActionListener} interface.
	 * Handles the "Add Parameter" button. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) 
	{
		insertParam(TextParam.TEXT_LINE_PARAM);
		
	}
}
