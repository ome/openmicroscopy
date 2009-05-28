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

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalNameFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalSymbolsEditer;
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMERegexFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.Position;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView;

//Java imports

//Third-party libraries

//Application-internal dependencies

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
	 * Will match any sequence contained by [ and ].
	 */
	public static final String PARAM_REGEX = "\\[.*?\\]";
	
	/** Formatter for changing appearance of text according to regex matching */
	private OMERegexFormatter 			regexFormatter;
	
	/** 
	 * Formatter for changing appearance of text according to regex matching
	 * of recognized chemical formlae. E.g H2O, MgCl2,
	 */
	private ChemicalNameFormatter 		chemicalNameFormatter;
	
	/** 
	 * Formatter for editing text to appropriate chemical symbols.
	 * E.g. 'C becomes ¼C
	 */
	private ChemicalSymbolsEditer	chemicalSymbolEditer;
	
	/** A simple attribute set that defines the plain text for this TextPane */
	private SimpleAttributeSet 			plainText;
	
	/** The document of the TextPane we're editing */
	private StyledDocument 				doc;
	
	/** List of the positions of parameters */
	private	List<Position> 				paramPositionList;
	
	/** 
	 * Map holding the point(start and end) locations of the 
	 * parameters, as Identified by ID 
	 */
	private Map<Position, Integer>		paramLocations; 
	
	/** Flag to enable on/off of auto-editing of Eg ul to µl. */
	private boolean 					toggleSymbolEdit = true;
	
	/**  Set to true by document listener, set to false when saved to model. */
	private boolean 					hasDataToSave;
	
	/**
	 * Creates an instance. 
	 */
	public EditorTextComponent() {
		
		doc = getStyledDocument();
		
		// configure the regex formatter to colour any [parameter] in blue. 
		regexFormatter = new OMERegexFormatter(plainText);
        SimpleAttributeSet set = new SimpleAttributeSet();
        StyleConstants.setForeground(set, Color.blue);
        StyleConstants.setBold(set, true);
        regexFormatter.addRegex(PARAM_REGEX, set);
        
        // make formatters for chemical names and symbols. 
        chemicalNameFormatter = new ChemicalNameFormatter(plainText);
        chemicalSymbolEditer = new ChemicalSymbolsEditer(plainText);        
        
	}
	
	
	/**
	 * Sets the {@link #hasDataToSave} flag to <code>true</code>
	 */
	public void dataEdited() {
	    hasDataToSave = true;
	}
	
	/**
	 * Sets the {@link #hasDataToSave} flag to <code>false</code>
	 */
	public void dataSaved() {
	    hasDataToSave = false;
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
     * Returns true if the specified <code>offset</code> is within the current
     * set of parameters.
     * 
     * @param offset	The character position
     * 
     * @return	boolean 	see above. 
     */
    public boolean isOffsetWithinParam(int offset)
	{
    	if (offset == 0) return false;
    	
    	Iterator<Position> 
		positionIterator = paramLocations.keySet().iterator();
		Position p;
		while (positionIterator.hasNext())
		{
			p = positionIterator.next();
			if (p.contains(offset, offset)) {
				return true;
			}
		} 
    	return false;
	}

	/**
	 * Overrides the {@link #setText(String)} method of {@link JEditorPane} in
	 * order to preserve the current location of the caret position. 
	 * After delegating to the superclass {@link #setText(String)} method, the
	 * caret position is reset. 
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
			setCaretPosition(0);
		}
		
		// update the Map of paramLocations
		updateParamMap(text);
	}
	
	private void updateParamMap(String text) 
	{
		WikiView.findExpressions(text, PARAM_REGEX, paramPositionList);
    	paramLocations = new HashMap<Position, Integer>();
    	int i = 0;
    	for (Position p: paramPositionList) {
    		paramLocations.put(p, i++);
    	}
	}
	
	public void changedUpdate(DocumentEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void insertUpdate(DocumentEvent e) {
		parseRegex(e.getOffset());
		dataEdited();
	}

	public void removeUpdate(DocumentEvent e) {
		parseRegex(e.getOffset());
		dataEdited();
	}
	
	/**
     * Called whenever the document is edited
     * Parses regex. 
     */
    private void parseRegex(int index)
    {
    	
    	// first, make all the text plain.
    	SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	doc.setCharacterAttributes(0, doc.getLength(), plainText, true);
	        }
    	});
    	// now apply formatting according to regex patterns
    	chemicalNameFormatter.parseRegex(doc, true);	// true: clear formatting
    	regexFormatter.parseRegex(doc, false);	// false: don't clear formatting
    	
    	// edit symbols according to regex
    	chemicalSymbolEditer.parseRegex(doc, (toggleSymbolEdit ? index : 0));
    	
    	
    	String text = "";
    	
    	try {
			text = doc.getText(0, doc.getLength());
		} catch (BadLocationException e2) { e2.printStackTrace();}
    	
		List<Position> newPositionList = new ArrayList<Position>();
		WikiView.findExpressions(text, PARAM_REGEX, newPositionList);
		
    	// need to know if and which parameter removed. 
		if (paramPositionList.size() > newPositionList.size()) {
			//changeLog.append("Param deleted!\n");
    	
		} else if (paramPositionList.size() < newPositionList.size()) {
			//changeLog.append("Param created!\n");
		} else {
			// check if edit is within a param
			
			if (isOffsetWithinParam(index)) {
				System.out.println("EditorTextComponent param edited!");
			}
		}
			
    	paramPositionList = newPositionList;	
    	
    	updateParamMap(text);
    }
	
}
