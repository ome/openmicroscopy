/*
 * org.openmicroscopy.shoola.util.ui.OMETextArea 
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
package org.openmicroscopy.shoola.util.ui;


//Java imports
import java.awt.Color;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

//Third-party libraries

//Application-internal dependencies

/** 
 * Updates the color of the fields when the value is modified.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class OMETextArea
	extends JTextArea
	implements DocumentListener
{

	/** The color used for the foreground when the user is editing the value. */
	private Color	editedColor;
	
	/** The default foreground color. */
	private Color	defaultForeground;
	
	/** The default Text. */
	private String	originalText;
	
	/**
	 * Updates the <code>foreground</code> color depending on the text entered.
	 */
	private void updateForeground()
	{
		String text = getText();
		if (editedColor != null) {
			if (originalText != null) {
				if (originalText.equals(text)) setForeground(defaultForeground);
				else setForeground(editedColor);
			}
		}
		if (originalText == null) {
			originalText = text;
			defaultForeground = getForeground();
		}
	}
	
	/** Creates a default instance. */
	public OMETextArea()
	{
		this(null);
	}
	
	/**
	 * Creates a default instance.
	 * 
	 * @param editedColor The foreground when the value is modified.
	 */
	public OMETextArea(Color editedColor)
	{
		setEditedColor(editedColor);
		getDocument().addDocumentListener(this);
	}
	
	/**
	 * Sets the edited color. 
	 * 
	 * @param editedColor The value to set.
	 */
	public void setEditedColor(Color editedColor)
	{ 
		this.editedColor = editedColor;
	}
	
	/**
     * Updates the <code>foreground</code> color depending on the text entered.
     * @see DocumentListener#insertUpdate(DocumentEvent)
     */
	public void insertUpdate(DocumentEvent e) { updateForeground(); }

	/**
     * Updates the <code>foreground</code> color depending on the text entered.
     * @see DocumentListener#removeUpdate(DocumentEvent)
     */
	public void removeUpdate(DocumentEvent e) { updateForeground(); }
	
    /**
     * Required by the {@link DocumentListener} I/F but no-op implementation
     * in our case.
     * @see DocumentListener#changedUpdate(DocumentEvent)
     */
	public void changedUpdate(DocumentEvent e) {}
	
}
