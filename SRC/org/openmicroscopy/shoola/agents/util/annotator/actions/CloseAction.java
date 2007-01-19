/*
 * org.openmicroscopy.shoola.agents.util.annotator.actions.CloseAction 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.annotator.actions;

//Java imports
import java.awt.event.ActionEvent;
import javax.swing.Action;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.util.annotator.view.Annotator;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Cancels the operation and closes.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class CloseAction
	extends AnnotatorAction
{
	
	 /** The name of the action. */
	private static final String NAME = "Close";
	
	/** The description of the action. */
	private static final String DESCRIPTION = "Cancels the operation and " +
			"closes the window.";
	 
	/** 
	 * Overriden to enable the action. 
	 * @see AnnotatorAction#onStateChange()
	 */
	protected void onStateChange() { setEnabled(true); }
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	public CloseAction(Annotator model)
	{
		super(model);
		setEnabled(true);
		putValue(Action.NAME, NAME);
	    putValue(Action.SHORT_DESCRIPTION, 
	                UIUtilities.formatToolTipText(DESCRIPTION));
	}
	
	/**
	 * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) { model.discard(); }

}
