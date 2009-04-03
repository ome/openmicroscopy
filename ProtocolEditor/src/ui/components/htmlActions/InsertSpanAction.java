 /*
 * ui.components.htmlActions.InsertSpanAction 
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
package ui.components.htmlActions;

//Java imports

import java.awt.Color;
import java.awt.event.ActionEvent;

import javax.swing.JEditorPane;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledEditorKit.StyledTextAction;
import javax.swing.text.html.HTML;

//Third-party libraries

//Application-internal dependencies

/** 
 * This Action inserts a span element into an html document, with a class 
 * attribute.
 * It is possible to add various other attributes, but all style attributes are
 * ignored by JEditorPane. 
 * This class allows you to specify a background colour, which will be visible 
 * in the JEditorPane, but will not appear in the html generated. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class InsertSpanAction 
	extends StyledTextAction {

	String spanClassValue;
	
	Color bgColour;
	
	/**
	 * Creates an instance of this Action, which will add a span element to 
	 * selected text, with the class attribute equal to spanClassValue,
	 * and a background of bgColor (JEditorPane display only, not in html).
	 * The class attribute and colour can be null.
	 * The generated span element replaces other span elements in the 
	 * selected text. 
	 * 
	 * @param actionName		The name of the action.
	 * @param spanClassValue	The value of the class attribute in the span
	 * 							element. eg class = "value". 
	 * 							Can be null: No class attribute will be added.
	 * @param bgColour		A background colour to indicate the extent of the
	 * 						span element in the JEditorPane. Not saved as html.
	 * 						If null, no Colour will be added. 
	 */
	public InsertSpanAction(String actionName, String spanClassValue, 
			Color bgColour) {
		super(actionName);
		
		this.spanClassValue = spanClassValue;
		
		this.bgColour = bgColour;
	}

	public void actionPerformed(ActionEvent e) {
		
		JEditorPane editor = getEditor(e);
		if (editor != null) {
			/*
			 * An attribute set, to hold a list of attributes (not html specific)
			 */
			MutableAttributeSet divAttributes = new SimpleAttributeSet();
			if (spanClassValue != null)
			divAttributes.addAttribute(HTML.Attribute.CLASS, spanClassValue);
			
			/*
			 * These methods work for adding attributes to the <div> block,
			 * but, CSS (and other formatting for <div>) is ignored by 
			 * the JEditorPane! 
			 */
			//divAttributes.addAttribute(HTML.Attribute.STYLE, "background: red");
			//divAttributes.addAttribute(HTML.Attribute.BORDER, "1");
			//divAttributes.addAttribute("id", "001");
			//divAttributes.addAttribute(HTML.Attribute.BACKGROUND, "red");
			
			
			/*
			 * This set of attributes will be added to the document (as a 
			 * set of tags.)
			 */
			MutableAttributeSet tagAttributes = new SimpleAttributeSet();
			
			/*
			 * Seems to have no effect on divAttributes or outerAttributes
			 */
			// StyleConstants.setLeftIndent(tagAttributes, new Float(1.0));
			
			/*
			 * Adds a <div> tag, containing the attribute set: attr
			 * JEditorPane seems to ignore all CSS styles added to <div>
			 */
			tagAttributes.addAttribute(HTML.Tag.SPAN, divAttributes);
			
			/*
			 * Adds a <font color="#ff0000"> tag
			 * Always seems to go outside the <div> tag in the same attributeSet
			 */
			//StyleConstants.setForeground(tagAttributes, Color.red);
			
			/*
			 * Has a visual effect of changing the background for the selected
			 * region, BUT has no effect on the html produced! 
			 * Also, has no effect if added to divAttributes. 
			 */
			if (bgColour != null)
			StyleConstants.setBackground(tagAttributes, bgColour);
			
			
			setCharacterAttributes(editor, tagAttributes, false);
		}
	}

}
