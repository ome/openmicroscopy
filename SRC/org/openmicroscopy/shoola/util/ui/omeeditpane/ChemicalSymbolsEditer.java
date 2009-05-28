 /*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalSymbolsFormatter 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;

//Third-party libraries

//Application-internal dependencies

/** 
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
public class ChemicalSymbolsEditer 
	implements DocumentListener 
{
	
	/** The Doc to parse  */
	Document doc;

	/** A Map of the symbol patterns, 
	 * each with the value with which to replace it.*/
	private Map <String, String> 	symbols;
	
	/** The Style of the plain text */
	private SimpleAttributeSet					plainText;
	
	/** The index at which the editing occurs */
	private int 						characterIndex;
	

	/**
	 * Called by the update edits. 
	 * Delegates to {@link #parseRegex(StyledDocument)}
	 * 
	 * @param e		The DocumentEvent
	 */
	private void parseRegex(DocumentEvent e) {
		parseRegex(e.getDocument(), e.getOffset());
	}

	/**
	 * Creates an instance. 
	 */
	public ChemicalSymbolsEditer(SimpleAttributeSet plainText) {
		this.plainText = plainText;
		
		symbols = new HashMap<String, String>();
		symbols.put(" ul", " µl");
		symbols.put(" ug", " µg");
		symbols.put(" uM", " µM");
		symbols.put(" 'C", " ¼C");
		symbols.put(" oC", " ¼C");
	}
	
	/**
	 * Add a Regex to the Map parsed for matches. 
	 * 
	 * @param regex		The Regex to find. 
	 * @param style		The Style to apply to matching text. 
	 */
	public void addSymbol(String regex, String replacement) {
		symbols.put(regex, replacement);
	}
	
	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Null implementation here, since Regex matching should not be affected
	 * by changes to fonts etc. 
	 */
	public void changedUpdate(DocumentEvent e) {
		// parseRegex(e);
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Calls {@link #parseRegex(DocumentEvent)}
	 */
	public void insertUpdate(DocumentEvent e) {
		parseRegex(e);
	}

	/**
	 * Implemented as specified by the {@link DocumentListener} interface.
	 * Calls {@link #parseRegex(DocumentEvent)}
	 */
	public void removeUpdate(DocumentEvent e) {
		parseRegex(e);
	}
	
	/**
	 * Parse the document, find the regex matches and edit the text for each.
	 * Editing will only occur for regex matches that contain the index, so 
	 * that the document will not be edited in other places. 
	 * If you want to apply edits to all the regex matches, use index = -1
	 * 
	 * @param e		The Edit Event. 
	 * @param index		Look for regex matches that contain this index. 
	 * 					Use -1 if you want to edit all regex matches in doc. 
	 */
	public void parseRegex(Document document, int index) {
		doc = document;
		characterIndex = index;
		
		SwingUtilities.invokeLater(new Runnable() {
	        public void run() {
	        	
	        	// remove this class as a listener, to avoid recursive edits
	        	//doc.removeDocumentListener(ChemicalSymbolsFormatter.this);
	        	try {
		    		
		    		Iterator<String> i = symbols.keySet().iterator();
		    		
		    		String text = doc.getText(0, doc.getLength());
		    		
		    		List<Position> positionList = new ArrayList<Position>();
		    		String regex;
		    		String replacement;
		    		while (i.hasNext()) {
						regex = i.next();
						replacement = symbols.get(regex);
						positionList.clear();
						WikiView.findExpressions(text, regex, positionList);
						
						// edit the regex
						int start, end;
						for (Position p : positionList) {
							if ((characterIndex > -1) && 
								(! p.contains(characterIndex, characterIndex)))
									continue;
							start = p.getStart();
							end = p.getEnd();
							
							doc.remove(start, end-start);
							doc.insertString(start, replacement, plainText);
						}
		    		}
					
				} catch (BadLocationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//doc.addDocumentListener(ChemicalSymbolsFormatter.this);
	        }
		});
	}

}

