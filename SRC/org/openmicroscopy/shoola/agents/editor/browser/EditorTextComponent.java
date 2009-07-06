 /*
 * org.openmicroscopy.shoola.agents.editor.browser.EditorTextComponent 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.util.ui.BrowserLauncher;
import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalNameFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalSymbolsEditer;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMERegexFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.Position;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView;

/** 
 * A Text Editor with Regex capability for recognizing E.g. [parameters] etc. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class EditorTextComponent
	extends JTextPane 
	implements DocumentListener 
{
	/**
	 * The regex pattern that matches a user-entered parameter. 
	 * Will match any sequence contained by [ and ], but not containing
	 * [ or ]!
	 * 
	 * Translate regex as "[ ( ( not] not[ ANY) . ) atLeastOnce ]"
	 * Where [ is escaped \\[, ( is [, ANY is .
	 * atLeastOnce is +
	 */
	public static final String PARAM_REGEX = "\\[[[^\\]^\\[.].]+\\]";
	// simpler regex but doesn't allow full stop! 
	// public static final String PARAM_REGEX = "\\[[^\\]^\\[.]+\\]";
	
	public static final String URL_REGEX = 
		"((https?|ftp|file)://|www)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
	
	/** Formatter for changing appearance of text according to regex matching */
	private OMERegexFormatter 			regexFormatter;
	
	/** 
	 * Formatter for changing appearance of text according to regex matching
	 * of recognized chemical formlae. E.g H2O, MgCl2,
	 */
	private ChemicalNameFormatter 		chemicalNameFormatter;
	
	/** 
	 * Formatter for editing text to appropriate chemical symbols.
	 * E.g. 'C 
	 */
	private ChemicalSymbolsEditer	chemicalSymbolEditer;
	
	/** A simple attribute set that defines the plain text for this TextPane */
	private SimpleAttributeSet 			plainText;
	
	/** The document of the TextPane we're editing */
	private StyledDocument 				doc;
	
	/** List of the positions of parameters */
	private	List<Position> 				paramPositionList;
	
	/** Flag to enable on/off of auto-editing of Eg ul . */
	private boolean 					toggleSymbolEdit = true;
	
	/**  Set to true by document listener, set to false when saved to model. */
	private boolean 					hasDataToSave;
	
	/** A bound property indicating that a paramter has been created */
	public static final String 			PARAM_CREATED = "paramCreated";
	
	/** A bound property indicating that a paramter has been deleted */
	public static final String 			PARAM_DELETED = "paramDeleted";
	
	/** A bound property indicating that a paramter has been edited */
	public static final String 			PARAM_EDITED = "paramEdited";
	
	/**
	 * Updates the list of parameter positions according to the given text.
	 * 
	 * @param text		The current text for this editor. 
	 */
	private void updateParamMap(String text) 
	{
		if (text == null)	{
			paramPositionList.clear();
			return;
		}
		
		WikiView.findExpressions(text, PARAM_REGEX, paramPositionList);
	}
	
	/**
	 * Populates the map of symbols to recognise and replace, using 
	 * values from the editor.xml file. 
	 */
	private void populateSymbolsList()
	{
		String list = (String)EditorAgent.getRegistry().lookup("/model/symbols");
		String[] chemicals = list.split(",");

		String find, replace;
		String[] colonSplit;
		// each find:replace pair is joined with a colon:
		for (int i = 0; i < chemicals.length; i++) {
			colonSplit = chemicals[i].split(":");
			find = " " + colonSplit[0].trim();
			if (colonSplit.length > 1) {
				replace = " " + colonSplit[1].trim();
				chemicalSymbolEditer.addSymbol(find, replace);
			}
		}
	}

	/** 
	 * Looks up a list of chemicals from editor.xml and adds them to the 
	 * formulas list. 
	 * List of formulas found here 
	 * http://openwetware.org/wiki/Materials
	 */
	private void populateChemicalList()
	{
		String list = (String)EditorAgent.getRegistry().lookup
														("/model/chemicals");
		String[] chemicals = list.split(",");

		for (int i = 0; i < chemicals.length; i++) {
			chemicalNameFormatter.addFormula(chemicals[i].trim());
		}
	}

	/**
	 * Called whenever the document is edited
	 * Parses regex. 
	 */
	private void parseRegex(int caretPosition)
	{
		// apply formatting according to regex patterns
		chemicalNameFormatter.parseRegex(doc, true);   // true: clear formatting
		regexFormatter.parseRegex(doc, false);	// false: don't clear formatting
		
		// edit symbols according to regex
		chemicalSymbolEditer.parseRegex
								(doc, (toggleSymbolEdit ? caretPosition : 0));
		
		String text = "";
		
		try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e2) { e2.printStackTrace();}
		
		List<Position> newPositionList = new ArrayList<Position>();
		WikiView.findExpressions(text, PARAM_REGEX, newPositionList);
		
		// need to know if and which parameter removed. 
		int oldParamCount = paramPositionList.size();
		int newParamCount = newPositionList.size();
		
		if (oldParamCount > newParamCount) {
			firePropertyChange(PARAM_CREATED, oldParamCount, newParamCount);
		} else if (oldParamCount < newParamCount) {
			firePropertyChange(PARAM_DELETED, oldParamCount, newParamCount);
		} else {
			// check if edit is within a param
			int regexIndex = offsetParamIndex(caretPosition);
			if (regexIndex > -1) {
				Position p = newPositionList.get(regexIndex);
				String newText = "";
				try {
					newText = doc.getText(p.getStart()+1, p.getEnd()-p.getStart()-2);
				} catch (BadLocationException e) {}
				firePropertyChange(PARAM_EDITED, null, newText);
			}
		}
			
		paramPositionList = newPositionList;	
		
	}
	
	/**
	 * MouseListener for handling double-clicks on URLs 
	 * @author will
	 */
	private class UrlMouseListener extends MouseAdapter
    {
    	/**
    	 * Handles double-clicks, checks whether the character clicked is 
    	 * within a URL (parses all text for URL regexs).
    	 * If so, gets the URL and opens web browser. 
    	 * Display of url is handled separately by regexFormatter. 
    	 */
    	public void mouseClicked(MouseEvent e) 
    	{
    		Point mouseLoc = e.getPoint();
    		int offset = viewToModel(mouseLoc);
    		
    		if (e.getClickCount() == 2) {
    			
    			String text = "";
    			
    			try {
    				text = doc.getText(0, doc.getLength());
    				List<Position> urlPositionList = new ArrayList<Position>();
    				WikiView.findExpressions(text, URL_REGEX, urlPositionList);
    			
    				for (Position p: urlPositionList) {
    					if (p.contains(offset, offset)) {
    						String link = doc.getText(
    								p.getStart(), p.getEnd()-p.getStart());
    						// if url is "www", needs "http://"
    						if (link.startsWith("www")){
    							link = "http://" + link;
    						}
    						new BrowserLauncher().openURL(link);
    						return;
    					}
    				}
    			} catch (BadLocationException e2) { e2.printStackTrace();}
    		}
    	}
    }


	/**
	 * Sets the {@link #hasDataToSave} flag to <code>true</code>
	 */
	void dataEdited() {
	    hasDataToSave = true;
	}


	/**
	 * Sets the {@link #hasDataToSave} flag to <code>false</code>
	 */
	void dataSaved() {
	    hasDataToSave = false;
	}


	/**
	 * Gets a list of all the parameters. Each token has start, stop and
	 * text, but no ID. 
	 * @return	see above.
	 */
	List<TextToken> getParamTokens() 
	{
		List<TextToken> params = new ArrayList<TextToken>();
		
		int start, end;
		String text;
		for (Position p : paramPositionList)
		{
			start = p.getStart();
			end = p.getEnd();
			try {
				text = doc.getText(start, end-start);
			} catch (BadLocationException e) {
				e.printStackTrace();
				text = "";
			}
			
			params.add(new TextToken(p.getStart(), p.getEnd(), text));
		} 
		return params;
	}


	/**
	 * Creates an instance. 
	 */
	public EditorTextComponent() {
		
		doc = getStyledDocument();
		paramPositionList = new ArrayList<Position>();
		//Put the initial text into the text pane.
        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, "Arial");
        StyleConstants.setFontSize(plainText, 13);
		
		// configure the regex formatter to colour any [parameter] in blue. 
        // This only changes appearance of text. 
        // Doesn't handle param reporting, edits etc
		regexFormatter = new OMERegexFormatter(plainText);
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, Color.blue);
        regexFormatter.addRegex(PARAM_REGEX, set);
        SimpleAttributeSet urlSet = new SimpleAttributeSet();
        StyleConstants.setForeground(urlSet, Color.blue);
        StyleConstants.setUnderline(urlSet, true);		// urls underlined
        regexFormatter.addRegex(URL_REGEX, urlSet);
        
        // make formatters for chemical names and symbols. 
        chemicalNameFormatter = new ChemicalNameFormatter(plainText);
        populateChemicalList();
        chemicalSymbolEditer = new ChemicalSymbolsEditer(plainText);   
        populateSymbolsList();
        
        getDocument().addDocumentListener(this);
        // add a mouseListener for opening a URL with a double-click
        addMouseListener(new UrlMouseListener());
	}
	
	/**
	 * Returns the {@link #hasDataToSave} boolean flag.
	 * 
	 * @return	see above
	 */
	public boolean hasDataToSave() {
		return hasDataToSave;
	}
	
	/**
     * Returns the index of the [parameter] identified by regex matching on
     * the text, if the offset falls within a [parameter]. 
     * Otherwise returns -1 if the offset is not within a paramter. 
     * 
     * @param offset	The character position
     * 
     * @return	index of parameter that contains offset, or -1 
     */
    public int offsetParamIndex(int offset)
	{
    	if (offset == 0) return -1;
    	
    	int index = 0;
    	for (Position p: paramPositionList) {
    		if (p.contains(offset, offset)) {
				return index;
			}
    		index++;
		}
		
    	return -1;
	}
    
    /**
	 * Overrides the {@link #setText(String)} method of {@link JEditorPane} in
	 * order to preserve the current location of the caret position. 
	 * After delegating to the superclass {@link #setText(String)} method, the
	 * caret position is reset. 
	 * Setting caret has no effect if this panel does not have focus, BUT  
 	 * setText() is often called by editing and clicking within the editor. 
	 * 
	 * @see JEditorPane#setText(String)
	 */
	public void setText(String text) 
	{
		int caret = getCaretPosition();
		super.setText(text);
		
		try {
			setCaretPosition(caret);
		} catch (IllegalArgumentException ex) {
			setCaretPosition(0);	// if there is no text, this must be 0 
		}
		
		// update the Map of paramLocations
		updateParamMap(text);
	}
	
	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Null implementation, since we don't want to recognize changes in 
	 * formatting. Only want to APPLY formatting changes when the document is
	 * edited. 
	 */
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Causes the edited document to be parsed for regex matches.
	 */
	public void insertUpdate(DocumentEvent e) {
		parseRegex(e.getOffset());
		dataEdited();
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Causes the edited document to be parsed for regex matches.
	 */
	public void removeUpdate(DocumentEvent e) {
		parseRegex(e.getOffset());
		dataEdited();
	}
	
}
