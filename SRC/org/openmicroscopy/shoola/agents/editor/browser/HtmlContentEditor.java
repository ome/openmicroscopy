 /*
 * org.openmicroscopy.shoola.agents.editor.browser.HtmlContentEditor 
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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLDocument.Iterator;
import javax.swing.text.html.HTMLDocument.RunElement;

//Third-party libraries

//Application-internal dependencies

/** 
 * This is a {@link JEditorPane} for editing HTML content. 
 * It has some useful methods for querying and manipulating the HTML content.
 * E.g. to tell which element the caret position is in, and to get elements
 * by their id or type. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class HtmlContentEditor 
	extends JEditorPane
	implements DocumentListener {

	/**
	 * Set to true by document listener, set to false when saved to model.
	 */
	private boolean 				hasDataToSave;

	
	/**
	 * initialises the UI
	 */
	private void initialise()
	{
		setEditable(true);
		setBackground(null);
		
		getDocument().addDocumentListener(this);
		
		HTMLDocument doc = (HTMLDocument)getDocument();
		doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
		doc.setPreservesUnknownTags(false);

	}

	/**
	 * Creates an instance.
	 */
    public HtmlContentEditor() 
	{
		super("text/html", "");
		initialise();
	}

	/**
     * Gets the "id" attribute of the {@link Tag.A} or {@link Tag.SPAN} 
     * element that contains the offset, or null if the offset does not 
     * fall within either of these tags. 
     * 
     * @param offset	The offset character position
     * @return String 	The id attribute of the element.
     */
    public String getElementId(int offset) 
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
    				if (id != null) return id.toString();
    			}
    		}
			
    	}
    	return null;
    }
    
    /**
     * Creates a list of {@link TextToken} objects that correspond to the HTML
     * elements of type <code>tag</code> in the current document. 
     * {@link TextToken} defines the start, stop, text and id (if exists) of
     * each element. 
     * 
     * @param tag	The type of tag to get. e.g. {@link Tag.A}
     * @return	see above. 
     */
    public List<TextToken> getElementsByTag(Tag tag) 
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
     * Returns true if the specified <code>offset</code> is within the type
     * of tag specified by <code>tag</code>
     * 
     * @param offset	The character position
     * @param tag		The type of tag. eg Tag.A
     * 
     * @return	boolean 	see above. 
     */
    public boolean isOffsetWithinTag(int offset, Tag tag)
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
