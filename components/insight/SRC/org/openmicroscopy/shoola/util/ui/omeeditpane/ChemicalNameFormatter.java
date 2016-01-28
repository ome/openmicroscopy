 /*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.ChemicalNameFormatter 
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
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


/** 
 * This class is designed to format Chemical Names E.g H2O so that the 
 * numbers are subscript, within a {@link StyledDocument} passed to the 
 * {@link #parseRegex(StyledDocument, boolean)} method. 
 * Chemicals are identified from a list using regex matching. 
 * Add chemicals to the list using the {@link #addFormula(String)} method.
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * @since 3.0-Beta4
 */
public class ChemicalNameFormatter {

    /** The Doc to parse  */
    StyledDocument doc;

    private boolean refresh;

    /** A list of formulas to recognize */
    List<String> formulas;

    /** The Style of the plain text */
    private SimpleAttributeSet plainText;

    /** The Style of the subscript */
    private SimpleAttributeSet subscript;


    /**
     * Creates an instance.
     * Sets the plain text to Sans-Serif, size 14.
     */
    public ChemicalNameFormatter() {
        formulas = new ArrayList<String>();

        plainText = new SimpleAttributeSet();
        StyleConstants.setFontFamily(plainText, "SansSerif");
        StyleConstants.setFontSize(plainText, 14);
        subscript = new SimpleAttributeSet();
        StyleConstants.setSubscript(subscript, true);
        StyleConstants.setFontSize(subscript, 11);
    }

    /**
     * Creates an instance.
     *
     * @param plainText The Style of text with no regex matches.
     */
    public ChemicalNameFormatter(SimpleAttributeSet plainText) {
        this();
        this.plainText = plainText;
    }

    /**
     * Add a Formula to the List parsed for matches.
     * 
     * @param formula The Formula to find.
     */
    public void addFormula(String formula) {
        formulas.add(formula);
    }

    /**
     * Parse the document, find the regex matches and apply the appropriate 
     * Style to each. The Source of the Edit Event should be a 
     * StyledDocument in order that the styles are applied. 
     * Method is public so that it can be called to apply styles before any
     * editing occurs. 
     * Editing occurs on a new thread, so that concurrent editing does not 
     * occur when this method is called from a Document Listener.
     *
     * @param document The document style
     * @param refreshStyle Pass <code>true</code> to refresh the style,
     *                     <code>false</code> otherwise.
     */
    public void parseRegex(StyledDocument document, boolean refreshStyle) {
        doc = document;
        refresh = refreshStyle;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                // first, make all the text plain.
                if (refresh)
                    doc.setCharacterAttributes(0, doc.getLength(), 
                            plainText, true);
                try {

                    String text = doc.getText(0, doc.getLength());

                    List<Position> positionList = new ArrayList<Position>();
                    for (String formula: formulas){
                        positionList.clear();
                        WikiView.findExpressions(text, formula, positionList);

                        // paint the regex
                        int start, end;
                        for (Position p : positionList) {
                            start = p.getStart();
                            end = p.getEnd();

                            Character c;
                            while (start < end) {
                                c = doc.getText(start, 1).charAt(0);
                                if (Character.isDigit(c)) {
                                    doc.setCharacterAttributes(start, 1, 
                                            subscript, false);
                                }
                                start++;
                            }
                        }
                    }

                } catch (BadLocationException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });
    }

}

