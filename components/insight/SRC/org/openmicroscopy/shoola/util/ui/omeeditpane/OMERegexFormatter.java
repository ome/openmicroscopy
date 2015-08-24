 /*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMERegexFormatter 
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/** 
 * This is a {@link DocumentListener} that takes a set of Regular expressions
 * and {@link SimpleAttributeSet} for each.
 * When the {@link Document} is edited, it is parsed and if it is a 
 * {@link StyledDocument} applies a Style to the matching Strings. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class OMERegexFormatter
    implements DocumentListener
{

    /** The Doc to parse  */
    StyledDocument doc;

    /** A Map of the Regex patterns, each with a Style */
    private Map <String, SimpleAttributeSet> styles;

    /** The Style of the plain text */
    private SimpleAttributeSet plainText;

    boolean refresh;

    /**
     * Called by the update edits.
     * Delegates to {@link #parseRegex(StyledDocument)}
     *
     * @param e The DocumentEvent
     * @param refresh True if you want to make the whole document
     *                      plain before applying styles
     */
    private void parseRegex(DocumentEvent e, boolean refresh)
    {
        if (e.getDocument() instanceof StyledDocument) {
            parseRegex((StyledDocument)e.getDocument(), refresh);
        } else {
            return;
        }
    }

    /**
     * Creates an instance.
     * Sets the plain text to Sans-Serif, size 14.
     */
    public OMERegexFormatter()
    {
        styles = new HashMap<String, SimpleAttributeSet>();
        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, "SansSerif");
        StyleConstants.setFontSize(plainText, 14);
    }

    /**
     * Creates an instance.
     *
     * @param plainText The Style of text with no regex matches.
     */
    public OMERegexFormatter(SimpleAttributeSet plainText)
    {
        this();
        this.plainText = plainText;
    }

    /**
     * Add a Regex to the Map parsed for matches.
     *
     * @param regex The Regex to find.
     * @param style The Style to apply to matching text.
     */
    public void addRegex(String regex, SimpleAttributeSet style)
    {
        styles.put(regex, style);
    }

    /**
     * Implemented as specified by the {@link DocumentListener} interface.
     * Null implementation here, since Regex matching should not be affected
     * by changes to fonts etc. 
     */
    public void changedUpdate(DocumentEvent e) {}

    /**
     * Implemented as specified by the {@link DocumentListener} interface.
     * Calls {@link #parseRegex(DocumentEvent, boolean)}
     */
    public void insertUpdate(DocumentEvent e)
    {
        parseRegex(e, true);
    }

    /**
     * Implemented as specified by the {@link DocumentListener} interface.
     * Calls {@link #parseRegex(DocumentEvent, boolean)}
     */
    public void removeUpdate(DocumentEvent e)
    {
        parseRegex(e, true);
    }

    /**
     * Parse the document, find the regex matches and apply the appropriate 
     * Style to each. The Source of the Edit Event should be a 
     * StyledDocument in order that the styles are applied.
     * Method is public so that it can be called to apply styles before any
     * editing occurs.
     * 
     * @param document The document to handle.
     * @param refreshStyle True if you want to make the whole document
     *                      plain before applying styles
     */
    public void parseRegex(StyledDocument document, boolean refreshStyle)
    {
        doc = document;
        refresh = refreshStyle;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // remove this class as a listener, to avoid recursive edits
                doc.removeDocumentListener(OMERegexFormatter.this);
                // first, make all the text plain.
                if (refresh)
                    doc.setCharacterAttributes(0, doc.getLength(),
                            plainText, true);
                try {

                    Iterator<String> i = styles.keySet().iterator();

                    String text = doc.getText(0, doc.getLength());

                    List<Position> positionList = new ArrayList<Position>();
                    String regex;
                    SimpleAttributeSet style;
                    while (i.hasNext()) {
                        regex = i.next();
                        style = styles.get(regex);
                        positionList.clear();
                        WikiView.findExpressions(text, regex, positionList);

                        // paint the regex
                        int start, end;
                        for (Position p : positionList) {
                            start = p.getStart();
                            end = p.getEnd();

                            doc.setCharacterAttributes(start, end-start, 
                                    style, false);
                        }
                    }

                } catch (BadLocationException e1) {
                }
                doc.addDocumentListener(OMERegexFormatter.this);
            }
        });
    }

}
