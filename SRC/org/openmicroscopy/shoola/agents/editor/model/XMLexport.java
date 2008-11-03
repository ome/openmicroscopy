 /*
 * org.openmicroscopy.shoola.agents.editor.model.XMLexport 
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
package org.openmicroscopy.shoola.agents.editor.model;

//Java imports

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

//Third-party libraries

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.XMLWriter;

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.model.params.IParam;

/** 
 * A class for exporting the {@link TreeModel} as an XML document.
 * Specifically, this class uses HTML element names, and includes a link to 
 * as style-sheet, so the resulting XHTML can be viewed directly in a browser. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class XMLexport {
	
	/**
	 * The name of the XML document's root element. 
	 * This is simply "html" for XHTML export. 
	 */
	protected String 				rootElementName = "html";
	
	/**
	 * The name of the "head" element of the XHTML document 
	 */
	protected String 				headElementName = "head";
	
	/**
	 * The name of the "body" element of the XHTML document 
	 */
	protected String 				bodyElementName = "body";
	
	/**
	 * The URL (or relative file path) of a style-sheet, 
	 * which is linked to from the XHTML document.
	 */
	protected String 				styleSheetUrl = "editorStyles.css";
	
	/**
	 * The (HTML) element name of a "Field", which maps to a node of the 
	 * {@link TreeModel}. This is "div" for XHTML, since each field will
	 * always start a new row. 
	 */
	protected String 				fieldTag = "div";
	
	/**
	 * The class name of the "Field" element. Used for CSS.
	 */
	protected String 				fieldClass = "field";
	
	/**
	 * The class name of the "Title", which is applied to the root element
	 *  of the XML tree.
	 */
	protected String 				titleClass = "docTitle";
	
	/**
	 * The name of the XHTML element that displays the "Field Name". e.g. "h3".
	 */
	protected String 				fieldNameTag = "h3";
	
	/**
	 * The class of the XHTML element that displays the "Field Name". For CSS.
	 */
	protected String 				fieldNameClass = "fieldName";
	
	/**
	 * The name of the XHTML element that contains the field content. 
	 * This is a "div" tag, since the content is on a new line. 
	 */
	protected String 				contentTag = "div";
	
	/**
	 * The class of the XHTML element that contains the field content. For CSS.
	 */
	protected String 				contentClass = "content";
	
	/**
	 * The name of the XHTML element that displays a parameter
	 * This is a "span" tag, since the parameter is not on a new line. 
	 */
	protected String 				paramTag = "span";
	
	/**
	 * The class of the XHTML element that displays a parameter. For CSS.
	 */
	protected String 				paramClass = "param";
	
	/**
	 * The name of the "class" attribute. 
	 */
	public static final String 		CLASS = "class";
	

	/**
	 * This adds a "link" element to the <code>head</code> element,
	 * with attributes required to link a style-sheet. 
	 * 
	 * @param head				The link element will become a child of this
	 * @param styleSheetRef		A URL (or relative link) to the style-sheet.
	 */
	protected void addStyleSheetLink(IXMLElement head, String styleSheetRef) 
	{
		if (head == null) return;
		
		IXMLElement link = new XMLElement("link");
		link.setAttribute("rel", "stylesheet");
		link.setAttribute("type", "text/css");
		link.setAttribute("href", styleSheetRef);
		head.addChild(link);
	}
	
	/**
	 * A recursive method that converts the {@link TreeModel} rooted at 
	 * <code>node</code> to an XML document rooted at <code>element</code>.
	 * For each child of <code>node</code>, a new {@link IXMLElement} is 
	 * created using {@link #createElement(TreeNode)}. This is added as a 
	 * child of <code>element</code> and then this method,
	 * {@link #buildTree(TreeNode, IXMLElement)} is called recursively on the
	 * child node / element. 
	 * 
	 * @param node			The root node of the tree
	 * @param element		The element corresponding to the node. 
	 */
	protected void buildTree(TreeNode node, IXMLElement element) 
	{
		if ((node == null) || (element == null)) return;
		
		TreeNode childNode;
		IXMLElement childElement;
		for(int i=0; i<node.getChildCount(); i++) {
			childNode = node.getChildAt(i);
			childElement = createElement(childNode);
			element.addChild(childElement);
			// recursively build the tree
			buildTree(childNode, childElement);
		}
		
	}
	
	/**
	 * Creates an {@link IXMLElement} from a {@link TreeNode}.
	 * Gets the {@link IField} object from the node, and uses it to add
	 * name and content elements to a new {@link IXMLElement};
	 * 
	 * @param treeNode		The node used to generate a new Element
	 * @return				A new Element, based on the node. 
	 */
	protected IXMLElement createElement(TreeNode treeNode) 
	{
		// create element, add essential attributes
		IXMLElement elt = new XMLElement(fieldTag);
		addClassAttribute(elt, fieldClass);

		// if treeNode isn't a DefaultMutableTreeNode, return black element
		if (treeNode == null) return elt;
		if (! (treeNode instanceof DefaultMutableTreeNode)) return elt;
		
		// get the userObject from the node. If it's a Field...
		DefaultMutableTreeNode dmNode = (DefaultMutableTreeNode)treeNode;
		Object userOb = dmNode.getUserObject();
		if (userOb instanceof IField) {
			IField field = (IField)userOb;
		
			// add children...
			
			// fieldName 
			String fn = field.getAttribute(Field.FIELD_NAME);
			if (fn != null && fn.length() > 0) {
				IXMLElement fieldName = new XMLElement(fieldNameTag);
				addClassAttribute(fieldName, fieldNameClass);
				fieldName.setContent(fn);
				elt.addChild(fieldName);
			}
			
			// content
			IXMLElement content = createContentElement(field);
			elt.addChild(content);
		}
		return elt;
	}
	
	/**
	 * Creates and returns an {@link IXMLElement} that contains the content of 
	 * a {@link IField}. 
	 * For each {@link IFieldContent} of the {@link IField}, a new 
	 * {@link IXMLElement} is added as a child of the returned element. 
	 * If the content is text (instance of {@link TextContent}) then a simple
	 * text element is added. 
	 * If the content is a parameter (instance of {@link IParam} then a 
	 * parameter element is added, to display a text representation of the
	 * parameter, with attributes defining the other properties of the 
	 * parameter (e.g. value, default value, units etc). 
	 * 
	 * @param field			The source of the content
	 * @return IXMLElement		A new element, with children defining content
	 */
	protected IXMLElement createContentElement(IField field)
	{
		IXMLElement content = new XMLElement(contentTag);
		addClassAttribute(content, contentClass);
		
		IFieldContent fieldContent;
		IParam param;
		IXMLElement contentElement;
		for (int i=0; i<field.getContentCount(); i++) {
			fieldContent = field.getContentAt(i);
			
			if (fieldContent instanceof TextContent) {
				contentElement = content.createPCDataElement();
				contentElement.setContent(fieldContent.toString() + " ");
				content.addChild(contentElement);
			} 
			else 
			if (fieldContent instanceof IParam) {
				param = (IParam)fieldContent;
				contentElement = new XMLElement(paramTag);
				addClassAttribute(contentElement, paramClass);
				// content is string representation of parameter
				contentElement.setContent(param.toString());
				
				// all parameter attributes are saved as attributes
				String[] paramAts = param.getParamAttributes();
				String attValue;
				for (int a=0; a<paramAts.length; a++){
					attValue = param.getAttribute(paramAts[a]);
					if (attValue != null)
					contentElement.setAttribute(paramAts[a], attValue);
				}
				
				content.addChild(contentElement);
				
				contentElement = content.createPCDataElement();
				contentElement.setContent(" ");
				content.addChild(contentElement);
			}
		}
		
		return content;
	}
	
	/**
	 * Adds a attribute named {@link #CLASS}, with the value <code>className</code>
	 * 
	 * @param element		Attribute added to this element
	 * @param className		The value of the attribute. 
	 */
	protected static void addClassAttribute(IXMLElement element, String className)
	{
		element.setAttribute(CLASS, className);
	}

	/**
	 * Exports an XHTML document created from the {@link TreeModel} to the
	 * {@link File} specified. 
	 * 
	 * @param treeModel
	 * @param file
	 */
	public void export(TreeModel treeModel, File file) 
	{
		// start with the root of the XHTML document
		IXMLElement html = new XMLElement(rootElementName);
		
		// add head element, with stylesheet
		IXMLElement head = new XMLElement(headElementName);
		html.addChild(head);
		addStyleSheetLink(head, styleSheetUrl);
		
		// add body element
		IXMLElement body = new XMLElement(bodyElementName);
		html.addChild(body);
		
		// create the root element of the XML tree, as a child of body
		TreeNode root = (TreeNode)treeModel.getRoot();
		IXMLElement protocolTitle = createElement(root);
		body.addChild(protocolTitle);
		
		// recursively builds a tree of elements from the treeModel
		buildTree(root, protocolTitle);
		
		Writer output;
		try {
			output = new FileWriter(file);
			XMLWriter xmlwriter = new XMLWriter(output);
			xmlwriter.write(html);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
