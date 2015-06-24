/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.OMEEditPane 
 *
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
 */
package org.openmicroscopy.shoola.util.ui.omeeditpane;


//Java imports
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JEditorPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * The component where the text is added to.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
class OMEEditPane
	extends JEditorPane//JTextPane
	implements ActionListener, FocusListener
{
	
	/** The delay of the timer. */
	private static final int 	DELAY = 400;
	
	/** The type of the document. */
	private static String DOC_TYPE = "text/wiki";
	
	/** The editor kit	 */
	private OMEEditorKit		editorKit;
	
	/** Reference to the main component. */
	private OMEWikiComponent 	component;
	
	/** The timer. */
	private Timer			timer;

	/** Count the number of mouse clicked. */
	private int				count;
	
	/** The location of the mouse clicked. */
	private Point			location;
	

	/** Handles mouse pressed. */
	private void handleMousePressed()
	{
		if (timer == null) {
			timer = new Timer(DELAY, this);
			timer.setRepeats(false);
		}
		timer.start();
	}
	
	/**
	 * Creates a new instance.
	 * Initializes the default maps and styles.
	 *  
	 * @param component	 Reference to the main component. 
	 * @param formatters The formatters for the text.
	 */
	OMEEditPane(OMEWikiComponent component, 
			Map<String, FormatSelectionAction> formatters)
	{
		super();
		this.component = component;
		editorKit = new OMEEditorKit(formatters);
		setEditorKitForContentType(DOC_TYPE, editorKit);
		setContentType(DOC_TYPE);
		addMouseListener(new MouseAdapter() {

			/**
			 * Starts the timer.
			 * @see MouseAdapter#mousePressed(MouseEvent)
			 */
			public void mousePressed(MouseEvent e)
			{
				handleMousePressed();
				count++;
				location = e.getPoint();
			}
			
		});
	    addFocusListener(this);
	    getDocument().addDocumentListener(new DocumentListener() {
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                component.onUpdate();
            }
            
            @Override
            public void insertUpdate(DocumentEvent e) {
                component.onUpdate();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
	}
	
	/**
	 * Adds the specified formatter.
	 * 
	 * @param value The value to identify the formatter.
	 * @param action The action associated.
	 */
	void addFormatter(String value, FormatSelectionAction action)
	{
		editorKit.addFormatter(value, action);
	}
	
	/**
	 * Selects the text and sets the position of the caret.
	 * @see FocusListener#focusGained(FocusEvent)
	 */
	public void focusGained(FocusEvent e)
	{
		String text = getText();
		if (text != null) {
			if (component.getDefaultText().equals(text))
				selectAll();
			else {
				setText(text);
				int n = getDocument().getLength();
				if (n >= 0) setCaretPosition(n);
			}
		}
	}

	/**
	 * Deselects the text.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) { select(0, 0); }

	/**
	 * Selects the data.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		if (count == 1 || count == 2) {
			int index = viewToModel(location);
			WikiView view = editorKit.getView();
			SelectionAction action = view.getSelectionAction(index);
			component.onSelection(action, view.getSelectedText(index), count);
			timer.stop();
			count = 0;
		}
	}
	 
}



