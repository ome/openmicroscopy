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
package org.openmicroscopy.shoola.util.ui.omeeditpane;

//Java imports

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.omeeditpane.OMERegexFormatter;
import org.openmicroscopy.shoola.util.ui.omeeditpane.Position;
import org.openmicroscopy.shoola.util.ui.omeeditpane.WikiView;

/** 
 * A Text Editor with Regex capability.
 *  
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class RegexTextPane
	extends JTextPane 
	implements DocumentListener 
{
	
	/** Bound property. Indicates a regex has been clicked **/
	public static final String REGEXDBLCLICKED = "regexClicked";
	
	/** Formatter for changing appearance of text according to regex matching */
	private OMERegexFormatter 			regexFormatter;
	
	/** A simple attribute set that defines the plain text for this TextPane */
	private SimpleAttributeSet 			plainText;
	
	/** The document of the TextPane we're editing */
	private StyledDocument 				doc;
	
	private Set<String>					regexStrings;

	/**
	 * Called whenever the document is edited
	 * Parses regex. 
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
    				
    				Map<Position,String> matches = new HashMap<Position,String>();
    				
    				Iterator<String> i = regexStrings.iterator();
    				while(i.hasNext()) {
    					String regex = i.next();
    					WikiView.findGroups(text, regex, matches);
    	    			
    					Iterator<Position> m = matches.keySet().iterator();
    					while (m.hasNext()) {
    						Position p = m.next();
        					if (p.contains(offset, offset)) {
        						String fullMatch = doc.getText(
        								p.getStart(), p.getEnd()-p.getStart());
        						String group = matches.get(p);
        						firePropertyChange(REGEXDBLCLICKED, null, 
        								new WikiDataObject(regex, group, fullMatch));
        						return;
        					}
        				}
    				}
    				
    			} catch (BadLocationException e2) { e2.printStackTrace();}
    		}
    	}
    }


	/**
	 * Creates an instance. 
	 */
	public RegexTextPane() {
		
		doc = getStyledDocument();
		regexStrings = new HashSet<String>();
			
		//Put the initial text into the text pane.
        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, "Arial");
        StyleConstants.setFontSize(plainText, 13);
		
		// make the regexFormatter
		regexFormatter = new OMERegexFormatter(plainText);
        
        getDocument().addDocumentListener(this);
     // add a mouseListener for opening a URL with a double-click
        addMouseListener(new UrlMouseListener());
	}
	
	public void addRegex(String regex, SimpleAttributeSet styleSet) {
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
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Causes the edited document to be parsed for regex matches.
	 */
	public void removeUpdate(DocumentEvent e) {
		parseRegex(e.getOffset());
	}
	
	/**
	 * Main method for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		JFrame frame = new JFrame("RegexTextPane test");
		
		RegexTextPane editor = new RegexTextPane();
		
		SimpleAttributeSet urlSet = new SimpleAttributeSet();
        StyleConstants.setForeground(urlSet, Color.blue);
        StyleConstants.setUnderline(urlSet, true);		// urls underlined
        String urlRegex =
    		"((https?|ftp|file)://|www.)[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
        editor.addRegex(urlRegex, urlSet);
        
        SimpleAttributeSet imageSet = new SimpleAttributeSet();
        StyleConstants.setForeground(imageSet, Color.red);
        String imageRegex = "Image ID: ([0-9]+)";
        editor.addRegex(imageRegex, imageSet);
        
        SimpleAttributeSet pSet = new SimpleAttributeSet();
        StyleConstants.setForeground(pSet, Color.green);
        String pRegex = "Project ID: ([0-9]+)";
        editor.addRegex(pRegex, pSet);
        
        SimpleAttributeSet dSet = new SimpleAttributeSet();
        StyleConstants.setForeground(dSet, Color.cyan);
        String dRegex = "Dataset ID: ([0-9]+)";
        editor.addRegex(dRegex, dSet);
        
        editor.addPropertyChangeListener(new PropertyChangeListener() {
			
			public void propertyChange(PropertyChangeEvent evt) {
				String propName = evt.getPropertyName();
				
				if (REGEXDBLCLICKED.equals(propName)) {
					Object newVal = evt.getNewValue();
					if (newVal instanceof WikiDataObject) {
						WikiDataObject data = (WikiDataObject)newVal;
						//String regex = data.getRegex();
						long id = data.getId();
						int index = data.getIndex();
						
						if (index == WikiDataObject.IMAGE) {
							System.out.println("Clicked Image ID: " + id);
						} else if (index == WikiDataObject.DATASET) {
							System.out.println("Clicked Dataset ID: " + id);
						} else if (index == WikiDataObject.PROJECT) {
							System.out.println("Clicked Project ID: " + id);
						} else {
							// E.g. url. 
							System.out.println("Clicked " + data.getMatchedText());
						}
						
					}
				}
			}
		});
        
        frame.getContentPane().add(editor);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
	}
	
}
