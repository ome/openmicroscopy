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

import java.awt.Event;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public class SimpleHTMLEditorPane extends JEditorPane {
	
	Action[] actionsArray;
	
	Action boldFontAction;
	Action underlineAction;

	HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
	
	public SimpleHTMLEditorPane() {
		
		super();
		
		this.setEditorKit(htmlEditorKit);
		Border bevelBorder = BorderFactory.createLoweredBevelBorder();
		Border emptyBorder = BorderFactory.createEmptyBorder(3, 3, 3, 3);
		Border compoundBorder = BorderFactory.createCompoundBorder(bevelBorder, emptyBorder);
		this.setBorder(compoundBorder);
		
		
		actionsArray = getActions();
		HashMap <Object, Action> actions = new HashMap<Object, Action>();
	    for (int i = 0; i < actionsArray.length; i++) {
	        Action a = actionsArray[i];
	        actions.put(a.getValue(Action.NAME), a);
	       // System.out.println(a.getValue(Action.NAME));
	    }
	    
		boldFontAction = actions.get("font-bold");
		//boldFontAction.putValue(Action.NAME, "Bold");
		
		underlineAction = actions.get("font-underline");
		//underlineAction.putValue(Action.NAME, "U");


	}
	
	/*
	 * set the text with this style, so that new line-breaks are incorporated as new <p> blocks.
	 * otherwise, new lines are not defined in html and are lost when getText() is called. 
	 * @see javax.swing.JEditorPane#setText(java.lang.String)
	*
	** this method keeps causing java.lang.OutOfMemoryError: Java heap space ** !?!?
	*
	public void setText(String value) {
		this.setText("<p style='margin-top: 0'>" + value + "</p>");
	}
	*/
	
	public void addHtmlTagsAndSetText(String text) {
		if (text == null)
			text = "";
		this.setText("<p style='margin-top: 0'>" + text + "</p>");
	};
	 
	
	public Action getBoldAction() {
		return boldFontAction;
	}
	public Action getUnderlineAciton() {
		return underlineAction;
	}
	
	/*
	 * getText() returns the text, removing <html><body> and <p style="margin-top: 0"> tags
	 * which are used by HTMLEditorKit to format the text in the EditorPane.
	 * ie. no tags EXCEPT <br>, <u>, and <b>
	 * @see javax.swing.JEditorPane#getText()
	 */
	public String getText() {
		
		String htmlText = super.getText();
		
		String removeThisTagAndBefore = "<p style=\"margin-top: 0\">";
		
		if (htmlText.indexOf(removeThisTagAndBefore) == -1) {
			removeThisTagAndBefore = "<body>";
		}
		
		int bodyStartIndex = htmlText.indexOf(removeThisTagAndBefore) + removeThisTagAndBefore.length();
		int bodyEndIndex = htmlText.indexOf("</body>");
		
		htmlText =  htmlText.substring(bodyStartIndex, bodyEndIndex);
		
		htmlText = htmlText.replaceAll("</p>", "");
		htmlText = htmlText.replaceAll("<p style=\"margin-top: 0\">", "<br>");
		
		return htmlText;
	}

}
