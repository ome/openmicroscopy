/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.EditFormatter 
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
import javax.swing.text.BadLocationException;

//Third-party libraries

//Application-internal dependencies

/** 
 * 
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
public class EditFormatter
{	
	/** The action triggered when the pattern is matched. */
	protected EditAction 	patternMatchAction;
	
	/** The action triggered when the selection is made. */
	protected EditAction	selectionAction;
	
	/**
	 * Define the EditFormatter for the regex. 
	 * @param patternMatchAction The action triggered when the text is to be 
	 * 							 formatted.
	 * @param selectionAction	 The action triggered when the text is selected.
	 */
	public EditFormatter(EditAction patternMatchAction, EditAction selectionAction)
	{
		this.patternMatchAction = patternMatchAction;
		this.selectionAction = selectionAction;
	}
	
	/**
	 * On the regex matching the text, this is the action performed to format 
	 * the text.
	 * @param pane The OMEEditPane containing the text.
	 * @param start The start of the text (in chars)
	 * @param end The end of the text (in chars)
	 * @throws BadLocationException Thrown if start and end are out of bounds.
	 */
	public void onPatternMatch(OMEEditPane pane,int start, int end) 
				throws BadLocationException
	{
		patternMatchAction.actionPerformed(pane, start, end);
	}
	
	/**
	 * Action performed when the user selects 
	 * text matching the regex, this is the action 
	 * 
	 * @param pane The OMEEditPane containing the text.
	 * @param start The start of the text (in chars)
	 * @param end The end of the text (in chars)
	 * @throws BadLocationException 
	 * @throws BadLocationException Thrown if start and end are out of bounds.
	 */
	public void onSelection(OMEEditPane pane, int start, int end) 
				throws BadLocationException
	{
		selectionAction.actionPerformed(pane, start, end);
	}
}


