/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.util.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A {@link JTextField} which shows a fainted default text when empty and not
 * active. Fires {@link #EDIT_PROPERTY} events when text is entered/modified and
 * {@link #SUBMIT_PROPERTY} events when enter key is pressed.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FancyTextField extends JTextField {

    /** Property fired on text modification */
    public static final String EDIT_PROPERTY = "EDIT_PROPERTY";

    /** Property fired when Enter key pressed */
    public static final String SUBMIT_PROPERTY = "SUBMIT_PROPERTY";

    /** The default text */
    private final String DEFAULT_FILTER_TEXT;

    /**
     * @see JTextField#JTextField()
     */
    public FancyTextField() {
        super();
        DEFAULT_FILTER_TEXT = "";
        init();
    }

    /**
     * @see JTextField#JTextField(int)
     */
    public FancyTextField(int columns) {
        super(columns);
        DEFAULT_FILTER_TEXT = "";
        init();
    }

    /**
     * @see JTextField#JTextField(String, int)
     */
    public FancyTextField(String text, int columns) {
        super(text, columns);
        DEFAULT_FILTER_TEXT = text;
        init();
    }

    /**
     * @see JTextField#JTextField(String)
     */
    public FancyTextField(String text) {
        super(text);
        DEFAULT_FILTER_TEXT = text;
        init();
    }

    private void init() {
        setText(DEFAULT_FILTER_TEXT);

        final Font defaultFont = getFont();
        final Font italicFont = getFont().deriveFont(Font.ITALIC);
        setFont(italicFont);
        setForeground(UIUtilities.DEFAULT_FONT_COLOR);

        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().equals("")) {
                    setText(DEFAULT_FILTER_TEXT);
                    setFont(italicFont);
                    setForeground(UIUtilities.DEFAULT_FONT_COLOR);
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(DEFAULT_FILTER_TEXT)) {
                    setText("");
                } else {
                    selectAll();
                }
                setFont(defaultFont);
                setForeground(Color.BLACK);
            }
        });

        getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!getText().equals(DEFAULT_FILTER_TEXT))
                    firePropertyChange(EDIT_PROPERTY, null, getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!getText().equals(DEFAULT_FILTER_TEXT))
                    firePropertyChange(EDIT_PROPERTY, null, getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!getText().equals(DEFAULT_FILTER_TEXT))
                    firePropertyChange(EDIT_PROPERTY, null, getText());
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!getText().equals(DEFAULT_FILTER_TEXT)) {
                    switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        firePropertyChange(SUBMIT_PROPERTY, null, getText());
                    }
                }
            }
        });

    }
}
