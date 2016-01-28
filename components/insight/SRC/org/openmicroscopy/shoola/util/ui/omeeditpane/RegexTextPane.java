 /*
  * org.openmicroscopy.shoola.util.ui.omeeditpane.RegexTextPane
  *
  *------------------------------------------------------------------------------
  *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.util.ui.omeeditpane;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** 
 * A Text Editor with Regular expression capability.
 *
 * @author William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @since 3.0-Beta4
 */
public class RegexTextPane
	extends JTextPane 
	implements DocumentListener 
{
	
	/** The default font family. */
	public static final String FONT_FAMILY = "Arial";
	
	/** The default font size. */
	public static final int FONT_SIZE = 12;
	
	/** 
	 * Bound property indicating that a regular expression has been clicked. 
	 */
	public static final String REGEX_DBL_CLICKED_PROPERTY = "regexDblClicked";
	
	/** 
	 * Formatter for changing appearance of text according to regular 
	 * expression matching.
	 */
	private OMERegexFormatter 			regexFormatter;
	
	/** A simple attribute set that defines the plain text for this TextPane. */
	private SimpleAttributeSet 			plainText;
	
	/** The document of the TextPane we're editing. */
	private StyledDocument 				doc;
	
	/** Store the regular expressions. */
	private Set<String>					regexStrings;

	/**
	 * Called whenever the document is edited.
	 * Parses the regular expression. 
	 */
	private void parseRegex(int caretPosition)
	{
		// apply formatting according to regex patterns
		regexFormatter.parseRegex(doc, true);	// true: clear formatting
	}
	
	/**
	 * MouseListener for handling double-clicks on URLs 
	 * @author will
	 */
	private class UrlMouseListener extends MouseAdapter
    {
		
    	/**
    	 * Handles double-clicks, checks whether the character clicked is 
    	 * within a URL (parses all text for URL regular expressions).
    	 * If so, gets the URL and opens web browser. 
    	 * Display of URL is handled separately by regexFormatter. 
    	 */
    	public void mouseClicked(MouseEvent e) 
    	{
    		Point mouseLoc = e.getPoint();
    		int offset = viewToModel(mouseLoc);
    		
    		if (e.getClickCount() == 2) {
    			
    			String text = "";
    			
    			try {
    				text = doc.getText(0, doc.getLength());
    				
    				Map<Position, String> matches = 
    					new HashMap<Position, String>();
    				String regex;
    				Iterator<String> i = regexStrings.iterator();
    				Entry entry;
    				Iterator j;
    				Position p;
    				String group, fullMatch;
    				while(i.hasNext()) {
    					regex = i.next();
    					WikiView.findGroups(text, regex, matches);
    					j = matches.entrySet().iterator();
    					while (j.hasNext()) {
    						entry = (Entry) j.next();
    						p = (Position) entry.getKey();
        					if (p.contains(offset, offset)) {
        						fullMatch = doc.getText(
        								p.getStart(), p.getEnd()-p.getStart());
        						group = (String) entry.getValue();
        						
        						firePropertyChange(REGEX_DBL_CLICKED_PROPERTY, 
        								null, new WikiDataObject(regex, group, 
        										fullMatch));
        						return;
        					}
        				}
    				}
    				
    			} catch (BadLocationException e2) {}
    		}
    	}
    }

	/** Installs the listeners. */
	private void installListeners()
	{
        getDocument().addDocumentListener(this);
        //add a mouseListener for opening a URL with a double-click
        addMouseListener(new UrlMouseListener());
	}
	
	/**
	 * Initializes the components.
	 * 
	 * @param fontFamily The font family to use.
	 * @param fontSize   The size of the font.
	 */
	private void initialize(String fontFamily, int fontSize)
	{
		if (fontFamily == null || fontFamily.length() == 0)
			fontFamily = FONT_FAMILY;
		if (fontSize < 4) fontSize = FONT_SIZE;
		doc = getStyledDocument();
		regexStrings = new HashSet<String>();
			
		//Put the initial text into the text pane.
        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, fontFamily);
        StyleConstants.setFontSize(plainText, fontSize);
		
		// make the regexFormatter
		regexFormatter = new OMERegexFormatter(plainText);
		installListeners();
	}
	
	/** Creates a default instance. */
	public RegexTextPane()
	{
		initialize(getFont().getFamily(), getFont().getSize());
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param fontFamily The font family to use.
	 * @param fontSize   The size of the font.
	 */
	public RegexTextPane(String fontFamily, int fontSize)
	{
		initialize(fontFamily, fontSize);
	}
	
	/**  Installs the default regular expression.  */
	public void installDefaultRegEx()
	{
		addRegex(OMEWikiConstants.IMAGEREGEX, 
				AttributeSetFactory.createURLAttributeSet());
		/*
		addRegex(OMEWikiConstants.PROJECTREGEX, 
				AttributeSetFactory.createDefaultAttibuteSet());
		addRegex(OMEWikiConstants.DATASETREGEX, 
				AttributeSetFactory.createDefaultAttibuteSet());
				*/
		addRegex(OMEWikiConstants.URLREGEX, 
				AttributeSetFactory.createURLAttributeSet());
	}
	
	/**
	 * Adds a new regular expression.
	 * 
	 * @param regex    The regular expression to use.
	 * @param styleSet The style associated to the regular expression.
	 */
	public void addRegex(String regex, SimpleAttributeSet styleSet)
	{
		if (regex == null || styleSet == null) return;
		if (regex.trim().length() == 0) return;
		regexStrings.add(regex);
		regexFormatter.addRegex(regex, styleSet);
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
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Causes the edited document to be parsed for regular expression matches.
	 */
	public void insertUpdate(DocumentEvent e) { parseRegex(e.getOffset()); }

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Causes the edited document to be parsed for regular expression matches.
	 */
	public void removeUpdate(DocumentEvent e) { parseRegex(e.getOffset()); }
	
	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Null implementation, since we don't want to recognize changes in 
	 * formatting. Only want to APPLY formatting changes when the document is
	 * edited. 
	 */
	public void changedUpdate(DocumentEvent e) {}
	
}
