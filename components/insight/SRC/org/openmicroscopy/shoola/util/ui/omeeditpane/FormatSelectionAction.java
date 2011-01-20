/*
 * org.openmicroscopy.shoola.util.ui.omeeditpane.FormatSelectionAction 
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
class FormatSelectionAction
{	
	
	/** The formatter action for the text */
	private Formatter 			formatter;
	
	/** The selection action for the text action. */
	private SelectionAction 	selectionAction;
	
	/**
	 * Create the selection action for formatters and selectionAction events.
	 * 
	 * @param formatter see above.
	 * @param selectionAction see above.
	 */
	public FormatSelectionAction(Formatter formatter, 
			SelectionAction selectionAction)
	{
		this.formatter = formatter;
		this.selectionAction = selectionAction;
	}
	
	/** 
	 * Returns the formatter for the text.
	 * 
	 * @return See above.
	 */
	Formatter getFormatter() { return formatter; }
	
	/**
	 * Returns the selection action for text.
	 * @return see above.
	 */
	SelectionAction getSelectionAction() { return selectionAction; }
	
}


