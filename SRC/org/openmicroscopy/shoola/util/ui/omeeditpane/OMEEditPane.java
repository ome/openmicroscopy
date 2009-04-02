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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.JTextPane;

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
	extends JTextPane
	implements FocusListener
{
	
	/** The type of the document. */
	private static String DOC_TYPE = "text/wiki";
	
	/** The editor kit	 */
	private OMEEditorKit		editorKit;
	
	/** Reference to the main component. */
	private OMEWikiComponent 	component;
	
	/**
	 * Handles the text selection.
	 * 
	 * @param e The event to handle.
	 */
	private void onTextSelection(MouseEvent e)
	{
		int index = viewToModel(new Point(e.getX(), e.getY()));
		WikiView view = editorKit.getView();
		SelectionAction action = view.getSelectionAction(index);
		component.onSelection(action, view.getSelectedText(index), 
				e.getClickCount());
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
			 * Handles text selection.
			 * @see MouseAdapter#mouseClicked(MouseEvent)
			 */
			public void mouseClicked(MouseEvent e) {
				onTextSelection(e);
			}
		
		});
	    addFocusListener(this);
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
				int n = text.length();//-1;
				if (n >= 0) setCaretPosition(n);
			}
		}
	}

	/**
	 * Deselects the text.
	 * @see FocusListener#focusLost(FocusEvent)
	 */
	public void focusLost(FocusEvent e) { select(0, 0); }
	 
}



